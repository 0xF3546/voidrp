package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.manager.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MsgCommand implements CommandExecutor {
    private final PlayerManager playerManager;

    public MsgCommand(PlayerManager playerManager) {
        this.playerManager = playerManager;
        Main.registerCommand("msg", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (playerData.getPermlevel() >= 40) {
            if (args.length >= 2) {
                Player targetplayer = Bukkit.getPlayer(args[0]);
                String msg = args[1];
                for (int i = 2; i < args.length; i++) {
                    msg = msg + " " + args[i];
                }
                targetplayer.sendMessage("§d" + playerData.getRang() + " " + player.getName() + " zu dir: " + msg);
                player.sendMessage("§dDu zu " + targetplayer.getName() + ": " + msg);
            } else {
                player.sendMessage(Prefix.ADMIN_ERROR + "Syntax-Fehler: /msg [Spieler] [Nachricht]");
            }
        } else {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
        }
        return false;
    }
}
