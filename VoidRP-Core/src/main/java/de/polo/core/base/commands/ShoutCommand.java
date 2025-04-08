package de.polo.core.base.commands;

import de.polo.core.Main;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.utils.Prefix;
import de.polo.core.utils.Utils;
import de.polo.core.utils.player.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ShoutCommand implements CommandExecutor {
    private final Utils utils;
    private final PlayerManager playerManager;

    public ShoutCommand(Utils utils, PlayerManager playerManager) {
        this.utils = utils;
        this.playerManager = playerManager;
        Main.registerCommand("shout", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player);
        if (args.length < 1) {
            player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /shout [Nachricht]");
            return false;
        }
        String playerName = player.getName();
        if (Main.getInstance().gamePlay.getMaskState(player) != null) {
            playerName = "Maskierter";
        }
        for (Player players : Bukkit.getOnlinePlayers()) {
            if (players.getLocation().getWorld() != player.getLocation().getWorld()) continue;
            if (player.getLocation().distance(players.getLocation()) <= 28) {
                players.sendMessage("§8[§c" + playerData.getLevel() + "§8] §f" + playerName + " schreit: " + Utils.stringArrayToString(args) + "!");
            } else if (player.getLocation().distance(players.getLocation()) <= 38) {
                players.sendMessage("§8[§c" + playerData.getLevel() + "§8] §7" + playerName + " schreit: " + Utils.stringArrayToString(args) + "!");
            }
        }
        ChatUtils.logMessage(Utils.stringArrayToString(args), player.getUniqueId());
        return false;
    }
}
