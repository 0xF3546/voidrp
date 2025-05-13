package de.polo.core.commands;

import de.polo.api.VoidAPI;
import de.polo.core.Main;
import de.polo.core.faction.service.LawEnforcementService;
import de.polo.core.game.base.extra.PlaytimeReward;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.storage.WantedReason;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;

import static de.polo.core.Main.utils;

public class StatsCommand implements CommandExecutor {
    private final PlayerManager playerManager;

    public StatsCommand(PlayerManager playerManager) {
        this.playerManager = playerManager;
        Main.registerCommand("stats", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        player.sendMessage("§7    ===§8[§6Statistiken§8]§7===");
        player.sendMessage(" §8- §6Level§8:§c " + playerData.getLevel() + " (" + playerData.getExp() + "/" + playerData.getNeeded_exp() + ")");
        player.sendMessage(" §8- §6Visum§8:§c " + playerData.getVisum());
        if (playerData.getWanted() == null) {
            player.sendMessage(" §8- §6Fahndung§8:§c Keine Fahndung");
        } else {
            LawEnforcementService lawEnforcementService = VoidAPI.getService(LawEnforcementService.class);
            WantedReason wantedReason = lawEnforcementService.getWantedReason(playerData.getWanted().getWantedId());
            player.sendMessage(" §8- §6Fahndung§8:§c " + wantedReason.getWanted() + " WPS");
        }
        player.sendMessage(" §8- §6Bargeld§8:§c " + playerData.getBargeld() + "$");
        player.sendMessage(" §8- §6PayDay§8:§c " + playerManager.paydayDuration(player) + "/60");
        player.sendMessage(" §8- §6Spielzeit§8:§c " + playerData.getHours() + " Stunden & " + playerData.getMinutes() + " Minuten");
        if (playerData.getFaction() != null && !Objects.equals(playerData.getFaction(), "Zivilist")) {
            player.sendMessage(" §8- §6Fraktion§8:§c " + playerData.getFaction() + " (" + playerData.getFactionGrade() + "/6)");
        } else {
            player.sendMessage(" §8- §6Fraktion§8:§c Zivilist");
        }
        player.sendMessage(" §8- §6Rang§8:§c " + playerData.getRang());
        player.sendMessage(" §8- §6Krankenkasse§8:§c " + playerData.getHealthInsurance().getName());
        player.sendMessage(" §8- §6Votes§8:§c " + playerData.getVotes());
        if (playerData.getSubGroupId() != 0) {
            player.sendMessage(" §8- §6Gruppierung§8:§c " + playerData.getSubGroup().getName());
        }
        PlaytimeReward playtimeReward = playerManager.getPlaytimeReward(playerData.getRewardId());
        player.sendMessage("§8 - §6Spielzeitbelohnung§8: §c" + playtimeReward.getDisplayName() + " §8- §c" + playerData.getRewardTime() + "h verbleibend" + (playtimeReward.isPremiumOnly() ? "§8[§6Premium§8]" : ""));
        player.sendMessage("§8 - §6Inventar§8: §c" + playerData.getInventory().getWeight() + "/" + playerData.getInventory().getSize());
        player.sendMessage("§8 - §6Treuepunkte§8:§c " + playerData.getLoyaltyBonus());
        return false;
    }
}
