package de.polo.metropiacity.commands;

import de.polo.metropiacity.Main;
import de.polo.metropiacity.PlayerUtils.ChatUtils;
import de.polo.metropiacity.Utils.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class friskCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        if (args.length >= 1) {
            Player targetplayer = Bukkit.getPlayer(args[0]);
            if (targetplayer != null) {
                if (!targetplayer.getName().equals(player.getName())) {
                    if (player.getLocation().distance(targetplayer.getLocation()) < 5) {
                        if (!PlayerManager.canPlayerMove(targetplayer)) {
                            player.openInventory(targetplayer.getInventory());
                            ChatUtils.sendGrayMessageAtPlayer(player, player.getName() + " durchsucht " + targetplayer.getName());
                        } else {
                            player.sendMessage(Main.error + targetplayer.getName() + " ist nicht gefesselt oder in Handschellen.");
                        }
                    } else {
                        player.sendMessage(Main.error + targetplayer.getName() + " ist nicht in deiner nähe.");
                    }
                } else {
                    player.sendMessage(Main.error + "Du kannst dich nicht selbst durchsuchen.");
                }
            } else {
                player.sendMessage(Main.error + targetplayer.getName() + " ist nicht online.");
            }
        } else {
            player.sendMessage(Main.error + "Syntax-Fehler: /frisk [Spieler]");
        }
        return false;
    }
}
