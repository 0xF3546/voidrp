package de.polo.core.utils.inventorymanager;

import de.polo.api.utils.inventorymanager.InventoryApiRegister;
import de.polo.api.utils.inventorymanager.InventoryManager;
import de.polo.core.utils.Event;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.Optional;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
@Event
public class InventoryCloseListener implements Listener {

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        Optional<InventoryManager> inventoryManager = InventoryApiRegister.getCustomInventoryCache().getInventory(player);
        if (inventoryManager.isPresent()) {
            Runnable onClose = inventoryManager.get().getOnClose();
            if (onClose != null) {
                onClose.run();
            }
        }
    }
}
