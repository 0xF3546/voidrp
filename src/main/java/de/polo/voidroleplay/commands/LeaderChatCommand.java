package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.manager.PlayerManager;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LeaderChatCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final Utils utils;

    public LeaderChatCommand(PlayerManager playerManager, Utils utils) {
        this.playerManager = playerManager;
        this.utils = utils;
        Main.registerCommand("leaderchat", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (!playerData.isLeader() && playerData.getPermlevel() < 70) {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
            return false;
        }
        if (args.length < 1) {
            player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /leaderchat [Nachricht]");
            return false;
        }
        for (PlayerData pData : playerManager.getPlayers()) {
            if (pData == null) continue;
            if (pData.getPermlevel() >= 70 || pData.isLeader()) {
                if (pData.getFaction() == null) continue;
                Player targetplayer = Bukkit.getPlayer(pData.getUuid());
                String msg = Utils.stringArrayToString(args);
                if (targetplayer == null) continue;
                targetplayer.sendMessage("§8[§6Leader§8]§e " + playerData.getFaction() + " " + player.getName() + ": " + msg);
            }
        }
        return false;
    }
}
