package de.polo.voidroleplay.player.entities;

import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.utils.Prefix;
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
    UUID getUuid();
    Player getPlayer();
    String getName();
    PlayerData getData();
    Location getLocation();
    void sendMessage(String message);
    void sendMessage(Component component, Prefix prefix);
    void sendMessage(Component component);
}
