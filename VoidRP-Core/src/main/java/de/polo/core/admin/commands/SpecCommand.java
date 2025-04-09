package de.polo.core.admin.commands;

import de.polo.api.VoidAPI;
import de.polo.api.player.VoidPlayer;
import de.polo.core.Main;
import de.polo.core.admin.services.AdminService;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.utils.Prefix;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpecCommand implements CommandExecutor {
    private final PlayerManager playerManager;

    public SpecCommand(PlayerManager playerManager) {
        this.playerManager = playerManager;
        Main.registerCommand("spec", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (playerData.getPermlevel() >= 60) {
            VoidPlayer voidPlayer = VoidAPI.getPlayer(player);
            if (voidPlayer.isAduty()) {
                AdminService adminService = VoidAPI.getService(AdminService.class);
                if (playerData.getVariable("isSpec") == null) {
                    if (args.length >= 1) {
                        Player targetplayer = Bukkit.getPlayer(args[0]);
                        playerData.setLocationVariable("specLoc", player.getLocation());
                        player.teleport(targetplayer.getLocation());
                        player.setGameMode(GameMode.SPECTATOR);
                        player.setSpectatorTarget(targetplayer);
                        playerData.setVariable("isSpec", targetplayer.getUniqueId().toString());
                        player.sendMessage(Prefix.ADMIN + "§cDu Spectatest nun §7" + targetplayer.getName() + "§c.");
                        adminService.send_message(player.getName() + " beobachtet nun " + targetplayer.getName(), Color.RED);
                    } else {
                        player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /spec [Spieler]");
                    }
                } else {
                    player.setGameMode(GameMode.SURVIVAL);
                    player.setAllowFlight(true);
                    player.sendMessage(Prefix.ADMIN + "Du hast den Spectator-Modus verlassen");
                    player.teleport(playerData.getLocationVariable("specLoc"));
                    playerData.setVariable("isSpec", null);
                    adminService.send_message(player.getName() + " hat den Beobachter-Modus verlassen.", Color.RED);
                }
            } else {
                player.sendMessage(Prefix.ERROR + "Du bist nicht im Admindienst.");
            }
        } else {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
        }
        return false;
    }

    public void leaveSpec(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        player.setGameMode(GameMode.SURVIVAL);
        player.setAllowFlight(true);
        player.teleport(playerData.getLocationVariable("specLoc"));
        playerData.setVariable("isSpec", null);
    }
}
