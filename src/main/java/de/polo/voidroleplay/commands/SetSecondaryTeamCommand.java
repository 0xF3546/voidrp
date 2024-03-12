package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.utils.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.sql.Statement;

public class SetSecondaryTeamCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    public SetSecondaryTeamCommand(PlayerManager playerManager) {
        this.playerManager = playerManager;
        Main.registerCommand("setsecondaryteam", this);
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (playerData.getPermlevel() >= 90) {
            if (args.length >= 1) {
                Player targetplayer = Bukkit.getPlayer(args[0]);
                if (targetplayer != null) {
                    PlayerData targetplayerData = playerManager.getPlayerData(targetplayer.getUniqueId());
                    targetplayerData.setSecondaryTeam(args[1]);
                    targetplayer.sendMessage("§8[§6" + args[1] + "§8]§e " + player.getName() + " hat dich in das Team hinzugefügt.");
                    player.sendMessage("§8[§6" + args[1] + "§8]§e Du hast " + targetplayer.getName() + " in das Team hinzugefügt.");
                    try {
                        Statement statement = Main.getInstance().mySQL.getStatement();
                        statement.executeUpdate("UPDATE `players` SET `secondaryTeam` = '" + args[1] + "' WHERE `uuid` = '" + targetplayer.getUniqueId() + "'");
                        statement.close();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    player.sendMessage(Main.error + args[0] + " ist nicht online.");
                }
            } else {
                player.sendMessage(Main.error + "Syntax-Fehler: /setsecondaryteam [Spieler] [Team]");
                player.sendMessage("§8 ➥ §bInfo§8:§f Folgende Teams gibt es: Bau-Team, PR-Team, Event-Team");
            }
        } else {
            player.sendMessage(Main.error_nopermission);
        }
        return false;
    }
}
