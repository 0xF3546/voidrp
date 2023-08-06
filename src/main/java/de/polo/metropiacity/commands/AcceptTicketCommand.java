package de.polo.metropiacity.commands;

import de.polo.metropiacity.dataStorage.PlayerData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.utils.PlayerManager;
import de.polo.metropiacity.utils.SupportManager;
import de.polo.metropiacity.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AcceptTicketCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        if (playerData.getPermlevel() >= 40) {
            if (args.length >= 1) {
                Player targetplayer = Bukkit.getPlayer(args[0]);
                if (targetplayer != null) {
                    if (!SupportManager.isInConnection(player)) {
                        if (SupportManager.ticketCreated(targetplayer)) {
                            SupportManager.createTicketConnection(player, targetplayer);
                            targetplayer.sendMessage(Main.support_prefix + "§c" + PlayerManager.rang(player) + " " + player.getName() + "§7 bearbeitet nun dein Ticket!");
                            player.sendMessage(Main.support_prefix + "Du bearbeitest nun das Ticket von §c" + targetplayer.getName() + "§7.");
                            ADutyCommand.send_message(player.getName() + " bearbeitet nun das Ticket von " + targetplayer.getName()+ ".", ChatColor.YELLOW);
                            Utils.sendActionBar(targetplayer, "§a§lDein Ticket wurde angenommen!");
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
