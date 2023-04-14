package de.polo.void_roleplay.commands;

import de.polo.void_roleplay.Main;
import de.polo.void_roleplay.DataStorage.FactionData;
import de.polo.void_roleplay.DataStorage.PlayerData;
import de.polo.void_roleplay.Utils.FactionManager;
import de.polo.void_roleplay.Utils.PlayerManager;
import de.polo.void_roleplay.Utils.StaatUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.Objects;

public class arrestCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        if (Objects.equals(playerData.getFaction(), "FBI") || Objects.equals(playerData.getFaction(), "Polizei")) {
            FactionData factionData = FactionManager.factionDataMap.get(playerData.getFaction());
            if (playerData.canInteract()) {
                if (args.length > 0) {
                    Player targetplayer = Bukkit.getPlayer(args[0]);
                    if (targetplayer.isOnline()) {
                        try {
                            if (StaatUtil.arrestPlayer(targetplayer, player)) {
                                player.sendMessage(factionData.getPrimaryColor() + factionData.getName() + "§8 » §7Du hast " + targetplayer.getName() + " §aerfolgreich§7 inhaftiert.");
                            } else {
                                player.sendMessage(Main.error + targetplayer.getName() + " hat keine offene Akte mit Hafteinheiten.");
                            }
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        player.sendMessage(Main.error + "§c" + args[0] + " ist nicht online.");
                    }
                } else {
                    player.sendMessage(Main.error + "Syntax-Fehler: /arrest [Spieler]");
                }
            } else {
                player.sendMessage(Main.error_cantinteract);
            }
        } else {
            player.sendMessage(Main.error_nopermission);
        }
        return false;
    }
}
