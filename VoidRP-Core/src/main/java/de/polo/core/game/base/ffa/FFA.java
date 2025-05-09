package de.polo.core.game.base.ffa;

import de.polo.api.Utils.inventorymanager.CustomItem;
import de.polo.api.Utils.inventorymanager.InventoryManager;
import de.polo.api.VoidAPI;
import de.polo.core.Main;
import de.polo.core.location.services.LocationService;
import de.polo.core.manager.ItemManager;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.storage.WeaponType;
import de.polo.core.utils.Prefix;
import de.polo.core.utils.enums.FFALobbyType;
import de.polo.core.utils.enums.FFAStatsType;
import de.polo.core.utils.enums.RoleplayItem;
import de.polo.core.utils.enums.Weapon;
import de.polo.core.utils.player.PlayerFFAStats;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.SneakyThrows;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class FFA implements CommandExecutor {
    private final PlayerManager playerManager;

    private final List<FFALobby> lobbies = new ObjectArrayList<>();

    private final List<Player> joinedPlayers = new ObjectArrayList<>();
    private final List<PlayerFFAStats> playerFFAStats = new ObjectArrayList<>();

    public FFA(PlayerManager playerManager) {
        this.playerManager = playerManager;

        load();

        Main.registerCommand("ffa", this);
    }

    @SneakyThrows
    private void load() {
        lobbies.clear();
        playerFFAStats.clear();

        try (Connection connection = Main.getInstance().coreDatabase.getConnection();
             PreparedStatement lobbyStatement = connection.prepareStatement("SELECT * FROM ffa_lobbies");
             ResultSet lobbyResult = lobbyStatement.executeQuery()) {

            while (lobbyResult.next()) {
                FFALobby lobby = new FFALobby(lobbyResult.getInt("id"), lobbyResult.getString("name"), lobbyResult.getInt("maxPlayer"));
                lobby.setFfaLobbyType(FFALobbyType.valueOf(lobbyResult.getString("lobbyType")));
                lobbies.add(lobby);

                try (PreparedStatement spawnPoints = connection.prepareStatement("SELECT * FROM ffa_spawnpoints WHERE lobbyId = ?")) {
                    spawnPoints.setInt(1, lobby.getId());
                    try (ResultSet spawn = spawnPoints.executeQuery()) {
                        while (spawn.next()) {
                            Location location = new Location(
                                    Bukkit.getWorld(spawn.getString("welt")),
                                    spawn.getInt("x"),
                                    spawn.getInt("y"),
                                    spawn.getInt("z"),
                                    spawn.getFloat("yaw"),
                                    spawn.getFloat("pitch")
                            );
                            FFASpawn ffaSpawn = new FFASpawn(spawn.getInt("id"), spawn.getInt("lobbyId"), location);
                            lobby.addSpawn(ffaSpawn);
                        }
                    }
                }
            }

            try (PreparedStatement statsStatement = connection.prepareStatement("SELECT * FROM player_ffa_stats");
                 ResultSet stats = statsStatement.executeQuery()) {
                while (stats.next()) {
                    PlayerFFAStats playerStats = new PlayerFFAStats(
                            stats.getString("uuid"),
                            stats.getInt("kills"),
                            stats.getInt("deaths")
                    );
                    playerStats.setFfaStatsType(FFAStatsType.valueOf(stats.getString("statsType")));
                    playerStats.setId(stats.getInt("id"));
                    playerFFAStats.add(playerStats);
                }
            }
        }
    }

    public void addPlayerStats(PlayerFFAStats stats) {
        playerFFAStats.add(stats);
    }

    public Collection<PlayerFFAStats> getStats() {
        return playerFFAStats;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        if (args.length < 1) {
            player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /ffa [join/leave/leaderboard]");
            return false;
        }
        PlayerData playerData = playerManager.getPlayerData(player);
        LocationService locationService = VoidAPI.getService(LocationService.class);
        if (args[0].equalsIgnoreCase("join")) {
            if (playerData.getVariable("ffa") != null) {
                player.sendMessage(Prefix.ERROR + "Du bist bereits in einem FFA.");
                return false;
            }
            if (locationService.getDistanceBetweenCoords(player, "ffa") < 5) {
                openFFAMenu(player);
            } else {
                player.sendMessage(Prefix.ERROR + "Du bist nicht in der nähe der FFA-Arena!");
            }
        } else if (args[0].equalsIgnoreCase("leave")) {
            if (playerData.getVariable("ffa") != null) {
                player.sendMessage("§8[§cFFA§8]§a Du hast die FFA-Arena verlassen.");
                leaveFFA(player);
            } else {
                player.sendMessage(Prefix.ERROR + "Du bist nicht in FFA.");
            }
        } else if (args[0].equalsIgnoreCase("leaderboard")) {
            openLeaderboard(player, FFAStatsType.ALL_TIME);
        } else {
            player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /ffa [join/leave/leaderboard]");
        }
        return false;
    }

    public void openLeaderboard(Player player, FFAStatsType type) {
        List<PlayerFFAStats> sortedStats = playerFFAStats.stream()
                .sorted(Comparator.comparing(PlayerFFAStats::getKD).reversed())
                .toList();

        String playerUUID = player.getUniqueId().toString();
        int playerRank = -1;

        for (int i = 0; i < sortedStats.size(); i++) {
            if (sortedStats.get(i).getUuid().equals(playerUUID) && sortedStats.get(i).getFfaStatsType().equals(type)) {
                playerRank = i + 1;
                break;
            }
        }

        List<PlayerFFAStats> top5Players = sortedStats.stream().limit(5).collect(Collectors.toList());

        player.sendMessage("§6§lLeaderboard:");
        /*for (int i = 0; i < top5Players.size(); i++) {
            PlayerFFAStats stats = top5Players.get(i);
            player.sendMessage("§7#" + (i + 1) + " §e" + stats.getUuid() + " §7| KD: §e" + stats.getKD());
        }

        if (playerRank != -1) {
            player.sendMessage("§aDein Rang: §e#" + playerRank + " §7| KD: §e" + sortedStats.get(playerRank - 1).getKD());
        } else {
            player.sendMessage("§cDu bist nicht in der Rangliste.");
        }*/

        InventoryManager inventoryManager = new InventoryManager(player, 27, Component.text("§8 » §eStats§8 - " + type.getDisplayName()));
        inventoryManager.setItem(new CustomItem(4, ItemManager.createItemHead(player.getUniqueId().toString(), 1, 0, "§6§l#" + playerRank + "§7 " + player.getName())) {
            @Override
            public void onClick(InventoryClickEvent event) {

            }
        });
        int i = 9;
        int rank = 1;
        for (PlayerFFAStats stats : top5Players) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(stats.getUuid()));
            if (offlinePlayer.getName() == null) continue;
            inventoryManager.setItem(new CustomItem(i, ItemManager.createItemHead(stats.getUuid(), 1, 0, "§6§l#" + rank + "§7 " + offlinePlayer.getName())) {
                @Override
                public void onClick(InventoryClickEvent event) {

                }
            });
            i++;
            rank++;
        }

        i = 18;
        for (FFAStatsType statsType : FFAStatsType.values()) {
            inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(Material.PAPER, 1, 0, statsType.getDisplayName())) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    openLeaderboard(player, statsType);
                }
            });
            i++;
        }
    }


    private void openFFAMenu(Player player) {
        InventoryManager inventoryManager = new InventoryManager(player, 27, Component.text("§8 » §cFree for All"));
        int i = 0;
        for (FFALobby lobby : lobbies) {
            int players = getPlayersInLobby(lobby.getId()).size();
            if (players >= lobby.getMaxPlayer()) {
                inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(Material.PAPER, 1, 0, "§c" + lobby.getName() + " §8[§cVOLL§8]", Arrays.asList("§8 ➥ §e" + players + "§8/§6" + lobby.getMaxPlayer(), "§8 ➥ " + lobby.getFfaLobbyType().getDisplayName()))) {
                    @Override
                    public void onClick(InventoryClickEvent event) {
                        joinFFA(player, lobby);
                    }
                });
            } else {
                inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(Material.PAPER, 1, 0, "§c" + lobby.getName(), Arrays.asList("§8 ➥ §e" + players + "§8/§6" + lobby.getMaxPlayer(), "§8 ➥ " + lobby.getFfaLobbyType().getDisplayName()))) {
                    @Override
                    public void onClick(InventoryClickEvent event) {
                        player.closeInventory();
                        joinFFA(player, lobby);
                    }
                });
            }
            i++;
        }
    }

    private Collection<Player> getPlayersInLobby(int lobby) {
        List<Player> players = new ObjectArrayList<>();
        for (PlayerData playerData : playerManager.getPlayers()) {
            if (playerData.getVariable("ffa") == null) continue;
            FFALobby ffaLobby = playerData.getVariable("ffa");
            if (ffaLobby.getId() != lobby) continue;
            players.add(playerData.getPlayer());
        }
        return players;
    }

    public void joinFFA(Player player, FFALobby lobby) {
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (playerData.isDead()) {
            player.sendMessage(Prefix.ERROR + "Du kannst aktuell keinem FFA beitreten.");
            return;
        }
        if (lobby.getSpawns().size() == 0) {
            player.sendMessage(Prefix.ERROR + "Die Lobby hat keine Spawnpunkte.");
            return;
        }
        playerData.setVariable("inventory::ffa", player.getInventory().getContents());
        player.getInventory().clear();
        playerData.setVariable("ffa", lobby);
        equipPlayer(player);
        joinedPlayers.add(player);
        playerData.getPlayerFFAStatsManager().handleJoin();
        FFASpawn spawn = getRandomSpawn(lobby);
        player.teleport(spawn.getLocation());
        sendMessageToLobby(lobby, "§8[§cFFA§8]§7 " + player.getName() + " ist der Lobby beigetreten.");
    }

    private void sendMessageToLobby(FFALobby lobby, String message) {
        for (Player player : getPlayersInLobby(lobby)) {
            player.sendMessage(message);
        }
    }

    public void equipPlayer(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            for (Weapon weaponData : Weapon.values()) {
                if (weaponData.getMaterial() != null && item != null) {
                    if (item.getType() == weaponData.getMaterial()) {
                        ItemMeta meta = item.getItemMeta();
                        de.polo.core.storage.Weapon weapon = Main.weaponManager.getWeaponFromItemStack(item);
                        if (weapon.getWeaponType() == WeaponType.GANGWAR) {
                            Main.weaponManager.removeWeapon(player, item);
                        }
                    }
                }
            }
        }
        player.getInventory().clear();
        Main.weaponManager.giveWeapon(player, Weapon.ASSAULT_RIFLE, WeaponType.FFA, 300);
        // Main.getInstance().weapons.giveWeapon(player, Weapon.HUNTING_RIFLE.getMaterial(), WeaponType.FFA);
        Main.weaponManager.giveWeapon(player, Weapon.PISTOL, WeaponType.FFA, 300);
        player.getInventory().addItem(ItemManager.createItem(RoleplayItem.SMARTPHONE.getMaterial(), 1, 0, RoleplayItem.SMARTPHONE.getDisplayName()));
    }

    public void leaveFFA(Player player) {
        joinedPlayers.remove(player);
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        // if (playerData.getFaction() == null) return;
        // playerData.removeBossBar("ffa");
        Main.utils.deathUtil.revivePlayer(player, false);
        FFALobby lobby = playerData.getVariable("ffa");
        sendMessageToLobby(lobby, "§8[§cFFA§8]§7 " + player.getName() + " hat die Lobby verlassen.");
        if (playerData.getVariable("ffa") == null) {
            return;
        }
        LocationService locationService = VoidAPI.getService(LocationService.class);
        locationService.useLocation(player, "ffa");
        playerData.setVariable("ffa", null);
        for (ItemStack item : player.getInventory().getContents()) {
            for (Weapon weaponData : Weapon.values()) {
                if (weaponData.getMaterial() != null && item != null) {
                    if (item.getType() == weaponData.getMaterial()) {
                        ItemMeta meta = item.getItemMeta();
                        de.polo.core.storage.Weapon weapon = Main.weaponManager.getWeaponFromItemStack(item);
                        if (weapon.getWeaponType() == WeaponType.GANGWAR) {
                            Main.weaponManager.removeWeapon(player, item);
                        }
                    }
                }
            }
        }
        player.getInventory().clear();
        player.getInventory().setContents(playerData.getVariable("inventory::ffa"));
        playerData.getPlayerFFAStatsManager().save();
    }

    public void handleDeath(Player player) {
        if (!joinedPlayers.contains(player)) return;
        PlayerData playerData = playerManager.getPlayerData(player);
        FFALobby lobby = playerData.getVariable("ffa");
        if (player.getKiller() == null) return;
        sendMessageToLobby(lobby, "§8[§cFFA§8]§7 " + player.getKiller().getName() + " §8»§7 " + player.getName());
        playerData.getPlayerFFAStatsManager().addDeath();

        PlayerData targetData = playerManager.getPlayerData(player.getKiller());
        targetData.getPlayerFFAStatsManager().addKill();
    }

    public void respawnPlayer(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player);
        FFALobby lobby = playerData.getVariable("ffa");
        FFASpawn spawn = getRandomSpawn(lobby);
        player.teleport(spawn.getLocation());
        equipPlayer(player);
        if (player.getKiller() == null) return;
        heal(player.getKiller());
    }

    private void heal(Player player) {
        player.setHealth(player.getMaxHealth());
    }

    private Collection<Player> getPlayersInLobby(FFALobby lobby) {
        List<Player> players = new ObjectArrayList<>();
        for (Player player : joinedPlayers) {
            PlayerData playerData = playerManager.getPlayerData(player);
            if (playerData.getVariable("ffa") == null) continue;
            FFALobby ffaLobby = playerData.getVariable("ffa");
            if (lobby == ffaLobby) players.add(player);
        }
        return players;
    }

    private FFASpawn getRandomSpawn(FFALobby lobby) {
        List<FFASpawn> spawns = lobby.getSpawns();
        int randomIndex = ThreadLocalRandom.current().nextInt(spawns.size());
        return spawns.get(randomIndex);
    }

    public void clearStats(FFAStatsType type) {
        playerFFAStats.removeIf(ffaStats -> ffaStats.getFfaStatsType().equals(type));
    }
}
