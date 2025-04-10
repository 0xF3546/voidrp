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

public class TPHereCommand implements CommandExecutor {
    private final PlayerManager playerManager;

    public TPHereCommand(PlayerManager playerManager) {
        this.playerManager = playerManager;
        Main.registerCommand("tphere", this);
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
            player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /tphere [Spieler]");
            return false;
        }
        Player targetplayer = Bukkit.getPlayer(args[0]);
        if (!targetplayer.isOnline()) {
            player.sendMessage(Prefix.ERROR + args[0] + " ist nicht online.");
            return false;
        }
        targetplayer.teleport(player.getLocation());
        player.sendMessage(Prefix.ADMIN + "Du hast §c" + targetplayer.getName() + "§7 zu dir teleportiert.");
        targetplayer.sendMessage(Prefix.MAIN + "§c" + playerData.getRang() + " " + player.getName() + "§7 hat dich zu sich teleportiert.");
        AdminService adminService = VoidAPI.getService(AdminService.class);
        adminService.sendMessage(player.getName() + " hat " + targetplayer.getName() + " zu sich teleportiert.", Color.RED);
        return false;
    }
}
