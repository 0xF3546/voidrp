package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.utils.Utils;
import de.polo.voidroleplay.utils.playerUtils.ChatUtils;
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
            player.sendMessage(Main.error + "Syntax-Fehler: /shout [Nachricht]");
            return false;
        }
        for (Player players : Bukkit.getOnlinePlayers()) {
            if (player.getLocation().distance(players.getLocation()) <= 28) {
                players.sendMessage("§8[§c" + playerData.getLeve() + "§8] §f" + player.getName() + " schreit§8:§f " + utils.stringArrayToString(args) + "!");
            }
        }
        ChatUtils.LogMessage(utils.stringArrayToString(args), player.getUniqueId());
        return false;
    }
}
