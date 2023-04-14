package de.polo.void_roleplay.commands;

import de.polo.void_roleplay.DataStorage.PlayerData;
import de.polo.void_roleplay.Main;
import de.polo.void_roleplay.Utils.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class kickCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        if (playerData.getPermlevel() >= 70) {
            if (args.length > 1) {
                Player targetplayer = Bukkit.getPlayer(args[0]);
                if (targetplayer != null && targetplayer.isOnline()) {
                    StringBuilder message = new StringBuilder();
                    for (int i = 1; i < args.length; i++) {
                        message.append(" ").append(args[i]);
                    }
                    PlayerManager.kickPlayer(targetplayer, String.valueOf(message));
                    Bukkit.broadcastMessage("§c" + playerData.getRang() + " " + player.getName() + " hat " + targetplayer.getName() + " gekickt. Grund: " + args[1]);
                } else {
                    player.sendMessage(Main.admin_error + args[0] + " ist nicht online.");
                }
            } else {
                player.sendMessage(Main.admin_error + "Syntax-Fehler: /kick [Spieler] [Grund]");
            }
        } else {
            player.sendMessage(Main.error_nopermission);
        }
        return false;
    }
}
