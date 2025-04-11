package de.polo.core.storage;

import de.polo.core.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class LocationData {
    private int id;
    private String name;
    private int x;
    private int y;
    private int z;
    private String welt;
    private float yaw;
    private float pitch;
    private String type;
    private String info;
    public LocationData() {}
    public LocationData(int id, String name, int x, int y, int z, String welt, float yaw, float pitch, String type, String info) {
        this.id = id;
        this.name = name;
        this.x = x;
        this.y = y;
        this.z = z;
        this.welt = welt;
        this.yaw = yaw;
        this.pitch = pitch;
        this.type = type;
        this.info = info;
    }

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

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public String getWelt() {
        return welt;
    }

    public void setWelt(String welt) {
        this.welt = welt;
    }

    public double getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public double getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public Location getLocation() {
        return Utils.getLocation(getX(), getY(), getZ(), Bukkit.getWorld(getWelt()), yaw, pitch);
    }
}
