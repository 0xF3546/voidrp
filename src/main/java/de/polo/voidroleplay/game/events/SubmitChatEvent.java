package de.polo.voidroleplay.game.events;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class SubmitChatEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final String message;

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
        return Main.getInstance().playerManager.getPlayerData(player.getUniqueId());
    }
    public String getSubmitTo() {
        return Main.getInstance().playerManager.getPlayerData(player.getUniqueId()).getVariable("chatblock");
    }
    public void end() {
        Main.getInstance().playerManager.getPlayerData(player.getUniqueId()).setVariable("chatblock", null);
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
