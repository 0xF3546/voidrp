package de.polo.metropiacity.commands;

import de.polo.metropiacity.PlayerUtils.ChatUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class meCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        StringBuilder message = new StringBuilder(player.getName());
        for (String arg : args) {
            message.append(" ").append(arg);
        }
        ChatUtils.sendMeMessageAtPlayer(player, message.toString());
     return false;
    }
}
