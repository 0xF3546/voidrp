package de.polo.metropiacity.commands;

import de.polo.metropiacity.Main;
import de.polo.metropiacity.utils.playerUtils.ChatUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MeCommand implements CommandExecutor {
    public MeCommand() {
        Main.registerCommand("me", this);
    }
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
