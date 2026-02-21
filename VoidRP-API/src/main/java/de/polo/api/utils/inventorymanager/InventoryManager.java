package de.polo.api.utils.inventorymanager;

import de.polo.api.utils.ItemBuilder;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * This class is the heart of the InventoryManager API.
 * It is used to create a new inventory for a player.
 * It is also used to add items to the inventory.
 *
 * @author Erik Pförtner
 * @version 1.0.0
 * @see InventoryApiRegister
 */
public class InventoryManager {

    public boolean canceled;
    private Component name;
    private int size;
    private UUID uuid;
    private Inventory inv;
    private boolean fillRest;
    @Getter
    @Setter
    private Runnable onClose;

    @Getter
    @Setter
    private Consumer<PlayerDropItemEvent> onDrop;

    /**
     * This constructor is used to create a new inventory for a player.
     *
     * @param player   The player who should get the inventory.
     * @param size     The size of the inventory.
     * @param name     The name of the inventory.
     * @param canceled If the inventory should be canceled.
     * @since 1.0.0
     */
    public InventoryManager(Player player, int size, String name, boolean canceled) {
        this.size = size;
        this.name = Component.text(name);
        this.uuid = player.getUniqueId();
        this.canceled = canceled;
        this.inv = Bukkit.createInventory(null, size, name);
        InventoryApiRegister.getCustomInventoryCache().addInventory(player, this);
        player.openInventory(inv);
    }

    /**
     * This constructor is used to create a new inventory for a player.
     *
     * @param player The player who should get the inventory.
     * @param size   The size of the inventory.
     * @param name   The name of the inventory.
     */
    public InventoryManager(Player player, int size, Component name) {
        create(player, size, name, true, true);
    }

    /**
     * This constructor is used to create a new inventory for a player.
     *
     * @param player   The player who should get the inventory.
     * @param size     The size of the inventory.
     * @param name     The name of the inventory.
     * @param canceled If the inventory should be canceled.
     * @since 1.0.0
     */
    public InventoryManager(Player player, int size, Component name, boolean canceled) {
        create(player, size, name, canceled, true);
    }

    /**
     * This constructor is used to create a new inventory for a player.
     *
     * @param player   The player who should get the inventory.
     * @param size     The size of the inventory.
     * @param name     The name of the inventory.
     * @param canceled If the inventory should be canceled.
     * @param fillRest If the rest of the inventory should be filled with Black Stained Glass.
     * @since 1.0.0
     */
    public InventoryManager(Player player, int size, Component name, boolean canceled, boolean fillRest) {
        create(player, size, name, canceled, fillRest);
    }

    private void create(Player player, int size, Component name, boolean canceled, boolean fillRest) {
        this.size = size;
        this.name = name;
        this.uuid = player.getUniqueId();
        this.canceled = canceled;
        this.fillRest = fillRest;
        this.inv = Bukkit.createInventory(null, size, name);
        InventoryApiRegister.getCustomInventoryCache().addInventory(player, this);
        player.openInventory(inv);

        if (fillRest) {
            for (int i = 0; i < size; i++) {
                if (inv.getItem(i) == null) {
                    ItemStack stack = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setName(" ").build();
                    inv.setItem(i, stack);
                }
            }
        }
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
    public Component getName() {
        return name;
    }

    /**
     * Returns the UUID of the {@link Player} who owns the {@link Inventory}.
     *
     * @return The UUID of the {@link Player} who owns the {@link Inventory}.
     * @since 1.0.0
     */
    @NotNull
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
    @NotNull
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
    public void setItem(@NotNull final CustomItem customItem) {
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
    public void addItem(@NotNull final CustomItem customItem) {
        getInventory().addItem(customItem.itemStack);
        CustomItemInventoryCache.getInstance().addCustomItem(this, customItem);
    }
}
