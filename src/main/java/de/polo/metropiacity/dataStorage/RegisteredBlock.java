package de.polo.metropiacity.dataStorage;

import org.bukkit.Location;
import org.bukkit.block.Block;

import java.time.LocalDate;

public class RegisteredBlock {
    private int id;
    private Block block;
    private String info;
    private String infoValue;
    private Location location;

    public Block getBlock() {
        return block;
    }

    public void setBlock(Block block) {
        this.block = block;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getInfoValue() {
        return infoValue;
    }

    public void setInfoValue(String infoValue) {
        this.infoValue = infoValue;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
