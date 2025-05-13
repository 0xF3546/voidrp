package de.polo.core.zone.entities;

import de.polo.api.player.VoidPlayer;
import de.polo.api.zone.Region;
import de.polo.api.zone.Zone;
import de.polo.api.zone.enums.ZoneType;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;

import java.util.List;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class CoreZone implements Zone {

    @Getter
    private final String name;

    @Getter
    private final Region region;

    @Getter
    private final ZoneType type;

    private final List<VoidPlayer> playersInZone = new ObjectArrayList<>();

    public CoreZone(String name, Region region, ZoneType type) {
        this.name = name;
        this.region = region;
        this.type = type;
    }

    @Override
    public List<VoidPlayer> getPlayersInZone() {
        return playersInZone;
    }

    @Override
    public void addPlayer(VoidPlayer player) {
        if (!playersInZone.contains(player)) {
            playersInZone.add(player);
        }
    }

    @Override
    public void removePlayer(VoidPlayer player) {
        playersInZone.remove(player);
    }
}
