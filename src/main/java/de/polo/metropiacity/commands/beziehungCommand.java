package de.polo.metropiacity.commands;

import de.polo.metropiacity.DataStorage.PlayerData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.Utils.PlayerManager;
import de.polo.metropiacity.Utils.VertragUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class beziehungCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        if (args.length >= 1) {
            Player player1 = Bukkit.getPlayer(args[0]);
            if (player1.isOnline()) {
                if (player.getLocation().distance(player1.getLocation()) < 5) {
                    PlayerData targetplayerData = PlayerManager.playerDataMap.get(player1.getUniqueId().toString());
                    PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
                    if (playerData.getRelationShip().isEmpty()) {
                        if (targetplayerData.getRelationShip().isEmpty()) {
                            try {
                                VertragUtil.deleteVertrag(player1);
                                if (VertragUtil.setVertrag(player, player1, "beziehung", player.getUniqueId().toString())) {
                                    player.sendMessage("§6Du hast " + player1.getName() + " nach einer Beziehung gefragt.");
                                    player1.sendMessage("§6" + player.getName() + " möchte mit dir zusammen sein.");
                                    VertragUtil.sendInfoMessage(player1);
                                } else {
                                    player.sendMessage(Main.error + "Es ist ein Fehler unterlaufen.");
                                }
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                        } else {
                            player.sendMessage(Main.error + player1.getName() + " ist bereits in einer Beziehung.");
                        }
                    } else {
                        player.sendMessage(Main.error + "Du bist bereits in einer Beziehung.");
                    }
                } else {
                    player.sendMessage(Main.error + player1.getName() + " ist nicht in deiner nähe.");
                }
            } else {
                player.sendMessage(Main.error + "Spieler konnte nicht gefunden werden.");
            }
        } else {
            player.sendMessage(Main.error + "Syntax-Fehler: /beziehung [Spieler]");
        }
        return false;
    }
}
