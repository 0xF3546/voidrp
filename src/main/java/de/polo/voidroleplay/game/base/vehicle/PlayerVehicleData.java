package de.polo.voidroleplay.game.base.vehicle;

import de.polo.voidroleplay.Main;
import lombok.SneakyThrows;
import org.bukkit.World;

import java.sql.Statement;

public class PlayerVehicleData {
    private int id;
    private String uuid;
    private String type;
    private int km;
    private float fuel;
    private boolean isParked;
    private int x;
    private int y;
    private int z;
    private World welt;
    private float yaw;
    private float pitch;
    private int garage;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getKm() {
        return km;
    }

    public void setKm(int km) {
        this.km = km;
    }

    public float getFuel() {
        return fuel;
    }

    public void setFuel(float fuel) {
        this.fuel = fuel;
    }

    public boolean isParked() {
        return isParked;
    }

    public void setParked(boolean parked) {
        isParked = parked;
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

    public int getGarage() {
        return garage;
    }

    public void setGarage(int garage) {
        this.garage = garage;
    }

    @SneakyThrows
    public void save() {
        Statement statement = Main.getInstance().mySQL.getStatement();
        statement.executeUpdate("UPDATE `player_vehicles` SET `km` = " + getKm() + ", `fuel` = " + getFuel() + ", `x` = " + getX() + ", `y` = " + getY() + ", `z` = " + getZ() + ", `welt` = '" + getWelt() + "', `yaw` = " + getYaw() + ", `pitch` = " + getPitch() + " WHERE `id` = " + getId());

    }
}
