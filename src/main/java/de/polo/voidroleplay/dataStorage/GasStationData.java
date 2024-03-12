package de.polo.voidroleplay.dataStorage;

import org.bukkit.World;

public class GasStationData {
    private int id;
    private String name;
    private int x;
    private int y;
    private int z;
    private World welt;
    private float yaw;
    private float pitch;
    private String uuid;
    private int price;
    private int literprice;
    private int liter;

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

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getLiterprice() {
        return literprice;
    }

    public void setLiterprice(int literprice) {
        this.literprice = literprice;
    }

    public int getLiter() {
        return liter;
    }

    public void setLiter(int liter) {
        this.liter = liter;
    }
}
