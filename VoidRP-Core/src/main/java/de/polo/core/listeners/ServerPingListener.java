package de.polo.core.listeners;

import de.polo.core.Main;
import de.polo.core.faction.entity.Faction;
import de.polo.core.utils.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

import java.sql.*;

import static de.polo.core.Main.factionManager;

@Event
public class ServerPingListener implements Listener {

    @EventHandler
    public void onServerPing(ServerListPingEvent event) {
        String firstline = "§6§lVoidRoleplay V2 §8| §eReallife & Roleplay";
        String secondline = "§8➥ §cRoleplay mit Stil. §8 × §bⓘ §adiscord.gg/void-roleplay";

        String sql = "SELECT level, visum, faction FROM players WHERE adress = ?";

        try (Connection connection = Main.getInstance().coreDatabase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            String cleanAddress = event.getAddress().toString().replace("/", "");
            statement.setString(1, cleanAddress);

            try (ResultSet res = statement.executeQuery()) {
                if (res.next()) {
                    int level = res.getInt("level");
                    int visum = res.getInt("visum");
                    String factionName = res.getString("faction");

                    if (factionName != null) {
                        Faction factionData = factionManager.getFactionData(factionName);
                        String factionDisplay = factionData != null
                                ? "§" + factionData.getPrimaryColor() + factionData.getName()
                                : "§7Unbekannt";

                        secondline = "§8 » §6Level§8:§e " + level + " §8| §6Visum§8: §e" + visum + " §8| §6Fraktion§8: " + factionDisplay;
                    } else {
                        secondline = "§8 » §6Level§8:§e " + level + " §8| §6Visum§8: §e" + visum + " §8| §6Fraktion§8: §7Zivilist";
                    }
                }
            }

            event.setMotd(firstline + "\n" + secondline);

        } catch (SQLException e) {
            e.printStackTrace();
            event.setMotd(firstline + "\n§cFehler beim Laden der Spielerinformationen.");
        }
    }
}