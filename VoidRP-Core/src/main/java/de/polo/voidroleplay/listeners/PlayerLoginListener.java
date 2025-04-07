package de.polo.voidroleplay.listeners;

import de.polo.voidroleplay.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class PlayerLoginListener implements Listener {
    public PlayerLoginListener() {
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        try {
            Statement statement = Main.getInstance().coreDatabase.getStatement();
            ResultSet res = statement.executeQuery("SELECT *, DATE_FORMAT(date, '%d.%m.%Y | %H:%i:%s') AS formatted_timestamp FROM player_bans WHERE uuid = '" + player.getUniqueId() + "'");
            if (res.next()) {
                boolean isPerm = res.getBoolean("isPermanent");
                java.util.Date utilDate = new java.util.Date(res.getDate(6).getTime());

                LocalDateTime localDateTime = utilDate.toInstant().atZone(ZoneId.of("Europe/Berlin")).toLocalDateTime();

                LocalDateTime date = localDateTime.atZone(ZoneId.of("Europe/Berlin")).toLocalDateTime();
                if (date.isBefore(LocalDateTime.now()) && !isPerm) {
                    event.setResult(PlayerLoginEvent.Result.ALLOWED);
                    statement.execute("DELETE FROM player_bans WHERE uuid = '" + player.getUniqueId() + "'");
                    return;
                }
                event.setResult(PlayerLoginEvent.Result.KICK_BANNED);
                if (!isPerm) {
                    event.setKickMessage("§8 • §6VoidRoleplay§8 •\n\n§cDu bist vom Server gesperrt.\nGrund: " + res.getString(4) + "\nGebannt durch: " + res.getString(5) + "\nBan läuft ab: " + res.getString("formatted_timestamp") + "\n\n§8 • §6VoidRoleplay§8 •");
                } else {
                    event.setKickMessage("§8 • §6VoidRoleplay§8 •\n\n§cDu bist Permanent vom Server gesperrt.\nGrund: " + res.getString(4) + "\nGebannt durch: " + res.getString(5) + "\n\n§8 • §6VoidRoleplay§8 •");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
