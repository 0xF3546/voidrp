package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.utils.AdminManager;
import de.polo.voidroleplay.utils.FactionManager;
import de.polo.voidroleplay.utils.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class LeadFrakCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final AdminManager adminManager;
    private final FactionManager factionManager;

    public LeadFrakCommand(PlayerManager playerManager, AdminManager adminManager, FactionManager factionManager) {
        this.playerManager = playerManager;
        this.adminManager = adminManager;
        this.factionManager = factionManager;
        Main.registerCommand("leadfrak", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (playerData.getPermlevel() < 75) {
            player.sendMessage(Main.error_nopermission);
            return false;
        }
        if (args.length < 2) {
            player.sendMessage(Main.admin_error + "Syntax-Fehler: /leadfrak [Spieler] [Fraktion]");
            return false;
        }
        Player targetplayer = Bukkit.getPlayer(args[0]);
        String frak = args[1];
        player.sendMessage(Main.admin_prefix + "Du hast §c" + targetplayer.getName() + "§7 in die Fraktion §c" + frak + "§7 gesetzt.");
        targetplayer.sendMessage(Main.faction_prefix + "Du bist nun Leader der Fraktion §c" + frak + "§7!");
        try {
            factionManager.setPlayerInFrak(targetplayer, frak, 8);
            adminManager.send_message(player.getName() + " hat " + targetplayer.getName() + " in die Fraktion " + frak + " gesetzt.", ChatColor.DARK_PURPLE);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false;
    }
}
