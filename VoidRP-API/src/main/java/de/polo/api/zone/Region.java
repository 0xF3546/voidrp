package de.polo.api.zone;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface Region {
    int getId();
    String getName();
    Location getLowerCorner();

    Location getUpperCorner();

    boolean isInside(Player player);
}
