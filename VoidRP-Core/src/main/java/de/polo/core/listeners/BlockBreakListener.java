package de.polo.core.listeners;

import de.polo.api.VoidAPI;
import de.polo.api.jobs.MiningJob;
import de.polo.api.player.VoidPlayer;
import de.polo.core.Main;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.storage.RegisteredBlock;
import de.polo.core.game.events.BreakPersistentBlockEvent;
import de.polo.core.game.events.MinuteTickEvent;
import de.polo.core.manager.ItemManager;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.manager.ServerManager;
import de.polo.core.utils.Prefix;
import de.polo.core.utils.Utils;
import de.polo.core.utils.enums.Farmer;
import de.polo.core.utils.enums.PickaxeType;
import de.polo.core.utils.enums.RoleplayItem;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Openable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class BlockBreakListener implements Listener {
    private final PlayerManager playerManager;
    private final Main.Commands commands;

    private final HashMap<LocalDateTime, Block> brokenBlocks = new HashMap<>();

    public BlockBreakListener(PlayerManager playerManager, Main.Commands commands) {
        this.playerManager = playerManager;
        this.commands = commands;
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        try {
            if (!player.getGameMode().equals(GameMode.CREATIVE)) {
                if (!ServerManager.canDoJobs()) {
                    player.sendMessage(Prefix.ERROR + "Vor dem Restart kannst du keine Jobs ausführen, bei welchen du Blöcke ab baust.");
                    event.setCancelled(true);
                    return;
                }
                for (Farmer farmer : Farmer.values()) {
                    if (player.getLocation().distance(farmer.getLocation()) < farmer.getRange() && event.getBlock().getType() == farmer.getFarmingItem()) {
                        event.setCancelled(true);
                        event.getBlock().setType(Material.AIR);
                        Main.getInstance().seasonpass.didQuest(player, 1);
                        ItemManager.addCustomItem(player, RoleplayItem.ARAMID, 1);
                        brokenBlocks.put(Utils.getTime(), event.getBlock());
                    }
                }
                VoidPlayer voidPlayer = VoidAPI.getPlayer(player);
                if (voidPlayer.getMiniJob() != null) {
                    event.setCancelled(true);
                    if (voidPlayer.getActiveJob() instanceof MiningJob) {
                        ((MiningJob) voidPlayer.getActiveJob()).handleBlockBreak(voidPlayer, event);
                    }
                    Bukkit.getPluginManager().callEvent(new BreakPersistentBlockEvent(player, event.getBlock()));
                } else {
                    event.setCancelled(true);
                    if (player.getInventory().getItemInMainHand().getType() == Material.AIR) return;
                    if (player.getInventory().getItemInMainHand().getType() == Material.IRON_AXE && player.getInventory().getItemInMainHand().getItemMeta().getDisplayName().equals(RoleplayItem.FEUERWEHR_AXT.getDisplayName())) {
                        Block block = event.getBlock();

                        if (block.getType() != Material.OAK_DOOR) return;

                        RegisteredBlock registeredBlock = Main.getInstance().blockManager.getBlockAtLocation(block.getLocation());

                        if (registeredBlock != null) {
                            return;
                        }

                        BlockState state = block.getState();
                        if (state.getBlockData() instanceof Openable) {
                            Openable openable = (Openable) state.getBlockData();
                            if (openable.isOpen()) {
                                player.sendMessage(Prefix.ERROR + "Die Tür ist bereits geöffnet.");
                            } else {
                                openable.setOpen(true);
                                state.setBlockData(openable);
                                state.update();
                                player.sendMessage(Prefix.MAIN + "Die Tür wurde geöffnet.");
                            }
                        } else {
                            player.sendMessage(Prefix.ERROR + "Dies ist keine Tür.");
                        }
                    }

                    if (Arrays.stream(PickaxeType.values()).anyMatch(x -> x.getMaterial().equals(event.getPlayer().getInventory().getItemInMainHand().getType()))) {
                        Main.getInstance().commands.minerJobCommand.blockBroke(player, event.getBlock());
                    }
                }
            }
        } catch (Exception ex) {
            event.setCancelled(true);
            ex.printStackTrace();
        }
    }

    @EventHandler
    public void everyMinute(MinuteTickEvent event) {
        LocalDateTime now = Utils.getTime();
        Iterator<Map.Entry<LocalDateTime, Block>> iterator = brokenBlocks.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<LocalDateTime, Block> entry = iterator.next();
            LocalDateTime timeBroken = entry.getKey();
            Block block = entry.getValue();

            if (Duration.between(timeBroken, now).toMinutes() >= 2) {
                block.setType(Material.DEAD_BUSH);
                iterator.remove();
            }
        }
    }
}
