package de.polo.api.Utils.inventorymanager;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class is used to cache {@link CustomItem}s in {@link InventoryManager}s.
 * It caches the {@link CustomItem}s in a {@link ConcurrentHashMap} with the {@link InventoryManager} as key and a {@link Set} of {@link CustomItem}s as value.
 * This class is used to prevent {@link CustomItem}s from being duplicated in {@link InventoryManager}s.
 *
 * @author Erik Pförtner
 * @see InventoryManager
 * @see CustomItem
 * @version 1.0.0
 */
public class CustomItemInventoryCache {

    private final ConcurrentHashMap<InventoryManager, Set<CustomItem>> customItemHashMap;
    private static CustomItemInventoryCache instance;

    /**
     * Creates a new {@link CustomItemInventoryCache} instance.
     * It initializes the {@link ConcurrentHashMap} and sets the {@link #instance} to this instance.
     *
     * @see ConcurrentHashMap
     * @since 1.0.0
     */
    public CustomItemInventoryCache() {
        this.customItemHashMap = new ConcurrentHashMap<>();
        instance = this;
    }

    /**
     * Returns the {@link ConcurrentHashMap} that contains the {@link CustomItem}s.
     *
     * @return the {@link ConcurrentHashMap} that contains the {@link CustomItem}s
     * @see ConcurrentHashMap
     * @since 1.0.0
     */
    public ConcurrentHashMap<InventoryManager, Set<CustomItem>> getCustomItemHashMap() {
        return customItemHashMap;
    }

    /**
     * Returns the {@link CustomItemInventoryCache} instance.
     *
     * @return the {@link CustomItemInventoryCache} instance
     * @since 1.0.0
     */
    public static CustomItemInventoryCache getInstance() {
        return instance;
    }

    /**
     * Adds a {@link CustomItem} to the {@link ConcurrentHashMap}.
     * If the {@link InventoryManager} or the {@link CustomItem} is null, an {@link IllegalArgumentException} is thrown.
     * It is synchronized to prevent {@link CustomItem}s from being duplicated.
     *
     * @param inventoryManager the {@link InventoryManager} to add the {@link CustomItem} to
     * @param customItem       the {@link CustomItem} to add
     * @throws IllegalArgumentException if the {@link InventoryManager} or the {@link CustomItem} is null
     * @see InventoryManager
     * @see CustomItem
     * @since 1.0.0
     */
    public synchronized void addCustomItem(InventoryManager inventoryManager, CustomItem customItem) {
        if (inventoryManager == null || customItem == null)
            throw new IllegalArgumentException("inventoryManager and customItem cannot be null");

        Set<CustomItem> customItemSet = this.customItemHashMap.getOrDefault(inventoryManager, new HashSet<>());
        customItemSet.add(customItem);
        this.customItemHashMap.put(inventoryManager, customItemSet);
    }

    /**
     * Removes a {@link CustomItem} from the {@link ConcurrentHashMap}.
     * If the {@link InventoryManager} or the {@link CustomItem} is null, an {@link IllegalArgumentException} is thrown.
     * It is synchronized to prevent {@link CustomItem}s from being duplicated.
     *
     * @param inventoryManager the {@link InventoryManager} to remove the {@link CustomItem} from
     * @param customItem       the {@link CustomItem} to remove
     * @throws IllegalArgumentException if the {@link InventoryManager} or the {@link CustomItem} is null
     * @see InventoryManager
     * @see CustomItem
     * @since 1.0.0
     */
    public synchronized void removeCustomItem(InventoryManager inventoryManager, CustomItem customItem) {
        if (inventoryManager == null || customItem == null) {
            throw new IllegalArgumentException("inventoryManager and customItem cannot be null");
        }
        Set<CustomItem> customItemSet = this.customItemHashMap.get(inventoryManager);
        if (customItemSet != null) {
            customItemSet.remove(customItem);
        }
    }

    /**
     * Removes all {@link CustomItem}s from the {@link ConcurrentHashMap} that are in the {@link InventoryManager}.
     * If the {@link InventoryManager} is null, an {@link IllegalArgumentException} is thrown.
     * It is synchronized to prevent {@link CustomItem}s from being duplicated.
     *
     * @param inventoryManager the {@link InventoryManager} to remove the {@link CustomItem}s from
     * @throws IllegalArgumentException if the {@link InventoryManager} is null
     * @see InventoryManager
     * @see CustomItem
     * @since 1.0.0
     */
    public synchronized void removeInventoryManager(InventoryManager inventoryManager) {
        if (inventoryManager == null) {
            throw new IllegalArgumentException("inventoryManager cannot be null");
        }
        this.customItemHashMap.remove(inventoryManager);
    }

    /**
     * Returns a {@link Set} of {@link CustomItem}s that are in the {@link InventoryManager}.
     * If the {@link InventoryManager} is null, an {@link IllegalArgumentException} is thrown.
     * It is synchronized to prevent {@link CustomItem}s from being duplicated.
     *
     * @param inventoryManager the {@link InventoryManager} to get the {@link CustomItem}s from
     * @return a {@link Set} of {@link CustomItem}s that are in the {@link InventoryManager}
     * @throws IllegalArgumentException if the {@link InventoryManager} is null
     * @see InventoryManager
     * @see CustomItem
     * @since 1.0.0
     */
    public synchronized boolean containsInventoryManager(InventoryManager inventoryManager) {
        if (inventoryManager == null) {
            throw new IllegalArgumentException("inventoryManager cannot be null");
        }
        return this.customItemHashMap.containsKey(inventoryManager);
    }
}
