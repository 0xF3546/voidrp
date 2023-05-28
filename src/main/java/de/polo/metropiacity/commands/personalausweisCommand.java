package de.polo.metropiacity.commands;

import de.polo.metropiacity.DataStorage.PlayerData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.PlayerUtils.tutorial;
import de.polo.metropiacity.Utils.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class personalausweisCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        if (PlayerManager.firstname(player) != null && PlayerManager.lastname(player) != null) {
            if (args.length >= 1) {
                if (args[0].equalsIgnoreCase("show")) {
                    Player targetplayer = Bukkit.getPlayer(args[1]);
                    if (targetplayer != null) {
                        if (player.getLocation().distance(targetplayer.getLocation()) <= 5) {
                            player.sendMessage(Main.prefix + "Du hast §c" + targetplayer.getName() + "§7 deinen Personalausweis gezeigt.");
                            targetplayer.sendMessage("");
                            targetplayer.sendMessage("§7     ===§8[§6FREMDER PERSONALAUSWEIS§8]§7===");
                            targetplayer.sendMessage(" ");
                            targetplayer.sendMessage("§8 ➥ §eVorname§8:§7 " + PlayerManager.firstname(player));
                            targetplayer.sendMessage("§8 ➥ §eNachname§8:§7 " + PlayerManager.lastname(player));
                            targetplayer.sendMessage("§8 ➥ §eGeschlecht§8:§7 " + playerData.getGender());
                            targetplayer.sendMessage("§8 ➥ §eGeburtsdatum§8:§7 " + playerData.getBirthday());
                            targetplayer.sendMessage(" ");
                            targetplayer.sendMessage("§8 ➥ §eVisumstufe§8:§7 " + PlayerManager.visum(player));
                        } else {
                            player.sendMessage(Main.error + targetplayer.getName() + " ist nicht in der nähe.");
                        }
                    } else {
                        player.sendMessage(Main.error + "Es wurde kein Spieler mit diesem Namen gefunden.");
                    }
                }
            } else {
                player.sendMessage("");
                player.sendMessage("§7     ===§8[§6PERSONALAUSWEIS§8]§7===");
                player.sendMessage(" ");
                player.sendMessage("§8 ➥ §eVorname§8:§7 " + PlayerManager.firstname(player));
                player.sendMessage("§8 ➥ §eNachname§8:§7 " + PlayerManager.lastname(player));
                player.sendMessage("§8 ➥ §eGeschlecht§8:§7 " + playerData.getGender());
                player.sendMessage("§8 ➥ §eGeburtsdatum§8:§7 " + playerData.getBirthday());
                player.sendMessage(" ");
                player.sendMessage("§8 ➥ §eVisumstufe§8:§7 " + PlayerManager.visum(player));
                tutorial.usedAusweis(player);
            }
        } else {
            player.sendMessage(Main.error + "Du besitzt noch keinen Personalausweis.");
        }
        return false;
    }
    @Nullable
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>();
            suggestions.add("show");

            return suggestions;
        }
        return null;
    }
}
