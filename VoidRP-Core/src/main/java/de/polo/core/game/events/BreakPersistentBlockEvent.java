package de.polo.core.game.events;

import de.polo.core.Main;
import de.polo.core.player.entities.PlayerData;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BreakPersistentBlockEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final Block block;

    public BreakPersistentBlockEvent(Player player, Block block) {
        this.player = player;
        this.block = block;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Block getBlock() {
        return block;
    }

    public Player getPlayer() {
        return player;
    }

    public PlayerData getPlayerData() {
        return Main.playerManager.getPlayerData(player.getUniqueId());
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}