package de.polo.metropiacity.listeners;

import de.polo.metropiacity.Main;
import de.polo.metropiacity.database.MySQL;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;
import java.util.UUID;

public class PlayerLoginListener implements Listener {
    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        try {
            Statement statement = Main.getInstance().mySQL.getStatement();
            ResultSet res = statement.executeQuery("SELECT *, DATE_FORMAT(date, '%d.%m.%Y | %H:%i:%s') AS formatted_timestamp FROM player_bans WHERE uuid = '" + player.getUniqueId() + "'");
            if (res.next()) {
                java.util.Date utilDate = new java.util.Date(res.getDate(6).getTime());

                LocalDateTime localDateTime = utilDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

                LocalDateTime date = localDateTime.atZone(ZoneId.systemDefault()).toLocalDateTime();
                if (date.isBefore(LocalDateTime.now())) {
                    event.setResult(PlayerLoginEvent.Result.ALLOWED);
                    statement.execute("DELETE FROM player_bans WHERE uuid = '" + player.getUniqueId() + "'");
                    return;
                }
                event.setResult(PlayerLoginEvent.Result.KICK_BANNED);
                event.setKickMessage("§8 • §6MetropiaCity§8 •\n\n§cDu bist vom Server gesperrt.\nGrund: " + res.getString(4) + "\nGebannt durch: " + res.getString(5) + "\nBan läuft ab: " + res.getString("formatted_timestamp")+ "\n\n§8 • §6MetropiaCity§8 •");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
