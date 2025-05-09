package de.polo.core.faction.commands;

import de.polo.api.Utils.ApiUtils;
import de.polo.api.VoidAPI;
import de.polo.api.player.VoidPlayer;
import de.polo.core.Main;
import de.polo.core.faction.entity.Faction;
import de.polo.core.faction.entity.FactionPlayerData;
import de.polo.core.faction.service.impl.FactionManager;
import de.polo.core.manager.ServerManager;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.utils.Prefix;
import de.polo.core.utils.Utils;
import net.kyori.adventure.text.Component;
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
        Faction factionData = null;
        VoidPlayer voidPlayer = VoidAPI.getPlayer(player);
        if (!voidPlayer.isAduty()) {
            factionData = factionManager.getFactionData(playerData.getFaction());
        } else {
            if (args.length >= 1) {
                factionData = factionManager.getFactionData(args[0]);
            } else {
                factionData = factionManager.getFactionData(playerData.getFaction());
            }
        }
        player.sendMessage("§7   ===§8[§" + factionData.getPrimaryColor() + "Statistiken§8]§7===");
        player.sendMessage("§8 - §6Name§8:§c " + factionData.getName());
        player.sendMessage("§8 - §6Voller Name§8:§c " + factionData.getFullname());
        if (playerData.getFactionGrade() >= 4 || voidPlayer.isAduty()) {
            player.sendMessage("§8 - §6Bank§8:§c " + factionData.getBank() + "$");
        }
        player.sendMessage("§8 - §6Equip§8:§c " + factionData.getEquipPoints() + " Punkte");
        int member = 0;
        for (FactionPlayerData factionPlayerData : ServerManager.factionPlayerDataMap.values()) {
            if (factionPlayerData.getFaction().equals(factionData.getName())) {
                member++;
            }
        }
        player.sendMessage(Component.text("§8 - §6Typ§8:§c " + factionData.getFactionType().getName()));
        player.sendMessage("§8 - §6Mitglieder§8:§c " + member + "/" + factionData.getMaxMember());
        player.sendMessage("§8 - §6PayDay§8:§c " + ApiUtils.getTime().getMinute() + "/60");
        return false;
    }
}
