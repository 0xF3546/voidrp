package de.polo.api;

import de.polo.api.player.VoidPlayer;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public final class VoidAPI {
    private static Server plugin;
    private static final List<VoidPlayer> players = new ObjectArrayList<>();

    public static void setPlugin(Server p) {
        plugin = p;
    }

    public static void addPlayer(VoidPlayer player) {
        players.add(player);
    }

    public static void removePlayer(Player player) {
        players.removeIf(voidPlayer -> voidPlayer.getUuid().equals(player.getUniqueId()));
    }

    public static VoidPlayer getPlayer(Player player) {
        return getPlayer(player.getUniqueId());
    }
    public static VoidPlayer getPlayer(UUID uuid) {
        return players.stream()
                .filter(voidPlayer -> voidPlayer.getUuid().equals(uuid))
                .findFirst()
                .orElse(null);
    }
}
