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

public class MinuteTickEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private int minute;
    public MinuteTickEvent(int minute) {
        this.minute = minute;
    }

    public int getMinute() {
        return minute;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}