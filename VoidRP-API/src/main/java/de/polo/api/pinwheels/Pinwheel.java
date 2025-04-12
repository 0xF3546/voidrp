package de.polo.api.pinwheels;

import org.bukkit.Location;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public interface Pinwheel {
    Location getLocation();
    String getName();
    void setBroken(boolean broken);
    boolean isBroken();
}
