package de.polo.voidroleplay.dataStorage;

import org.bukkit.Location;

import java.time.LocalDateTime;

public class ATM {
    private int id;
    private String name;
    private Location location;
    private LocalDateTime lastTimeBlown;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public LocalDateTime getLastTimeBlown() {
        return lastTimeBlown;
    }

    public void setLastTimeBlown(LocalDateTime lastTimeBlown) {
        this.lastTimeBlown = lastTimeBlown;
    }
}
