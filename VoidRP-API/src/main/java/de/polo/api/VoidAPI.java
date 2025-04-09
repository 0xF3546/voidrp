package de.polo.api;

import de.polo.api.player.VoidPlayer;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public final class VoidAPI {
    private static Server server;
    private static final List<VoidPlayer> players = new ObjectArrayList<>();

    public static void setPlugin(Server p) {
        server = p;
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

    public static List<VoidPlayer> getPlayers() {
        return players;
    }

    /**
     * Retrieves a Spring-managed bean from the application context by its type.
     *
     * <p>This method provides access to Spring components and services used by the plugin.</p>
     *
     * @param <T>   the type of the bean to retrieve.
     * @param clazz the {@code Class} object representing the type of the bean.
     * @return the Spring-managed bean of the specified type.
     * @throws IllegalStateException if the server instance has not been set.
     */
    @NotNull
    public static <T> T getBean(@NotNull final Class<T> clazz) {
        if (server == null) {
            throw new IllegalStateException("Server has not been initialized");
        }
        return server.getBean(clazz);
    }
}
