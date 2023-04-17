package de.polo.void_roleplay.commands;

import de.polo.void_roleplay.DataStorage.PlayerData;
import de.polo.void_roleplay.Main;
import de.polo.void_roleplay.Utils.FactionManager;
import de.polo.void_roleplay.Utils.PlayerManager;
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
        if (playerData.getFactionGrade() >= 7) {
            if (args.length >= 2) {
                try {
                    if (FactionManager.changeRankPayDay(playerData.getFaction(), Integer.parseInt(args[0]), Integer.parseInt(args[1]))) {
                        player.sendMessage(Main.faction_prefix + "PayDay von Rang §l" + args[0] + "§7 zu §l" + args[1] + "§§7 geändert.");
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            } else {
                player.sendMessage(Main.faction_prefix + "Syntax-Fehler: /setrankpayday [Rang] [Name]");
            }
        } else {
            player.sendMessage(Main.error_nopermission);
        }
        return false;
    }
}
