package de.polo.metropiacity.dataStorage;

import java.time.LocalDateTime;

public class Apotheke {
    private int id;
    private boolean staat;
    private String owner;
    private LocalDateTime lastAttack;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isStaat() {
        return staat;
    }

    public void setStaat(boolean staat) {
        this.staat = staat;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public LocalDateTime getLastAttack() {
        return lastAttack;
    }

    public void setLastAttack(LocalDateTime lastAttack) {
        this.lastAttack = lastAttack;
    }
}
