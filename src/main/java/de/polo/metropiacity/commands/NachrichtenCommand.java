package de.polo.metropiacity.commands;

import de.polo.metropiacity.DataStorage.PlayerData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.Utils.*;
import de.polo.metropiacity.Utils.Events.SubmitChatEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;

import java.sql.SQLException;

public class NachrichtenCommand implements CommandExecutor, Listener {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        if (LocationManager.getDistanceBetweenCoords(player, "nachrichten") < 5) {
            PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
            playerData.setVariable("current_inventory", "news");
            Inventory inv = Bukkit.createInventory(player, 27, "§8 » §6Nachrichtengebäude");
            inv.setItem(11, ItemManager.createItem(Material.GREEN_DYE, 1, 0, "§2Werbung schalten", "§8 ➥ §7" + ServerManager.getPayout("werbung") + "$/Zeichen"));
            if (playerData.getFaction().equals("News")) {
                if (playerData.getFactionGrade() >= 2) {
                    inv.setItem(15, ItemManager.createItem(Material.YELLOW_DYE, 1, 0, "§6Nachricht schalten", null));
                } else {
                    inv.setItem(15, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§6§mNachricht schalten", "§8 ➥ §7Du musst mindestens Rang 2 sein."));
                }
            } else {
                inv.setItem(15, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§6§mNachricht schalten", "§8 ➥ §7Du bist kein Mitglied des Nachrichtendienstes"));
            }
            for (int i = 0; i < 27; i++) {
                if (inv.getItem(i) == null) {
                    inv.setItem(i, ItemManager.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, 0, "§8", null));
                }
            }
            player.openInventory(inv);
        } else {
            player.sendMessage(Main.error + "Du bist nicht am Nachrichtengebäude.");
        }
        return false;
    }

    @EventHandler
    public void onChatSubmit(SubmitChatEvent event) throws SQLException {
        Player player = event.getPlayer();
        if (event.getSubmitTo().equalsIgnoreCase("werbung")) {
            if (event.isCancel()) {
                event.sendCancelMessage();
                event.end();
                return;
            }
            int price = event.getMessage().length() * ServerManager.getPayout("werbung");
            if (event.getPlayerData().getBargeld() >= price) {
                player.sendMessage("§8[§2Werbung§8]§a Werbung erfolgreich geschalten. §c-" + price + "$");
                Bukkit.broadcastMessage("§8[§2Werbung§8] §7" + player.getName() + "§8:§f " + event.getMessage());
                PlayerManager.removeMoney(player, price, "Werbung");
            } else {
                player.sendMessage(Main.error + "Du benötigst " + price + "$.");
            }
            event.end();
        }
        if (event.getSubmitTo().equalsIgnoreCase("news")) {
            if (event.isCancel()) {
                event.sendCancelMessage();
                event.end();
                return;
            }
            if (event.getPlayerData().getFaction().equals("News")) {
                player.sendMessage("§8[§6News§8]§a News erfolgreich geschalten.");
                Bukkit.broadcastMessage("§8[§6News§8] §7" + player.getName() + "§8:§f " + event.getMessage());
            }
            event.end();
        }
    }
}
