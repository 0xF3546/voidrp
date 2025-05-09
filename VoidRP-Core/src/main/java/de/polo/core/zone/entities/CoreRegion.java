package de.polo.core.zone.entities;

import de.polo.api.zone.Region;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class CoreRegion implements Region {
    @Getter
    private final int id;

    @Getter
    private final String name;
    @Getter
    private final Location lowerCorner;
    @Getter
    private final Location upperCorner;

    public CoreRegion(int id, String name, Location corner1, Location corner2) {
        this.id = id;
        this.name = name;
        this.lowerCorner = calculateLowerCorner(corner1, corner2);
        this.upperCorner = calculateUpperCorner(corner1, corner2);
    }

    private Location calculateLowerCorner(Location loc1, Location loc2) {
        return new Location(
                loc1.getWorld(),
                Math.min(loc1.getX(), loc2.getX()),
                Math.min(loc1.getY(), loc2.getY()),
                Math.min(loc1.getZ(), loc2.getZ())
        );
    }

    private Location calculateUpperCorner(Location loc1, Location loc2) {
        return new Location(
                loc1.getWorld(),
                Math.max(loc1.getX(), loc2.getX()),
                Math.max(loc1.getY(), loc2.getY()),
                Math.max(loc1.getZ(), loc2.getZ())
        );
    }

    public boolean isInside(Player player) {
        Location loc = player.getLocation();
        return loc.getWorld().equals(lowerCorner.getWorld()) &&
                loc.getX() >= lowerCorner.getX() && loc.getX() <= upperCorner.getX() &&
                loc.getY() >= lowerCorner.getY() && loc.getY() <= upperCorner.getY() &&
                loc.getZ() >= lowerCorner.getZ() && loc.getZ() <= upperCorner.getZ();
    }
}
