package de.polo.core.admin.commands;

import de.polo.api.VoidAPI;
import de.polo.api.player.VoidPlayer;
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

public class BauteamChat implements CommandExecutor {
    private final PlayerManager playerManager;
    private final Utils utils;

    public BauteamChat(PlayerManager playerManager, Utils utils) {
        this.playerManager = playerManager;
        this.utils = utils;
        Main.registerCommand("bauteamchat", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (playerData.getSecondaryTeam() == null) {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
            return false;
        }
        VoidPlayer voidPlayer = VoidAPI.getPlayer(player);
        if (playerData.getSecondaryTeam().equals("Bau-Team") || playerData.getPermlevel() >= 70) {
            for (Player players : Bukkit.getOnlinePlayers()) {
                PlayerData playersData = playerManager.getPlayerData(players.getUniqueId());
                if (playersData.getSecondaryTeam() != null) {
                    VoidPlayer voidPlayers = VoidAPI.getPlayer(players);
                        if (playersData.getSecondaryTeam().equals("Bau-Team") || voidPlayers.isAduty() || playersData.getPermlevel() >= 70) {
                            players.sendMessage("§6BauTeam §8┃ §e" + player.getName() + "§8:§7 " + Utils.stringArrayToString(args));
                    }
                }
            }
        } else {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
        }
        return false;
    }
}
