package de.polo.metropiacity.commands;

import de.polo.metropiacity.DataStorage.PlayerData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.Utils.PlayerManager;
import de.polo.metropiacity.Utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class leaderchatCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        if (playerData.getFactionGrade() >= 7 || playerData.getPermlevel() >= 70) {
            for (PlayerData pData : PlayerManager.playerDataMap.values()) {
                if (pData.getPermlevel() >= 70 || pData.getFactionGrade() >= 7) {
                    Player targetplayer = Bukkit.getPlayer(pData.getUuid());
                    String msg = Utils.stringArrayToString(args);
                    targetplayer.sendMessage("§8[§6Leader§8]§e " + playerData.getFaction() + " " + player.getName() + ": " + msg);
                }
            }
        } else {
            player.sendMessage(Main.error_nopermission);
        }
        return false;
    }
}
