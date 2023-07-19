package de.polo.metropiacity.commands;

import de.polo.metropiacity.dataStorage.PlayerData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.utils.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PRTeamChat implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        if (playerData.getSecondaryTeam().equals("PR-Team") || playerData.isAduty()) {
            StringBuilder msg = new StringBuilder(args[0]);
            for (int i = 1; i < args.length; i++) {
                msg.append(' ').append(args[i]);
            }
            for (Player players : Bukkit.getOnlinePlayers()) {
                PlayerData playersData = PlayerManager.playerDataMap.get(players.getUniqueId().toString());
                if (playersData.getBusiness() != null || playersData.isAduty()) {
                    if (playersData.getSecondaryTeam().equals("PR-Team") || playersData.isAduty()) {
                        players.sendMessage("§8[§6PRTeam§8]§e " + player.getName() + "§8:§7 " + msg);
                    }
                }
            }
        } else {
            player.sendMessage(Main.error_nopermission);
        }
        return false;
    }
}
