package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.manager.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CheckInvCommand implements CommandExecutor {
    private final PlayerManager playerManager;

    public CheckInvCommand(PlayerManager playerManager) {
        this.playerManager = playerManager;
        Main.registerCommand("checkinv", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());

        if (playerData.getPermlevel() < 60) {
            player.sendMessage(Main.error_nopermission);
            return false;
        }
        if (!playerData.isAduty()) {
            player.sendMessage(Main.admin_error + "Du bist nicht im Admindienst!");
            return false;
        }
        if (args.length >= 1) {
            Player targetplayer = Bukkit.getPlayer(args[0]);
            player.openInventory(targetplayer.getInventory());
        } else {
            player.sendMessage(Main.admin_error + "Syntax-Fehler: /checkinv [Spieler]");
        }
        return false;
    }
}
