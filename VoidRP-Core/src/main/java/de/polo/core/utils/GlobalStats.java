package de.polo.core.utils;

import de.polo.core.Main;
import lombok.SneakyThrows;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class GlobalStats {
    private static final HashMap<String, String> values = new HashMap<>();

    @SneakyThrows
    public static void load() {
        Connection connection = Main.getInstance().coreDatabase.getConnection();
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM globalvariables");
        ResultSet result = statement.executeQuery();
        while (result.next()) {
            values.put(result.getString("type"), result.getString("value"));
        }
    }

    public static String getValue(String value) {
        return values.get(value);
    }

    @SneakyThrows
    public static void setValue(String key, String value, boolean save) {
        values.replace(key, value);
        if (!save) return;
        Connection connection = Main.getInstance().coreDatabase.getConnection();
        PreparedStatement statement = connection.prepareStatement("UPDATE globalvariables SET value = ? WHERE type = ?");
        statement.setString(1, value);
        statement.setString(2, key);
        statement.execute();
        statement.close();
        connection.close();
    }
}
