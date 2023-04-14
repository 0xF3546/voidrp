package de.polo.void_roleplay.commands;

import de.polo.void_roleplay.Main;
import de.polo.void_roleplay.Utils.FactionManager;
import de.polo.void_roleplay.Utils.VertragUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.Objects;

public class inviteCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        Player targetplayer = Bukkit.getPlayer(args[0]);
        assert targetplayer != null;
        String playerfac = FactionManager.faction(player);
        if (FactionManager.faction_grade(player) >= 7) {
            if (args.length > 0) {
                System.out.println(FactionManager.faction(targetplayer));
                if (Objects.equals(FactionManager.faction(targetplayer), "Zivilist") || FactionManager.faction(targetplayer) == null) {
                    try {
                        if (VertragUtil.setVertrag(player, targetplayer, "faction_invite", playerfac)) {
                            player.sendMessage("§" + FactionManager.getFactionPrimaryColor(playerfac) + playerfac + "§8 » §7" + targetplayer.getName() + " wurde in die Fraktion §aeingeladen§7.");
                            targetplayer.sendMessage("§6" + player.getName() + "§7 hat dich in die §" + FactionManager.getFactionPrimaryColor(playerfac) + playerfac + "§7 eingeladen.");
                            VertragUtil.sendInfoMessage(targetplayer);
                        } else {
                            player.sendMessage("§" + FactionManager.getFactionPrimaryColor(playerfac) + playerfac + "§8 » §7" + targetplayer.getName() + " hat noch einen Vertrag offen.");
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    player.sendMessage("§" + FactionManager.getFactionPrimaryColor(playerfac) + playerfac + "§8 » §c" + targetplayer.getName() + "§7 ist bereits in einer Fraktion.");
                }
            } else {
                player.sendMessage(Main.error + "Syntax-Fehler: /invite [Spieler]");
            }
        } else {
            player.sendMessage(Main.error_nopermission);
        }
        return false;
    }
}
