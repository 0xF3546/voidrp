package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.dataStorage.Ticket;
import de.polo.voidroleplay.utils.InventoryManager.CustomItem;
import de.polo.voidroleplay.utils.InventoryManager.InventoryManager;
import de.polo.voidroleplay.utils.ItemManager;
import de.polo.voidroleplay.utils.PlayerManager;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.utils.SupportManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

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
            player.sendMessage(Prefix.error_nopermission);
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
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8 » §e" + target.getName() + "'s Ticket");
        inventoryManager.setItem(new CustomItem(0, ItemManager.createItemHead(target.getUniqueId().toString(), 1, 0, "§6" + target.getName(), "§8 ➥ §eTicket-Ersteller")) {
            @Override
            public void onClick(InventoryClickEvent event) {

            }
        });
        int i = 1;
        for (Player p : Bukkit.getOnlinePlayers()) {
            PlayerData pData = playerManager.getPlayerData(p);
            if (pData.getPermlevel() >= 40) {
                inventoryManager.setItem(new CustomItem(i, ItemManager.createItemHead(p.getUniqueId().toString(), 1, 0, "§6" + p.getName(), "§8 ➥ §e" + pData.getRang())) {
                    @Override
                    public void onClick(InventoryClickEvent event) {

                    }
                });
                i++;
            }
        }
        inventoryManager.setItem(new CustomItem(25, ItemManager.createItem(Material.REDSTONE, 1, 0, "§cTicket schließen")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                player.performCommand("closesupport");
            }
        });
        inventoryManager.setItem(new CustomItem(24, ItemManager.createItem(Material.GREEN_DYE, 1, 0, "§cTicket verlassen")) {
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
                        player.sendMessage(Main.support_prefix + player.getName() + " hat das Ticket verlassen.");
                    }
                }
            }
        });
        return false;
    }
}
