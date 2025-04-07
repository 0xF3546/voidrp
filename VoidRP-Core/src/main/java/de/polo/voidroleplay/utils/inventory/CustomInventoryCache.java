package de.polo.voidroleplay.utils.inventory;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Optional;

public class CustomInventoryCache {

    private final HashMap<Player, InventoryManager> inventoryHashMap;

    public CustomInventoryCache() {
        this.inventoryHashMap = new HashMap<>();
    }

    /**
     * Returns the Optional containing the {@link InventoryManager} for the given player.
     *
     * @param player The player that should be searched for.
     * @return Optional containing the {@link InventoryManager} for the given player.
     * @see InventoryManager
     * @since 1.0.0
     */
    public Optional<InventoryManager> getInventory(Player player) {
        return Optional.ofNullable(inventoryHashMap.get(player));
    }

    /**
     * Adds a new {@link InventoryManager} to the cache.
     *
     * @param player           The player that should be added to the cache.
     * @param inventoryManager The {@link InventoryManager} that should be added to the cache.
     * @throws IllegalArgumentException if player or inventoryManager is null.
     * @see InventoryManager
     * @since 1.0.0
     */
    public void addInventory(Player player, InventoryManager inventoryManager) {
        if (player == null || inventoryManager == null)
            throw new IllegalArgumentException("player and inventoryManager cannot be null");
        this.inventoryHashMap.put(player, inventoryManager);
    }

    /**
     * Removes a {@link InventoryManager} from the cache.
     *
     * @param player The player that should be removed from the cache.
     * @throws IllegalArgumentException if player is null.
     * @see InventoryManager
     * @since 1.0.0
     */
    public void removeInventory(Player player) {
        if (player == null) throw new IllegalArgumentException("player cannot be null");
        this.inventoryHashMap.remove(player);
    }

    /**
     * Checks if the given player has a {@link InventoryManager} in the cache.
     *
     * @param player The player that should be searched for.
     * @return true if the player has a {@link InventoryManager} in the cache, false if not.
     * @throws IllegalArgumentException if player is null.
     * @see InventoryManager
     * @since 1.0.0
     */
    public boolean hasInventory(Player player) {
        if (player == null) throw new IllegalArgumentException("player cannot be null");
        return this.inventoryHashMap.containsKey(player);
    }
}
