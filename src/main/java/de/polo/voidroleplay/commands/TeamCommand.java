package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.manager.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TeamCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    public TeamCommand(PlayerManager playerManager) {
        this.playerManager = playerManager;
        Main.registerCommand("team", this);
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        player.sendMessage("§6§lTeamübersicht§8:");
        for (Player players : Bukkit.getOnlinePlayers()) {
            if (playerManager.getPlayerData(players.getUniqueId()).getPermlevel() >= 50) {
                player.sendMessage("§8 ➥ §e" + playerManager.rang(players) + " " + players.getName());
            }
        }
        return false;
    }
}
