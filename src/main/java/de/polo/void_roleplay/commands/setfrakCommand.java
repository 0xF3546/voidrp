package de.polo.void_roleplay.commands;

import de.polo.void_roleplay.Main;
import de.polo.void_roleplay.Utils.FactionManager;
import de.polo.void_roleplay.Utils.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class setfrakCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        String playerGroup = PlayerManager.rang(player);
        if (playerGroup.equalsIgnoreCase("Administrator") || playerGroup.equalsIgnoreCase("Fraktionsmanager")) {
            if (args.length >= 3) {
                Player targetplayer = Bukkit.getPlayer(args[0]);
                String frak = args[1];
                int rang = Integer.parseInt(args[2]);
                try {
                    if (rang >= 0 && rang <= 8) {
                        FactionManager.setPlayerInFrak(targetplayer, frak, rang);
                    } else {
                        player.sendMessage(Main.admin_error + "Syntax-Fehler: /setfraktion [Spieler] [Fraktion] [Rang(1-8)]");
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                player.sendMessage(Main.admin_prefix + "Du hast §c" + targetplayer.getName() + "§7 in die Fraktion §c" + frak + "§7 (Rang §c" + rang + "§7) gesetzt.");
                targetplayer.sendMessage(Main.faction_prefix + "Du bist Rang §c" + rang + "§7 der Fraktion §c" + frak + "§7!");
            } else {
                player.sendMessage(Main.admin_error + "Syntax-Fehler: /setfrak [Spieler] [Fraktion] [Rang]");
            }
        } else {
            player.sendMessage(Main.error_nopermission);
        }
        return false;
    }
}
