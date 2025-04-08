package de.polo.core.game.base.ffa;

import lombok.Getter;
import org.bukkit.Location;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class FFASpawn {
    @Getter
    private final int id;

    @Getter
    private final int lobbyId;

    @Getter
    private final Location location;

    public FFASpawn(int id, int lobbyId, Location location) {
        this.id = id;
        this.lobbyId = lobbyId;
        this.location = location;
    }
}
