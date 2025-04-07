package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.admin.services.impl.AdminManager;
import de.polo.voidroleplay.player.services.impl.PlayerManager;
import de.polo.voidroleplay.utils.Prefix;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TPCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final AdminManager adminManager;

    public TPCommand(PlayerManager playerManager, AdminManager adminManager) {
        this.playerManager = playerManager;
        this.adminManager = adminManager;
        Main.registerCommand("tp", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (playerData.getPermlevel() < 70) {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
            return false;
        }
        if (!playerData.isAduty()) {
            player.sendMessage(Prefix.ADMIN_ERROR + "Du bist nicht im Admindienst!");
            return false;
        }
        if (args.length < 1) {
            player.sendMessage(Prefix.ADMIN_ERROR + "Syntax-Fehler: /tp [Spieler]");
            return false;
        }
        Player targetplayer = Bukkit.getPlayer(args[0]);
        // ISSUE VRP-10003: fixed by adding null check for targetplayer
        if (targetplayer == null || !targetplayer.isOnline()) {
            player.sendMessage(Prefix.ADMIN_ERROR + args[0] + " ist nicht online.");
            return false;
        }
        player.teleport(targetplayer.getLocation());
        player.sendMessage(Prefix.ADMIN + "Du hast dich zu §c" + targetplayer.getName() + "§7 teleportiert.");
        adminManager.send_message(player.getName() + " hat sich zu " + targetplayer.getName() + " teleportiert.", ChatColor.RED);
        return false;
    }
}
