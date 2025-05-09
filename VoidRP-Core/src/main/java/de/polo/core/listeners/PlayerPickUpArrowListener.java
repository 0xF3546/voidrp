package de.polo.core.listeners;

import de.polo.core.utils.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupArrowEvent;

@Event
public class PlayerPickUpArrowListener implements Listener {
    @EventHandler
    public void onPlayerPickUpArrow(PlayerPickupArrowEvent event) {
        event.setCancelled(true);
        event.getArrow().remove();
    }
}
