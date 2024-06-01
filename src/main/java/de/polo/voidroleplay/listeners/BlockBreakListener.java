package de.polo.voidroleplay.listeners;

import com.jeff_media.customblockdata.CustomBlockData;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.game.events.BreakPersistentBlockEvent;
import de.polo.voidroleplay.utils.ItemManager;
import de.polo.voidroleplay.utils.PlayerManager;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.utils.ServerManager;
import de.polo.voidroleplay.utils.enums.Farmer;
import de.polo.voidroleplay.utils.enums.RoleplayItem;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.persistence.PersistentDataContainer;

import java.util.List;

public class BlockBreakListener implements Listener {
    private final PlayerManager playerManager;
    private final Main.Commands commands;

    public BlockBreakListener(PlayerManager playerManager, Main.Commands commands) {
        this.playerManager = playerManager;
        this.commands = commands;
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (!player.getGameMode().equals(GameMode.CREATIVE)) {
            if (!ServerManager.canDoJobs()) {
                player.sendMessage(Prefix.ERROR + "Vor dem Restart kannst du keine Jobs ausführen, bei welchen du Blöcke ab baust.");
                return;
            }
            for (Farmer farmer : Farmer.values()) {
                if (player.getLocation().distance(farmer.getLocation()) < 50 && event.getBlock().getType() == farmer.getFarmingItem()) {
                    event.setCancelled(true);
                    event.getBlock().setType(Material.AIR);
                    ItemManager.addCustomItem(player, RoleplayItem.ARAMID, 1);
                    Main.waitSeconds(120, () -> {
                        event.getBlock().setType(Material.DEAD_BUSH);
                    });
                }
            }
            if (playerData.getVariable("job") != null) {
                switch (playerData.getVariable("job").toString().toLowerCase()) {
                    case "holzfäller":
                        commands.lumberjackCommand.blockBroken(player, event.getBlock(), event);
                        break;
                    case "mine":
                        commands.mineCommand.blockBroken(player, event.getBlock(), event);
                        break;
                    case "farmer":
                        commands.farmerCommand.blockBroken(player, event.getBlock(), event);
                        break;
                    case "winzer":
                        commands.winzerCommand.blockBroken(player, event.getBlock(), event);
                        break;
                    case "muschelsammler":
                        commands.muschelSammlerCommand.blockBroken(player, event.getBlock(), event);
                        break;
                    default:
                        event.setCancelled(true);
                        break;
                }
                PersistentDataContainer container = new CustomBlockData(event.getBlock(), Main.getInstance());
                Bukkit.getPluginManager().callEvent(new BreakPersistentBlockEvent(player, event.getBlock()));
            } else {
                event.setCancelled(true);
            }
        }
    }
}
