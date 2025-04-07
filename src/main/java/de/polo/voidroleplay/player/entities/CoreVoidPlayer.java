package de.polo.voidroleplay.player.entities;

import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.utils.Prefix;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

import static de.polo.voidroleplay.Main.playerManager;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class CoreVoidPlayer implements VoidPlayer{
    @Getter
    private final Player player;

    public CoreVoidPlayer(Player player) {
        this.player = player;
    }

    @Override
    public UUID getUuid() {
        return player.getUniqueId();
    }

    @Override
    public String getName() {
        return player.getName();
    }

    @Override
    public PlayerData getData() {
        return playerManager.getPlayerData(player);
    }

    @Override
    public Location getLocation() {
        return player.getLocation();
    }

    @Override
    public void sendMessage(String message) {

    }

    @Override
    public void sendMessage(Component component, Prefix prefix) {

    }

    @Override
    public void sendMessage(Component component) {

    }
}
