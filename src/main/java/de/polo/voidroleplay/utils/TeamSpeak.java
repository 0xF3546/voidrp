package de.polo.voidroleplay.utils;


import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.manager.FactionManager;
import de.polo.voidroleplay.manager.PlayerManager;
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

public class TeamSpeak {
    static int GastChannel = 9;
    private static TeamSpeak teamSpeak;
    private final Utils utils;
    private final PlayerManager playerManager;
    private final FactionManager factionManager;

    public TeamSpeak(PlayerManager playerManager, FactionManager factionManager, Utils utils) {
        this.utils = utils;
        this.playerManager = playerManager;
        this.factionManager = factionManager;

    }

    @SneakyThrows
    public static void reloadPlayer(UUID uuid) {
        PlayerData playerData = Main.getInstance().playerManager.getPlayerData(uuid);
        System.out.println("RELOADING " + uuid);
        Connection connection = Main.getInstance().mySQL.getConnection();
        PreparedStatement statement = connection.prepareStatement("SELECT teamSpeakUID FROM players WHERE uuid = ?");
        statement.setString(1, uuid.toString());
        ResultSet result = statement.executeQuery();
        if (result.next()) {

            try {
                String jsonInputString = "{\"uid\": \"" + result.getString("teamSpeakUID") + "\"}";
                byte[] postData = jsonInputString.getBytes(StandardCharsets.UTF_8);
                int postDataLength = postData.length;

                URL url = new URL("https://api.voidroleplay.de/teamspeak/reloaduser");
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
            System.out.println("PlayerData or TeamSpeakUID is null");
        }
    }

    @SneakyThrows
    public static void unlinkPlayer(UUID uuid) {
        PlayerData playerData = Main.getInstance().playerManager.getPlayerData(uuid);
        System.out.println("RELOADING " + uuid);
        Connection connection = Main.getInstance().mySQL.getConnection();
        PreparedStatement statement = connection.prepareStatement("SELECT teamSpeakUID FROM players WHERE uuid = ?");
        statement.setString(1, uuid.toString());
        ResultSet result = statement.executeQuery();
        if (result.next()) {

            try {
                String jsonInputString = "{\"uid\": \"" + result.getString("teamSpeakUID") + "\"}";
                byte[] postData = jsonInputString.getBytes(StandardCharsets.UTF_8);
                int postDataLength = postData.length;

                URL url = new URL("https://api.voidroleplay.de/teamspeak/unlink");
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
                    playerData.setTeamSpeakUID(null);
                    PreparedStatement removeal = connection.prepareStatement("UPDATE players SET teamSpeakUID = NULL WHERE uuid = ?");
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
            System.out.println("PlayerData or TeamSpeakUID is null");
        }
    }

    public static void verifyUser(Player player, String uid) {
        PlayerData playerData = Main.getInstance().playerManager.getPlayerData(player.getUniqueId());
        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {
            try {
                Connection connection = Main.getInstance().getMySQL().getConnection();
                PreparedStatement statement = connection.prepareStatement("UPDATE players SET tsToken = ?");
                statement.setString(1, String.valueOf(Main.random(43258, 213478234)));
                statement.executeQuery();
                statement.close();
                connection.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            try {
                String jsonInputString = "{\"uid\": \"" + uid + "\"}";
                byte[] postData = jsonInputString.getBytes(StandardCharsets.UTF_8);
                int postDataLength = postData.length;

                URL url = new URL("https://api.voidroleplay.de/teamspeak/verify");
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

    public static TeamSpeak getTeamSpeak() {
        return teamSpeak;
    }

}
