package de.polo.voidroleplay.game.faction.plants;

import de.polo.voidroleplay.Main;
import lombok.SneakyThrows;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Plant {
    private int id;
    private String owner;
    private float multiplier;
    private int storage;
    private LocalDateTime lastAttack;
    private Player attacker;
    private String attackerFaction;
    private List<UUID> tookOutThisHour = new ArrayList<>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public float getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(float multiplier) {
        this.multiplier = multiplier;
    }

    public int getStorage() {
        return storage;
    }

    public void setStorage(int storage) {
        this.storage = storage;
    }

    public LocalDateTime getLastAttack() {
        return lastAttack;
    }

    public void setLastAttack(LocalDateTime lastAttack) {
        this.lastAttack = lastAttack;
    }

    public Player getAttacker() {
        return attacker;
    }

    public void setAttacker(Player attacker) {
        this.attacker = attacker;
    }

    public String getAttackerFaction() {
        return attackerFaction;
    }

    public void setAttackerFaction(String attackerFaction) {
        this.attackerFaction = attackerFaction;
    }

    public void addTookout(UUID uuid) {
        tookOutThisHour.add(uuid);
    }

    public void clearTookout() {
        tookOutThisHour.clear();
    }

    public boolean hasTookout(UUID uuid) {
        return tookOutThisHour.contains(uuid);
    }

    @SneakyThrows
    public void save() {
        Main.getInstance().getMySQL().updateAsync("UPDATE plantagen SET owner = ?, lastAttack = ?, storage = ? WHERE id = ?",
                getOwner(),
                getLastAttack(),
                getStorage(),
                getId());
    }
}
