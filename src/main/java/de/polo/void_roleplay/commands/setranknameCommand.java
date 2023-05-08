package de.polo.void_roleplay.commands;

import de.polo.void_roleplay.DataStorage.PlayerData;
import de.polo.void_roleplay.Main;
import de.polo.void_roleplay.Utils.FactionManager;
import de.polo.void_roleplay.Utils.PlayerManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.PlayerNamePrompt;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class setranknameCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        if (playerData.getFactionGrade() >= 7) {
            if (args.length >= 2) {
                try {
                    String newName = args[1];
                    for (int i = 2; i < args.length; i++) {
                        newName = newName + " " + args[i];
                    }
                    if (FactionManager.changeRankName(playerData.getFaction(), Integer.parseInt(args[0]), newName)) {
                        player.sendMessage(Main.faction_prefix + "Rangname von Rang §l" + args[0] + "§7 zu §l" + newName + "§7 geändert.");
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            } else {
                player.sendMessage(Main.faction_prefix + "Syntax-Fehler: /setrankname [Rang] [Name]");
            }
        } else {
            player.sendMessage(Main.error_nopermission);
        }
        return false;
    }
}
