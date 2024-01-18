package de.polo.metropiacity.commands;

import de.polo.metropiacity.Main;
import de.polo.metropiacity.dataStorage.PlayerData;
import de.polo.metropiacity.utils.PlayerManager;
import de.polo.metropiacity.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AdminReviveCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final Utils utils;
    public AdminReviveCommand(PlayerManager playerManager, Utils utils) {
        this.playerManager = playerManager;
        this.utils = utils;
        Main.registerCommand("adminrevive", this);
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (playerData.isAduty()) {
            if (args.length >= 1) {
                Player targetplayer = Bukkit.getPlayer(args[0]);
                if (targetplayer != null) {
                    utils.deathUtil.RevivePlayer(targetplayer);
                    player.sendMessage(Main.admin_prefix + "Du hast §c" + targetplayer.getName() + "§7 wiederbelebt.");
                    targetplayer.sendMessage(Main.prefix + "Du wurdest wiederbelebt.");
                } else {
                    player.sendMessage(Main.admin_error + "§c" + args[0] + "§7 ist nicht online.");
                }
            } else {
                utils.deathUtil.RevivePlayer(player);
                player.sendMessage(Main.admin_prefix + "Du hast dich wiederbelebt.");
            }
        } else {
            player.sendMessage(Main.admin_error + "Du bist nicht im Admindienst!");
        }
        return false;
    }
}
