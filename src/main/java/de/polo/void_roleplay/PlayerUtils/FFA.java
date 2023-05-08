package de.polo.void_roleplay.PlayerUtils;

import de.polo.void_roleplay.DataStorage.FFALobbyData;
import de.polo.void_roleplay.DataStorage.FactionData;
import de.polo.void_roleplay.DataStorage.PlayerData;
import de.polo.void_roleplay.Main;
import de.polo.void_roleplay.MySQl.MySQL;
import de.polo.void_roleplay.Utils.FactionManager;
import de.polo.void_roleplay.Utils.ItemManager;
import de.polo.void_roleplay.Utils.LocationManager;
import de.polo.void_roleplay.Utils.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class FFA implements CommandExecutor {
    public static Map<Integer, FFALobbyData> FFAlobbyDataMap = new HashMap<>();

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
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("join")) {
                openFFAMenu(player, 1);
            } else if (args[0].equalsIgnoreCase("leave")) {
                LocationManager.useLocation(player, "ffa");
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
                inv.setItem(i, ItemManager.createItem(Material.LIME_DYE, 1, 0, lobbyData.getDisplayname().replace("&", "§"), "§8 ➥ §eSpieler§8:§7 " + lobbyData.getPlayers() + "/" + lobbyData.getMaxPlayer()));
                ItemMeta meta = inv.getItem(i).getItemMeta();
                meta.getPersistentDataContainer().set(new NamespacedKey(Main.plugin, "id"), PersistentDataType.INTEGER, lobbyData.getId());
                inv.getItem(i).setItemMeta(meta);
                i++;
            }
            inv.setItem(26, ItemManager.createItem(Material.GOLD_NUGGET, 1, 0, "§cNächste Seite", null));
            inv.setItem(18, ItemManager.createItem(Material.NETHER_WART, 1, 0, "§cVorherige Seite", null));
        }
        player.openInventory(inv);
    }

    public static void joinLobby(Player player, int id) {
        player.sendMessage("§8[§6FFA§8]§e FFA ist in Entwicklung, deswegen kannst du noch keiner Lobby beitreten.");
    }
}
