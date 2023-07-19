package de.polo.metropiacity.dataStorage;

import org.bukkit.World;

public class FFASpawnPoints {
    private int id;
    private String lobby_type;
    private int x;
    private int y;
    private int z;
    private World welt;
    private float yaw;
    private float pitch;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
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

    public String getLobby_type() {
        return lobby_type;
    }

    public void setLobby_type(String lobby_type) {
        this.lobby_type = lobby_type;
    }
}
