package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.FactionData;
import de.polo.voidroleplay.listeners.PlayerInteractListener;
import de.polo.voidroleplay.utils.FactionManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RoadBlockCommand implements CommandExecutor {

    List<Block> roadblocks = Main.getInstance().gamePlay.roadblocks;

    private final FactionManager factionManager;

    public RoadBlockCommand(FactionManager factionManager) {
        this.factionManager = factionManager;

        Main.getInstance().registerCommand("rbreset", this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }

        for (Block block : roadblocks) {
            block.setType(Material.AIR);
        }

        roadblocks.clear();

        Player player = (Player) sender;

        factionManager.sendCustomMessageToFactions(Main.faction_prefix + player.getName() + " hat die Roadblocks zurückgesetzt", "FBI", "Polizei", "Medic");

        return true;
    }
}
