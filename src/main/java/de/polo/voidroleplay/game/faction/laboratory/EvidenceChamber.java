package de.polo.voidroleplay.game.faction.laboratory;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.utils.enums.RoleplayItem;
import lombok.SneakyThrows;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class EvidenceChamber {
    private int id;
    private int weed;
    private int joints;
    private int cocaine;
    private int noble_joints;
    public EvidenceChamber(int weed, int joints, int cocaine, int noble_joints) {
        this.weed = weed;
        this.joints = joints;
        this.cocaine = cocaine;
        this.noble_joints = noble_joints;
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
            case COCAINE:
                amount = cocaine;
                break;
            case MARIHUANA:
                amount = weed;
                break;
            case NOBLE_JOINT:
                amount = noble_joints;
                break;
            case JOINT:
                amount = joints;
                break;
        }
        return amount;
    }

    public void removeItem(RoleplayItem item, int amount) {
        switch (item) {
            case COCAINE:
                setCocaine(cocaine - amount);
                break;
            case MARIHUANA:
                setWeed(weed - amount);
                break;
            case NOBLE_JOINT:
                setNoble_joints(noble_joints - amount);
                break;
            case JOINT:
                setJoints(joints - amount);
                break;
        }
    }

    public void addItem(RoleplayItem item, int amount) {
        switch (item) {
            case COCAINE:
                setCocaine(cocaine + amount);
                break;
            case MARIHUANA:
                setWeed(weed + amount);
                break;
            case NOBLE_JOINT:
                setNoble_joints(noble_joints + amount);
                break;
            case JOINT:
                setJoints(joints + amount);
                break;
        }
    }

    @SneakyThrows
    public void save() {
        Connection connection = Main.getInstance().mySQL.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement("UPDATE evidenceChamber SET weed = ?, joints = ?, cocaine = ?, noble_joints = ? WHERE id = ?");
        preparedStatement.setInt(1, weed);
        preparedStatement.setInt(2, joints);
        preparedStatement.setInt(3, cocaine);
        preparedStatement.setInt(4, noble_joints);
        preparedStatement.setInt(5, id);
        preparedStatement.executeUpdate();
        preparedStatement.close();
        connection.close();
    }
}
