package de.polo.core.laboratory.entities;

import de.polo.api.faction.IFaction;
import de.polo.api.laboratory.Laboratory;
import de.polo.api.laboratory.enums.LaboratoryType;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;

import java.time.LocalDateTime;

public class CoreLaboratory implements Laboratory {

    @Getter
    private final int id;

    @Getter
    private final Location location;

    @Getter
    private final String name;

    @Getter
    private final LaboratoryType type;

    @Getter
    @Setter
    private LocalDateTime lastAttack;

    @Getter
    @Setter
    private IFaction faction;

    public CoreLaboratory(final int id, final LaboratoryType type, final Location location, final String name) {
        this.id = id;
        this.type = type;
        this.location = location;
        this.name = name;
    }
}
