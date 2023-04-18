package de.polo.void_roleplay.commands;

import de.polo.void_roleplay.Main;
import de.polo.void_roleplay.Utils.PhoneUtils;
import de.polo.void_roleplay.Utils.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class callCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        if (PhoneUtils.hasPhone(player)) {
            if (args.length >= 1) {
                for (Player players : Bukkit.getOnlinePlayers()) {
                    if (PlayerManager.playerDataMap.get(players.getUniqueId().toString()).getNumber() == Integer.parseInt(args[0])) {
                        if (PhoneUtils.getConnection(players) == null) {
                            PhoneUtils.callNumber(player, PlayerManager.playerDataMap.get(players.getUniqueId().toString()).getNumber());
                        } else {
                            player.sendMessage(Main.error + players.getName() + " ist bereits in einem Gespräch.");
                        }
                    }
                }
            } else {
                player.sendMessage(Main.error + "Syntax-Fehler: /call [Nummer]");
            }
        } else {
            player.sendMessage(PhoneUtils.error_nophone);
        }
        return false;
    }
}
