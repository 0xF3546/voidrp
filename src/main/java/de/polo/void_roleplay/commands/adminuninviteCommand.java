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

public class adminuninviteCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        String playergroup = PlayerManager.rang(player);
        if (playergroup.equalsIgnoreCase("Administrator") || playergroup.equalsIgnoreCase("Fraktionsmanager")) {
            if (args.length >= 1) {
                try {
                    Player targetplayer = Bukkit.getPlayer(args[0]);
                    if (targetplayer != null && targetplayer.isOnline()) {
                            FactionManager.removePlayerFromFrak(targetplayer);
                            player.sendMessage(Main.admin_prefix + "Du hast §c" + targetplayer.getName() + "§7 Administrativ aus der Fraktion geworfen.");
                            targetplayer.sendMessage(Main.support_prefix + "Du wurdest von §c" + player.getName() + "§7 Administrativ aus der Fraktion geworfen.");
                    } else {
                        FactionManager.removeOfflinePlayerFromFrak(args[0]);
                        player.sendMessage(Main.admin_prefix + "Du hast §c" + args[0] + "§7 Administrativ aus der Fraktion geworfen (Spieler ist offline).");
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            } else {
                player.sendMessage(Main.admin_error + "Syntax-Fehler: /adminuninvite [Spieler]");
            }
        } else {
            player.sendMessage(Main.error_nopermission);
        }
        return false;
    }
}
