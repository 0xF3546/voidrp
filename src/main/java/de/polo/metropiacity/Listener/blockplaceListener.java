package de.polo.metropiacity.Listener;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class blockplaceListener implements Listener {
    @EventHandler
    public void onBlockBreak(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (!player.getGameMode().equals(GameMode.CREATIVE)) {
            event.setCancelled(true);
        }
    }
}
