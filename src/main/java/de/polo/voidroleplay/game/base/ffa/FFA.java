package de.polo.voidroleplay.game.base.ffa;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.*;
import de.polo.voidroleplay.utils.*;
import de.polo.voidroleplay.utils.InventoryManager.CustomItem;
import de.polo.voidroleplay.utils.InventoryManager.InventoryManager;
import de.polo.voidroleplay.utils.enums.FFALobbyType;
import de.polo.voidroleplay.utils.enums.FFAStatsType;
import de.polo.voidroleplay.utils.enums.RoleplayItem;
import de.polo.voidroleplay.utils.playerUtils.PlayerFFAStats;
import lombok.SneakyThrows;
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
import java.util.stream.Collectors;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class FFA implements CommandExecutor {
    private final PlayerManager playerManager;
    private final LocationManager locationManager;

    private final List<FFALobby> lobbies = new ArrayList<>();

    private final List<Player> joinedPlayers = new ArrayList<>();
    private final List<PlayerFFAStats> playerFFAStats = new ArrayList<>();

    public FFA(PlayerManager playerManager, LocationManager locationManager) {
        this.playerManager = playerManager;
        this.locationManager = locationManager;

        load();

        Main.registerCommand("ffa", this);
    }

    @SneakyThrows
    private void load() {
        lobbies.clear();
        playerFFAStats.clear();
        Connection connection = Main.getInstance().mySQL.getConnection();
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM ffa_lobbies");
        ResultSet result = statement.executeQuery();
        while (result.next()) {
            FFALobby lobby = new FFALobby(result.getInt("id"), result.getString("name"), result.getInt("maxPlayer"));
            lobby.setFfaLobbyType(FFALobbyType.valueOf(result.getString("lobbyType")));
            lobbies.add(lobby);
            PreparedStatement spawnPoints = connection.prepareStatement("SELECT * FROM ffa_spawnpoints WHERE lobbyId = ?");
            spawnPoints.setInt(1, lobby.getId());
            ResultSet spawn = spawnPoints.executeQuery();
            while (spawn.next()) {
                Location location = new Location(Bukkit.getWorld(spawn.getString("welt")), spawn.getInt("x"), spawn.getInt("y"), spawn.getInt("z"), spawn.getFloat("yaw"), spawn.getFloat("pitch"));
                FFASpawn ffaSpawn = new FFASpawn(spawn.getInt("id"), spawn.getInt("lobbyId"), location);
                lobby.addSpawn(ffaSpawn);
            }
        }

        PreparedStatement statsStatement = connection.prepareStatement("SELECT * FROM player_ffa_stats");
        ResultSet stats = statsStatement.executeQuery();
        while (stats.next()) {
            PlayerFFAStats playerStats = new PlayerFFAStats(stats.getString("uuid"), stats.getInt("kills"), stats.getInt("deaths"));
            playerStats.setFfaStatsType(FFAStatsType.valueOf(stats.getString("statsType")));
            playerStats.setId(stats.getInt("id"));
            playerFFAStats.add(playerStats);
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
            player.sendMessage(Main.error + "Syntax-Fehler: /ffa [join/leave/leaderboard]");
            return false;
        }
        PlayerData playerData = playerManager.getPlayerData(player);
        if (args[0].equalsIgnoreCase("join")) {
            if (playerData.getVariable("ffa") != null) {
                player.sendMessage(Main.error + "Du bist bereits in einem FFA.");
                return false;
            }
            if (locationManager.getDistanceBetweenCoords(player, "ffa") < 5) {
                openFFAMenu(player);
            } else {
                player.sendMessage(Main.error + "Du bist nicht in der nähe der FFA-Arena!");
            }
        } else if (args[0].equalsIgnoreCase("leave")) {
            if (playerData.getVariable("ffa") != null) {
                player.sendMessage("§8[§cFFA§8]§a Du hast die FFA-Arena verlassen.");
                leaveFFA(player);
            } else {
                player.sendMessage(Main.error + "Du bist nicht in FFA.");
            }
        } else if (args[0].equalsIgnoreCase("leaderboard")) {
            openLeaderboard(player, FFAStatsType.ALL_TIME);
        } else {
            player.sendMessage(Main.error + "Syntax-Fehler: /ffa [join/leave/leaderboard]");
        }
        return false;
    }

    public void openLeaderboard(Player player, FFAStatsType type) {
        List<PlayerFFAStats> sortedStats = playerFFAStats.stream()
                .sorted(Comparator.comparing(PlayerFFAStats::getKD).reversed())
                .collect(Collectors.toList());

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

        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8 » §eStats§8 - " + type.getDisplayName());
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
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8 » §cFree for All");
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
        List<Player> players = new ArrayList<>();
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
            player.sendMessage(Main.error + "Du kannst aktuell keinem FFA beitreten.");
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
            for (WeaponData weaponData : Weapons.weaponDataMap.values()) {
                if (weaponData.getMaterial() != null && item != null) {
                    if (item.getType() == weaponData.getMaterial()) {
                        ItemMeta meta = item.getItemMeta();
                        Weapon weapon = Main.getInstance().weapons.getWeaponFromItemStack(item);
                        if (weapon.getWeaponType() == WeaponType.GANGWAR) {
                            Main.getInstance().weapons.removeWeapon(player, item);
                        }
                    }
                }
            }
        }
        player.getInventory().clear();
        Main.getInstance().weapons.giveWeaponToPlayer(player, Material.DIAMOND_HORSE_ARMOR, WeaponType.FFA);
        Main.getInstance().weapons.giveWeaponToPlayer(player, Material.IRON_HORSE_ARMOR, WeaponType.FFA);
        Main.getInstance().weapons.giveWeaponToPlayer(player, Material.GOLDEN_SHOVEL, WeaponType.FFA);
        player.getInventory().addItem(ItemManager.createItem(RoleplayItem.SNUFF.getMaterial(), 5, 0, RoleplayItem.SNUFF.getDisplayName()));
        player.getInventory().addItem(ItemManager.createItem(RoleplayItem.CIGAR.getMaterial(), 5, 0, RoleplayItem.CIGAR.getDisplayName()));
        player.getInventory().addItem(ItemManager.createItem(RoleplayItem.SMARTPHONE.getMaterial(), 1, 0, RoleplayItem.SMARTPHONE.getDisplayName()));
    }

    public void leaveFFA(Player player) {
        joinedPlayers.remove(player);
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        // if (playerData.getFaction() == null) return;
        // playerData.removeBossBar("ffa");
        Main.getInstance().utils.deathUtil.revivePlayer(player, false);
        FFALobby lobby = playerData.getVariable("ffa");
        sendMessageToLobby(lobby, "§8[§cFFA§8]§7 " + player.getName() + " hat die Lobby verlassen.");
        if (playerData.getVariable("ffa") == null) {
            return;
        }
        locationManager.useLocation(player, "ffa");
        playerData.setVariable("ffa", null);
        for (ItemStack item : player.getInventory().getContents()) {
            for (WeaponData weaponData : Weapons.weaponDataMap.values()) {
                if (weaponData.getMaterial() != null && item != null) {
                    if (item.getType() == weaponData.getMaterial()) {
                        ItemMeta meta = item.getItemMeta();
                        Weapon weapon = Main.getInstance().weapons.getWeaponFromItemStack(item);
                        if (weapon.getWeaponType() == WeaponType.GANGWAR) {
                            Main.getInstance().weapons.removeWeapon(player, item);
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
    }

    private Collection<Player> getPlayersInLobby(FFALobby lobby) {
        List<Player> players = new ArrayList<>();
        for (Player player : joinedPlayers) {
            PlayerData playerData = playerManager.getPlayerData(player);
            if (playerData.getVariable("ffa") == null) continue;
            FFALobby ffaLobby = playerData.getVariable("ffa");
            if (lobby == ffaLobby) players.add(player);
        }
        return players;
    }

    private FFASpawn getRandomSpawn(FFALobby lobby) {
        Random random = new Random();
        List<FFASpawn> spawns = lobby.getSpawns();
        int randomIndex = random.nextInt(spawns.size());
        return spawns.get(randomIndex);
    }
}
