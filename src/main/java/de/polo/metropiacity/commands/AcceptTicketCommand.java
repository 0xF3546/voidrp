package de.polo.metropiacity.commands;

import de.polo.metropiacity.dataStorage.PlayerData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.utils.AdminManager;
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
    private final PlayerManager playerManager;
    private final AdminManager adminManager;
    private final SupportManager supportManager;
    private final Utils utils;
    public AcceptTicketCommand(PlayerManager playerManager, AdminManager adminManager, SupportManager supportManager, Utils utils) {
        this.playerManager = playerManager;
        this.adminManager = adminManager;
        this.supportManager = supportManager;
        this.utils = utils;
        Main.registerCommand("acceptsupport", this);
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (playerData.getPermlevel() >= 40) {
            if (args.length >= 1) {
                Player targetplayer = Bukkit.getPlayer(args[0]);
                if (targetplayer != null) {
                    if (!supportManager.isInConnection(player)) {
                        if (supportManager.ticketCreated(targetplayer)) {
                            supportManager.createTicketConnection(player, targetplayer);
                            targetplayer.sendMessage(Main.support_prefix + "§c" + playerManager.rang(player) + " " + player.getName() + "§7 bearbeitet nun dein Ticket!");
                            player.sendMessage(Main.support_prefix + "Du bearbeitest nun das Ticket von §c" + targetplayer.getName() + "§7.");
                            adminManager.send_message(player.getName() + " bearbeitet nun das Ticket von " + targetplayer.getName()+ ".", ChatColor.YELLOW);
                            utils.sendActionBar(targetplayer, "§a§lDein Ticket wurde angenommen!");
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
