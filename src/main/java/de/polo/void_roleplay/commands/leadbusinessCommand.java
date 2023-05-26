package de.polo.void_roleplay.commands;

import de.polo.void_roleplay.Main;
import de.polo.void_roleplay.Utils.BusinessManager;
import de.polo.void_roleplay.Utils.FactionManager;
import de.polo.void_roleplay.Utils.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class leadbusinessCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        String playerGroup = PlayerManager.rang(player);
        if (PlayerManager.perms(player) >= 75) {
            if (args.length >= 2) {
                Player targetplayer = Bukkit.getPlayer(args[0]);
                String frak = args[1];
                try {
                    BusinessManager.setPlayerInBusiness(targetplayer, frak, 8);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                player.sendMessage(Main.admin_prefix + "Du hast §c" + targetplayer.getName() + "§7 in das Business §c" + frak + "§7 gesetzt.");
                targetplayer.sendMessage(Main.business_prefix + "Du bist nun Leader des Business §c" + frak + "§7!");
            } else {
                player.sendMessage(Main.admin_error + "Syntax-Fehler: /leadbusiness [Spieler] [Business]");
            }
        } else {
            player.sendMessage(Main.error_nopermission);
        }
        return false;
    }
}
