package de.polo.metropiacity.commands;

import de.polo.metropiacity.DataStorage.FactionData;
import de.polo.metropiacity.DataStorage.FactionPlayerData;
import de.polo.metropiacity.DataStorage.PlayerData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.Utils.FactionManager;
import de.polo.metropiacity.Utils.PlayerManager;
import de.polo.metropiacity.Utils.ServerManager;
import de.polo.metropiacity.Utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FrakstatsCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        if (playerData.getFaction() != null) {
            FactionData factionData = null;
            if (!playerData.isAduty()) {
                factionData = FactionManager.factionDataMap.get(playerData.getFaction());
            } else {
                if (args.length >= 1) {
                    factionData = FactionManager.factionDataMap.get(args[0]);
                } else {
                    factionData = FactionManager.factionDataMap.get(playerData.getFaction());
                }
            }
            player.sendMessage("§7   ===§8[§" + factionData.getPrimaryColor() + "Statistiken§8]§7===");
            player.sendMessage("§8 - §eName§8:§7 " + factionData.getName());
            player.sendMessage("§8 - §eVoller Name§8:§7 " + factionData.getFullname());
            if (playerData.getFactionGrade() >= 5 || playerData.isAduty()) {
                player.sendMessage("§8 - §eBank§8:§7 " + factionData.getBank() + "$");
            }
            int member = 0;
            for (FactionPlayerData factionPlayerData : ServerManager.factionPlayerDataMap.values()) {
                if (factionPlayerData.getFaction().equals(factionData.getName())) {
                    member++;
                }
            }
            player.sendMessage("§8 - §eMitglieder§8:§7 " + member + "/" + factionData.getMaxMember());
            player.sendMessage("§8 - §ePayDay§8:§7 " + Utils.getCurrentMinute() + "/60");
        } else {
            player.sendMessage(Main.error + "Du bist in keiner Fraktion.");
        }
        return false;
    }
}
