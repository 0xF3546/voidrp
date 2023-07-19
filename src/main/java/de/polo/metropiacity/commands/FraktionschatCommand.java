package de.polo.metropiacity.commands;

import de.polo.metropiacity.Main;
import de.polo.metropiacity.DataStorage.FactionData;
import de.polo.metropiacity.Utils.FactionManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;

public class FraktionschatCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        String uuid = player.getUniqueId().toString();
        if (FactionManager.faction(player) != null) {
            if (args.length >= 1) {
                String msg = args[0];
                for (int i = 1; i < args.length; i++) {
                    msg = msg + ' ' + args[i];
                }
                String playerfac = FactionManager.faction(player);
                FactionData factionData = FactionManager.factionDataMap.get(playerfac);
                for (Player players : Bukkit.getOnlinePlayers()) {
                    if (Objects.equals(FactionManager.faction(players), playerfac)) {
                        players.sendMessage("§"+FactionManager.getFactionPrimaryColor(playerfac) + FactionManager.getPlayerFactionRankName(player) + " " + player.getName() + "§8:§7 " + msg);
                    }
                }
            } else {
                player.sendMessage(Main.error + "Syntax-Error: /fraktionschat [Nachricht]");
            }
        } else {
            player.sendMessage(Main.error_nopermission);
        }
        return false;
    }
}
