package de.polo.void_roleplay.commands;

import de.polo.void_roleplay.Utils.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class teamCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        player.sendMessage("§6§lTeamübersicht§8:");
        for (Player players : Bukkit.getOnlinePlayers()) {
            if (PlayerManager.isTeam(players)) {
                player.sendMessage("§8 ➥ §e" + PlayerManager.rang(players) + " " + players.getName());
            }
        }
        return false;
    }
}
