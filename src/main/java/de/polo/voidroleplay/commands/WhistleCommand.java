package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.manager.PlayerManager;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.utils.Utils;
import de.polo.voidroleplay.utils.player.ChatUtils;
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
            player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /whistle [Nachricht]");
            return false;
        }
        String playerName = player.getName();
        if (Main.getInstance().gamePlay.getMaskState(player) != null) {
            playerName = "Maskierter";
        }
        for (Player players : Bukkit.getOnlinePlayers()) {
            if (players.getLocation().getWorld() != player.getLocation().getWorld()) continue;
            if (player.getLocation().distance(players.getLocation()) <= 3) {
                players.sendMessage("§8[§c" + playerData.getLevel() + "§8] §7" + playerName + " flüstert: " + Utils.stringArrayToString(args));
            } else if (player.getLocation().distance(players.getLocation()) <= 5) {
                players.sendMessage("§8[§c" + playerData.getLevel() + "§8] §8" + playerName + " flüstert: " + Utils.stringArrayToString(args));
            }
        }
        ChatUtils.LogMessage(Utils.stringArrayToString(args), player.getUniqueId());
        return false;
    }
}
