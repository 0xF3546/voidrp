package de.polo.core.game.faction.laboratory;

import de.polo.core.Main;
import de.polo.core.utils.enums.RoleplayItem;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class EvidenceChamber {
    private int id;
    private int weed;
    private int joints;
    private int cocaine;
    private int noble_joints;
    @Getter
    @Setter
    private int crystal;

    public EvidenceChamber(int weed, int joints, int cocaine, int noble_joints, int crystal) {
        this.weed = weed;
        this.joints = joints;
        this.cocaine = cocaine;
        this.noble_joints = noble_joints;
        this.crystal = crystal;
    }

    public int getJoints() {
        return joints;
    }

    public void setJoints(int joints) {
        this.joints = joints;
    }

    public int getCocaine() {
        return cocaine;
    }

    public void setCocaine(int cocaine) {
        this.cocaine = cocaine;
    }

    public int getNoble_joints() {
        return noble_joints;
    }

    public void setNoble_joints(int noble_joints) {
        this.noble_joints = noble_joints;
    }

    public int getWeed() {
        return weed;
    }

    public void setWeed(int weed) {
        this.weed = weed;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAmount(RoleplayItem item) {
        int amount = 0;
        switch (item) {
            case SNUFF:
                amount = cocaine;
                break;
            case PIPE_TOBACCO:
                amount = weed;
                break;
            case CIGAR:
                amount = noble_joints;
                break;
            case PIPE:
                amount = joints;
                break;
            case CRYSTAL:
                amount = crystal;
                break;
        }
        return amount;
    }

    public void removeItem(RoleplayItem item, int amount) {
        switch (item) {
            case SNUFF:
                setCocaine(cocaine - amount);
                break;
            case PIPE_TOBACCO:
                setWeed(weed - amount);
                break;
            case CIGAR:
                setNoble_joints(noble_joints - amount);
                break;
            case PIPE:
                setJoints(joints - amount);
                break;
            case CRYSTAL:
                setCrystal(crystal - amount);
                break;
        }
    }

    public void addItem(RoleplayItem item, int amount) {
        switch (item) {
            case SNUFF:
                setCocaine(cocaine + amount);
                break;
            case PIPE_TOBACCO:
                setWeed(weed + amount);
                break;
            case CIGAR:
                setNoble_joints(noble_joints + amount);
                break;
            case PIPE:
                setJoints(joints + amount);
                break;
            case CRYSTAL:
                setCrystal(crystal + amount);
                break;
        }
    }

    @SneakyThrows
    public void save() {
        Connection connection = Main.getInstance().coreDatabase.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement("UPDATE evidenceChamber SET weed = ?, joints = ?, cocaine = ?, noble_joints = ?, crystal = ? WHERE id = ?");
        preparedStatement.setInt(1, weed);
        preparedStatement.setInt(2, joints);
        preparedStatement.setInt(3, cocaine);
        preparedStatement.setInt(4, noble_joints);
        preparedStatement.setInt(5, crystal);
        preparedStatement.setInt(6, id);
        preparedStatement.executeUpdate();
        preparedStatement.close();
        connection.close();
    }
}
