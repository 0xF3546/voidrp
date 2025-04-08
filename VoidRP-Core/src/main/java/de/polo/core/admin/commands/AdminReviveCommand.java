package de.polo.core.admin.commands;

import de.polo.core.Main;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.utils.Prefix;
import de.polo.core.utils.Utils;
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
                    player.sendMessage(Prefix.ERROR + "§c" + args[0] + "§7 ist nicht online.");
                }
            } else {
                utils.deathUtil.revivePlayer(player, false);
                player.sendMessage(Prefix.ADMIN + "Du hast dich wiederbelebt.");
            }
        } else {
            player.sendMessage(Prefix.ERROR + "Du bist nicht im Admindienst!");
        }
        return false;
    }
}
