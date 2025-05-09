package de.polo.core.commands;

import de.polo.api.Utils.inventorymanager.CustomItem;
import de.polo.api.Utils.inventorymanager.InventoryManager;
import de.polo.api.VoidAPI;
import de.polo.core.Main;
import de.polo.core.location.services.LocationService;
import de.polo.core.news.entities.CoreAdvertisement;
import de.polo.core.news.services.NewsService;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.game.events.SubmitChatEvent;
import de.polo.core.manager.ItemManager;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.manager.ServerManager;
import de.polo.core.utils.Prefix;
import net.kyori.adventure.text.Component;
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

import static de.polo.core.Main.factionManager;

public class NachrichtenCommand implements CommandExecutor, Listener {
    private final PlayerManager playerManager;

    public NachrichtenCommand(PlayerManager playerManager) {
        this.playerManager = playerManager;
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
        Main.registerCommand("nachrichten", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        LocationService locationService = VoidAPI.getService(LocationService.class);
        if (locationService.getDistanceBetweenCoords(player, "nachrichten") < 5) {
            PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
            InventoryManager inventoryManager = new InventoryManager(player, 27, Component.text("§8 » §6Nachrichtengebäude"), true, true);
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
                NewsService newsService = VoidAPI.getService(NewsService.class);
                newsService.addAdvertisementQueue(new CoreAdvertisement(VoidAPI.getPlayer(event.getPlayer()), event.getMessage()));
                player.sendMessage("§8[§2Werbung§8]§a Werbung wird nun geprüft. §c-" + price + "$");
                playerManager.removeMoney(player, price, "Werbung");
                factionManager.addFactionMoney("News", price, "Werbung - " + player.getName());
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
                Bukkit.broadcast(Component.text("§8[§6News§8]§a " + player.getName() + ": " + event.getMessage()));
            }
            event.end();
        }
    }
}
