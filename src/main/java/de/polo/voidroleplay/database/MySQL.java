package de.polo.voidroleplay.database;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class MySQL {
    public MySQL() {
        loadDBData();
    }
    static final String url = "jdbc:mysql://45.13.227.171/minecraft?autoReconnect=true&useSSL=false";
    static String user = null;
    static String password = null;
    static int port = 3306;
    private static boolean error;
    public static Connection connection;
    //6~nPp?hL
    public boolean loadDBData() {
        File file = new File("plugins//roleplay//database.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
                FileConfiguration cfg = YamlConfiguration.loadConfiguration(new File("plugins//roleplay//database.yml"));
                cfg.set("password", "Datenbank-Passwort");
                cfg.set("user", "Datenbank-Benutzer");
                cfg.save(file);
                return false;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        password = cfg.getString("password");
        user = cfg.getString("user");
        System.out.println("Database loaded");
        return true;
    }
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    public Statement getStatement() throws SQLException {
        if(connection != null) {
            return (Statement) connection;
        }
        Connection connection = DriverManager.getConnection(url, user, password);

        return connection.createStatement();
    }
    public static boolean isError() {
        return error;
    }

    public static void setError(boolean error) {
        MySQL.error = error;
    }

    public static void endConnection() throws SQLException {
        MySQL.connection.close();
    }
    public interface forum {
        String url = "jdbc:mysql://localhost/wcf?autoReconnect=true&useSSL=false";
        int port = 3306;
        static Connection getConnection() throws SQLException {
            if(connection != null) {
                return connection;
            }
            Connection connection = DriverManager.getConnection(url, user, password);

            for (int i = 0; i < 5; i++) {
                System.out.println("[MySQL]: Datenbank verbunden");
            }
            return connection;
        }
        static Statement getStatement() throws SQLException {
            if(connection != null) {
                return (Statement) connection;
            }
            Connection connection = DriverManager.getConnection(url, user, password);

            return connection.createStatement();
        }
    }

}
