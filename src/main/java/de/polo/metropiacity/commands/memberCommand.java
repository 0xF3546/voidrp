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

import java.util.UUID;

public class memberCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        if (playerData.getFaction() != null && !playerData.getFaction().equals("Zivilist")) {
            FactionData factionData = FactionManager.factionDataMap.get(playerData.getFaction());
            player.sendMessage("§7   ===§8[§" + factionData.getPrimaryColor() + factionData.getFullname() + "§8]§7===");
            for (FactionPlayerData factionPlayerData : ServerManager.factionPlayerDataMap.values()) {
                if (factionPlayerData.getFaction().equals(playerData.getFaction())) {
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(factionPlayerData.getUuid()));
                    if (offlinePlayer.isOnline()) {
                        player.sendMessage("§8 ➥ §a" + offlinePlayer.getName() + "§8 | §aRang " + factionPlayerData.getFaction_grade());
                    } else {
                        player.sendMessage("§8 ➥ §c" + offlinePlayer.getName() + "§8 | §cRang " + factionPlayerData.getFaction_grade());
                    }
                }
            }
            player.sendMessage(" ");
        } else {
            player.sendMessage(Main.error_nopermission);
        }
        return false;
    }
}
