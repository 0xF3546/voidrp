package de.polo.voidroleplay.dataStorage;

import de.polo.voidroleplay.utils.enums.ShopType;
import org.bukkit.World;

public class ShopData {
    private int id;
    private String name;
    private int x;
    private int y;
    private int z;
    private World welt;
    private float yaw;
    private float pitch;
    private ShopType type;

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

    public World getWelt() {
        return welt;
    }

    public void setWelt(World welt) {
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

    public ShopType getType() {
        return type;
    }

    public void setType(ShopType type) {
        this.type = type;
    }
}
