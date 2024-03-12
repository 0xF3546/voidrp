package de.polo.voidroleplay.dataStorage;

import org.bukkit.Location;

public class ServiceData {
    private int number;
    private String reason;
    private Location location;
    private String uuid;
    private String acceptedByUuid;

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getAcceptedByUuid() {
        return acceptedByUuid;
    }

    public void setAcceptedByUuid(String acceptedByUuid) {
        this.acceptedByUuid = acceptedByUuid;
    }
}
