package de.polo.metropiacity.dataStorage;

import org.bukkit.Material;

public class FarmingData {
    private int id;
    private boolean isFarmer;
    private String type;
    private int amount;
    private int duration;
    private Material item;
    private String needed_item;
    private String itemName;
    private String drug;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isFarmer() {
        return isFarmer;
    }

    public void setFarmer(boolean farmer) {
        isFarmer = farmer;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }




    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public Material getItem() {
        return item;
    }

    public void setItem(Material item) {
        this.item = item;
    }

    public String getNeeded_item() {
        return needed_item;
    }

    public void setNeeded_item(String needed_item) {
        this.needed_item = needed_item;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getDrug() {
        return drug;
    }

    public void setDrug(String drug) {
        this.drug = drug;
    }
}