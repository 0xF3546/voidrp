package de.polo.void_roleplay.commands;

import de.polo.void_roleplay.DataStorage.ContractData;
import de.polo.void_roleplay.DataStorage.NaviData;
import de.polo.void_roleplay.DataStorage.PlayerData;
import de.polo.void_roleplay.Main;
import de.polo.void_roleplay.Utils.LocationManager;
import de.polo.void_roleplay.Utils.PlayerManager;
import de.polo.void_roleplay.Utils.ServerManager;
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

public class contractsCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        if (playerData.getFaction().equals("ICA")) {
            if (args.length >= 1) {
                if (args[0].equalsIgnoreCase("all")) {
                    player.sendMessage("§7   ===§8[§cAlle Kopfgelder§8]§7===");
                    for (ContractData contractData : ServerManager.contractDataMap.values()) {
                        OfflinePlayer offPLayer = Bukkit.getOfflinePlayer(UUID.fromString(contractData.getUuid()));
                        player.sendMessage("§8 ➥ §e" + offPLayer.getName() + "§8 | §e" + contractData.getAmount() + "$");
                    }
                }
            } else {
                player.sendMessage("§7   ===§8[§cKopfgelder§8]§7===");
                for (ContractData contractData : ServerManager.contractDataMap.values()) {
                    OfflinePlayer offPLayer = Bukkit.getOfflinePlayer(UUID.fromString(contractData.getUuid()));
                    if (offPLayer.isOnline()) {
                        player.sendMessage("§8 ➥ §e" + offPLayer.getName() + "§8 | §e" + contractData.getAmount() + "$");
                    }
                }
            }
        } else {
            player.sendMessage(Main.error_nopermission);
        }
        return false;
    }
    @Nullable
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>();
            suggestions.add("all");

            return suggestions;
        }
        return null;
    }
}
