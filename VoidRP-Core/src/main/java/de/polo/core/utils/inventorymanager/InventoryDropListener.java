package de.polo.core.utils.inventorymanager;

import de.polo.api.utils.inventorymanager.InventoryApiRegister;
import de.polo.api.utils.inventorymanager.InventoryManager;
import de.polo.core.utils.Event;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

import java.util.Optional;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
@Event
public class InventoryDropListener implements Listener {

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        Optional<InventoryManager> inventoryManager = InventoryApiRegister.getCustomInventoryCache().getInventory(player);
        if (inventoryManager.isPresent() && inventoryManager.get().getOnDrop() != null) {
            inventoryManager.get().getOnDrop().accept(event);
        }
    }
}
