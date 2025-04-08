package de.polo.core.utils.inventory;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Optional;

public class InventoryClickListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            Optional<InventoryManager> inventoryManagerOptional = InventoryApiRegister.getCustomInventoryCache().getInventory(player);
            inventoryManagerOptional.ifPresent(inventoryManager -> {
                if (inventoryManager.getInventory().equals(event.getClickedInventory())) {
                    event.setCancelled(inventoryManager.canceled);
                    if (event.getCurrentItem() != null && event.getCurrentItem().getType() != null) {
                        Optional<CustomItem> customItemOptional = CustomItemInventoryCache.getInstance().getCustomItemHashMap().keySet().stream()
                                .filter(inventoryManager::equals)
                                .flatMap(customItem -> CustomItemInventoryCache.getInstance().getCustomItemHashMap().get(customItem).stream())
                                .filter(customItem -> event.getCurrentItem().getType().equals(customItem.itemStack.getType())
                                        && event.getCurrentItem().getItemMeta().getDisplayName().equals(customItem.itemStack.getItemMeta().getDisplayName())
                                        && (event.getSlot() == customItem.slot || customItem.slot == -1))
                                .findFirst();
                        customItemOptional.ifPresent(customItem -> customItem.onClick(event));
                    }
                }
            });
        }
    }
}
