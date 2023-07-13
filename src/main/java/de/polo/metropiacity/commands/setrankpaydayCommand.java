package de.polo.metropiacity.commands;

import de.polo.metropiacity.DataStorage.PlayerData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.Utils.FactionManager;
import de.polo.metropiacity.Utils.PlayerManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class setrankpaydayCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
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
        try {
            if (FactionManager.changeRankPayDay(playerData.getFaction(), Integer.parseInt(args[0]), Integer.parseInt(args[1]))) {
                player.sendMessage(Main.faction_prefix + "PayDay von Rang §l" + args[0] + "§7 zu §l" + args[1] + "§§7 geändert.");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false;
    }
}
