package de.polo.void_roleplay.PlayerUtils;

import de.polo.void_roleplay.DataStorage.*;
import de.polo.void_roleplay.Main;
import de.polo.void_roleplay.MySQl.MySQL;
import de.polo.void_roleplay.Utils.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class FFA implements CommandExecutor {
    public static Map<Integer, FFALobbyData> FFAlobbyDataMap = new HashMap<>();
    public static Map<String, FFASpawnPoints> FFAspawnpointDataMap = new HashMap<>();

    public static void loadFFALobbys() throws SQLException {
        Statement statement = MySQL.getStatement();
        ResultSet lobby = statement.executeQuery("SELECT * FROM `ffa_lobbys`");
        while (lobby.next()) {
            FFALobbyData ffaLobbyData = new FFALobbyData();
            ffaLobbyData.setId(lobby.getInt(1));
            ffaLobbyData.setName(lobby.getString(2));
            ffaLobbyData.setDisplayname(lobby.getString(3));
            ffaLobbyData.setMaxPlayer(lobby.getInt(4));
            ffaLobbyData.setPlayers(0);
            FFAlobbyDataMap.put(lobby.getInt(1), ffaLobbyData);
        }

        ResultSet result = statement.executeQuery("SELECT * FROM ffa_spawnpoints");
        while (result.next()) {
            FFASpawnPoints ffaSpawnPoints = new FFASpawnPoints();
            ffaSpawnPoints.setId(result.getInt(1));
            ffaSpawnPoints.setLobby_type(result.getString(2));
            ffaSpawnPoints.setX(result.getInt(3));
            ffaSpawnPoints.setY(result.getInt(4));
            ffaSpawnPoints.setZ(result.getInt(5));
            ffaSpawnPoints.setWelt(Bukkit.getWorld(result.getString(6)));
            ffaSpawnPoints.setYaw(result.getFloat(7));
            ffaSpawnPoints.setPitch(result.getFloat(8));
            FFAspawnpointDataMap.put(result.getString(2), ffaSpawnPoints);
        }
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("join")) {
                if (PlayerManager.playerDataMap.get(player.getUniqueId().toString()).getVariable("current_lobby") == null) {
                    openFFAMenu(player, 1);
                } else {
                    player.sendMessage(Main.error + "Du bist bereits in einem FFA.");
                }
            } else if (args[0].equalsIgnoreCase("leave")) {
                if (PlayerManager.playerDataMap.get(player.getUniqueId().toString()).getVariable("current_lobby") != null) {
                    leaveFFA(player);
                } else {
                    player.sendMessage(Main.error + "Du bist nicht in FFA.");
                }
            } else {
                player.sendMessage(Main.error + "Syntax-Fehler: /ffa [join/leave]");
            }
        } else {
            player.sendMessage(Main.error + "Syntax-Fehler: /ffa [join/leave]");
        }
        return false;
    }

    public static void openFFAMenu(Player player, int page) {
        if (page <= 0) return;
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        playerData.setVariable("current_inventory", "ffa_menu");
        playerData.setIntVariable("current_page", page);
        Inventory inv = Bukkit.createInventory(player, 27, "§8» §6FFA-Lobbys §8- §6Seite§8:§7 " + page);
        int i = 0;
        for (FFALobbyData lobbyData : FFAlobbyDataMap.values()) {
            if (i == 26 && i == 18) {
                i++;
            } else if (i >= (25 * (page - 1)) && i <= (25 * page)) {
                if (lobbyData.getPlayers() < lobbyData.getMaxPlayer()) {
                    inv.setItem(i, ItemManager.createItem(Material.LIME_DYE, 1, 0, lobbyData.getDisplayname().replace("&", "§"), "§8 ➥ §eSpieler§8:§7 " + lobbyData.getPlayers() + "/" + lobbyData.getMaxPlayer()));
                    ItemMeta meta = inv.getItem(i).getItemMeta();
                    meta.getPersistentDataContainer().set(new NamespacedKey(Main.plugin, "id"), PersistentDataType.INTEGER, lobbyData.getId());
                    inv.getItem(i).setItemMeta(meta);
                } else {
                    inv.setItem(i, ItemManager.createItem(Material.GRAY_DYE, 1, 0, lobbyData.getDisplayname().replace("&", "§"), "§8 ➥ §eSpieler§8:§7 " + lobbyData.getPlayers() + "/" + lobbyData.getMaxPlayer()));
                }
                i++;
            }
            inv.setItem(26, ItemManager.createItem(Material.GOLD_NUGGET, 1, 0, "§cNächste Seite", null));
            inv.setItem(18, ItemManager.createItem(Material.NETHER_WART, 1, 0, "§cVorherige Seite", null));
        }
        player.openInventory(inv);
    }

    public static void joinLobby(Player player, int id) {
        FFALobbyData lobbyData = FFAlobbyDataMap.get(id);
        if (lobbyData.getPlayers() < lobbyData.getMaxPlayer()) {
            player.closeInventory();
            PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
            playerData.setIntVariable("current_lobby", id);
            playerData.setVariable("current_lobby", lobbyData.getName());
            lobbyData.setPlayers(lobbyData.getPlayers() + 1);
            player.sendMessage("§8[§6FFA§8]§e Du betrittst: " + lobbyData.getDisplayname().replace("&", "§"));
            Weapons.giveWeaponToPlayer(player, Material.DIAMOND_HORSE_ARMOR, "FFA");
            useSpawn(player, id);
        } else {
            player.sendMessage("§8[§6FFA§8]§c Diese Lobby ist voll!");
        }
    }

    public static void leaveFFA(Player player) {
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        FFALobbyData lobbyData = FFAlobbyDataMap.get(playerData.getIntVariable("current_lobby"));
        lobbyData.setPlayers(lobbyData.getPlayers() - 1);
        for (ItemStack item : player.getInventory().getContents()) {
            for (WeaponData weaponData : Weapons.weaponDataMap.values()) {
                if (item.getType() == weaponData.getMaterial()) {
                    ItemMeta meta = item.getItemMeta();
                    if (meta.getPersistentDataContainer().get(new NamespacedKey(Main.plugin, "type"), PersistentDataType.STRING) != null) {
                        if (meta.getPersistentDataContainer().get(new NamespacedKey(Main.plugin, "type"), PersistentDataType.STRING) == "FFA") {
                            player.getInventory().remove(item);
                        }
                    }
                }
            }
        }
        LocationManager.useLocation(player, "ffa");
        playerData.setIntVariable("current_lobby", null);
        playerData.setVariable("current_lobby", null);
    }

    public static void useSpawn(Player player, int ffa) {
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        List<FFASpawnPoints> keysWithIdOne = new ArrayList<>();
        for (FFASpawnPoints spawnPoints : FFAspawnpointDataMap.values()) {
            if (spawnPoints.getLobby_type().equals(playerData.getVariable("current_lobby"))) {
                keysWithIdOne.add(spawnPoints);
            }
        }

        Random random = new Random();
        int randomKeyIndex = random.nextInt(keysWithIdOne.size());
        FFASpawnPoints randomValue = keysWithIdOne.get(randomKeyIndex);

        player.teleport(new Location(randomValue.getWelt(), randomValue.getX(), randomValue.getY(), randomValue.getZ()));
    }
}
