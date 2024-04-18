package de.polo.voidroleplay.game.faction.apotheke;

import de.polo.voidroleplay.Main;
import lombok.SneakyThrows;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class Apotheke {
    private int id;
    private boolean staat;
    private String owner;
    private LocalDateTime lastAttack;
    private Player attacker;
    private String attackerFaction;

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

    @SneakyThrows
    public void save() {
        try (PreparedStatement preparedStatement = Main.getInstance().mySQL.getConnection()
                .prepareStatement("UPDATE apotheken SET owner = ?, lastAttack = ? WHERE id = ?")) {

            preparedStatement.setString(1, getOwner());
            preparedStatement.setObject(2, getLastAttack());
            preparedStatement.setInt(3, getId());

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
}
