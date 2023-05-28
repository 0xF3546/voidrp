package de.polo.metropiacity.DataStorage;

import org.bukkit.Material;

public class NaviData {
    private int id;
    private boolean isGroup;
    private String group;
    private String name;
    private String location;
    private Material item;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isGroup() {
        return isGroup;
    }

    public void setisGroup(boolean group) {
        isGroup = group;
    }

    public String getGroup() {
        return group;
    }
    public void setGroup(String newgroup) {
        group = newgroup;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Material getItem() {
        return item;
    }

    public void setItem(Material item) {
        this.item = item;
    }
}
