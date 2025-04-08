package de.polo.core.utils.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */

@AllArgsConstructor
@Getter
public enum Storages {
    STADTHALLE("Stadthalle", new Location(Bukkit.getWorld("World"), -25, 66, -33), false, -1),
    BANK("Zentralbank", new Location(Bukkit.getWorld("World"), 77, 69, -334), false, -1),
    STADTHALLE_GEWORBEN("Stadthalle (Geworben)", new Location(Bukkit.getWorld("World"), -25, 66, -26), true, 2);

    private final String name;
    private final Location location;
    private final boolean isGeworben;
    private final int amount;
}