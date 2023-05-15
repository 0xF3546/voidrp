package de.polo.void_roleplay.commands;

import de.polo.void_roleplay.DataStorage.PlayerData;
import de.polo.void_roleplay.Utils.PlayerManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;

public class statsCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        player.sendMessage("§7    ===§8[§6Statistiken§8]§7===");
        player.sendMessage(" §8- §eLevel§8:§7 " + playerData.getLevel() + " (" + playerData.getExp() + "/" + playerData.getNeeded_exp() + ")");
        player.sendMessage(" §8- §eVisum§8:§7 " + playerData.getVisum());
        player.sendMessage(" §8- §eBargeld§8:§7 " + playerData.getBargeld() + "$");
        player.sendMessage(" §8- §ePayDay§8:§7 " + PlayerManager.paydayDuration(player) + "/60");
        if (playerData.getFaction() != null && !Objects.equals(playerData.getFaction(), "Zivilist")) {
            player.sendMessage(" §8- §eFraktion§8:§7 " + playerData.getFaction() + " (" + playerData.getFactionGrade() + "/8)");
        } else {
            player.sendMessage(" §8- §eFraktion§8:§7 Zivilist");
        }
        player.sendMessage(" §8- §eRang§8:§7 " + playerData.getRang());
        player.sendMessage(" §8- §eNummer§8:§7 " + playerData.getNumber());
        return false;
    }
}
