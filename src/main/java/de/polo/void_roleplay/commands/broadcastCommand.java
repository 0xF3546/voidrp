package de.polo.void_roleplay.commands;

import de.polo.void_roleplay.Utils.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class broadcastCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        StringBuilder message = new StringBuilder();
        for (String arg : args) {
            message.append(" ").append(arg);
        }
        if (PlayerManager.perms(player) >= 70) {
            Bukkit.broadcastMessage(" ");
            Bukkit.broadcastMessage("§7====§8[§c§lAnkündigung§8]§7====");
            Bukkit.broadcastMessage(" ");
            Bukkit.broadcastMessage("§8➥§c " + player.getName() + "§8: §7" + message);
            Bukkit.broadcastMessage(" ");
            Bukkit.broadcastMessage("§7====§8[§c§lAnkündigung§8]§7====");
            Bukkit.broadcastMessage(" ");
        }
        return false;
    }
}
