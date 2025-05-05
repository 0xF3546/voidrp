package de.polo.core.admin.commands;

import de.polo.api.Utils.inventorymanager.CustomItem;
import de.polo.api.Utils.inventorymanager.InventoryManager;
import de.polo.api.VoidAPI;
import de.polo.core.Main;
import de.polo.core.admin.services.AdminService;
import de.polo.core.location.services.LocationService;
import de.polo.core.location.services.NavigationService;
import de.polo.core.storage.NaviData;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.storage.Ticket;
import de.polo.core.manager.ItemManager;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.manager.SupportManager;
import de.polo.core.utils.Prefix;
import de.polo.core.utils.Utils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class TicketCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final SupportManager supportManager;

    public TicketCommand(PlayerManager playerManager, SupportManager supportManager) {
        this.playerManager = playerManager;
        this.supportManager = supportManager;

        Main.registerCommand("ticket", this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player);
        Ticket ticket = supportManager.getTicket(player);
        if (!playerManager.isTeam(player)) {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
            return false;
        }
        if (ticket == null) {
            player.sendMessage(Prefix.ERROR + "Du bist in keinem Ticket.");
            return false;
        }
        Player target = null;
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getUniqueId().equals(ticket.getCreator())) target = p;
        }
        if (target == null) {
            player.sendMessage(Prefix.ERROR + "Spieler ist nicht mehr online.");
            return false;
        }
        InventoryManager inventoryManager = new InventoryManager(player, 27, Component.text("§8 » §e" + target.getName() + "'s Ticket"));
        Player finalTarget = target;
        AdminService adminService = VoidAPI.getService(AdminService.class);
        NavigationService navigationService = VoidAPI.getService(NavigationService.class);
        LocationService locationService = VoidAPI.getService(LocationService.class);
        inventoryManager.setItem(new CustomItem(0, ItemManager.createItemHead(finalTarget.getUniqueId().toString(), 1, 0, "§6" + finalTarget.getName(), Arrays.asList("§8 ➥ §eTicket-Ersteller", "§8 ➥ §7[§6Linksklick§7]§e Zum Spieler teleportieren", "§8 ➥ §7[§6Rechtsklick§7]§e Spieler zum nächsten Navi teleportieren"))) {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (event.isLeftClick()) {
                    player.teleport(finalTarget.getLocation());
                    player.sendMessage(Prefix.SUPPORT + "Du hast dich zu " + finalTarget.getName() + " teleportiert.");
                    adminService.sendGuideMessage(player.getName() + " hat sich zu " + finalTarget.getName() + " teleportiert.", Color.AQUA);
                } else {
                    NaviData nearest = navigationService.getNearestNaviPoint(finalTarget.getLocation());
                    finalTarget.teleport(locationService.getLocation(nearest.getLocation()));
                    player.sendMessage(Prefix.SUPPORT + "Du hast " + finalTarget.getName() + " zu " + nearest.getName() + "§7 teleportiert.");
                    adminService.sendGuideMessage(player.getName() + " hat " + finalTarget.getName() + " teleportiert. - " + nearest.getName().replace("&", "§"), Color.AQUA);
                }
            }
        });
        int i = 1;
        for (Player p : Bukkit.getOnlinePlayers()) {
            PlayerData pData = playerManager.getPlayerData(p);
            if (pData.getPermlevel() >= 40 && ticket.getEditors().contains(p.getUniqueId())) {
                inventoryManager.setItem(new CustomItem(i, ItemManager.createItemHead(p.getUniqueId().toString(), 1, 0, "§6" + p.getName(), "§8 ➥ §e" + pData.getRang())) {
                    @Override
                    public void onClick(InventoryClickEvent event) {

                    }
                });
                i++;
            }
        }
        inventoryManager.setItem(new CustomItem(26, ItemManager.createItem(Material.REDSTONE, 1, 0, "§cTicket schließen")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                player.performCommand("closesupport");
            }
        });
        inventoryManager.setItem(new CustomItem(25, ItemManager.createItem(Material.GREEN_DYE, 1, 0, "§cTicket verlassen")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (playerData.getPermlevel() < 40) {
                    return;
                }
                if (ticket.getEditors().size() <= 1) {
                    player.sendMessage(Prefix.ERROR + "Du musst das Ticket schließen, da kein Teammitglied sonst im Ticket ist.");
                    return;
                }
                player.closeInventory();
                List<UUID> editors = ticket.getEditors();
                editors.remove(player.getUniqueId());
                ticket.setEditors(editors);
                for (Player editor : Bukkit.getOnlinePlayers()) {
                    if (ticket.getEditors().contains(editor.getUniqueId()) || ticket.getCreator().equals(editor.getUniqueId())) {
                        editor.sendMessage(Prefix.SUPPORT + player.getName() + " hat das Ticket verlassen.");
                    }
                }
                Utils.Tablist.updatePlayer(player);
            }
        });
        return false;
    }
}
