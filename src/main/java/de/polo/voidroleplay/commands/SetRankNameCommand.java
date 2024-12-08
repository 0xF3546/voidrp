package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.manager.AdminManager;
import de.polo.voidroleplay.manager.FactionManager;
import de.polo.voidroleplay.manager.PlayerManager;
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
        if (playerData.isLeader()) {
            if (args.length >= 2) {
                try {
                    StringBuilder newName = new StringBuilder(args[1]);
                    for (int i = 2; i < args.length; i++) {
                        newName.append(" ").append(args[i]);
                    }
                    if (factionManager.changeRankName(playerData.getFaction(), Integer.parseInt(args[0]), newName.toString())) {
                        player.sendMessage(Main.faction_prefix + "Rangname von Rang §l" + args[0] + "§7 zu §l" + newName + "§7 geändert.");
                        adminManager.send_message(player.getName() + " den Namen von Rang " + args[0] + " auf " + newName + " gesetzt (" + playerData.getFaction() + ").", ChatColor.DARK_PURPLE);
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
