package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.game.events.SubmitChatEvent;
import de.polo.voidroleplay.manager.inventory.CustomItem;
import de.polo.voidroleplay.manager.inventory.InventoryManager;
import de.polo.voidroleplay.manager.ItemManager;
import de.polo.voidroleplay.manager.LocationManager;
import de.polo.voidroleplay.manager.PlayerManager;
import de.polo.voidroleplay.manager.ServerManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.sql.SQLException;

public class NachrichtenCommand implements CommandExecutor, Listener {
    private final PlayerManager playerManager;
    private final LocationManager locationManager;

    public NachrichtenCommand(PlayerManager playerManager, LocationManager locationManager) {
        this.playerManager = playerManager;
        this.locationManager = locationManager;
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
        Main.registerCommand("nachrichten", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        if (locationManager.getDistanceBetweenCoords(player, "nachrichten") < 5) {
            PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
            InventoryManager inventoryManager = new InventoryManager(player, 27, "§8 » §6Nachrichtengebäude", true, true);
            inventoryManager.setItem(new CustomItem(11, ItemManager.createItem(Material.GREEN_DYE, 1, 0, "§2Werbung schalten", "§8 ➥ §7" + ServerManager.getPayout("werbung") + "$/Zeichen")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    playerData.setVariable("chatblock", "werbung");
                    player.sendMessage("§8[§2Werbung§8]§7 Gib nun deine Nachricht in den Chat ein.");
                    player.closeInventory();
                }
            });
            if (playerData.getFaction().equals("News")) {
                if (playerData.getFactionGrade() >= 2) {
                    inventoryManager.setItem(new CustomItem(15, ItemManager.createItem(Material.YELLOW_DYE, 1, 0, "§6Nachricht schalten")) {
                        @Override
                        public void onClick(InventoryClickEvent event) {
                            playerData.setVariable("chatblock", "news");
                            player.sendMessage("§8[§6News§8]§7 Gib nun deine Nachricht in den Chat ein.");
                            player.closeInventory();
                        }
                    });
                } else {
                    inventoryManager.setItem(new CustomItem(15, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§6§mNachricht schalten", "§8 ➥ §7Du musst mindestens Rang 2 sein.")) {
                        @Override
                        public void onClick(InventoryClickEvent event) {

                        }
                    });
                }
            } else {
                inventoryManager.setItem(new CustomItem(15, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§6§mNachricht schalten", "§8 ➥ §7Du bist kein Mitglied des Nachrichtendienstes")) {
                    @Override
                    public void onClick(InventoryClickEvent event) {

                    }
                });
            }
        } else {
            player.sendMessage(Prefix.ERROR + "Du bist nicht am Nachrichtengebäude.");
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
                playerManager.removeMoney(player, price, "Werbung");
            } else {
                player.sendMessage(Prefix.ERROR + "Du benötigst " + price + "$.");
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
