package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.manager.PlayerManager;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.utils.Utils;
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
        if (playerData.getSecondaryTeam().equals("Bau-Team") || playerData.getPermlevel() >= 70) {
            for (Player players : Bukkit.getOnlinePlayers()) {
                PlayerData playersData = playerManager.getPlayerData(players.getUniqueId());
                if (playersData.getSecondaryTeam() != null) {
                    if (playersData.getSecondaryTeam().equalsIgnoreCase("Bau-Team") || playersData.isAduty()) {
                        if (playersData.getSecondaryTeam().equals("Bau-Team") || playersData.isAduty() || playersData.getPermlevel() >= 70) {
                            players.sendMessage("§8[§6BauTeam§8]§e " + player.getName() + "§8:§7 " + utils.stringArrayToString(args));
                        }
                    }
                }
            }
        } else {
            player.sendMessage(Main.error_nopermission);
        }
        return false;
    }
}
