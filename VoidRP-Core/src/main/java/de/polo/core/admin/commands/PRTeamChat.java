package de.polo.core.admin.commands;

import de.polo.core.Main;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.utils.Prefix;
import de.polo.core.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PRTeamChat implements CommandExecutor {
    private final PlayerManager playerManager;
    private final Utils utils;

    public PRTeamChat(PlayerManager playerManager, Utils utils) {
        this.playerManager = playerManager;
        this.utils = utils;
        Main.registerCommand("prteamchat", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (playerData.getSecondaryTeam() == null) {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
            return false;
        }
        if (playerData.getSecondaryTeam().equals("PR-Team") || playerData.isAduty()) {
            for (Player players : Bukkit.getOnlinePlayers()) {
                PlayerData playersData = playerManager.getPlayerData(players.getUniqueId());
                if (playersData.getSecondaryTeam() != null) {
                    if (playersData.getSecondaryTeam().equalsIgnoreCase("PR-Team") || playersData.getPermlevel() >= 70) {
                        if (playersData.getSecondaryTeam().equals("PR-Team") || playersData.isAduty() || playersData.getPermlevel() >= 70) {
                            players.sendMessage("§8[§6PRTeam§8]§e " + player.getName() + "§8:§7 " + Utils.stringArrayToString(args));
                        }
                    }
                }
            }
        } else {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
        }
        return false;
    }
}
