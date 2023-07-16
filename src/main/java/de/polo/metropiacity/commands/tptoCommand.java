package de.polo.metropiacity.commands;

import de.polo.metropiacity.DataStorage.LocationData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.DataStorage.PlayerData;
import de.polo.metropiacity.Utils.LocationManager;
import de.polo.metropiacity.Utils.PlayerManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
public class tptoCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        if (playerData.isAduty()) {
            if (args.length >= 1) {
                StringBuilder message = new StringBuilder();
                for (String arg : args) {
                    message.append(" ").append(arg);
                }
                LocationManager.useLocation(player, String.valueOf(message).replace(" ", ""));
                player.sendMessage(Main.admin_prefix + "Du hast dich zu §c" + message + "§7 teleportiert.");
            } else {
                player.sendMessage(Main.admin_error + "Syntax-Fehler: /tpto [Punkt]");
            }
        } else {
            player.sendMessage(Main.admin_error + "Du bist nicht im Admindienst!");
        }
        return false;
    }
    @Nullable
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>();
            for (LocationData locationData : LocationManager.locationDataMap.values()) {
                suggestions.add(locationData.getName());
            }

            return suggestions;
        }
        return null;
    }
}
