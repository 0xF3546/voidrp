package de.polo.metropiacity.commands;

import de.polo.metropiacity.Main;
import de.polo.metropiacity.Utils.FactionManager;
import de.polo.metropiacity.Utils.PlayerManager;
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
        if (PlayerManager.isInStaatsFrak(player) && FactionManager.faction_grade(player) >= 5) {
            if (args.length >= 1) {
                StringBuilder message = new StringBuilder(args[0]);
                for (int i = 1; i < args.length; i++) {
                    message.append(" ").append(args[i]);
                }
                if (PlayerManager.perms(player) >= 70) {
                    Bukkit.broadcastMessage(" ");
                    Bukkit.broadcastMessage("§7§m====§8[§" + FactionManager.getFactionPrimaryColor(playerfac) + "§l" + FactionManager.getFactionFullname(playerfac) + "§8]§7§m====");
                    Bukkit.broadcastMessage(" ");
                    Bukkit.broadcastMessage("§8➥§" + FactionManager.getFactionSecondaryColor(playerfac) + " " + player.getName() + "§8: §7" + message);
                    Bukkit.broadcastMessage(" ");
                    Bukkit.broadcastMessage("§7§m====§8[§" + FactionManager.getFactionPrimaryColor(playerfac) + "§l" + FactionManager.getFactionFullname(playerfac) + "§8]§7§m====");
                    Bukkit.broadcastMessage(" ");
                }
            } else {
                player.sendMessage(Main.error + "Syntax-Fehler: /gov [Nachricht]");
            }
        }
        return false;
    }
}
