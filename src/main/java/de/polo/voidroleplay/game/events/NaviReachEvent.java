package de.polo.voidroleplay.game.events;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class NaviReachEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final String navi;

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
