package de.polo.metropiacity.commands;

import de.polo.metropiacity.DataStorage.PlayerData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.Utils.PlayerManager;
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
        if (playerData.getBusiness() == null) {
            player.sendMessage(Main.business_prefix + "Du bist in keinem Business.");
            return false;
        }
        if (args.length < 1) {
            player.sendMessage(Main.error + "Syntax-Fehler: /businesschat [Nachricht]");
            return false;
        }
        StringBuilder msg = new StringBuilder(args[0]);
        for (int i = 1; i < args.length; i++) {
            msg.append(" ").append(args[i]);
        }
        for (Player players : Bukkit.getOnlinePlayers()) {
            PlayerData playersData = PlayerManager.playerDataMap.get(players.getUniqueId().toString());
            if (playersData.getBusiness() != null) {
                if (playersData.getBusiness().equals(playerData.getBusiness())) {
                    players.sendMessage("§8[§6" + playerData.getBusiness() + "§8]§e " + player.getName() + "§8:§7 " + msg);
                }
            }
        }
        return false;
    }
}
