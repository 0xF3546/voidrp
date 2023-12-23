package de.polo.metropiacity.utils.events;

import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CertainInventoryClickEvent extends Event {
    private final Player player;
    private final String inventoryName;
    private final String inventoryPageName;
    private final int page;
    private final Object originClass;
    private final ItemStack item;

    public CertainInventoryClickEvent(Player player, ItemStack item, String inventoryName, String inventoryPageName, int page, Object originClass) {
        this.player = player;
        this.inventoryName = inventoryName;
        this.inventoryPageName = inventoryPageName;
        this.page = page;
        this.originClass = originClass;
        this.item = item;
    }
    public Player getPlayer() {
        return player;
    }

    public String getInventoryName() {
        return inventoryName;
    }

    public String getInventoryPageName() {
        return inventoryPageName;
    }
    public Integer getPage() {
        return page;
    }

    public Object getOriginClass() {
        return originClass;
    }

    public ItemStack getItem() {
        return item;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return null;
    }
}
