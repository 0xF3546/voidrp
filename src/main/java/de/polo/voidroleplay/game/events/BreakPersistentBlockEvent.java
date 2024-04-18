package de.polo.voidroleplay.game.events;

import com.jeff_media.customblockdata.CustomBlockData;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.Main;
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
        return Main.getInstance().playerManager.getPlayerData(player.getUniqueId());
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}