package de.polo.voidroleplay.dataStorage;

import de.polo.voidroleplay.Main;
import lombok.SneakyThrows;

import java.sql.PreparedStatement;
import java.util.UUID;

public class BusinessData {
    private int id;
    private String name;
    private String fullname;
    private int bank;
    private int maxMember;
    private UUID owner;
    private boolean active;

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

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public int getBank() {
        return bank;
    }

    public void setBank(int bank) {
        this.bank = bank;
    }

    public int getMaxMember() {
        return maxMember;
    }

    public void setMaxMember(int maxMember) {
        this.maxMember = maxMember;
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @SneakyThrows
    public void save() {
        PreparedStatement statement = Main.getInstance().mySQL.getConnection().prepareStatement("UPDATE business SET owner = ?, activated = ? WHERE id = ?");
        statement.setString(1, getOwner().toString());
        statement.setBoolean(2, isActive());
        statement.setInt(3, getId());
        statement.executeUpdate();
    }
}
