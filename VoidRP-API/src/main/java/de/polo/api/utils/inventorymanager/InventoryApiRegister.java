package de.polo.api.utils.inventorymanager;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;

/**
 * This class is used to register the InventoryAPI.
 * It only registers the events and creates the cache.
 * This class is the only one you need to register in your Main class.
 *
 * @author Erik Pförtner
 * @version 1.0.0
 * @see InventoryListener
 * @see InventoryClickListener
 * @see CustomInventoryCache
 * @see CustomItemInventoryCache
 */
public class InventoryApiRegister {

    private static CustomInventoryCache customInventoryCache;
    private static InventoryListener inventoryListener;
    private static InventoryClickListener inventoryClickListener;
    private static boolean registered = false;

    InventoryApiRegister() {
        // private constructor to prevent instantiation
    }

    public static void register(@NotNull final Plugin plugin) {
        if (registered) {
            throw new IllegalStateException("InventoryManager is already registered");
        }
        new CustomItemInventoryCache();
        customInventoryCache = new CustomInventoryCache();
        inventoryListener = new InventoryListener();
        inventoryClickListener = new InventoryClickListener();
        Bukkit.getPluginManager().registerEvents(inventoryListener, plugin);
        Bukkit.getPluginManager().registerEvents(inventoryClickListener, plugin);
        registered = true;
    }

    public static void unregister() {
        if (!registered) {
            throw new IllegalStateException("InventoryManager is not registered");
        }
        customInventoryCache = null;
        HandlerList.unregisterAll(inventoryListener);
        HandlerList.unregisterAll(inventoryClickListener);
        registered = false;
        try {
            Field field = CustomItemInventoryCache.class.getDeclaredField("instance");
            field.setAccessible(true);
            field.set(null, null);
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException("Could not find instance field in CustomItemInventoryCache", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Could not access instance field in CustomItemInventoryCache", e);
        }
    }

    public static CustomInventoryCache getCustomInventoryCache() {
        return customInventoryCache;
    }
}
