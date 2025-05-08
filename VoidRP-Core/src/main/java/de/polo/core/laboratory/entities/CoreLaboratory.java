package de.polo.core.laboratory.entities;

import de.polo.api.laboratory.Laboratory;
import lombok.Getter;
import org.bukkit.Location;

public class CoreLaboratory implements Laboratory {

    @Getter
    private final int id;

    @Getter
    private final Location location;

    @Getter
    private final String name;

    public CoreLaboratory(final int id, final Location location, final String name) {
        this.id = id;
        this.location = location;
        this.name = name;
    }
}
