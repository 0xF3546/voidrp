package de.polo.core.laboratory.dto;

import lombok.Getter;
import org.bukkit.Location;

public record CreateLaboratoryDto(@Getter Location location, @Getter String name) {
}
