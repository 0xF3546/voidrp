package de.polo.core.faction.commands;

import de.polo.api.VoidAPI;
import de.polo.core.Main;
import de.polo.core.faction.entity.Faction;
import de.polo.core.location.services.LocationService;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.storage.RegisteredBlock;
import de.polo.core.manager.BlockManager;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.utils.Prefix;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FDoorCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final BlockManager blockManager;

    public FDoorCommand(PlayerManager playerManager, BlockManager blockManager) {
        this.playerManager = playerManager;
        this.blockManager = blockManager;
        Main.registerCommand("fdoor", this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player);
        if (playerData.getFaction() == null) {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
            return false;
        }
        String faction = playerData.getFaction();
        if (playerData.getFaction().equalsIgnoreCase("FBI") || playerData.getFaction().equalsIgnoreCase("Polizei")) {
            if (args.length < 1) {
                player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /fdoor [Fraktion]");
                return false;
            }
            for (Faction factionData : Main.factionManager.getFactions()) {
                if (!factionData.getName().equalsIgnoreCase(args[0])) continue;
                faction = factionData.getName();
            }
            if (faction == null) {
                player.sendMessage(Prefix.ERROR + "Fraktion wurde nicht gefunden.");
                return false;
            }
        }
        LocationService locationService = VoidAPI.getService(LocationService.class);
        if (locationService.getLocation("fdoor_" + faction) == null) {
            player.sendMessage(Prefix.ERROR + "Deine Fraktion hat keine Fraktionstür.");
            return false;
        }
        if (locationService.getDistanceBetweenCoords(player, "fdoor_" + faction) > 5) {
            player.sendMessage(Prefix.ERROR + "Du bist nicht in der nähe deine Fraktionstür.");
            return false;
        }
        if (playerManager.isInStaatsFrak(player)) {
            Main.gamePlay.openGOVRaidGUI(Main.factionManager.getFactionData(faction), player);
            return false;
        }
        List<RegisteredBlock> blocks = new ObjectArrayList<>();
        for (RegisteredBlock block : blockManager.getBlocks()) {
            if (block.getInfo().equalsIgnoreCase("adoor_" + faction)) {
                blocks.add(block);
            }
        }
        for (int i = 0; i < blocks.size() / 2; i++) {
            for (RegisteredBlock block : blocks) {
                if (Integer.parseInt(block.getInfoValue()) == i + 1) {
                    Block block1 = block.getLocation().getBlock();
                    if (block1.getType().equals(block.getMaterial())) {
                        block1.setType(Material.AIR);
                    } else {
                        block1.setType(block.getMaterial());
                        block1.getState().update();
                    }
                }
            }
        }
        return false;
    }
}
