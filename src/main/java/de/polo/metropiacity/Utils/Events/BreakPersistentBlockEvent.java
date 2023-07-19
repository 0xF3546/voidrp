package de.polo.metropiacity.Utils.Events;

import com.jeff_media.customblockdata.CustomBlockData;
import de.polo.metropiacity.DataStorage.PlayerData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.Utils.PlayerManager;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class BreakPersistentBlockEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final Block block;

    public BreakPersistentBlockEvent(Player player, Block block) {
        this.player = player;
        this.block = block;
    }

    public Block getBlock() {
        return block;
    }
    public String getPersistentData(PersistentDataType type) {
        PersistentDataContainer container = new CustomBlockData(block, Main.plugin);
        return container.get(new NamespacedKey(Main.getInstance(), "value"), type).toString();
    }

    public Player getPlayer() {
        return player;
    }

    public PlayerData getPlayerData() {
        return PlayerManager.playerDataMap.get(player.getUniqueId().toString());
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}