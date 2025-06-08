package de.polo.core.utils;

import de.polo.api.VoidAPI;
import de.polo.core.Main;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.player.services.PlayerService;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class Discord {
    @SneakyThrows
    public static void reloadPlayer(UUID uuid) {
        PlayerService playerService = VoidAPI.getService(PlayerService.class);
        PlayerData playerData = playerService.getPlayerData(uuid);
        System.out.println("RELOADING " + uuid);
        Connection connection = Main.getInstance().coreDatabase.getConnection();
        PreparedStatement statement = connection.prepareStatement("SELECT discordId FROM players WHERE uuid = ?");
        statement.setString(1, uuid.toString());
        ResultSet result = statement.executeQuery();
        if (result.next()) {

            try {
                String jsonInputString = "{\"id\": \"" + result.getString("discordId") + "\"}";
                byte[] postData = jsonInputString.getBytes(StandardCharsets.UTF_8);
                int postDataLength = postData.length;

                URL url = new URL("https://api.voidroleplay.de/discord/reloaduser");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setDoOutput(true);
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json");
                con.setRequestProperty("charset", "utf-8");
                con.setRequestProperty("Content-Length", Integer.toString(postDataLength));
                con.setUseCaches(false);

                try (OutputStream os = con.getOutputStream()) {
                    os.write(postData);
                }

                int responseCode = con.getResponseCode();
                System.out.println("Response Code: " + responseCode);
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    System.out.println("Reloaded user successfully.");
                } else {
                    System.out.println("Failed to reload user.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("PlayerData or discordId is null");
        }
    }

    @SneakyThrows
    public static void unlinkPlayer(UUID uuid) {
        PlayerService playerService = VoidAPI.getService(PlayerService.class);
        PlayerData playerData = playerService.getPlayerData(uuid);
        System.out.println("RELOADING " + uuid);
        Connection connection = Main.getInstance().coreDatabase.getConnection();
        PreparedStatement statement = connection.prepareStatement("SELECT discordId FROM players WHERE uuid = ?");
        statement.setString(1, uuid.toString());
        ResultSet result = statement.executeQuery();
        if (result.next()) {

            try {
                String jsonInputString = "{\"uid\": \"" + result.getString("discordId") + "\"}";
                byte[] postData = jsonInputString.getBytes(StandardCharsets.UTF_8);
                int postDataLength = postData.length;

                URL url = new URL("https://api.voidroleplay.de/discord/unlink");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setDoOutput(true);
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json");
                con.setRequestProperty("charset", "utf-8");
                con.setRequestProperty("Content-Length", Integer.toString(postDataLength));
                con.setUseCaches(false);

                try (OutputStream os = con.getOutputStream()) {
                    os.write(postData);
                }

                int responseCode = con.getResponseCode();
                System.out.println("Response Code: " + responseCode);
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    playerData.setDiscordId(null);
                    PreparedStatement removeal = connection.prepareStatement("UPDATE players SET discordId = NULL WHERE uuid = ?");
                    removeal.setString(1, uuid.toString());
                    removeal.executeUpdate();
                    removeal.close();
                    System.out.println("Unlink user successfully.");
                } else {
                    System.out.println("Failed to reload user.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("PlayerData or discordId is null");
        }
    }

    public static void verifyUser(Player player, String uid) {
        PlayerService playerService = VoidAPI.getService(PlayerService.class);
        PlayerData playerData = playerService.getPlayerData(player.getUniqueId());
        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {
            String token = String.valueOf(Utils.random(43258, 213478234));
            try {
                Connection connection = Main.getInstance().getCoreDatabase().getConnection();
                PreparedStatement statement = connection.prepareStatement("UPDATE players SET syncToken = ? WHERE uuid = ?");
                statement.setString(1, token);
                statement.setString(2, player.getUniqueId().toString());
                statement.executeUpdate();
                statement.close();
                connection.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            try {
                String jsonInputString = "{\"id\": \"" + uid + "\", \"token\": \"" + token + "\"}";
                byte[] postData = jsonInputString.getBytes(StandardCharsets.UTF_8);
                int postDataLength = postData.length;

                URL url = new URL("https://api.voidroleplay.de/discord/verify");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setDoOutput(true);
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json");
                con.setRequestProperty("charset", "utf-8");
                con.setRequestProperty("Content-Length", Integer.toString(postDataLength));
                con.setUseCaches(false);

                try (OutputStream os = con.getOutputStream()) {
                    os.write(postData);
                }

                int responseCode = con.getResponseCode();
                System.out.println("Response Code: " + responseCode);
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    System.out.println("Reloaded user successfully.");
                } else {
                    System.out.println("Failed to reload user.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
