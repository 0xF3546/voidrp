package de.polo.metropiacity.commands;

import de.polo.metropiacity.Main;
import de.polo.metropiacity.PlayerUtils.DeathUtil;
import de.polo.metropiacity.DataStorage.PlayerData;
import de.polo.metropiacity.Utils.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class adminReviveCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        if (playerData.isAduty()) {
            if (args.length >= 1) {
                Player targetplayer = Bukkit.getPlayer(args[0]);
                if (targetplayer != null) {
                    DeathUtil.RevivePlayer(targetplayer);
                    player.sendMessage(Main.admin_prefix + "Du hast §c" + targetplayer.getName() + "§7 wiederbelebt.");
                    targetplayer.sendMessage(Main.prefix + "Du wurdest wiederbelebt.");
                } else {
                    player.sendMessage(Main.admin_error + "§c" + args[0] + "§7 ist nicht online.");
                }
            } else {
                DeathUtil.RevivePlayer(player);
                player.sendMessage(Main.admin_prefix + "Du hast dich wiederbelebt.");
            }
        } else {
            player.sendMessage(Main.admin_error + "Du bist nicht im Admindienst!");
        }
        return false;
    }
}
