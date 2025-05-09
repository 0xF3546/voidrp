package de.polo.api.laboratory;

import de.polo.api.faction.IFaction;
import de.polo.api.laboratory.enums.LaboratoryType;
import org.bukkit.Location;

import java.time.LocalDateTime;

public interface Laboratory {
    int getId();
    Location getLocation();
    String getName();
    IFaction getFaction();
    LocalDateTime getLastAttack();
    void setLastAttack(LocalDateTime lastAttack);

    LaboratoryType getType();
}
