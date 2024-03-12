package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.utils.ItemManager;
import de.polo.voidroleplay.utils.LocationManager;
import de.polo.voidroleplay.utils.PlayerManager;
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
    private final PlayerManager playerManager;
    private final LocationManager locationManager;
    public EinreiseCommand(PlayerManager playerManager, LocationManager locationManager) {
        this.playerManager = playerManager;
        this.locationManager = locationManager;
        Main.registerCommand("einreise", this);
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (playerData.getFirstname() == null || playerData.getLastname() == null) {
            openEinrese(player);
        } else {
            player.sendMessage(Main.error + "Du hast bereits deine Papiere erhalten.");
        }
        return false;
    }

    public void openEinrese(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        Inventory inv = Bukkit.createInventory(player, 9, "§8 » §6VoidRoleplay Einreiseamt");
        if (locationManager.getDistanceBetweenCoords(player, "einreise") < 10) {
            if (playerData.getVariable("einreise_firstname") == null) {
                inv.setItem(1, ItemManager.createItem(Material.PAPER, 1, 0, "§eVorname"));
            } else {
                inv.setItem(1, ItemManager.createItem(Material.PAPER, 1, 0, "§e" + playerData.getVariable("einreise_firstname")));
            }
            if (playerData.getVariable("einreise_lastname") == null) {
                inv.setItem(2, ItemManager.createItem(Material.PAPER, 1, 0, "§eNachname"));
            } else {
                inv.setItem(2, ItemManager.createItem(Material.PAPER, 1, 0, "§e" + playerData.getVariable("einreise_lastname")));
            }
            if (playerData.getVariable("einreise_gender") == null) {
                inv.setItem(4, ItemManager.createItem(Material.PAPER, 1, 0, "§eGeschlecht"));
            } else {
                inv.setItem(4, ItemManager.createItem(Material.PAPER, 1, 0, "§e" + playerData.getVariable("einreise_gender")));
            }
            ItemMeta meta = inv.getItem(4).getItemMeta();
            meta.setLore(Arrays.asList("§7 ➥ §8[§6Linksklick§8]§7 Männlich", "§7 ➥ §8[§6Rechtsklick§8]§7 Weiblich"));
            inv.getItem(4).setItemMeta(meta);
            if (playerData.getVariable("einreise_dob") == null) {
                inv.setItem(5, ItemManager.createItem(Material.PAPER, 1, 0, "§eGeburtstag"));
            } else {
                inv.setItem(5, ItemManager.createItem(Material.PAPER, 1, 0, "§e" + playerData.getVariable("einreise_dob")));
            }
            inv.setItem(8, ItemManager.createItem(Material.EMERALD, 1, 0, "§aBestätigen"));
            playerData.setVariable("current_inventory", "einreise");
            player.openInventory(inv);
        }
    }
}
