package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.utils.PlayerManager;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.utils.Utils;
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
                    utils.deathUtil.revivePlayer(targetplayer, false);
                    player.sendMessage(Prefix.ADMIN + "Du hast §c" + targetplayer.getName() + "§7 wiederbelebt.");
                    targetplayer.sendMessage(Prefix.MAIN + "Du wurdest wiederbelebt.");
                } else {
                    player.sendMessage(Prefix.admin_error + "§c" + args[0] + "§7 ist nicht online.");
                }
            } else {
                utils.deathUtil.revivePlayer(player, false);
                player.sendMessage(Prefix.ADMIN + "Du hast dich wiederbelebt.");
            }
        } else {
            player.sendMessage(Prefix.admin_error + "Du bist nicht im Admindienst!");
        }
        return false;
    }
}
