package de.polo.void_roleplay.DataStorage;

import java.util.Map;

public class FactionData {
    private int id;
    private String primaryColor;
    private String secondaryColor;
    private String fullname;
    private String name;
    private int bank;
    private boolean hasBlacklist;

    public String getPrimaryColor() {
        return primaryColor;
    }

    public void setPrimaryColor(String primaryColor) {
        this.primaryColor = primaryColor;
    }

    public String getSecondaryColor() {
        return secondaryColor;
    }

    public void setSecondaryColor(String secondaryColor) {
        this.secondaryColor = secondaryColor;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
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

    public int getBank() {
        return bank;
    }

    public void setBank(int bank) {
        this.bank = bank;
    }

    public boolean hasBlacklist() {
        return hasBlacklist;
    }

    public void setHasBlacklist(boolean hasBlacklist) {
        this.hasBlacklist = hasBlacklist;
    }
}
