package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.utils.AdminManager;
import de.polo.voidroleplay.utils.PlayerManager;
import de.polo.voidroleplay.utils.Prefix;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpecCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final AdminManager adminManager;
    public SpecCommand(PlayerManager playerManager, AdminManager adminManager) {
        this.playerManager = playerManager;
        this.adminManager = adminManager;
        Main.registerCommand("spec", this);
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (playerData.getPermlevel() >= 60) {
            if (playerData.isAduty()) {
                if (playerData.getVariable("isSpec") == null) {
                    if (args.length >= 1) {
                        Player targetplayer = Bukkit.getPlayer(args[0]);
                        playerData.setLocationVariable("specLoc", player.getLocation());
                        player.teleport(targetplayer.getLocation());
                        player.setGameMode(GameMode.SPECTATOR);
                        player.setSpectatorTarget(targetplayer);
                        playerData.setVariable("isSpec", targetplayer.getUniqueId().toString());
                        player.sendMessage(Prefix.ADMIN + "§cDu Spectatest nun §7" + targetplayer.getName() + "§c.");
                        adminManager.send_message(player.getName() + " beobachtet nun " + targetplayer.getName(), ChatColor.RED);
                    } else {
                        player.sendMessage(Prefix.admin_error + "Syntax-Fehler: /spec [Spieler]");
                    }
                } else {
                    player.setGameMode(GameMode.SURVIVAL);
                    player.setAllowFlight(true);
                    player.sendMessage(Prefix.ADMIN + "Du hast den Spectator-Modus verlassen");
                    player.teleport(playerData.getLocationVariable("specLoc"));
                    playerData.setVariable("isSpec", null);
                    adminManager.send_message(player.getName() + " hat den Beobachter-Modus verlassen.", ChatColor.RED);
                }
            } else {
                player.sendMessage(Prefix.admin_error + "Du bist nicht im Admindienst.");
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
