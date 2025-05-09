package de.polo.core.faction.commands;

import de.polo.core.Main;
import de.polo.core.faction.entity.Faction;
import de.polo.core.faction.entity.FactionPlayerData;
import de.polo.core.faction.service.impl.FactionManager;
import de.polo.core.handler.TabCompletion;
import de.polo.core.manager.ServerManager;
import de.polo.core.utils.Prefix;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class FrakInfoCommand implements CommandExecutor, TabCompleter {
    private final FactionManager factionManager;

    public FrakInfoCommand(FactionManager factionManager) {
        this.factionManager = factionManager;
        Main.registerCommand("frakinfo", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        if (args.length >= 1) {
            Faction factionData = null;
            for (Faction fdata : factionManager.getFactions()) {
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
                player.sendMessage(Prefix.ERROR + "Die Fraktion \"" + args[0] + "\" konnte nicht gefunden werden.");
            }
        } else {
            player.sendMessage("§7   ===§8[§6Fraktionen§8]§7===");
            for (Faction factionData : factionManager.getFactions()) {
                if (!factionData.isActive()) continue;
                int count = factionManager.getOnlineMemberCount(factionData.getName());
                player.sendMessage("§8 ➥ §" + factionData.getPrimaryColor() + factionData.getFullname() + "§8 - §7" + count + " online");
            }
        }
        return false;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return TabCompletion.getBuilder(args)
                .addAtIndex(1, factionManager.getFactions()
                        .stream()
                        .filter(Faction::isActive)
                        .map(Faction::getName)
                        .toList())
                .build();
    }
}
