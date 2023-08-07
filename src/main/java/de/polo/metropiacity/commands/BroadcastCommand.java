package de.polo.metropiacity.commands;

import de.polo.metropiacity.Main;
import de.polo.metropiacity.dataStorage.PlayerData;
import de.polo.metropiacity.utils.PlayerManager;
import de.polo.metropiacity.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BroadcastCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = PlayerManager.getPlayerData(player);
        if (playerData.getPermlevel() < 70) {
            player.sendMessage(Main.error_nopermission);
            return false;
        }
        if (!playerData.isAduty()) {
            player.sendMessage(Main.admin_error + "Du bist nicht im Admindienst!");
            return false;
        }
        if (args.length < 1) {
            player.sendMessage(Main.admin_error + "Syntax-Fehler: /announce [Nachricht]");
            return false;
        }
        Bukkit.broadcastMessage(" ");
        Bukkit.broadcastMessage("§7§m====§8[§c§lAnkündigung§8]§7§m====");
        Bukkit.broadcastMessage(" ");
        Bukkit.broadcastMessage("§8➥§c " + player.getName() + "§8: §7" + Utils.stringArrayToString(args));
        Bukkit.broadcastMessage(" ");
        Bukkit.broadcastMessage("§7§m====§8[§c§lAnkündigung§8]§7§m====");
        Bukkit.broadcastMessage(" ");
        return false;
    }
}
