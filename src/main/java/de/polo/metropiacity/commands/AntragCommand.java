package de.polo.metropiacity.commands;

import de.polo.metropiacity.dataStorage.PlayerData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.utils.PlayerManager;
import de.polo.metropiacity.utils.Utils;
import de.polo.metropiacity.utils.VertragUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class AntragCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final Utils utils;
    public AntragCommand(PlayerManager playerManager, Utils utils) {
        this.playerManager = playerManager;
        this.utils = utils;
        Main.registerCommand("antrag", this);
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        if (args.length >= 1) {
            Player player1 = Bukkit.getPlayer(args[0]);
            if (player1 != null) {
                if (player.getLocation().distance(player1.getLocation()) < 5) {
                    PlayerData targetplayerData = playerManager.getPlayerData(player1.getUniqueId());
                    PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
                    if (playerData.getRelationShip().get(player1.getUniqueId().toString()) != null) {
                        if (targetplayerData.getRelationShip().get(player.getUniqueId().toString()) != null) {
                            VertragUtil.deleteVertrag(player1);
                            if (VertragUtil.setVertrag(player, player1, "verlobt", player.getUniqueId().toString())) {
                                player.sendMessage("§dDu hast " + player1.getName() + " nach einer Verlobung gefragt.");
                                player1.sendMessage("§d" + player.getName() + " möchte sich mit dir verloben.");
                                utils.vertragUtil.sendInfoMessage(player1);
                            } else {
                                player.sendMessage(Main.error + "Es ist ein Fehler unterlaufen.");
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
            player.sendMessage(Main.error + "Syntax-Fehler: /antrag [Spieler]");
        }
        return false;
    }
}
