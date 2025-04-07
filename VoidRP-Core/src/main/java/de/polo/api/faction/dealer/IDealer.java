package de.polo.api.faction.dealer;

import org.bukkit.Location;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public interface IDealer {
    Location getLocation();

    String getName();

    boolean isFull();

    void setFull(boolean state);
}
