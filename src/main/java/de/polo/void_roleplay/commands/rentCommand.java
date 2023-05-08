package de.polo.void_roleplay.commands;

import de.polo.void_roleplay.Main;
import de.polo.void_roleplay.Utils.FactionManager;
import de.polo.void_roleplay.Utils.LocationManager;
import de.polo.void_roleplay.Utils.VertragUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class rentCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        if (args.length > 1) {
            Player targetplayer = Bukkit.getPlayer(args[0]);
            if (!targetplayer.isOnline()) player.sendMessage(Main.error + "Spieler ist nicht online.");
            Integer haus = LocationManager.isPlayerNearOwnHouse(player);
            if (haus != 0) {
                if (player.getLocation().distance(targetplayer.getLocation()) < 5) {
                    try {
                        if (VertragUtil.setVertrag(player, targetplayer, "rental", haus + "_" + args[1])) {
                            player.sendMessage("§8[§6Haus§8]§e Du hast " + targetplayer.getName() + " einen Mietvertrag ausgestellt.");
                            targetplayer.sendMessage("§6" + player.getName() + " hat dir einen Mietvertrag für Haus " + haus + " in höhe von " + args[1] + "$/PayDay angeboten.");
                            VertragUtil.sendInfoMessage(targetplayer);
                        } else {
                            player.sendMessage(Main.error + "§7" + targetplayer.getName() + " hat noch einen Vertrag offen.");
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    player.sendMessage(Main.error + targetplayer.getName() + " ist nicht in deiner nähe.");
                }
            } else {
                player.sendMessage(Main.error + "Du bist nicht in der nähe deines Hauses.");
            }
        } else {
            player.sendMessage(Main.error + "Syntax-Fehler: /rent [Spieler] [Preis]");
        }
        return false;
    }
}
