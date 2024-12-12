package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.manager.AdminManager;
import de.polo.voidroleplay.manager.FactionManager;
import de.polo.voidroleplay.manager.PlayerManager;
import de.polo.voidroleplay.utils.Prefix;
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
        if (!(playerData.isLeader())) {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
            return false;
        }
        if (!(args.length >= 2)) {
            player.sendMessage(Prefix.FACTION + "Syntax-Fehler: /setrankpayday [Rang] [Gehalt]");
            return false;
        }
        if (Integer.parseInt(args[1]) < 0) {
            player.sendMessage(Prefix.ERROR + "Der PayDay muss größer gleich 0 sein!");
            return false;
        }
        if (Integer.parseInt(args[1]) > 20000) {
            player.sendMessage(Prefix.ERROR + "Der PayDay darf nicht größer als 20.000$ sein!");
            return false;
        }
        try {
            if (factionManager.changeRankPayDay(playerData.getFaction(), Integer.parseInt(args[0]), Integer.parseInt(args[1]))) {
                player.sendMessage(Prefix.FACTION + "PayDay von Rang §l" + args[0] + "§7 zu §l" + args[1] + "§§7 geändert.");
                adminManager.send_message(player.getName() + " den PayDay von Rang " + args[0] + " auf " + args[1] + "$ gesetzt (" + playerData.getFaction() + ").", ChatColor.DARK_PURPLE);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false;
    }
}
