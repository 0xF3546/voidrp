package de.polo.core.utils.inventory;

import de.polo.core.Main;
import org.bukkit.Bukkit;

public class InventoryApiRegister {

    private static CustomInventoryCache customInventoryCache;
    private final CustomItemInventoryCache customItemInventoryCache;
    private final Main plugin;

    public InventoryApiRegister(Main plugin) {
        this.plugin = plugin;
        customInventoryCache = new CustomInventoryCache();
        customItemInventoryCache = new CustomItemInventoryCache();
        Bukkit.getPluginManager().registerEvents(new InventoryListener(), this.plugin);
        Bukkit.getPluginManager().registerEvents(new InventoryClickListener(), this.plugin);
    }

    public static CustomInventoryCache getCustomInventoryCache() {
        return customInventoryCache;
    }
}
