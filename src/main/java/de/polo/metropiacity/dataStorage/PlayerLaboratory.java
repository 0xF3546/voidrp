package de.polo.metropiacity.dataStorage;

import de.polo.metropiacity.Main;
import de.polo.metropiacity.utils.Game.Laboratory;
import de.polo.metropiacity.utils.enums.RoleplayItem;
import lombok.SneakyThrows;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.UUID;

public class PlayerLaboratory {
    private UUID owner;
    private int weedAmount;
    private float jointAmount;
    private boolean started;
    public UUID getOwner() {
        return owner;
    }

    private final Laboratory laboratory;

    public PlayerLaboratory(Laboratory laboratory) {
        this.laboratory = laboratory;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public float getJointAmount() {
        return jointAmount;
    }

    public void setJointAmount(float jointAmount) {
        this.jointAmount = jointAmount;
    }

    public int getWeedAmount() {
        return weedAmount;
    }

    public void setWeedAmount(int weedAmount) {
        this.weedAmount = weedAmount;
    }

    @SneakyThrows
    public void create(UUID owner) {
        Statement statement = Main.getInstance().mySQL.getStatement();
        ResultSet result = statement.executeQuery("SELECT * FROM player_laboratory WHERE uuid = '" + owner + "'");
        setOwner(owner);
        if (result.next()) {
            setJointAmount(result.getInt("joints"));
            setWeedAmount(result.getInt("weed"));
            return;
        }
        setJointAmount(0);
        setWeedAmount(0);
        statement.execute("INSERT INTO player_laboratory (uuid, joints, weed) VALUES ('" + owner + "', " + jointAmount + ", " + weedAmount + ")");
    }

    public void start() {
        setStarted(true);
        laboratory.addPlayerLaboratory(this);
    }

    public void stop() {
        setStarted(false);
        laboratory.removePlayerLaboratory(this);
    }

    public void add(int amount) {
        setWeedAmount(weedAmount += amount);
    }

    public void remove(RoleplayItem item, int amount) {
        switch (item) {
            case MARIHUANA:
                setWeedAmount(weedAmount -= amount);
                break;
            case JOINT:
                setJointAmount(jointAmount -= amount);
                break;
        }
    }

    @SneakyThrows
    public void save() {
        Statement statement = Main.getInstance().mySQL.getStatement();
        statement.execute("UPDATE player_laboratory SET joints = " + getJointAmount() + ", weed = " + getWeedAmount() + " WHERE uuid = '" + getOwner() + "'");
    }

    @SneakyThrows
    public void delete() {
        Statement statement = Main.getInstance().mySQL.getStatement();
        statement.execute("DELETE FROM player_laboratory WHERE uuid = '" + getOwner() + "'");
        Main.getInstance().laboratory.removePlayerLaboratory(this);
    }

    public boolean isStarted() {
        return started;
    }

    private void setStarted(boolean started) {
        this.started = started;
    }
}
