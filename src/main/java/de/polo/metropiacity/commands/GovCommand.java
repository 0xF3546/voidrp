package de.polo.metropiacity.commands;

import de.polo.metropiacity.Main;
import de.polo.metropiacity.dataStorage.PlayerData;
import de.polo.metropiacity.utils.FactionManager;
import de.polo.metropiacity.utils.PlayerManager;
import de.polo.metropiacity.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GovCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        String playerfac = FactionManager.faction(player);
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        if (!PlayerManager.isInStaatsFrak(player)) {
            player.sendMessage(Main.error_nopermission);
            return false;
        }
        if (playerData.getFactionGrade() < 5) {
            player.sendMessage(Main.error + "Du musst mindestens Rang 5+ sein.");
            return false;
        }
        if (args.length < 1) {
            player.sendMessage(Main.error + "Syntax-Fehler: /gov [Nachricht]");
            return false;
        }
        Bukkit.broadcastMessage(" ");
        Bukkit.broadcastMessage("§7§m====§8[§" + FactionManager.getFactionPrimaryColor(playerfac) + "§l" + FactionManager.getFactionFullname(playerfac) + "§8]§7§m====");
        Bukkit.broadcastMessage(" ");
        Bukkit.broadcastMessage("§8➥§" + FactionManager.getFactionSecondaryColor(playerfac) + " " + player.getName() + "§8: §7" + Utils.stringArrayToString(args));
        Bukkit.broadcastMessage(" ");
        Bukkit.broadcastMessage("§7§m====§8[§" + FactionManager.getFactionPrimaryColor(playerfac) + "§l" + FactionManager.getFactionFullname(playerfac) + "§8]§7§m====");
        Bukkit.broadcastMessage(" ");
        return false;
    }
}
