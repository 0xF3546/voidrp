package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.manager.FactionManager;
import de.polo.voidroleplay.manager.PlayerManager;
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
    List<Block> roadblocks = Main.getInstance().gamePlay.roadblocks;

    public RoadBlockCommand(FactionManager factionManager, PlayerManager playerManager) {
        this.factionManager = factionManager;
        this.playerManager = playerManager;

        Main.registerCommand("rbreset", this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }

        for (Block block : roadblocks) {
            block.setType(Material.AIR);
        }

        Player player = (Player) sender;

        PlayerData playerData = playerManager.getPlayerData(player);

        if (!playerData.getFaction().equalsIgnoreCase("FBI") || !playerData.getFaction().equalsIgnoreCase("Polizei") || !playerData.getFaction().equalsIgnoreCase("Medic")) {
            player.sendMessage(Main.error_nopermission);
            return false;
        }

        roadblocks.clear();

        factionManager.sendCustomMessageToFactions(Main.faction_prefix + player.getName() + " hat die Roadblocks zurückgesetzt", "FBI", "Polizei", "Medic");

        return false;
    }
}
