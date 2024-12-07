package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.FactionData;
import de.polo.voidroleplay.dataStorage.FactionPlayerData;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.manager.FactionManager;
import de.polo.voidroleplay.manager.PlayerManager;
import de.polo.voidroleplay.utils.Utils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MemberCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final FactionManager factionManager;

    public MemberCommand(PlayerManager playerManager, FactionManager factionManager) {
        this.playerManager = playerManager;
        this.factionManager = factionManager;
        Main.registerCommand("member", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (playerData.getFaction() != null && !playerData.getFaction().equals("Zivilist")) {
            FactionData factionData = factionManager.getFactionData(playerData.getFaction());
            player.sendMessage("§7   ===§8[§" + factionData.getPrimaryColor() + factionData.getFullname() + "§8]§7===");

            Map<Integer, List<FactionPlayerData>> sort = new HashMap<>();

            for (FactionPlayerData factionPlayerData : factionManager.getFactionMember(playerData.getFaction())) {
                if (factionPlayerData.getFaction().equals(playerData.getFaction())) {
                    int grade = factionPlayerData.getFaction_grade();
                    sort.computeIfAbsent(grade, k -> new ObjectArrayList<>()).add(factionPlayerData);
                }
            }

            // Display faction members by grade
            for (int value = 6; value >= 0; value--) {
                String message = "§8 » §" + factionData.getSecondaryColor() + "[" + value + "] - " + factionManager.getRankName(playerData.getFaction(), value) + " - " + factionManager.getPaydayFromFaction(playerData.getFaction(), value) + "$/PayDay";
                player.sendMessage(message);

                List<FactionPlayerData> members = sort.get(value);
                if (members != null) {
                    for (FactionPlayerData factionPlayerData : members) {
                        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(factionPlayerData.getUuid()));
                        LocalDateTime lastLogin = factionPlayerData.getLastLogin();

                        if (offlinePlayer.isOnline()) {
                            player.sendMessage("§8 → §a" + offlinePlayer.getName());
                        } else {
                            String lastLoginTime = Utils.localDateTimeToReadableString(lastLogin);
                            if (playerData.isLeader()) {
                                player.sendMessage("§8 → §c" + offlinePlayer.getName() + " - (" + lastLoginTime + ")");
                            } else {
                                player.sendMessage("§8 → §c" + offlinePlayer.getName());
                            }
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