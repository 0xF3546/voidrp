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
        if (args.length > 0) {
            Player targetplayer = Bukkit.getPlayer(args[0]);
            if (!targetplayer.isOnline()) player.sendMessage(Main.error + "Spieler ist nicht online.");
            Integer haus = LocationManager.isPlayerNearOwnHouse(player);
            if (haus != 0) {
                try {
                    if (VertragUtil.setVertrag(player, targetplayer, "rental", haus + "_" + args[1])) {
                        player.sendMessage("§" + FactionManager.getFactionPrimaryColor(String.valueOf(haus)) + haus + "§8 » §7" + targetplayer.getName() + " wurde in die Fraktion §aeingeladen§7.");
                        targetplayer.sendMessage("§6" + player.getName() + "§7 hat dir einen Mietvertrag für Haus + " + haus + " in höhe von " + args[1] + " angeboten.");
                        targetplayer.sendMessage("§8 ➥§7 Nutze §8/§6annehmen§7 oder §8/§6ablehnen§7.");
                    } else {
                        player.sendMessage(Main.error + "§7" + targetplayer.getName() + " hat noch einen Vertrag offen.");
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
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
