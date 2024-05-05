package de.polo.voidroleplay.dataStorage;

import de.polo.api.faction.dealer.IDealer;
import org.bukkit.Location;

public class Dealer implements IDealer {
    private int id;
    private Location location;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public Location getLocation() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public boolean isFull() {
        return false;
    }

    @Override
    public void setFull(boolean state) {

    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
