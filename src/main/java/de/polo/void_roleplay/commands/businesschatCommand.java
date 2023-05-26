package de.polo.void_roleplay.commands;

import de.polo.void_roleplay.DataStorage.PlayerData;
import de.polo.void_roleplay.Main;
import de.polo.void_roleplay.Utils.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class businesschatCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        if (playerData.getBusiness() != null) {
            if (args.length >= 1) {
                StringBuilder msg = new StringBuilder(args[0]);
                for (int i = 1; i < args.length; i++) {
                    msg.append(" ").append(args[i]);
                }
                for (Player players : Bukkit.getOnlinePlayers()) {
                    if (PlayerManager.playerDataMap.get(players.getUniqueId().toString()).getBusiness().equals(playerData.getBusiness())) {
                        players.sendMessage("§8[§6" + playerData.getBusiness() + "§8]§e " + player.getName() + "§8:§7 " + msg);
                    }
                }
            } else {
                player.sendMessage(Main.error + "Syntax-Fehler: /businesschat [Nachricht]");
            }
        } else {
            player.sendMessage(Main.business_prefix + "Du bist in keinem Business.");
        }
        return false;
    }
}
