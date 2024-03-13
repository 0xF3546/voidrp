package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.utils.Utils;
import de.polo.voidroleplay.utils.playerUtils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WhistleCommand implements CommandExecutor {
    private final Utils utils;
    private final PlayerManager playerManager;
    public WhistleCommand(Utils utils, PlayerManager playerManager) {
        this.utils = utils;
        this.playerManager = playerManager;
        Main.registerCommand("whistle", this);
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player);
        if (args.length < 1) {
            player.sendMessage(Main.error + "Syntax-Fehler: /whistle [Nachricht]");
            return false;
        }
        for (Player players : Bukkit.getOnlinePlayers()) {
            if (player.getLocation().distance(players.getLocation()) <= 3) {
                players.sendMessage("§8[§c" + playerData.getLevel() +"§8] §8" + player.getName() + " flüstert§8:§8 " + utils.stringArrayToString(args));
            }
        }
        ChatUtils.LogMessage(utils.stringArrayToString(args), player.getUniqueId());
        return false;
    }
}
