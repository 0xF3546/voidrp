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
public enum Workstation {
    BULLETPROOF("Schutzwesten", new Location(Bukkit.getWorld("World"), 1, 1, 1), RoleplayItem.KEVLAR, RoleplayItem.BULLETPROOF, 2, 1),
    KEVLAR("Kevlar", new Location(Bukkit.getWorld("World"), 1, 1, 1), RoleplayItem.ARAMID, RoleplayItem.KEVLAR, 2, 1);

    private final String name;
    private final Location location;
    private final RoleplayItem inputItem;
    private final RoleplayItem outputItem;
    private final int tickInput;
    private final int tickOutput;
}
