package de.polo.voidroleplay.storage;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.faction.entity.FactionData;
import lombok.SneakyThrows;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class SubGroup {
    private int id;
    private int factionId;
    private String name;
    private int bank;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setFactionId(int factionId) {
        this.factionId = factionId;
    }

    public FactionData getFaction() {
        return Main.getInstance().factionManager.getFactionData(factionId);
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

    @SneakyThrows
    public void save() {
        Connection connection = Main.getInstance().mySQL.getConnection();
        PreparedStatement statement = connection.prepareStatement("UPDATE subgroups SET faction = ?, name = ?, bank = ? WHERE id = ?");
        statement.setInt(1, factionId);
        statement.setString(2, name);
        statement.setInt(3, bank);
        statement.setInt(4, id);
        statement.execute();
        statement.close();
        connection.close();
    }
}
