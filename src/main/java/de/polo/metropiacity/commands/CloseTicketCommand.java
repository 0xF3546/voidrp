package de.polo.metropiacity.commands;

import de.polo.metropiacity.dataStorage.PlayerData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.utils.PlayerManager;
import de.polo.metropiacity.utils.SupportManager;
import de.polo.metropiacity.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CloseTicketCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        if (playerData.getPermlevel() >= 40) {
            Player targetplayer = null;
            for (Player players : Bukkit.getOnlinePlayers()) {
                if (SupportManager.getConnection(player).equalsIgnoreCase(players.getUniqueId().toString())) {
                    targetplayer = players;
                }
            }
            if (!SupportManager.deleteTicketConnection(player, targetplayer)) {
                player.sendMessage(Main.support_prefix + "Du bearbeitest kein Ticket.");
                return false;
            }
            targetplayer.sendMessage(Main.support_prefix + "§c" + PlayerManager.rang(player) + " " + player.getName() + " hat dein Ticket geschlossen!");
            Utils.sendActionBar(targetplayer, "§c§lDein Ticket wurde geschlossen!");
            player.sendMessage(Main.support_prefix + "§aDu hast das Ticket von §2" + targetplayer.getName() + "§a geschlossen.");
            ADutyCommand.send_message(player.getName() + " hat das Ticket von " + targetplayer.getName()+ " geschlossen.");
        } else {
            player.sendMessage(Main.error_nopermission);
        }
        return false;
    }
}
