package de.polo.void_roleplay.commands;

import de.polo.void_roleplay.Main;
import de.polo.void_roleplay.Utils.PlayerManager;
import de.polo.void_roleplay.Utils.SupportManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class closeticketCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        if (PlayerManager.isTeam(player)) {
            Player targetplayer = null;
            for (Player players : Bukkit.getOnlinePlayers()) {
                if (SupportManager.getConnection(player).equalsIgnoreCase(players.getUniqueId().toString())) {
                    targetplayer = players;
                }
            }
            SupportManager.deleteTicketConnection(player, targetplayer);
            assert targetplayer != null;
            targetplayer.sendMessage(Main.support_prefix + "§c" + PlayerManager.rang(player) + " " + player.getName() + " hat dein Ticket geschlossen!");
            player.sendMessage(Main.support_prefix + "§aDu hast das Ticket von §2" + targetplayer.getName() + "§a geschlossen.");
        } else {
            player.sendMessage(Main.error_nopermission);
        }
        return false;
    }
}
