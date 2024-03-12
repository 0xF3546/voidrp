package de.polo.voidroleplay.utils.InventoryManager;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public abstract class CustomItem {

    public final int slot;
    public final ItemStack itemStack;

    /**
     * Creates a new CustomItem with a slot and an {@link ItemStack}.
     *
     * @param slot      The slot where the item should be placed.
     * @param itemStack The {@link ItemStack} that should be placed in the inventory.
     * @see ItemStack
     * @since 1.0.0
     */
    public CustomItem(int slot, ItemStack itemStack) {
        this.slot = slot;
        this.itemStack = itemStack;
    }

    /**
     * Creates a new CustomItem with an {@link ItemStack}.
     * This constructor is used if you don't want to specify a slot.
     * But please note that if you don't specify a slot, you must use the {@link InventoryManager#addItem(CustomItem)} method to add the item to the inventory.
     * The {@link InventoryManager#addItem(CustomItem)} method will automatically find a free slot for the item.
     *
     * @param itemStack The {@link ItemStack} that should be placed in the inventory.
     * @see ItemStack
     * @since 1.0.0
     */
    public CustomItem(ItemStack itemStack) {
        this.slot = -1;
        this.itemStack = itemStack;
    }

    /**
     * This method is called when the player clicks on the item.<br>
     * <b> NOTE: It is highly recommended to do nothing with this method. </b>
     *
     * @param event The {@link InventoryClickEvent} that was called when the player clicked on the item.
     * @see InventoryClickEvent
     * @since 1.0.0
     */
    public abstract void onClick(InventoryClickEvent event);
}
