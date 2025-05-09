package de.polo.core.zone.dto;

import lombok.Getter;
import org.bukkit.Location;

public record CreateRegionDto(String name, Location location1, Location location2) {

}
