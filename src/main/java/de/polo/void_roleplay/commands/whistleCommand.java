package de.polo.void_roleplay.commands;

import de.polo.void_roleplay.Main;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class whistleCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        if (args.length >= 1) {
            StringBuilder message = new StringBuilder(args[0]);
            for (String arg : args) {
                message.append(" ").append(arg);
            }
            for (Player players : Bukkit.getOnlinePlayers()) {
                if (player.getLocation().distance(players.getLocation()) <= 3) {
                    players.sendMessage("§8" + player.getName() + " flüstert§8:§8 " + message);
                }
            }
        } else {
            player.sendMessage(Main.error + "Syntax-Fehler: /whistle [Nachricht]");
        }
        return false;
    }
}
