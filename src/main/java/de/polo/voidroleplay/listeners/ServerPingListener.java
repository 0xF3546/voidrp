package de.polo.voidroleplay.listeners;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.FactionData;
import de.polo.voidroleplay.utils.FactionManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;

public class ServerPingListener implements Listener {
    private final FactionManager factionManager;
    public ServerPingListener(FactionManager factionManager) {
        this.factionManager = factionManager;
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }
    @EventHandler
    public void onServerPing(ServerListPingEvent event) {
        String firstline = "§6§lVoidRoleplay §8| §eReallife & Roleplay §8[§e1.16.5§8]";
        String secondline = "§8➥ §cRoleplay mit Stil. §8 × §bⓘ§adiscord.voidroleplay.de!";
        String motd = null;

        try {
            Statement statement = Main.getInstance().mySQL.getStatement();
            ResultSet res = statement.executeQuery("SELECT level, visum, faction FROM players WHERE adress = '" + event.getAddress().toString().replace("/", "") + "'");
            System.out.println("Server Ping erhalten von: " + event.getAddress().toString().replace("/", ""));
            if (res.next()) {
                if (res.getString(3) != null) {
                    FactionData factionData = factionManager.getFactionData(res.getString(3));
                    secondline = "§8 » §6Level§8:§e " + res.getInt(1) + " §8| §6Visum§8: §e" + res.getInt(2) + " §8| §6Fraktion§8: §" + factionData.getPrimaryColor() + factionData.getName();
                } else {
                    secondline = "§8 » §6Level§8:§e " + res.getInt(1) + " §8| §6Visum§8: §e" + res.getInt(2) + " §8| §6Fraktion§8: §7Zivilist";
                }
            } else {
                SQLWarning warning = statement.getWarnings();
                if (warning != null) {
                    System.out.println("Warnung: " + warning.getMessage());
                } else {
                    System.out.println("Keine übereinstimmenden Datensätze gefunden.");
                }
            }
            event.setMotd(firstline + "\n" + secondline);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
