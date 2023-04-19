package de.polo.void_roleplay.Utils;

import de.polo.void_roleplay.MySQl.MySQL;
import de.polo.void_roleplay.DataStorage.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

public class StaatUtil {
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
            LocationManager.useLocation(player, "gefaengnis");
            player.sendMessage("§cGefängnis §8» §7Du wurdest für §6" + hafteinheiten + " Hafteinheiten§7 inhaftiert.");
            player.sendMessage("§cGefängnis §8» §7Tatvorwürfe§8:§7 " + reason + ".");
            playerData.setJailed(true);
            playerData.setHafteinheiten(hafteinheiten);
            FactionManager.addFactionMoney(arresterData.getFaction(), 75, "Inhaftierung von " + player.getName() + ", durch " + arrester.getName());
            for (Player players : Bukkit.getOnlinePlayers()) {
                PlayerData playerData1 = PlayerManager.playerDataMap.get(players.getUniqueId().toString());
                if (Objects.equals(playerData1.getFaction(), "FBI") || Objects.equals(playerData1.getFaction(), "Polizei")) {
                    players.sendMessage("§cGefängnis §8» §7 " + FactionManager.getTitle(arrester) + " " + arrester.getName() + " hat " + player.getName() + " in das Gefängnis inhaftiert.");
                }
            }
            if (geldstrafe > 0) {
                if (playerData.getBank() >= geldstrafe) {
                    PlayerManager.removeBankMoney(player, geldstrafe, "Gefängnis Geldstrafe");
                    player.sendMessage("§cGefängnis §8» §7Strafzahlung§8:§7 " + geldstrafe + "$.");
                } else if (playerData.getBank() > 0) {
                    PlayerManager.removeBankMoney(player, playerData.getBank(), "Gefängnis Geldstrafe");
                    player.sendMessage("§cGefängnis §8» §7Strafzahlung§8:§7 " + geldstrafe + "$.");
                }
            }
            statement.execute("DELETE FROM `player_akten` WHERE `uuid` = '" + player.getUniqueId().toString() + "'");
            statement.execute("INSERT INTO `Jail` (`uuid`, `hafteinheiten`, `reason`, `hafteinheiten_verbleibend`) VALUES ('" + player.getUniqueId().toString() + "', " + hafteinheiten + ", '" + reason + "', " + hafteinheiten + ")");
            return true;
        } else {
            return false;
        }
    }

    public static void unarrestPlayer(Player player) throws SQLException {
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        playerData.setJailed(false);
        playerData.setHafteinheiten(0);
        Statement statement = MySQL.getStatement();
        LocationManager.useLocation(player, "gefaengnis_out");
        player.sendMessage("§cGefängnis §8» §7Du wurdest entlassen.");
        statement.execute("DELETE FROM `Jail` WHERE `uuid` = '" + player.getUniqueId().toString() + "'");
    }

    public static void addAkteToPlayer(Player vergeber, Player player, int hafteinheiten, String akte, int geldstrafe) throws SQLException {
        Statement statement = MySQL.getStatement();
        statement.execute("INSERT INTO `player_akten` (`uuid`, `hafteinheiten`, `akte`, `geldstrafe`, `vergebendurch`) VALUES ('" + player.getUniqueId().toString() + "', " + hafteinheiten + ", '" + akte + "', " + geldstrafe + ", '" + vergeber.getName() + "')");
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        for (Player players : Bukkit.getOnlinePlayers()) {
            PlayerData playerData1 = PlayerManager.playerDataMap.get(players.getUniqueId().toString());
            if (Objects.equals(playerData1.getFaction(), "FBI") || Objects.equals(playerData1.getFaction(), "Polizei")) {
                players.sendMessage("§9Zentrale §8» §7 " + FactionManager.getTitle(player) + " " + player.getName() + " hat " + player.getName() + " eine Akte hinzugefügt.");
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
                players.sendMessage("§9Zentrale §8» §7 " + FactionManager.getTitle(player) + " " + player.getName() + " hat " + player.getName() + " eine Akte entfernt.");
            }
        }
        return true;
    }
}
