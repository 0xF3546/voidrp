package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.utils.PlayerManager;
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
        String playerName = player.getName();
        if (Main.getInstance().gamePlay.getMaskState(player) != null) {
            playerName = "Maskierter";
        }
        for (Player players : Bukkit.getOnlinePlayers()) {
            if (player.getLocation().distance(players.getLocation()) <= 28) {
                players.sendMessage("§8[§c" + playerData.getLevel() + "§8] §f" + playerName + " schreit: " + utils.stringArrayToString(args) + "!");
            } else if (player.getLocation().distance(players.getLocation()) <= 38) {
                players.sendMessage("§8[§c" + playerData.getLevel() + "§8] §7" + playerName + " schreit: " + utils.stringArrayToString(args) + "!");
            }
        }
        ChatUtils.LogMessage(utils.stringArrayToString(args), player.getUniqueId());
        return false;
    }
}
