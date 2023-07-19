package de.polo.metropiacity.commands;

import de.polo.metropiacity.dataStorage.PlayerData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.utils.PlayerManager;
import de.polo.metropiacity.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.swing.text.Utilities;

public class EventTeamChat implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        if (playerData.getSecondaryTeam().equals("Event-Team") || playerData.isAduty()) {
            for (Player players : Bukkit.getOnlinePlayers()) {
                PlayerData playersData = PlayerManager.playerDataMap.get(players.getUniqueId().toString());
                if (playersData.getSecondaryTeam().equalsIgnoreCase("Event-Team") || playersData.isAduty()) {
                    if (playersData.getSecondaryTeam().equals("Event-Team") || playersData.isAduty()) {
                        players.sendMessage("§8[§6Event-Team§8]§e " + player.getName() + "§8:§7 " + Utils.stringArrayToString(args));
                    }
                }
            }
        } else {
            player.sendMessage(Main.error_nopermission);
        }
        return false;
    }
}
