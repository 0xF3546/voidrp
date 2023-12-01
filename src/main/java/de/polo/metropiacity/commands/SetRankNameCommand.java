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

public class SetRankNameCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final AdminManager adminManager;
    private final FactionManager factionManager;
    public SetRankNameCommand(PlayerManager playerManager, AdminManager adminManager, FactionManager factionManager) {
        this.playerManager = playerManager;
        this.adminManager = adminManager;
        this.factionManager = factionManager;
        Main.registerCommand("setrankname", this);
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (playerData.getFactionGrade() >= 7) {
            if (args.length >= 2) {
                try {
                    String newName = args[1];
                    for (int i = 2; i < args.length; i++) {
                        newName = newName + " " + args[i];
                    }
                    if (factionManager.changeRankName(playerData.getFaction(), Integer.parseInt(args[0]), newName)) {
                        player.sendMessage(Main.faction_prefix + "Rangname von Rang §l" + args[0] + "§7 zu §l" + newName + "§7 geändert.");
                        adminManager.send_message(player.getName() + " den Namen von Rang " + args[0] + " auf " + args[1] + " gesetzt (" + playerData.getFaction() + ").", ChatColor.DARK_PURPLE);
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
