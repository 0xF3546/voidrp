package de.polo.core.game.faction.streetwar;

import de.polo.core.Main;
import lombok.SneakyThrows;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;

public class StreetwarData {
    private int id;
    private String attacker;
    private String defender;
    private int attacker_points;
    private int defender_points;
    private LocalDateTime started;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAttacker() {
        return attacker;
    }

    public void setAttacker(String attacker) {
        this.attacker = attacker;
    }

    public String getDefender() {
        return defender;
    }

    public void setDefender(String defender) {
        this.defender = defender;
    }

    public int getAttacker_points() {
        return attacker_points;
    }

    public void setAttacker_points(int attacker_points) {
        this.attacker_points = attacker_points;
    }

    public int getDefender_points() {
        return defender_points;
    }

    public void setDefender_points(int defender_points) {
        this.defender_points = defender_points;
    }

    public LocalDateTime getStarted() {
        return started;
    }

    public void setStarted(LocalDateTime started) {
        this.started = started;
    }

    @SneakyThrows
    public void save() {
        Connection connection = Main.getInstance().coreDatabase.getConnection();
        PreparedStatement statement = connection.prepareStatement("UPDATE streetwar SET attacker = ?, defender = ?, attacker_points = ?, defender_points = ? WHERE id = ?");
        statement.setString(1, attacker);
        statement.setString(2, defender);
        statement.setInt(3, attacker_points);
        statement.setInt(4, defender_points);
        statement.setInt(5, id);
        statement.execute();
        statement.close();
        connection.close();
    }
}
