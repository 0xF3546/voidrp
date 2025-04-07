package de.polo.voidroleplay.admin.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.player.services.impl.PlayerManager;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.utils.Utils;
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
        if (playerData.getPermlevel() < 40) {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
            return false;
        }
        if (args.length < 1) {
            player.sendMessage(Prefix.ADMIN_ERROR + "Syntax-Error: /guidechat [Nachricht]");
            return false;
        }

        String msg = Utils.stringArrayToString(args);
        for (Player players : Bukkit.getOnlinePlayers()) {
            PlayerData playersData = playerManager.getPlayerData(players.getUniqueId());
            if (playersData.getPermlevel() >= 40) {
                players.sendMessage(Prefix.SUPPORT + "§b" + playerManager.rang(player) + " " + player.getName() + "§8:§7 " + msg);
            }
        }
        return false;
    }
}
