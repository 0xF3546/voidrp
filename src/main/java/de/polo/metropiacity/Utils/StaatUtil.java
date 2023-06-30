package de.polo.metropiacity.Utils;

import de.polo.metropiacity.DataStorage.JailData;
import de.polo.metropiacity.DataStorage.ServiceData;
import de.polo.metropiacity.MySQl.MySQL;
import de.polo.metropiacity.DataStorage.PlayerData;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class StaatUtil {
    public static Map<String, JailData> jailDataMap = new HashMap<>();
    public static Map<String, ServiceData> serviceDataMap = new HashMap<>();

    public static void loadJail() throws SQLException {
        Statement statement = MySQL.getStatement();
        ResultSet result = statement.executeQuery("SELECT * FROM `Jail`");
        while (result.next()) {
            JailData jailData = new JailData();
            jailData.setId(result.getInt(1));
            jailData.setUuid(result.getString(2));
            jailData.setHafteinheiten(result.getInt(3));
            jailData.setReason(result.getString(4));
            jailDataMap.put(result.getString(2), jailData);
        }
    }
    public static boolean arrestPlayer(Player player, Player arrester) throws SQLException {
        Statement statement = MySQL.getStatement();
        ResultSet result = statement.executeQuery("SELECT `hafteinheiten`, `akte`, `geldstrafe` FROM `player_akten` WHERE `uuid` = '" + player.getUniqueId().toString() + "'");
        int hafteinheiten = 0;
        int geldstrafe = 0;
        StringBuilder reason = new StringBuilder();
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        PlayerData arresterData = PlayerManager.playerDataMap.get(arrester.getUniqueId().toString());
        while (result.next()) {
            hafteinheiten += result.getInt(1);
            assert false;
            reason.append(result.getString(2)).append(", ");
            geldstrafe += result.getInt(3);
        }
        if (hafteinheiten > 0) {
            JailData jailData = new JailData();
            LocationManager.useLocation(player, "gefaengnis");
            player.sendMessage("§8[§cGefängnis§8] §7Du wurdest für §6" + hafteinheiten + " Hafteinheiten§7 inhaftiert.");
            player.sendMessage("§8[§cGefängnis§8] §7Tatvorwürfe§8:§7 " + reason.substring(0, reason.length() - 2) + ".");
            playerData.setJailed(true);
            playerData.setHafteinheiten(hafteinheiten);
            FactionManager.addFactionMoney(arresterData.getFaction(), ServerManager.getPayout("arrest"), "Inhaftierung von " + player.getName() + ", durch " + arrester.getName());
            if (geldstrafe > 0) {
                if (playerData.getBank() >= geldstrafe) {
                    PlayerManager.removeBankMoney(player, geldstrafe, "Gefängnis Geldstrafe");
                    player.sendMessage("§8[§cGefängnis§8] §7Strafzahlung§8:§7 " + geldstrafe + "$.");
                } else if (playerData.getBank() > 0) {
                    PlayerManager.removeBankMoney(player, playerData.getBank(), "Gefängnis Geldstrafe");
                    player.sendMessage("§8[§cGefängnis§8] §7Strafzahlung§8:§7 " + geldstrafe + "$.");
                }
            }
            statement.execute("DELETE FROM `player_akten` WHERE `uuid` = '" + player.getUniqueId().toString() + "'");
            for (Player players : Bukkit.getOnlinePlayers()) {
                PlayerData playerData1 = PlayerManager.playerDataMap.get(players.getUniqueId().toString());
                if (Objects.equals(playerData1.getFaction(), "FBI") || Objects.equals(playerData1.getFaction(), "Polizei")) {
                    players.sendMessage("§8[§cGefängnis§8] §7" + FactionManager.getTitle(arrester) + " " + arrester.getName() + " hat " + player.getName() + " in das Gefängnis inhaftiert.");
                }
            }
            statement.execute("INSERT INTO `Jail` (`uuid`, `hafteinheiten`, `reason`, `hafteinheiten_verbleibend`) VALUES ('" + player.getUniqueId().toString() + "', " + hafteinheiten + ", '" + reason + "', " + hafteinheiten + ")");
            jailData.setUuid(player.getUniqueId().toString());
            jailData.setHafteinheiten(hafteinheiten);
            jailData.setReason(String.valueOf(reason));
            jailDataMap.put(player.getUniqueId().toString(), jailData);
            return true;
        } else {
            return false;
        }
    }

    public static void unarrestPlayer(Player player) throws SQLException {
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        playerData.setJailed(false);
        playerData.setHafteinheiten(0);
        jailDataMap.remove(player.getUniqueId().toString());
        Statement statement = MySQL.getStatement();
        LocationManager.useLocation(player, "gefaengnis_out");
        player.sendMessage("§8[§cGefängnis§8] §7Du wurdest entlassen.");
        statement.execute("DELETE FROM `Jail` WHERE `uuid` = '" + player.getUniqueId().toString() + "'");
    }

    public static void addAkteToPlayer(Player vergeber, Player player, int hafteinheiten, String akte, int geldstrafe) throws SQLException {
        Statement statement = MySQL.getStatement();
        statement.execute("INSERT INTO `player_akten` (`uuid`, `hafteinheiten`, `akte`, `geldstrafe`, `vergebendurch`) VALUES ('" + player.getUniqueId().toString() + "', " + hafteinheiten + ", '" + akte + "', " + geldstrafe + ", '" + vergeber.getName() + "')");
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        for (Player players : Bukkit.getOnlinePlayers()) {
            PlayerData playerData1 = PlayerManager.playerDataMap.get(players.getUniqueId().toString());
            if (Objects.equals(playerData1.getFaction(), "FBI") || Objects.equals(playerData1.getFaction(), "Polizei")) {
                players.sendMessage("§8[§9Zentrale§8]§7 " + FactionManager.getTitle(vergeber) + " " + vergeber.getName() + " hat " + player.getName() + " eine Akte hinzugefügt.");
            }
        }
    }

    public static boolean removeAkteFromPlayer(Player player, int id) throws SQLException {
        Statement statement = MySQL.getStatement();
        statement.execute("DELETE FROM `player_akten` WHERE `id` = " + id);
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        for (Player players : Bukkit.getOnlinePlayers()) {
            PlayerData playerData1 = PlayerManager.playerDataMap.get(players.getUniqueId().toString());
            if (Objects.equals(playerData1.getFaction(), "FBI") || Objects.equals(playerData1.getFaction(), "Polizei")) {
                players.sendMessage("§8[§9Zentrale§8]§7 " + FactionManager.getTitle(player) + " " + player.getName() + " hat " + player.getName() + " eine Akte entfernt.");
            }
        }
        return true;
    }

    public static void createService(Player player, int service, String reason) {
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        playerData.setIntVariable("service", service);
        ServiceData serviceData = new ServiceData();
        serviceData.setLocation(player.getLocation());
        serviceData.setNumber(service);
        serviceData.setReason(reason);
        serviceData.setUuid(player.getUniqueId().toString());
        serviceDataMap.put(player.getUniqueId().toString(), serviceData);
        player.sendMessage("§8[§6Notruf§8]§e Du hast einen Notruf abgesetzt.");
        if (service == 110) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (PlayerManager.playerDataMap.get(p.getUniqueId().toString()).getFaction().equals("Polizei")) {
                    p.sendMessage("§8[§9Zentrale§8]§3 " + player.getName() + " hat ein Notruf abgesendet: " + reason);
                    TextComponent message = new TextComponent("§8 ➥ §bAnnehmen [" + (int) p.getLocation().distance(player.getLocation()) + "m]");
                    message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§3§oNotruf annehmen")));
                    message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/acceptservice " + player.getName()));
                    p.spigot().sendMessage(message);
                }
            }
        }
        if (service == 112) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (PlayerManager.playerDataMap.get(p.getUniqueId().toString()).getFaction().equals("Medic")) {
                    p.sendMessage("§8[§9Zentrale§8]§3 " + player.getName() + " hat ein Notruf abgesendet: " + reason);
                    TextComponent message = new TextComponent("§8 ➥ §bAnnehmen [" + (int) p.getLocation().distance(player.getLocation()) + "m]");
                    message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§3§oNotruf annehmen")));
                    message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/acceptservice " + player.getName()));
                    p.spigot().sendMessage(message);
                }
            }
        }
    }

    public static void cancelservice(Player player) {
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        ServiceData serviceData = serviceDataMap.get(player.getUniqueId().toString());
        playerData.setVariable("service", null);
        player.sendMessage("§8[§6Notruf§8]§e Du hast deinen Notruf abgebrochen.");
        if (serviceData.getAcceptedByUuid() != null) {
            Player accepter = Bukkit.getPlayer(UUID.fromString(serviceData.getAcceptedByUuid()));
            assert accepter != null;
            accepter.sendMessage("§8[§6Notruf§8]§e " + player.getName() + " hat seinen Notruf abgebrochen.");
        }
        StaatUtil.serviceDataMap.remove(player.getUniqueId().toString());
    }
}
