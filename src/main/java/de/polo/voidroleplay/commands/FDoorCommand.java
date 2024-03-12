package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.dataStorage.RegisteredBlock;
import de.polo.voidroleplay.utils.BlockManager;
import de.polo.voidroleplay.utils.LocationManager;
import de.polo.voidroleplay.utils.PlayerManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class FDoorCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final BlockManager blockManager;
    private final LocationManager locationManager;

    public FDoorCommand(PlayerManager playerManager, BlockManager blockManager, LocationManager locationManager) {
        this.playerManager = playerManager;
        this.blockManager = blockManager;
        this.locationManager = locationManager;
        Main.registerCommand("fdoor", this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player);
        if (playerData.getFaction() == null) {
            player.sendMessage(Main.error_nopermission);
            return false;
        }
        if (locationManager.getLocation("fdoor_" + playerData.getFaction()) == null) {
            player.sendMessage(Main.error + "Deine Fraktion hat keine Fraktionstür");
            return false;
        }
        if (locationManager.getDistanceBetweenCoords(player, "fdoor_" + playerData.getFaction()) > 5) {
            player.sendMessage(Main.error + "Du bist nicht in der nähe deine Fraktionstür.");
            return false;
        }
        List<RegisteredBlock> blocks = new ArrayList<>();
        for (RegisteredBlock block : blockManager.getBlocks()) {
            if (block.getInfo().equalsIgnoreCase("adoor_" + playerData.getFaction())) {
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
