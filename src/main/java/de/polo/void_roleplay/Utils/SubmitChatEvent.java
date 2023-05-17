package de.polo.void_roleplay.Utils;

import de.polo.void_roleplay.DataStorage.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class SubmitChatEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private Player player;
    private String message;

    public SubmitChatEvent(Player player, String message) {
        this.player = player;
        this.message = message;
    }

    public boolean isCancel() {
        return message.equalsIgnoreCase("cancel");
    }

    public String getMessage() {
        return message;
    }

    public Player getPlayer() {
        return player;
    }

    public void sendCancelMessage() {
        player.sendMessage("§cVorgang beendet.");
    }

    public PlayerData getPlayerData() {
        return PlayerManager.playerDataMap.get(player.getUniqueId().toString());
    }
    public String getSubmitTo() {
        return PlayerManager.playerDataMap.get(player.getUniqueId().toString()).getVariable("chatblock");
    }
    public void end() {
        PlayerManager.playerDataMap.get(player.getUniqueId().toString()).setVariable("chatblock", null);
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
