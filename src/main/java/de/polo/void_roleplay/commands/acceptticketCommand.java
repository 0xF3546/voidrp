package de.polo.void_roleplay.commands;

import de.polo.void_roleplay.Main;
import de.polo.void_roleplay.Utils.PlayerManager;
import de.polo.void_roleplay.Utils.SupportManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class acceptticketCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        if (PlayerManager.isTeam(player) || PlayerManager.rang(player).equalsIgnoreCase("Assistent")) {
            if (args.length >= 1) {
                Player targetplayer = Bukkit.getPlayer(args[0]);
                if (targetplayer.isOnline()) {
                    if (!SupportManager.isInConnection(player)) {
                        if (SupportManager.ticketCreated(targetplayer)) {
                            SupportManager.createTicketConnection(player, targetplayer);
                            targetplayer.sendMessage(Main.support_prefix + "§c" + PlayerManager.rang(player) + " " + player.getName() + "§7 bearbeitet nun dein Ticket!");
                            player.sendMessage(Main.support_prefix + "Du bearbeitest nun das Ticket von §c" + targetplayer.getName() + "§7.");
                        } else {
                            player.sendMessage(Main.support_prefix + "§c" + targetplayer.getName() + "§7 hat kein Ticket erstellt.");
                        }
                    } else {
                        player.sendMessage(Main.error + "Du bearbeitest bereits ein Ticket.");
                    }
                } else {
                    player.sendMessage(Main.support_prefix + "§c" + args[0] + "§7 ist §cnicht §7online.");
                }
            } else {
                player.sendMessage(Main.support_prefix + "Syntax-Fehler: /acceptsupport [Spieler]");
            }
        } else {
            player.sendMessage(Main.error_nopermission);
        }
        return false;
    }
}
