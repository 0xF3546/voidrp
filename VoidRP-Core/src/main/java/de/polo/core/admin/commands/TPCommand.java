package de.polo.core.admin.commands;

import de.polo.api.VoidAPI;
import de.polo.api.player.VoidPlayer;
import de.polo.core.Main;
import de.polo.core.admin.services.AdminService;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.utils.Prefix;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TPCommand implements CommandExecutor {
    private final PlayerManager playerManager;

    public TPCommand(PlayerManager playerManager) {
        this.playerManager = playerManager;
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
        VoidPlayer voidPlayer = VoidAPI.getPlayer(player);
        if (!voidPlayer.isAduty()) {
            player.sendMessage(Prefix.ERROR + "Du bist nicht im Admindienst!");
            return false;
        }
        if (args.length < 1) {
            player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /tp [Spieler]");
            return false;
        }
        Player targetplayer = Bukkit.getPlayer(args[0]);
        // ISSUE VRP-10003: fixed by adding null check for targetplayer
        if (targetplayer == null || !targetplayer.isOnline()) {
            player.sendMessage(Prefix.ERROR + args[0] + " ist nicht online.");
            return false;
        }
        player.teleport(targetplayer.getLocation());
        player.sendMessage(Prefix.ADMIN + "Du hast dich zu §c" + targetplayer.getName() + "§7 teleportiert.");

        AdminService adminService = VoidAPI.getService(AdminService.class);
        adminService.sendMessage(player.getName() + " hat sich zu " + targetplayer.getName() + " teleportiert.", Color.RED);
        return false;
    }
}
