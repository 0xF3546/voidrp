package de.polo.metropiacity.commands;

import de.polo.metropiacity.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class linkCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;
        player.sendMessage(Main.error + "§cDu kannst dein Forum noch nicht vernknüpfen.");
        return false;
    }
}
