package de.polo.metropiacity.commands;

import de.polo.metropiacity.Main;
import de.polo.metropiacity.Utils.SupportManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class cancelsupportCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        if (SupportManager.ticketCreated(player)) {
            if (!SupportManager.isInConnection(player)) {
                SupportManager.deleteTicket(player);
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
