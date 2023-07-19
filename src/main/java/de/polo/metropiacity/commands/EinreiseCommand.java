package de.polo.metropiacity.commands;

import de.polo.metropiacity.DataStorage.PlayerData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.Utils.ItemManager;
import de.polo.metropiacity.Utils.LocationManager;
import de.polo.metropiacity.Utils.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class EinreiseCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        if (playerData.getFirstname() == null || playerData.getLastname() == null) {
            openEinrese(player);
        } else {
            player.sendMessage(Main.error + "Du hast bereits deine Papiere erhalten.");
        }
        return false;
    }

    public static void openEinrese(Player player) {
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        Inventory inv = Bukkit.createInventory(player, 9, "§8 » §6MetropiaCity Einreiseamt");
        if (LocationManager.getDistanceBetweenCoords(player, "einreise") < 10) {
            if (playerData.getVariable("einreise_firstname") == null) {
                inv.setItem(1, ItemManager.createItem(Material.PAPER, 1, 0, "§eVorname", null));
            } else {
                inv.setItem(1, ItemManager.createItem(Material.PAPER, 1, 0, "§e" + playerData.getVariable("einreise_firstname"), null));
            }
            if (playerData.getVariable("einreise_lastname") == null) {
                inv.setItem(2, ItemManager.createItem(Material.PAPER, 1, 0, "§eNachname", null));
            } else {
                inv.setItem(2, ItemManager.createItem(Material.PAPER, 1, 0, "§e" + playerData.getVariable("einreise_lastname"), null));
            }
            if (playerData.getVariable("einreise_gender") == null) {
                inv.setItem(4, ItemManager.createItem(Material.PAPER, 1, 0, "§eGeschlecht", null));
            } else {
                inv.setItem(4, ItemManager.createItem(Material.PAPER, 1, 0, "§e" + playerData.getVariable("einreise_gender"), null));
            }
            ItemMeta meta = inv.getItem(4).getItemMeta();
            meta.setLore(Arrays.asList("§7 ➥ §8[§6Linksklick§8]§7 Männlich", "§7 ➥ §8[§6Rechtsklick§8]§7 Weiblich"));
            inv.getItem(4).setItemMeta(meta);
            if (playerData.getVariable("einreise_dob") == null) {
                inv.setItem(5, ItemManager.createItem(Material.PAPER, 1, 0, "§eGeburtstag", null));
            } else {
                inv.setItem(5, ItemManager.createItem(Material.PAPER, 1, 0, "§e" + playerData.getVariable("einreise_dob"), null));
            }
            inv.setItem(8, ItemManager.createItem(Material.EMERALD, 1, 0, "§aBestätigen", null));
            playerData.setVariable("current_inventory", "einreise");
            player.openInventory(inv);
        }
    }
}
