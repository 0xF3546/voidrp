package de.polo.api.player;

import de.polo.api.Utils.enums.Prefix;
import de.polo.api.jobs.enums.MiniJob;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public interface VoidPlayer {
    Player getPlayer();
    PlayerCharacter getData();
    MiniJob getMiniJob();
    void setMiniJob(MiniJob miniJob);
    default UUID getUuid() {
        return getPlayer().getUniqueId();
    }
    default String getName() {
        return getPlayer().getName();
    }
    default Location getLocation() {
        return getPlayer().getLocation();
    }
    default void sendMessage(String message) {
        this.sendMessage(Component.text(message));
    }
    default void sendMessage(String message, Prefix prefix) {
        this.sendMessage(Component.text(prefix.getPrefix() + message));
    }
    default void sendMessage(final Component component, final Prefix prefix) {
        this.sendMessage(Component.text(prefix.getPrefix() + component));
    }
    void sendMessage(Component component);

    void setVariable(String key, Object value);
    Object getVariable(String key);
}
