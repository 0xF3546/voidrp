package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.utils.SupportManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CancelSupportCommand implements CommandExecutor {
    private final SupportManager supportManager;
    public CancelSupportCommand(SupportManager supportManager) {
        this.supportManager = supportManager;
        Main.registerCommand("cancelsupport", this);
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        if (supportManager.ticketCreated(player)) {
            if (supportManager.isInConnection(player)) {
                supportManager.deleteTicket(player);
                player.sendMessage(Main.support_prefix + "Du hast dein Ticket §cgelöscht§7.");
            } else {
                player.sendMessage(Main.support_prefix + "Dein Ticket wird bereits bearbeitet.");
            }
        } else {
            player.sendMessage(Main.error + "Du hast kein Ticket erstellt.");
        }
        return false;
    }
}
