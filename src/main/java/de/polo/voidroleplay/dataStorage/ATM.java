package de.polo.voidroleplay.dataStorage;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;

import java.time.LocalDateTime;

public class ATM {
    @Getter
    @Setter
    private int id;
    @Getter
    @Setter
    private String name;
    @Getter
    @Setter
    private Location location;
    @Getter
    @Setter
    private LocalDateTime lastTimeBlown;
}
