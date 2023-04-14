package de.polo.void_roleplay.commands;

import de.polo.void_roleplay.Main;
import de.polo.void_roleplay.Utils.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class personalausweisCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        if (PlayerManager.firstname(player) != null && PlayerManager.lastname(player) != null) {
            if (args.length >= 1) {
                if (args[0].equalsIgnoreCase("show")) {
                    Player targetplayer = Bukkit.getPlayer(args[1]);
                    if (targetplayer != null) {
                        player.sendMessage(Main.prefix + "Du hast §c" + targetplayer.getName() + "§7 deinen Personalausweis gezeigt.");
                        targetplayer.sendMessage("");
                        targetplayer.sendMessage("§7     ===§8[§6FREMDER PERSONALAUSWEIS§8]§7===");
                        targetplayer.sendMessage("§8 ➥ §eVorname§8:§7 " + PlayerManager.firstname(player));
                        targetplayer.sendMessage("§8 ➥ §eNachname§8:§7 " + PlayerManager.lastname(player));
                        targetplayer.sendMessage(" ");
                        targetplayer.sendMessage("§8 ➥ §eVisumstufe§8:§7 " + PlayerManager.visum(player));
                    } else {
                        player.sendMessage(Main.error + "Es wurde kein Spieler mit diesem Namen gefunden.");
                    }
                }
            } else {
                player.sendMessage("");
                player.sendMessage("§7     ===§8[§6PERSONALAUSWEIS§8]§7===");
                player.sendMessage("§8 ➥ §eVorname§8:§7 " + PlayerManager.firstname(player));
                player.sendMessage("§8 ➥ §eNachname§8:§7 " + PlayerManager.lastname(player));
                player.sendMessage(" ");
                player.sendMessage("§8 ➥ §eVisumstufe§8:§7 " + PlayerManager.visum(player));
            }
        } else {
            player.sendMessage(Main.error + "Du besitzt noch keinen Personalausweis.");
        }
        return false;
    }
}
