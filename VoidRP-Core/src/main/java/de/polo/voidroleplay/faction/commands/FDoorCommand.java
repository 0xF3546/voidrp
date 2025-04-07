package de.polo.voidroleplay.faction.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.faction.entity.Faction;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.storage.RegisteredBlock;
import de.polo.voidroleplay.manager.BlockManager;
import de.polo.voidroleplay.location.services.impl.LocationManager;
import de.polo.voidroleplay.player.services.impl.PlayerManager;
import de.polo.voidroleplay.utils.Prefix;
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
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
            return false;
        }
        String faction = playerData.getFaction();
        if (playerData.getFaction().equalsIgnoreCase("FBI") || playerData.getFaction().equalsIgnoreCase("Polizei")) {
            if (args.length < 1) {
                player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /fdoor [Fraktion]");
                return false;
            }
            for (Faction factionData : Main.getInstance().factionManager.getFactions()) {
                if (!factionData.getName().equalsIgnoreCase(args[0])) continue;
                faction = factionData.getName();
            }
            if (faction == null) {
                player.sendMessage(Prefix.ERROR + "Fraktion wurde nicht gefunden.");
                return false;
            }
        }
        if (locationManager.getLocation("fdoor_" + faction) == null) {
            player.sendMessage(Prefix.ERROR + "Deine Fraktion hat keine Fraktionstür.");
            return false;
        }
        if (locationManager.getDistanceBetweenCoords(player, "fdoor_" + faction) > 5) {
            player.sendMessage(Prefix.ERROR + "Du bist nicht in der nähe deine Fraktionstür.");
            return false;
        }
        if (playerManager.isInStaatsFrak(player)) {
            Main.getInstance().gamePlay.openGOVRaidGUI(Main.getInstance().factionManager.getFactionData(faction), player);
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
