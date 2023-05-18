package de.polo.void_roleplay.Utils;

import de.polo.void_roleplay.DataStorage.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class NaviReachEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private Player player;
    private String navi;

    public NaviReachEvent(Player player, String navi) {
        this.player = player;
        this.navi = navi;
    }

    public String getNavi() {
        return navi;
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
