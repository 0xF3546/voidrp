package de.polo.core.admin.commands;

import de.polo.core.Main;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.utils.Prefix;
import de.polo.core.utils.TeamSpeak;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
                    Main.getInstance().getCoreDatabase().updateAsync("UPDATE players SET secondaryTeam = ? WHERE uuid = ?", args[1], targetplayer.getUniqueId().toString());
                    TeamSpeak.reloadPlayer(targetplayer.getUniqueId());
                } else {
                    player.sendMessage(Prefix.ERROR + args[0] + " ist nicht online.");
                }
            } else {
                player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /setsecondaryteam [Spieler] [Team]");
                player.sendMessage("§8 ➥ §bInfo§8:§f Folgende Teams gibt es: Bau-Team, PR-Team, Event-Team");
            }
        } else {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
        }
        return false;
    }
}
