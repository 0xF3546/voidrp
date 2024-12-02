package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.game.base.extra.PlaytimeReward;
import de.polo.voidroleplay.utils.PlayerManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;

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
        player.sendMessage(" §8- §eLevel§8:§7 " + playerData.getLevel() + " (" + playerData.getExp() + "/" + playerData.getNeeded_exp() + ")");
        player.sendMessage(" §8- §eVisum§8:§7 " + playerData.getVisum());
        player.sendMessage(" §8- §eBargeld§8:§7 " + playerData.getBargeld() + "$");
        player.sendMessage(" §8- §ePayDay§8:§7 " + playerManager.paydayDuration(player) + "/60");
        player.sendMessage(" §8- §eSpielzeit§8:§7 " + playerData.getHours() + " Stunden & " + playerData.getMinutes() + " Minuten");
        if (playerData.getFaction() != null && !Objects.equals(playerData.getFaction(), "Zivilist")) {
            player.sendMessage(" §8- §eFraktion§8:§7 " + playerData.getFaction() + " (" + playerData.getFactionGrade() + "/6)");
        } else {
            player.sendMessage(" §8- §eFraktion§8:§7 Zivilist");
        }
        player.sendMessage(" §8- §eRang§8:§7 " + playerData.getRang());
        player.sendMessage(" §8- §eNummer§8:§7 " + playerData.getNumber());
        player.sendMessage(" §8- §eVotes§8:§7 " + playerData.getVotes());
        if (playerData.getSubGroupId() != 0) {
            player.sendMessage(" §8- §eGruppierung§8:§7 " + playerData.getSubGroup().getName());
        }
        PlaytimeReward playtimeReward = playerManager.getPlaytimeReward(playerData.getRewardId());
        player.sendMessage("§8 - §eSpielzeitbelohnung§8: §7" + playtimeReward.getDisplayName() + " §8- §7" + playerData.getRewardTime() + "§7h verbleibend" + (playtimeReward.isPremiumOnly() ?  "§8[§6Premium§8]": ""));
        return false;
    }
}
