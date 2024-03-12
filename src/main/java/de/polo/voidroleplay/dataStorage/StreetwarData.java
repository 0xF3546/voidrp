package de.polo.voidroleplay.dataStorage;

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
}
