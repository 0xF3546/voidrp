package de.polo.metropiacity.utils.events;

import com.jeff_media.customblockdata.CustomBlockData;
import de.polo.metropiacity.dataStorage.PlayerData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.utils.PlayerManager;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class HourTickEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private int hour;
    public HourTickEvent(int hour) {
        this.hour = hour;
    }

    public int getHour() {
        return hour;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}