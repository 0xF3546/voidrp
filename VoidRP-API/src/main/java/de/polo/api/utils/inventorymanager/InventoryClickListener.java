package de.polo.api.utils.inventorymanager;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Objects;
import java.util.Optional;

public class InventoryClickListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            Optional<InventoryManager> inventoryManagerOptional = InventoryApiRegister.getCustomInventoryCache().getInventory(player);
            inventoryManagerOptional.ifPresent(inventoryManager -> {
                if (inventoryManager.getInventory().equals(event.getClickedInventory())) {
                    event.setCancelled(inventoryManager.canceled);
                    if (event.getCurrentItem() != null) {
                        Optional<CustomItem> customItemOptional = CustomItemInventoryCache.getInstance().getCustomItemHashMap().keySet().stream()
                                .filter(inventoryManager::equals)
                                .flatMap(customItem -> CustomItemInventoryCache.getInstance().getCustomItemHashMap().get(customItem).stream())
                                .filter(customItem -> event.getCurrentItem().getType().equals(customItem.itemStack.getType())
                                        && Objects.requireNonNull(event.getCurrentItem().getItemMeta()).getDisplayName().equals(Objects.requireNonNull(customItem.itemStack.getItemMeta()).getDisplayName())
                                        && (event.getSlot() == customItem.slot || customItem.slot == -1))
                                .findFirst();
                        customItemOptional.ifPresent(customItem -> customItem.onClick(event));
                    }
                } else if (player.getInventory().equals(event.getClickedInventory())) {
                    event.setCancelled(inventoryManager.canceled);
                }
            });
        }
    }
}
