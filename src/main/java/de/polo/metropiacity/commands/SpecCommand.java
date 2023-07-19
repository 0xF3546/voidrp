package de.polo.metropiacity.commands;

import de.polo.metropiacity.DataStorage.PlayerData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.Utils.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpecCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        if (playerData.getPermlevel() >= 60) {
            if (playerData.isAduty()) {
                if (playerData.getVariable("isSpec") == null) {
                    if (args.length >= 1) {
                        Player targetplayer = Bukkit.getPlayer(args[0]);
                        player.setGameMode(GameMode.SPECTATOR);
                        player.setSpectatorTarget(targetplayer);
                        playerData.setVariable("isSpec", targetplayer.getUniqueId().toString());
                        playerData.setLocationVariable("specLoc", player.getLocation());
                        player.sendMessage(Main.admin_prefix + "§cDu Spectatest nun §7" + targetplayer.getName() + "§c.");
                    } else {
                        player.sendMessage(Main.admin_error + "Syntax-Fehler: /spec [Spieler]");
                    }
                } else {
                    player.setGameMode(GameMode.SURVIVAL);
                    player.setAllowFlight(true);
                    player.sendMessage(Main.admin_prefix + "Du hast den Spectator-Modus verlassen");
                    player.teleport(playerData.getLocationVariable("specLoc"));
                    playerData.setVariable("isSpec", null);
                }
            } else {
                player.sendMessage(Main.admin_error + "Du bist nicht im Admindienst.");
            }
        } else {
            player.sendMessage(Main.error_nopermission);
        }
        return false;
    }

    public static void leaveSpec(Player player) {
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        player.setGameMode(GameMode.SURVIVAL);
        player.setAllowFlight(true);
        player.teleport(playerData.getLocationVariable("specLoc"));
        playerData.setVariable("isSpec", null);
    }
}
