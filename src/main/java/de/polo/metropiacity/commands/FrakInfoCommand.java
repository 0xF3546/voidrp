package de.polo.metropiacity.commands;

import de.polo.metropiacity.dataStorage.FactionData;
import de.polo.metropiacity.dataStorage.FactionPlayerData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.utils.FactionManager;
import de.polo.metropiacity.utils.ServerManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FrakInfoCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        if (args.length >= 1) {
            FactionData factionData = null;
            for (FactionData fdata : FactionManager.factionDataMap.values()) {
                if (fdata.getName().equalsIgnoreCase(args[0])) {
                    factionData = fdata;
                }
            }
            if (factionData != null) {
                player.sendMessage("§7   ===§8[§" + factionData.getPrimaryColor() + factionData.getFullname() + "§8]§7===");
                int count = 0;
                for (FactionPlayerData factionPlayerData : ServerManager.factionPlayerDataMap.values()) {
                    if (factionPlayerData.getFaction().equals(factionData.getName())) {
                        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(factionPlayerData.getUuid()));
                        if (offlinePlayer.isOnline()) {
                            count++;
                            player.sendMessage("§8 ➥ §" + factionData.getSecondaryColor() + offlinePlayer.getName() + " (Rang " + factionPlayerData.getFaction_grade() + ")");
                        }
                    }
                }
                player.sendMessage("§8 »§7 Es sind §a" + count + " Fraktionsmitglieder§7 der Fraktion §" + factionData.getPrimaryColor() + factionData.getName() + "§7 online.");
            } else {
                player.sendMessage(Main.error + "Die Fraktion \"" + args[0] + "\" konnte nicht gefunden werden.");
            }
        } else {
            player.sendMessage(Main.error + "Syntax-Fehler: /fraktionsinfo [Fraktion]");
        }
        return false;
    }
    @Nullable
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>();
            for (FactionData factionData : FactionManager.factionDataMap.values()) {
                suggestions.add(factionData.getName());
            }

            return suggestions;
        }
        return null;
    }
}
