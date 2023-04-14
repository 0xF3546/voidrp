package de.polo.void_roleplay.Listener;

import de.polo.void_roleplay.Utils.PlayerManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class playerMoveListener implements Listener {
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!PlayerManager.canPlayerMove(event.getPlayer())) {
            event.setCancelled(true);
        }
    }
}
