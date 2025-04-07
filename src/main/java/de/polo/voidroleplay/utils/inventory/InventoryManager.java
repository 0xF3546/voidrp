package de.polo.voidroleplay.utils.inventory;

import de.polo.voidroleplay.manager.ItemManager;
import de.polo.voidroleplay.player.entities.VoidPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.UUID;

public class InventoryManager {

    private final String name;
    private final int size;
    private final UUID uuid;
    private final boolean fillRest;
    private final Inventory inv;
    public boolean canceled;

    /**
     * This constructor is used to create a new inventory for a player.
     *
     * @param player   The player who should get the inventory.
     * @param size     The size of the inventory.
     * @param name     The name of the inventory.
     * @param canceled If the inventory should be canceled.
     * @since 1.0.0
     */
    public InventoryManager(Player player, int size, String name, boolean canceled, boolean fillRest) {
        this.size = size;
        this.name = name;
        this.uuid = player.getUniqueId();
        this.canceled = canceled;
        this.fillRest = fillRest;
        this.inv = Bukkit.createInventory(null, size, name);
        InventoryApiRegister.getCustomInventoryCache().addInventory(player, this);
        if (this.fillRest) {
            for (int i = 0; i < this.size; i++) {
                if (inv.getItem(i) == null) {
                    inv.setItem(i, ItemManager.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, 0, "§c"));
                }
            }
        }
        player.openInventory(inv);
    }

    public InventoryManager(Player player, int size, String name) {
        this.size = size;
        this.name = name;
        this.uuid = player.getUniqueId();
        this.canceled = true;
        this.fillRest = true;
        this.inv = Bukkit.createInventory(null, size, name);
        InventoryApiRegister.getCustomInventoryCache().addInventory(player, this);
        if (this.fillRest) {
            for (int i = 0; i < this.size; i++) {
                if (inv.getItem(i) == null) {
                    inv.setItem(i, ItemManager.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, 0, "§c"));
                }
            }
        }
        player.openInventory(inv);
    }

    /**
     * Returns the player who has the {@link Inventory}.
     *
     * @return The player who has the {@link Inventory}.
     * @since 1.0.0
     */
    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    /**
     * Returns the size of the {@link Inventory}.
     *
     * @return The size of the {@link Inventory}.
     * @since 1.0.0
     */
    public int getSize() {
        return size;
    }

    /**
     * Returns the name of the {@link Inventory}.
     *
     * @return The name of the {@link Inventory}.
     * @since 1.0.0
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the UUID of the {@link Inventory}.
     *
     * @return The UUID of the {@link Inventory}.
     * @since 1.0.0
     */
    public UUID getUUID() {
        return uuid;
    }

    /**
     * Returns if the {@link Inventory} is canceled.
     *
     * @return If the {@link Inventory} is canceled.
     * @since 1.0.0
     */
    public boolean isCanceled() {
        return canceled;
    }

    /**
     * Returns the {@link Inventory}.
     *
     * @return The {@link Inventory}.
     * @since 1.0.0
     */
    public Inventory getInventory() {
        return inv;
    }

    /**
     * This method is used to set a {@link CustomItem} in the {@link Inventory}.
     * It also adds the {@link CustomItem} to the {@link CustomItemInventoryCache}.
     * If you want to add a {@link CustomItem} to the {@link Inventory} use {@link #addItem(CustomItem)}.
     *
     * @param customItem The {@link CustomItem} which should be set.
     * @see CustomItemInventoryCache
     * @see CustomItem
     * @since 1.0.0
     */
    public void setItem(CustomItem customItem) {
        getInventory().setItem(customItem.slot, customItem.itemStack);
        CustomItemInventoryCache.getInstance().addCustomItem(this, customItem);
    }

    /**
     * This method is used to add a {@link CustomItem} to the {@link Inventory}.
     * It also adds the {@link CustomItem} to the {@link CustomItemInventoryCache}.
     * If you want to set a {@link CustomItem} in the {@link Inventory} use {@link #setItem(CustomItem)}.
     *
     * @param customItem The {@link CustomItem} which should be added.
     * @see CustomItemInventoryCache
     * @see CustomItem
     * @since 1.0.0
     */
    public void addItem(CustomItem customItem) {
        getInventory().addItem(customItem.itemStack);
        CustomItemInventoryCache.getInstance().addCustomItem(this, customItem);
    }
}
