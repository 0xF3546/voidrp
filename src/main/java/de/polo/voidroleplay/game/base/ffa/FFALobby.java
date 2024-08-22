package de.polo.voidroleplay.game.base.ffa;

import de.polo.voidroleplay.utils.enums.FFALobbyType;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class FFALobby {
    @Getter
    private final String name;

    @Getter
    private final int maxPlayer;

    @Getter
    private final int id;

    private final List<FFASpawn> spawns = new ArrayList<>();

    @Getter
    @Setter
    private FFALobbyType ffaLobbyType = FFALobbyType.BASIC;

    public FFALobby(int id, String name, int maxPlayer) {
        this.id = id;
        this.name = name;
        this.maxPlayer = maxPlayer;
    }

    public void addSpawn(FFASpawn ffaSpawn) {
        this.spawns.add(ffaSpawn);
    }

    public List<FFASpawn> getSpawns() {
        return spawns;
    }
}
