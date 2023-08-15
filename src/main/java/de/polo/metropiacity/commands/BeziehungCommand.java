package de.polo.metropiacity.commands;

import de.polo.metropiacity.dataStorage.PlayerData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.utils.PlayerManager;
import de.polo.metropiacity.utils.Utils;
import de.polo.metropiacity.utils.VertragUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class BeziehungCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        if (args.length < 1) {
            player.sendMessage(Main.error + "Syntax-Fehler: /beziehung [Spieler]");
            return false;
        } else {
            if (args[0].equalsIgnoreCase(player.getName())) {
                player.sendMessage(Main.error + "Du kannst mit dir selbst keine Beziehung eingehen.");
                return false;
            }
            OfflinePlayer offlinePlayer = Utils.getOfflinePlayer(args[0]);
            if (offlinePlayer == null) {
                player.sendMessage(Main.error + args[0] + " wurde nicht gefunden.");
                return false;
            }
            if (!offlinePlayer.isOnline()) {
                player.sendMessage(Main.error + offlinePlayer.getName() + " ist nicht online.");
                return false;
            }
        }
        Player player1 = Bukkit.getPlayer(args[0]);
        if (player.getLocation().distance(player1.getLocation()) > 5) {
            player.sendMessage(Main.error + player1.getName() + " ist nicht in deiner nähe.");
            return false;
        }
        PlayerData targetplayerData = PlayerManager.playerDataMap.get(player1.getUniqueId().toString());
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        if (playerData.getRelationShip().isEmpty()) {
            if (targetplayerData.getRelationShip().isEmpty()) {
                VertragUtil.deleteVertrag(player1);
                if (VertragUtil.setVertrag(player, player1, "beziehung", player.getUniqueId().toString())) {
                    player.sendMessage("§6Du hast " + player1.getName() + " nach einer Beziehung gefragt.");
                    player1.sendMessage("§6" + player.getName() + " möchte mit dir zusammen sein.");
                    VertragUtil.sendInfoMessage(player1);
                } else {
                    player.sendMessage(Main.error + "Es ist ein Fehler unterlaufen.");
                }
            } else {
                player.sendMessage(Main.error + player1.getName() + " ist bereits in einer Beziehung.");
            }
        } else {
            player.sendMessage(Main.error + "Du bist bereits in einer Beziehung.");
        }
        return false;
    }
}
