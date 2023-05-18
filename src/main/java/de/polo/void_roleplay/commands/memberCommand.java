package de.polo.void_roleplay.commands;

import de.polo.void_roleplay.DataStorage.FactionData;
import de.polo.void_roleplay.DataStorage.FactionPlayerData;
import de.polo.void_roleplay.DataStorage.PlayerData;
import de.polo.void_roleplay.Main;
import de.polo.void_roleplay.Utils.FactionManager;
import de.polo.void_roleplay.Utils.PlayerManager;
import de.polo.void_roleplay.Utils.ServerManager;
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
                    player.sendMessage("§8 ➥ §e" + offlinePlayer.getName() + "§8 | §eRang " + factionPlayerData.getFaction_grade());
                }
            }
            player.sendMessage(" ");
        } else {
            player.sendMessage(Main.error_nopermission);
        }
        return false;
    }
}
