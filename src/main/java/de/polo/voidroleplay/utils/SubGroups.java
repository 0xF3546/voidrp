package de.polo.voidroleplay.utils;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.dataStorage.SubGroup;
import lombok.SneakyThrows;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class SubGroups {
    private final FactionManager factionManager;
    private final List<SubGroup> subGroupList = new ArrayList<>();
    public SubGroups(FactionManager factionManager) {
        this.factionManager = factionManager;
        load();
    }

    @SneakyThrows
    private void load() {
        Connection connection = Main.getInstance().mySQL.getConnection();
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM subgroups");
        ResultSet result = statement.executeQuery();
        while (result.next()) {
            SubGroup group = new SubGroup();
            group.setId(result.getInt("id"));
            group.setFactionId(result.getInt("faction"));
            group.setName(result.getString("name"));
            group.setBank(result.getInt("bank"));
            subGroupList.add(group);
        }
    }

    public SubGroup getSubGroup(int groupId) {
        for (SubGroup group : subGroupList) {
            if (group.getId() == groupId) {
                return group;
            }
        }
        return null;
    }

    public void sendMessage(String message, SubGroup... subGroups) {
        for (SubGroup subGroup : subGroups) {
            for (PlayerData playerData : Main.getInstance().playerManager.getPlayers()) {
                if (playerData.getSubGroupId() == subGroup.getId()) {
                    playerData.getPlayer().sendMessage(message);
                }
            }
        }
    }

    public Collection<SubGroup> getSubGroups() {
        return subGroupList;
    }

    @SneakyThrows
    public void create(SubGroup subGroup) {
        Connection connection = Main.getInstance().mySQL.getConnection();
        PreparedStatement statement = connection.prepareStatement("INSERT INTO subgroups (name) VALUES (?)", Statement.RETURN_GENERATED_KEYS);
        statement.setString(1, subGroup.getName());
        statement.execute();
        ResultSet generatedKeys = statement.getGeneratedKeys();
        if (generatedKeys.next()) {
            int key = generatedKeys.getInt(1);
            subGroup.setId(key);
        }
        statement.close();
        connection.close();
        subGroupList.add(subGroup);
    }
}
