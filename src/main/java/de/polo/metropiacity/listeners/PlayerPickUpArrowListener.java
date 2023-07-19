package de.polo.metropiacity.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupArrowEvent;

public class PlayerPickUpArrowListener implements Listener {
    @EventHandler
    public void onPlayerPickUpArrow(PlayerPickupArrowEvent event) {
        event.setCancelled(true);
        event.getArrow().remove();
    }
}
