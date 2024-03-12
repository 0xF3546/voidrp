package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.utils.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class KickCommand implements CommandExecutor {
    private PlayerManager playerManager;

    public KickCommand(PlayerManager playerManager) {
        this.playerManager = playerManager;
        Main.registerCommand("kick", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (playerData.getPermlevel() < 70) {
            player.sendMessage(Main.error_nopermission);
            return false;
        }
        if (args.length < 1) {
            player.sendMessage(Main.admin_error + "Syntax-Fehler: /kick [Spieler] [Grund]");
            return false;
        }
        Player targetplayer = Bukkit.getPlayer(args[0]);
        if (targetplayer == null && !targetplayer.isOnline()) {
            player.sendMessage(Main.admin_error + args[0] + " ist nicht online.");
            return false;
        }
        StringBuilder message = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            message.append(" ").append(args[i]);
        }
        playerManager.kickPlayer(targetplayer, String.valueOf(message));
        Bukkit.broadcastMessage("§c" + playerData.getRang() + " " + player.getName() + " hat " + targetplayer.getName() + " gekickt. Grund: " + message);
        return false;
    }
}
