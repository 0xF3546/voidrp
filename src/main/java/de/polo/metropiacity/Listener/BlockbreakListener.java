package de.polo.metropiacity.Listener;

import com.jeff_media.customblockdata.CustomBlockData;
import de.polo.metropiacity.DataStorage.PlayerData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.Utils.Events.BreakPersistentBlockEvent;
import de.polo.metropiacity.Utils.PlayerManager;
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

public class BlockbreakListener implements Listener {
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        if (!player.getGameMode().equals(GameMode.CREATIVE)) {
            if (playerData.getVariable("job") != null) {
                switch (playerData.getVariable("job")) {
                    case "Holzfäller":
                        LumberjackCommand.blockBroken(player, event.getBlock(), event);
                        break;
                    case "mine":
                        MineCommand.blockBroken(player, event.getBlock(), event);
                        break;
                    case "farmer":
                        FarmerCommand.blockBroken(player, event.getBlock(), event);
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
