package de.polo.void_roleplay.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class oocCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        StringBuilder message = new StringBuilder();
        for (String arg : args) {
            message.append(" ").append(arg);
        }
        for (Player players : Bukkit.getOnlinePlayers()) {
            if (player.getLocation().distance(players.getLocation()) <= 5) {
                players.sendMessage("§8[§cOOC§8] §c" + player.getName() + "§8: §7" + message);
            }
        }
     return false;
    }
}
