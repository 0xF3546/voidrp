package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.manager.AdminManager;
import de.polo.voidroleplay.manager.PlayerManager;
import de.polo.voidroleplay.manager.SupportManager;
import de.polo.voidroleplay.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CloseTicketCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final SupportManager supportManager;
    private final AdminManager adminManager;
    private final Utils utils;
    public CloseTicketCommand(PlayerManager playerManager, SupportManager supportManager, AdminManager adminManager, Utils utils) {
        this.playerManager = playerManager;
        this.supportManager = supportManager;
        this.adminManager = adminManager;
        this.utils = utils;
        Main.registerCommand("closesupport", this);
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (playerData.getPermlevel() >= 40) {
            Player targetplayer = null;
            for (Player players : Bukkit.getOnlinePlayers()) {
                if (supportManager.getTicket(player).getCreator() == players.getUniqueId()) {
                    targetplayer = players;
                    Utils.Tablist.updatePlayer(players);
                }
                if (supportManager.getTicket(player).getEditors().contains(players.getUniqueId()) && player != players) {
                    players.sendMessage(Main.support_prefix + "§aDas Ticket wurde von §2" + player.getName() + "§a geschlossen.");
                    Utils.Tablist.updatePlayer(players);
                }
            }
            if (!supportManager.deleteTicketConnection(player, targetplayer)) {
                player.sendMessage(Main.support_prefix + "Du bearbeitest kein Ticket.");
                return false;
            }
            Utils.Tablist.updatePlayer(player);
            targetplayer.sendMessage(Main.support_prefix + "§c" + playerManager.rang(player) + " " + player.getName() + " hat dein Ticket geschlossen!");
            utils.sendActionBar(targetplayer, "§c§lDein Ticket wurde geschlossen!");
            player.sendMessage(Main.support_prefix + "§aDu hast das Ticket von §2" + targetplayer.getName() + "§a geschlossen.");
            adminManager.sendGuideMessage(player.getName() + " hat das Ticket von " + targetplayer.getName()+ " geschlossen.", ChatColor.YELLOW);
        } else {
            player.sendMessage(Main.error_nopermission);
        }
        return false;
    }
}
