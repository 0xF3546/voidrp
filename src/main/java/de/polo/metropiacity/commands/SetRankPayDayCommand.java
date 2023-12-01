package de.polo.metropiacity.commands;

import de.polo.metropiacity.dataStorage.PlayerData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.utils.AdminManager;
import de.polo.metropiacity.utils.FactionManager;
import de.polo.metropiacity.utils.PlayerManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class SetRankPayDayCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final AdminManager adminManager;
    private final FactionManager factionManager;
    public SetRankPayDayCommand(PlayerManager playerManager, AdminManager adminManager, FactionManager factionManager) {
        this.playerManager = playerManager;
        this.adminManager = adminManager;
        this.factionManager = factionManager;
        Main.registerCommand("setrankpayday", this);
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (!(playerData.getFactionGrade() >= 7)) {
            player.sendMessage(Main.error_nopermission);
            return false;
        }
        if (!(args.length >= 2)) {
            player.sendMessage(Main.faction_prefix + "Syntax-Fehler: /setrankpayday [Rang] [Name]");
            return false;
        }
        if (Integer.parseInt(args[1]) < 0) {
            player.sendMessage(Main.error + "Der PayDay muss größer gleich 0 sein!");
            return false;
        }
        if (Integer.parseInt(args[1]) > 20000) {
            player.sendMessage(Main.error + "Der PayDay darf nicht größer als 20.000$ sein!");
            return false;
        }
        try {
            if (factionManager.changeRankPayDay(playerData.getFaction(), Integer.parseInt(args[0]), Integer.parseInt(args[1]))) {
                player.sendMessage(Main.faction_prefix + "PayDay von Rang §l" + args[0] + "§7 zu §l" + args[1] + "§§7 geändert.");
                adminManager.send_message(player.getName() + " den PayDay von Rang " + args[0] + " auf " + args[1] + "$ gesetzt (" + playerData.getFaction() + ").", ChatColor.DARK_PURPLE);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false;
    }
}
