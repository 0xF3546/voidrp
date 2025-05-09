package de.polo.core.faction.commands;

import de.polo.core.Main;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.faction.service.impl.FactionManager;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.utils.Prefix;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RoadBlockCommand implements CommandExecutor {

    private final FactionManager factionManager;
    private final PlayerManager playerManager;
    List<Block> roadblocks = Main.gamePlay.roadblocks;

    public RoadBlockCommand(FactionManager factionManager, PlayerManager playerManager) {
        this.factionManager = factionManager;
        this.playerManager = playerManager;

        Main.registerCommand("rbreset", this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return false;
        }

        for (Block block : roadblocks) {
            block.setType(Material.AIR);
        }

        PlayerData playerData = playerManager.getPlayerData(player);

        if (!playerData.getFaction().equalsIgnoreCase("FBI") || !playerData.getFaction().equalsIgnoreCase("Polizei") || !playerData.getFaction().equalsIgnoreCase("Medic")) {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
            return false;
        }

        roadblocks.clear();

        factionManager.sendCustomMessageToFactions(Prefix.FACTION + player.getName() + " hat die Roadblocks zurückgesetzt", "FBI", "Polizei", "Medic");

        return false;
    }
}
