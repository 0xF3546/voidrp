package de.polo.metropiacity.listeners;

import com.jeff_media.customblockdata.CustomBlockData;
import de.polo.metropiacity.dataStorage.PlayerData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.utils.events.BreakPersistentBlockEvent;
import de.polo.metropiacity.utils.PlayerManager;
import de.polo.metropiacity.commands.FarmerCommand;
import de.polo.metropiacity.commands.LumberjackCommand;
import de.polo.metropiacity.commands.MineCommand;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.persistence.PersistentDataContainer;

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
            if (playerData.getVariable("job") != null) {
                switch (playerData.getVariable("job").toString()) {
                    case "Holzfäller":
                        commands.lumberjackCommand.blockBroken(player, event.getBlock(), event);
                        break;
                    case "mine":
                        commands.mineCommand.blockBroken(player, event.getBlock(), event);
                        break;
                    case "farmer":
                        commands.farmerCommand.blockBroken(player, event.getBlock(), event);
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
