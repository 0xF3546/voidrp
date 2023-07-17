package de.polo.metropiacity.commands;

import de.polo.metropiacity.Main;
import de.polo.metropiacity.DataStorage.FactionData;
import de.polo.metropiacity.DataStorage.PlayerData;
import de.polo.metropiacity.Utils.FactionManager;
import de.polo.metropiacity.Utils.PlayerManager;
import de.polo.metropiacity.Utils.StaatUtil;
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
                if (args.length > 0) {
                    Player targetplayer = Bukkit.getPlayer(args[0]);
                    if (targetplayer != null) {
                        if (!PlayerManager.canPlayerMove(targetplayer)) {
                            if (player.getLocation().distance(targetplayer.getLocation()) <= 5) {
                                try {
                                    if (StaatUtil.arrestPlayer(targetplayer, player)) {
                                        player.sendMessage("§" + factionData.getPrimaryColor() + factionData.getName() + "§8 » §7Du hast " + targetplayer.getName() + " §aerfolgreich§7 inhaftiert.");
                                        PlayerManager.addExp(player, Main.random(15, 44));
                                        PlayerManager.setPlayerMove(targetplayer, true);
                                    } else {
                                        player.sendMessage(Main.error + targetplayer.getName() + " hat keine offene Akte mit Hafteinheiten.");
                                    }
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }
                            } else {
                                player.sendMessage(Main.error + targetplayer.getName() + " ist nicht in deiner nähe.");
                            }
                        } else {
                            player.sendMessage(Main.error + targetplayer.getName() + " ist nicht in Handschellen.");
                        }
                    } else {
                        player.sendMessage(Main.error + "§c" + args[0] + " ist nicht online.");
                    }
                } else {
                    player.sendMessage(Main.error + "Syntax-Fehler: /arrest [Spieler]");
                }
        } else {
            player.sendMessage(Main.error_nopermission);
        }
        return false;
    }
}
