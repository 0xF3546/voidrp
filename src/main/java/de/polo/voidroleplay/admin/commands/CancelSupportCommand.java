package de.polo.voidroleplay.admin.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.manager.SupportManager;
import de.polo.voidroleplay.utils.Prefix;
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
                player.sendMessage(Prefix.SUPPORT + "Du hast dein Ticket §cgelöscht§7.");
            } else {
                player.sendMessage(Prefix.SUPPORT + "Dein Ticket wird bereits bearbeitet.");
            }
        } else {
            player.sendMessage(Prefix.ERROR + "Du hast kein Ticket erstellt.");
        }
        return false;
    }
}
