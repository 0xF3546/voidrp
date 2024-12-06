package de.polo.voidroleplay.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class MySQL {
    static final String url = "jdbc:mysql://185.117.3.65/minecraft?autoReconnect=true&useSSL=false";
    private static final Logger logger = LoggerFactory.getLogger(MySQL.class);
    static String user = null;
    static String password = null;
    static int port = 3306;
    private HikariDataSource dataSource;
    public MySQL() {
        loadDBData();
    }

    public void loadDBData() {
        File file = new File("plugins//roleplay//database.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
                FileConfiguration cfg = YamlConfiguration.loadConfiguration(new File("plugins//roleplay//database.yml"));
                cfg.set("password", "Datenbank-Passwort");
                cfg.set("user", "Datenbank-Benutzer");
                cfg.save(file);
                return;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        password = cfg.getString("password");
        user = cfg.getString("user");
        System.out.println("Database loaded");
        setupPool();
    }

    public void setupPool() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(user);
        config.setPassword(password);
        config.setMaximumPoolSize(10000);
        config.setIdleTimeout(30000);

        dataSource = new HikariDataSource(config);
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public Statement getStatement() throws SQLException {
        Connection connection = getConnection();
        return connection.createStatement();
    }

    public CompletableFuture<ResultSet> queryThreaded(String query) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Statement statement = getStatement();
                return statement.executeQuery(query);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<Integer> queryThreaded(String query, Object... args) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = getConnection();
                 PreparedStatement statement = connection.prepareStatement(query)) {

                for (int i = 0; i < args.length; i++) {
                    statement.setObject(i + 1, args[i]);
                }

                System.out.println("Executing query: " + query + " with args: " + Arrays.toString(args));
                int affectedRows = statement.executeUpdate();
                System.out.println("Query executed successfully: " + query);
                return affectedRows;

            } catch (SQLException e) {
                System.err.println("Error executing query: " + query);
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<Optional<Integer>> queryThreadedWithGeneratedKeys(String query, Object... args) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = getConnection();
                 PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

                for (int i = 0; i < args.length; i++) {
                    statement.setObject(i + 1, args[i]);
                }

                statement.executeUpdate();

                try (ResultSet resultSet = statement.getGeneratedKeys()) {
                    if (resultSet.next()) {
                        return Optional.of(resultSet.getInt(1));
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException("Error executing query", e);
            }
            return Optional.empty();
        });
    }

    public CompletableFuture<ResultSet> queryThreadedSelect(String query, Object... args) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = getConnection();
                 PreparedStatement statement = connection.prepareStatement(query)) {

                for (int i = 0; i < args.length; i++) {
                    statement.setObject(i + 1, args[i]);
                }

                System.out.println("Executing SELECT query: " + query + " with args: " + Arrays.toString(args));
                return statement.executeQuery();

            } catch (SQLException e) {
                System.err.println("Error executing SELECT query: " + query);
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        });
    }

    public <T> CompletableFuture<List<T>> executeQueryAsync(String query, ResultMapper<T> mapper, Object... args) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = getConnection();
                 PreparedStatement statement = connection.prepareStatement(query)) {

                for (int i = 0; i < args.length; i++) {
                    statement.setObject(i + 1, args[i]);
                }

                System.out.println("Executing query: {} with args: {}" + query + Arrays.toString(args));

                try (ResultSet resultSet = statement.executeQuery()) {
                    List<T> results = new ArrayList<>();

                    while (resultSet.next()) {
                        results.add(mapper.map(resultSet));
                    }

                    logger.info("Query executed successfully. Results: {}", results.size());
                    return results;
                }

            } catch (SQLException e) {
                logger.error("Error executing query: {}", query, e);
                e.printStackTrace();
                ///throw new DatabaseException("Error executing query", e);
            }
            return null;
        });
    }

    public CompletableFuture<List<Map<String, Object>>> executeQueryAsync(String query, Object... args) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = getConnection();
                 PreparedStatement statement = connection.prepareStatement(query)) {

                for (int i = 0; i < args.length; i++) {
                    statement.setObject(i + 1, args[i]);
                }

                System.out.println("Executing query: " + query + " with args: " + Arrays.toString(args));

                try (ResultSet resultSet = statement.executeQuery()) {
                    List<Map<String, Object>> results = new ArrayList<>();
                    ResultSetMetaData metaData = resultSet.getMetaData();
                    int columnCount = metaData.getColumnCount();

                    while (resultSet.next()) {
                        Map<String, Object> row = new HashMap<>();
                        for (int i = 1; i <= columnCount; i++) {
                            row.put(metaData.getColumnLabel(i), resultSet.getObject(i));
                        }
                        results.add(row);
                    }

                    logger.info("Query executed successfully. Results: {}", results.size());
                    return results;
                }

            } catch (SQLException e) {
                logger.error("Error executing query: {}", query, e);
                e.printStackTrace();
            }
            return null;
        });
    }

    public CompletableFuture<Integer> insertAsync(String query, Object... args) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = getConnection();
                 PreparedStatement statement = connection.prepareStatement(query)) {

                for (int i = 0; i < args.length; i++) {
                    statement.setObject(i + 1, args[i]);
                }

                logger.info("Executing insert: {} with args: {}", query, Arrays.toString(args));

                int affectedRows = statement.executeUpdate();
                logger.info("Insert executed successfully. Rows affected: {}", affectedRows);

                return affectedRows;

            } catch (SQLException e) {
                logger.error("Error executing insert: {}", query, e);
                throw new RuntimeException("Database insert failed", e);
            }
        });
    }

    public CompletableFuture<Optional<Integer>> insertAndGetKeyAsync(String query, Object... args) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = getConnection();
                 PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

                for (int i = 0; i < args.length; i++) {
                    statement.setObject(i + 1, args[i]);
                }

                logger.info("Executing insert with generated key: {} with args: {}", query, Arrays.toString(args));

                statement.executeUpdate();

                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int generatedKey = generatedKeys.getInt(1);
                        logger.info("Insert successful. Generated key: {}", generatedKey);
                        return Optional.of(generatedKey);
                    }
                }

                logger.warn("Insert executed, but no key was generated.");
                return Optional.empty();

            } catch (SQLException e) {
                logger.error("Error executing insert with generated key: {}", query, e);
                throw new RuntimeException("Database insert failed", e);
            }
        });
    }

    public CompletableFuture<Integer> updateAsync(String query, Object... args) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = getConnection();
                 PreparedStatement statement = connection.prepareStatement(query)) {

                for (int i = 0; i < args.length; i++) {
                    statement.setObject(i + 1, args[i]);
                }

                logger.info("Executing update: {} with args: {}", query, Arrays.toString(args));

                int affectedRows = statement.executeUpdate();
                logger.info("Update executed successfully. Rows affected: {}", affectedRows);

                return affectedRows;

            } catch (SQLException e) {
                logger.error("Error executing update: {}", query, e);
                throw new RuntimeException("Database update failed", e);
            }
        });
    }

    public CompletableFuture<Integer> deleteAsync(String query, Object... args) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = getConnection();
                 PreparedStatement statement = connection.prepareStatement(query)) {

                for (int i = 0; i < args.length; i++) {
                    statement.setObject(i + 1, args[i]);
                }

                logger.info("Executing delete: {} with args: {}", query, Arrays.toString(args));

                int affectedRows = statement.executeUpdate();
                logger.info("Delete executed successfully. Rows affected: {}", affectedRows);

                return affectedRows;

            } catch (SQLException e) {
                logger.error("Error executing delete: {}", query, e);
                throw new RuntimeException("Database delete failed", e);
            }
        });
    }

    public interface forum {
        String url = "jdbc:mysql://185.117.3.65/wcf?autoReconnect=true&useSSL=false";
        int port = 3306;

        static Statement getStatement() throws SQLException {
            Connection connection = DriverManager.getConnection(url, user, password);

            return connection.createStatement();
        }
    }

}
