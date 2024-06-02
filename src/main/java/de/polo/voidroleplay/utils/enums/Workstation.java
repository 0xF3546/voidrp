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
    BULLETPROOF("Schutzwesten", new Location(Bukkit.getWorld("World"), 388, 73, -278), RoleplayItem.KEVLAR, RoleplayItem.BULLETPROOF, 5, 1),
    KEVLAR("Kevlar", new Location(Bukkit.getWorld("World"), 515, 69, -284), RoleplayItem.ARAMID, RoleplayItem.KEVLAR, 50, 1),
    IRON("Eisen", new Location(Bukkit.getWorld("World"), 1, 1, 1), RoleplayItem.EISENERZ, RoleplayItem.EISEN, 40, 1),
    WAFFENTEILE("Waffenteile", new Location(Bukkit.getWorld("World"), 1, 1, 1), RoleplayItem.EISEN, RoleplayItem.WAFFENTEIL, 40, 1);

    private final String name;
    private final Location location;
    private final RoleplayItem inputItem;
    private final RoleplayItem outputItem;
    private final int tickInput;
    private final int tickOutput;
}
