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

public class AssistentchatCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final Utils utils;

    public AssistentchatCommand(PlayerManager playerManager, Utils utils) {
        this.playerManager = playerManager;
        this.utils = utils;
        Main.registerCommand("guidechat", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        String uuid = player.getUniqueId().toString();
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (playerData.getPermlevel() < 400) {
            player.sendMessage(Main.error_nopermission);
            return false;
        }
        if (args.length < 1) {
            player.sendMessage(Main.admin_error + "Syntax-Error: /assistentchat [Nachricht]");
            return false;
        }

        String msg = utils.stringArrayToString(args);
        for (Player players : Bukkit.getOnlinePlayers()) {
            PlayerData playersData = playerManager.getPlayerData(players.getUniqueId());
            if (playersData.getPermlevel() < 400) {
                players.sendMessage(Main.support_prefix + "§b" + playerManager.rang(player) + " " + player.getName() + "§8:§7 " + msg);
            }
        }
        return false;
    }
}
