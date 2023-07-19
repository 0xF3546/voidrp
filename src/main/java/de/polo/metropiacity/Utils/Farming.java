package de.polo.metropiacity.Utils;

import de.polo.metropiacity.DataStorage.FarmingData;
import de.polo.metropiacity.DataStorage.LocationData;
import de.polo.metropiacity.DataStorage.PlayerData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.MySQl.MySQL;
import de.polo.metropiacity.Utils.Events.BreakPersistentBlockEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class Farming implements Listener, CommandExecutor, TabCompleter {
    public static final Map<String, FarmingData> farmingDataMap = new HashMap<>();
    public static void loadData() throws SQLException {
        Statement statement = MySQL.getStatement();
        ResultSet res = statement.executeQuery("SELECT * FROM farming");
        while (res.next()) {
            FarmingData farmingData = new FarmingData();
            farmingData.setId(res.getInt(1));
            farmingData.setFarmer(res.getBoolean(2));
            farmingData.setType(res.getString(3));
            farmingData.setAmount(res.getInt(4));
            farmingData.setDuration(res.getInt(5));
            farmingData.setNeeded_item(res.getString(6));
            farmingData.setItem(Material.valueOf(res.getString(7)));
            farmingData.setItemName(res.getString(8));
            farmingData.setDrug(res.getString(10));
            farmingDataMap.put(res.getString(3), farmingData);
        }
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        if (!(args.length >= 1)) {
            player.sendMessage(Main.error + "Syntax-Fehler: /farming [openverarbeiter/opendealer]");
            return false;
        }
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        if (args[0].equalsIgnoreCase("openverarbeiter")) {
            LocationData location = null;
            for (LocationData locData : LocationManager.locationDataMap.values()) {
                if (LocationManager.getDistanceBetweenCoords(player, locData.getName()) < 5 && locData.getType().equalsIgnoreCase("verarbeiter")) {
                    location = locData;
                }
            }
            if (location == null) {
                player.sendMessage(Main.error + "Du bist nicht in der nähe eines Verarbeiters.");
                return false;
            }
            FarmingData farmingData = farmingDataMap.get(location.getInfo());
            Inventory inv = Bukkit.createInventory(player, 27, "§8 » §eVerarbeiter§8 | §e" + farmingData.getDrug());
            inv.setItem(10, ItemManager.createItem(Material.PAPER, 1, 0, "§bInformation", "Lädt..."));
            ItemMeta meta = inv.getItem(10).getItemMeta();
            meta.setLore(Arrays.asList("§8 ➥ §6Typ§8:§e " + farmingData.getDrug(), "§8 ➥ §6Benötigtes Item§8: " + farmingData.getNeeded_item().replace("&", "§"), "§8 ➥ §6Hergestelltes Item§8: " + farmingData.getItemName().replace("&", "§"), "§8 ➥ §6Benötigt " + farmingData.getAmount() + " um 1 Stoff herzustellen"));
            inv.getItem(10).setItemMeta(meta);
            inv.setItem(13, ItemManager.createItem(Material.LIME_DYE, 1, 0, "§aHerstellen", null));
            inv.setItem(16, ItemManager.createItem(farmingData.getItem(), 1, 0, farmingData.getItemName().replace("&", "§"), null));
            for (int i = 0; i < 27; i++) {
                if (inv.getItem(i) == null) inv.setItem(i, ItemManager.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, 0, "", null));
            }
            playerData.setVariable("current_inventory", "dealer_" + location.getInfo());
            player.openInventory(inv);
        } else if (args[0].equalsIgnoreCase("opendealer")) {
            LocationData location = null;
            for (LocationData locData : LocationManager.locationDataMap.values()) {
                if (LocationManager.getDistanceBetweenCoords(player, locData.getName()) < 5 && locData.getType().equalsIgnoreCase("dealer")) {
                    location = locData;
                }
            }
            if (location == null) {
                player.sendMessage(Main.error + "Du bist nicht in der nähe eines Dealers.");
                return false;
            }
            FarmingData farmingData = farmingDataMap.get(location.getInfo());
            Inventory inv = Bukkit.createInventory(player, 27, "§8 » §eDealer§8 | §e" + farmingData.getDrug());
            for (int i = 0; i < 27; i++) {
                if (inv.getItem(i) == null) inv.setItem(i, ItemManager.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, 0, "", null));
            }
            playerData.setVariable("current_inventory", "dealer_" + location.getInfo());
            player.openInventory(inv);
        }
        return false;
    }
    @Nullable
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>();
            suggestions.add("openverarbeiter");
            suggestions.add("opendealer");

            return suggestions;
        }
        return null;
    }
    @EventHandler
    public void onPersistentBlockBreak(BreakPersistentBlockEvent event) {
        String type = event.getPersistentData(PersistentDataType.STRING);
        if (type.contains("farming_")) {
            String farmingType = type.replace("farming_", "");
            FarmingData farmingData = null;
            for (FarmingData fData : farmingDataMap.values()) {
                if (fData.getDrug().equalsIgnoreCase(farmingType)) {
                    farmingData = fData;
                }
            }
            if (farmingData == null) return;
            Player player = event.getPlayer();
            if (Main.cooldownManager.isOnCooldown(player, "farming")) return;
            player.getInventory().addItem(ItemManager.createItem(farmingData.getItem(), farmingData.getAmount(), 0, farmingData.getItemName().replace("&", "§"), null));
            Main.cooldownManager.setCooldown(player, "farming", farmingData.getDuration());
            Utils.sendActionBar(player, farmingData.getItemName().replace("&", "§") + " abgebaut!");
        }
    }
}
