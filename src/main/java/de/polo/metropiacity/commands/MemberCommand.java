package de.polo.metropiacity.commands;

import de.polo.metropiacity.DataStorage.FactionData;
import de.polo.metropiacity.DataStorage.FactionPlayerData;
import de.polo.metropiacity.DataStorage.PlayerData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.Utils.FactionManager;
import de.polo.metropiacity.Utils.PlayerManager;
import de.polo.metropiacity.Utils.ServerManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class MemberCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        if (playerData.getFaction() != null && !playerData.getFaction().equals("Zivilist")) {
            FactionData factionData = FactionManager.factionDataMap.get(playerData.getFaction());
            player.sendMessage("§7   ===§8[§" + factionData.getPrimaryColor() + factionData.getFullname() + "§8]§7===");
            Map<Integer, List<OfflinePlayer>> sort = new HashMap<>();
            for (FactionPlayerData factionPlayerData : ServerManager.factionPlayerDataMap.values()) {
                if (factionPlayerData.getFaction().equals(playerData.getFaction())) {
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(factionPlayerData.getUuid()));
                    if (sort.containsKey(factionPlayerData.getFaction_grade())) {
                        sort.get(factionPlayerData.getFaction_grade()).add(offlinePlayer);
                        System.out.println(factionPlayerData.getFaction_grade() + " | " + offlinePlayer.getName());
                    } else {
                        sort.put(factionPlayerData.getFaction_grade(), new ArrayList<>());
                        sort.get(factionPlayerData.getFaction_grade()).add(offlinePlayer);
                    }
                }
            }
            for (int value = 8; value >= 0; value--) {
                String message = "§8 » §" + factionData.getSecondaryColor() + FactionManager.getRankName(playerData.getFaction(), value) + " - " + FactionManager.getPaydayFromFaction(playerData.getFaction(), value) + "$/PayDay";
                player.sendMessage(message);
                if (sort.get(value) != null) {
                    for (int i = 0; i < sort.get(value).size(); i++) {
                        OfflinePlayer offlinePlayer = sort.get(value).get(i);

                        if (offlinePlayer.isOnline()) {
                            player.sendMessage("§8 → §a" + offlinePlayer.getName());
                        } else {
                            player.sendMessage("§8 → §c" + offlinePlayer.getName());
                        }
                    }
                    sort.remove(value);
                }
            }
            player.sendMessage(" ");
        } else {
            player.sendMessage(Main.error_nopermission);
        }
        return false;
    }
}
