package de.polo.core.listeners;

import de.polo.core.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class PlayerLoginListener implements Listener {
    public PlayerLoginListener() {
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        String sql = "SELECT *, DATE_FORMAT(date, '%d.%m.%Y | %H:%i:%s') AS formatted_timestamp FROM player_bans WHERE uuid = ?";

        try (Connection connection = Main.getInstance().coreDatabase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, player.getUniqueId().toString());

            try (ResultSet res = statement.executeQuery()) {
                if (res.next()) {
                    boolean isPerm = res.getBoolean("isPermanent");
                    Timestamp banUntilTimestamp = res.getTimestamp("date");
                    LocalDateTime banUntil = banUntilTimestamp.toLocalDateTime();

                    if (!isPerm && banUntil.isBefore(LocalDateTime.now())) {
                        event.setResult(PlayerLoginEvent.Result.ALLOWED);

                        try (PreparedStatement deleteStmt = connection.prepareStatement("DELETE FROM player_bans WHERE uuid = ?")) {
                            deleteStmt.setString(1, player.getUniqueId().toString());
                            deleteStmt.executeUpdate();
                        }
                        return;
                    }

                    event.setResult(PlayerLoginEvent.Result.KICK_BANNED);

                    String reason = res.getString("reason");
                    String banner = res.getString("punisher");
                    String formattedDate = res.getString("formatted_timestamp");

                    String kickMessage;
                    if (isPerm) {
                        kickMessage = "§8 • §6VoidRoleplay§8 •\n\n§cDu bist Permanent vom Server gesperrt.\n" +
                                "Grund: " + reason + "\n" +
                                "Gebannt durch: " + banner + "\n\n§8 • §6VoidRoleplay§8 •";
                    } else {
                        kickMessage = "§8 • §6VoidRoleplay§8 •\n\n§cDu bist vom Server gesperrt.\n" +
                                "Grund: " + reason + "\n" +
                                "Gebannt durch: " + banner + "\n" +
                                "Ban läuft ab: " + formattedDate + "\n\n§8 • §6VoidRoleplay§8 •";
                    }

                    event.setKickMessage(kickMessage);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
            event.setKickMessage("§cFehler beim Überprüfen des Ban-Status.");
        }
    }
}
