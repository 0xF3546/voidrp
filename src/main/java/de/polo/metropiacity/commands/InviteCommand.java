package de.polo.metropiacity.commands;

import de.polo.metropiacity.DataStorage.FactionData;
import de.polo.metropiacity.DataStorage.PlayerData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.Utils.*;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class InviteCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        if (playerData.getFaction() == null) {
            player.sendMessage(Main.error + "Du bist in keiner Fraktion.");
            return false;
        }
        String playerfac = FactionManager.faction(player);
        FactionData factionData = FactionManager.factionDataMap.get(playerfac);
        if (FactionManager.faction_grade(player) < 7) {
            player.sendMessage(Main.error_nopermission);
            return false;
        }
        if (!(args.length >= 1)) {
            player.sendMessage(Main.error + "Syntax-Fehler: /invite [Spieler]");
            return false;
        }
        Player targetplayer = Bukkit.getPlayer(args[0]);
        if (targetplayer == null) {
            player.sendMessage(Main.error + args[0]+ " ist nicht online.");
            return false;
        }
        if (player.getLocation().distance(targetplayer.getLocation()) >= 5) {
            player.sendMessage(Main.error + targetplayer.getName() + " ist nicht in deiner nähe.");
            return false;
        }
        if (playerData.getFaction() != null) {
            player.sendMessage("§8[§" + FactionManager.getFactionPrimaryColor(playerfac) + playerfac + "§8] §c" + targetplayer.getName() + "§7 ist bereits in einer Fraktion.");
            return false;
        }
        if (FactionManager.getMemberCount(playerfac) >= factionData.getMaxMember()) {
            player.sendMessage(Main.error + "Deine Fraktion ist voll!");
            return false;
        }
        try {
            if (VertragUtil.setVertrag(player, targetplayer, "faction_invite", playerfac)) {
                player.sendMessage("§8[§" + FactionManager.getFactionPrimaryColor(playerfac) + playerfac + "§8] §7" + targetplayer.getName() + " wurde in die Fraktion §aeingeladen§7.");
                targetplayer.sendMessage("§6" + player.getName() + " hat dich in die Fraktion §" + FactionManager.getFactionPrimaryColor(playerfac) + playerfac + "§6 eingeladen.");
                VertragUtil.sendInfoMessage(targetplayer);
            } else {
                player.sendMessage("§8[§" + FactionManager.getFactionPrimaryColor(playerfac) + playerfac + "§8] §7" + targetplayer.getName() + " hat noch einen Vertrag offen.");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false;
    }
}
