package de.polo.voidroleplay.dataStorage;

import de.polo.voidroleplay.Main;
import lombok.SneakyThrows;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class BlacklistData {
    private int id;
    private String uuid;
    private String faction;
    private String reason;
    private String date;
    private int kills;
    private int price;

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

    public String getFaction() {
        return faction;
    }

    public void setFaction(String faction) {
        this.faction = faction;
    }

    public int getKills() {
        return kills;
    }

    public void setKills(int kills) {
        this.kills = kills;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @SneakyThrows
    public void save() {
        Connection connection = Main.getInstance().mySQL.getConnection();
        PreparedStatement statement = connection.prepareStatement("UPDATE blacklist SET reason = ?, kills = ?, price = ? WHERE id = ?");
        statement.setString(1, reason);
        statement.setInt(2, kills);
        statement.setInt(3, price);
        statement.setInt(4, id);
        statement.executeUpdate();
        statement.close();
        connection.close();
    }
}
