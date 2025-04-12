package de.polo.core.pinwheels.entities;

import de.polo.api.pinwheels.Pinwheel;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;

import java.util.Objects;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public final class CorePinwheel implements Pinwheel {
    @Getter
    private final int id;
    @Getter
    private final Location location;
    @Getter
    private final String name;
    @Getter
    @Setter
    private boolean broken = true;

    /**
     *
     */
    public CorePinwheel(int id, Location location, String name) {
        this.id = id;
        this.location = location;
        this.name = name;
    }
}
