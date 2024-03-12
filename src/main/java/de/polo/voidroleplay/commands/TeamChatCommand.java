package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.utils.PlayerManager;
import de.polo.voidroleplay.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TeamChatCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final Utils utils;

    public TeamChatCommand(PlayerManager playerManager, Utils utils) {
        this.playerManager = playerManager;
        this.utils = utils;
        Main.registerCommand("teamchat", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        String uuid = player.getUniqueId().toString();
        if (playerManager.getPlayerData(player.getUniqueId()).getPermlevel() < 50) {
            player.sendMessage(Main.error_nopermission);
            return false;
        }
        if (args.length < 1) {
            player.sendMessage(Main.admin_error + "Syntax-Error: /teamchat [Nachricht]");
            return false;
        }
        String msg = utils.stringArrayToString(args);
        for (Player players : Bukkit.getOnlinePlayers()) {
            if (playerManager.getPlayerData(players.getUniqueId()).getPermlevel() >= 50) {
                players.sendMessage(Main.admin_prefix + "§c" + playerManager.rang(player) + " " + player.getName() + "§8:§7 " + msg);
            }
        }
        return false;
    }
}
