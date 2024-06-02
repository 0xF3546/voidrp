package de.polo.voidroleplay.utils.enums;

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
    STADTHALLE("Stadthalle", new Location(Bukkit.getWorld("World"), 133, 72, 157));

    private final String name;
    private final Location location;
}