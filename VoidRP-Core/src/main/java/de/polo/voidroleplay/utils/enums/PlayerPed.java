package de.polo.voidroleplay.utils.enums;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Entity;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class PlayerPed {
    @Getter
    private final Pet pet;

    @Getter
    @Setter
    private boolean active;

    @Getter
    @Setter
    private Entity entity;

    public PlayerPed(Pet pet, boolean active) {
        this.pet = pet;
        this.active = active;
    }
}
