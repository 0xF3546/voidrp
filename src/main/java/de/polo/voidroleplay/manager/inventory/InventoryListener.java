package de.polo.voidroleplay.manager.inventory;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;

import java.util.Optional;

public class InventoryListener implements Listener {

    /**
     * This method is used to remove the inventory from the cache when the player closes the inventory.
     *
     * @param event The event that is called when the player closes the inventory.
     * @see InventoryCloseEvent
     * @see Player
     * @see Inventory
     * @see InventoryApiRegister
     * @see CustomItemInventoryCache
     * @since 1.0.0
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player) {
            Inventory inv = event.getInventory();
            Player player = (Player) event.getPlayer();
            Optional<InventoryManager> inventoryManagerOptional = InventoryApiRegister.getCustomInventoryCache().getInventory(player);
            if (inventoryManagerOptional.isPresent()) {
                InventoryManager inventoryManager = inventoryManagerOptional.get();
                if (inv.equals(inventoryManager.getInventory())) {
                    if (CustomItemInventoryCache.getInstance().containsInventoryManager(inventoryManager)) {
                        CustomItemInventoryCache.getInstance().removeInventoryManager(inventoryManager);
                    }
                    InventoryApiRegister.getCustomInventoryCache().removeInventory(player);
                }
            }
        }
    }

    /**
     * This method is used to remove the inventory from the cache when the player leaves the server.
     *
     * @param event The event that is called when the player leaves the server.
     * @see PlayerQuitEvent
     * @see Player
     * @see InventoryApiRegister
     * @see CustomItemInventoryCache
     * @since 1.0.0
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Optional<InventoryManager> inventoryManagerOptional = InventoryApiRegister.getCustomInventoryCache().getInventory(player);
        if (inventoryManagerOptional.isPresent()) {
            InventoryManager inventoryManager = inventoryManagerOptional.get();
            if (CustomItemInventoryCache.getInstance().containsInventoryManager(inventoryManager)) {
                CustomItemInventoryCache.getInstance().removeInventoryManager(inventoryManager);
            }
            InventoryApiRegister.getCustomInventoryCache().removeInventory(player);
        }
    }

    /**
     * This method is used to remove the inventory from the cache when the player is kicked from the server.
     *
     * @param event The event that is called when the player is kicked from the server.
     * @see PlayerKickEvent
     * @see Player
     * @see InventoryApiRegister
     * @see CustomItemInventoryCache
     * @since 1.0.0
     */
    @EventHandler
    public void onPlayerKickEvent(PlayerKickEvent event) {
        Player player = event.getPlayer();
        Optional<InventoryManager> inventoryManagerOptional = InventoryApiRegister.getCustomInventoryCache().getInventory(player);
        if (inventoryManagerOptional.isPresent()) {
            InventoryManager inventoryManager = inventoryManagerOptional.get();
            if (CustomItemInventoryCache.getInstance().containsInventoryManager(inventoryManager)) {
                CustomItemInventoryCache.getInstance().removeInventoryManager(inventoryManager);
            }
            InventoryApiRegister.getCustomInventoryCache().removeInventory(player);
        }
    }
}
