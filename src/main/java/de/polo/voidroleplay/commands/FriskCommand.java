package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.utils.playerUtils.ChatUtils;
import de.polo.voidroleplay.utils.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FriskCommand implements CommandExecutor {
    private PlayerManager playerManager;

    public FriskCommand(PlayerManager playerManager) {
        this.playerManager = playerManager;
        Main.registerCommand("frisk", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        if (args.length < 1) {
            player.sendMessage(Main.error + "Syntax-Fehler: /frisk [Spieler]");
            return false;
        }
        Player targetplayer = Bukkit.getPlayer(args[0]);
        if (targetplayer != null) {
            player.sendMessage(Main.error + targetplayer.getName() + " ist nicht online.");
            return false;
        }
        if (targetplayer.getName().equals(player.getName())) {
            player.sendMessage(Main.error + "Du kannst dich nicht selbst durchsuchen.");
            return false;
        }
        if (player.getLocation().distance(targetplayer.getLocation()) > 5) {
            player.sendMessage(Main.error + targetplayer.getName() + " ist nicht in deiner nähe.");
            return false;
        }
        if (playerManager.canPlayerMove(targetplayer)) {
            player.sendMessage(Main.error + targetplayer.getName() + " ist nicht gefesselt oder in Handschellen.");
            return false;
        }
        player.openInventory(targetplayer.getInventory());
        ChatUtils.sendGrayMessageAtPlayer(player, player.getName() + " durchsucht " + targetplayer.getName());
        return false;
    }
}
