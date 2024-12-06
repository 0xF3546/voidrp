package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.FactionData;
import de.polo.voidroleplay.dataStorage.FactionPlayerData;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.manager.FactionManager;
import de.polo.voidroleplay.manager.PlayerManager;
import de.polo.voidroleplay.manager.ServerManager;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FrakStatsCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final FactionManager factionManager;
    private final Utils utils;

    public FrakStatsCommand(PlayerManager playerManager, FactionManager factionManager, Utils utils) {
        this.playerManager = playerManager;
        this.factionManager = factionManager;
        this.utils = utils;
        Main.registerCommand("frakstats", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (playerData == null) return false;
        if (playerData.getFaction() == null) {
            player.sendMessage(Prefix.ERROR + "Du bist in keiner Fraktion.");
        }
        FactionData factionData = null;
        if (!playerData.isAduty()) {
            factionData = factionManager.getFactionData(playerData.getFaction());
        } else {
            if (args.length >= 1) {
                factionData = factionManager.getFactionData(args[0]);
            } else {
                factionData = factionManager.getFactionData(playerData.getFaction());
            }
        }
        player.sendMessage("§7   ===§8[§" + factionData.getPrimaryColor() + "Statistiken§8]§7===");
        player.sendMessage("§8 - §eName§8:§7 " + factionData.getName());
        player.sendMessage("§8 - §eVoller Name§8:§7 " + factionData.getFullname());
        if (playerData.getFactionGrade() >= 4 || playerData.isAduty()) {
            player.sendMessage("§8 - §eBank§8:§7 " + factionData.getBank() + "$");
        }
        player.sendMessage("§8 - §eEquip§8:§7 " + factionData.getEquipPoints() + " Punkte");
        int member = 0;
        for (FactionPlayerData factionPlayerData : ServerManager.factionPlayerDataMap.values()) {
            if (factionPlayerData.getFaction().equals(factionData.getName())) {
                member++;
            }
        }
        player.sendMessage("§8 - §eMitglieder§8:§7 " + member + "/" + factionData.getMaxMember());
        player.sendMessage("§8 - §ePayDay§8:§7 " + utils.getCurrentMinute() + "/60");
        return false;
    }
}
