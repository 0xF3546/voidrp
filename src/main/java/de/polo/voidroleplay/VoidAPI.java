package de.polo.voidroleplay;

import de.polo.voidroleplay.player.entities.CoreVoidPlayer;
import de.polo.voidroleplay.player.entities.VoidPlayer;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

import static de.polo.voidroleplay.Main.playerManager;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public final class VoidAPI {
    private static final List<VoidPlayer> players = new ObjectArrayList<>();
    private static Main plugin;

    public static void setPlugin(Main p) {
        plugin = p;
    }

    public static void addPlayer(Player player) {
        VoidPlayer voidPlayer = new CoreVoidPlayer(player);
        players.add(voidPlayer);
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
