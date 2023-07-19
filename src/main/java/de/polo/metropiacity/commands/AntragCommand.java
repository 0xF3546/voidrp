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

public class AntragCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        if (args.length >= 1) {
            Player player1 = Bukkit.getPlayer(args[0]);
            if (player1 != null) {
                if (player.getLocation().distance(player1.getLocation()) < 5) {
                    PlayerData targetplayerData = PlayerManager.playerDataMap.get(player1.getUniqueId().toString());
                    PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
                    if (playerData.getRelationShip().get(player1.getUniqueId().toString()) != null) {
                        if (targetplayerData.getRelationShip().get(player.getUniqueId().toString()) != null) {
                            try {
                                VertragUtil.deleteVertrag(player1);
                                if (VertragUtil.setVertrag(player, player1, "verlobt", player.getUniqueId().toString())) {
                                    player.sendMessage("§dDu hast " + player1.getName() + " nach einer Verlobung gefragt.");
                                    player1.sendMessage("§d" + player.getName() + " möchte sich mit dir verloben.");
                                    VertragUtil.sendInfoMessage(player1);
                                } else {
                                    player.sendMessage(Main.error + "Es ist ein Fehler unterlaufen.");
                                }
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                        } else {
                            player.sendMessage(Main.error + player1.getName() + " & du seid nicht in einer Beziehung.");
                        }
                    } else {
                        player.sendMessage(Main.error + player.getName() + " & du seid nicht in einer Beziehung.");
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
