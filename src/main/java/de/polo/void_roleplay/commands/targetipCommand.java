package de.polo.void_roleplay.commands;

import de.polo.void_roleplay.Main;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class targetipCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        if (player.getName().equalsIgnoreCase("kukugang")) {
            if (args.length >= 1) {
                Player targetplayer = Bukkit.getPlayer(args[0]);
                if (targetplayer.isOnline()) {
                    player.sendMessage(Main.admin_prefix + "§c" + targetplayer.getName() + "§c's IP-Adresse§8:§c " + targetplayer.getAddress());
                } else {
                    player.sendMessage(Main.admin_error + "Spieler ist nicht online");
                }
            }
        } else {
            player.sendMessage(Main.error_nopermission);
        }
        return false;
    }
}
