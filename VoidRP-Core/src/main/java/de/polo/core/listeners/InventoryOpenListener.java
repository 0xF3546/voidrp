package de.polo.core.listeners;

import de.polo.core.utils.Event;
import de.polo.core.utils.player.SoundManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;

@Event
public class InventoryOpenListener implements Listener {
    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        Player player = (Player) event.getPlayer();
        SoundManager.openSound(player);
    }
}
