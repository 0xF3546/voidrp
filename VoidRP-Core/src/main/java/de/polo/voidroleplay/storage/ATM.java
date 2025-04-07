package de.polo.voidroleplay.storage;

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

    @Getter
    @Setter
    private int moneyAmount = 100000;
}
