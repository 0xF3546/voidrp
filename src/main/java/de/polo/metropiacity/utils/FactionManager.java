package de.polo.metropiacity.utils;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;
import de.polo.metropiacity.dataStorage.*;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.database.MySQL;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class FactionManager {
    public static final Map<String, FactionData> factionDataMap = new HashMap<>();
    public static final Map<String, FactionGradeData> factionGradeDataMap = new HashMap<>();
    public static final Map<Integer, BlacklistData> blacklistDataMap = new HashMap<>();

    public static Object[][] faction_grades;
    public static void loadFactions() throws SQLException {
        Statement statement = MySQL.getStatement();
        ResultSet locs = statement.executeQuery("SELECT * FROM factions");
        while (locs.next()) {
            FactionData factionData = new FactionData();
            factionData.setId(locs.getInt(1));
            factionData.setName(locs.getString(2));
            factionData.setFullname(locs.getString(3));
            factionData.setPrimaryColor(locs.getString(4));
            factionData.setSecondaryColor(locs.getString(5));
            factionData.setBank(locs.getInt(6));
            factionData.setMaxMember(locs.getInt(7));
            factionData.setTeamSpeakID(locs.getInt(8));
            factionData.setChannelGroupID(locs.getInt(9));
            factionData.setHasBlacklist(locs.getBoolean(10));
            factionData.setDoGangwar(locs.getBoolean(11));
            factionData.setForumID(locs.getInt("forumID"));
            factionData.setForumID_Leader(locs.getInt("forumID_Leader"));
            factionDataMap.put(locs.getString(2), factionData);
        }

        ResultSet grades = statement.executeQuery("SELECT * FROM faction_grades");
        while (grades.next()) {
            FactionGradeData factionGradeData = new FactionGradeData();
            factionGradeData.setId(grades.getInt(1));
            factionGradeData.setFaction(grades.getString(2));
            factionGradeData.setGrade(grades.getInt(3));
            factionGradeData.setName(grades.getString(4));
            factionGradeData.setPayday(grades.getInt(5));
            factionGradeDataMap.put(grades.getString(2) + "_" + grades.getInt(3), factionGradeData);
        }

        ResultSet blacklist = statement.executeQuery("SELECT * FROM `blacklist`");
        while (blacklist.next()) {
            BlacklistData blacklistData = new BlacklistData();
            blacklistData.setId(blacklist.getInt(1));
            blacklistData.setUuid(blacklist.getString(2));
            blacklistData.setFaction(blacklist.getString(3));
            blacklistData.setReason(blacklist.getString(4));
            blacklistData.setKills(blacklist.getInt(5));
            blacklistData.setPrice(blacklist.getInt(6));
            blacklistData.setDate(blacklist.getString(7));
            blacklistDataMap.put(blacklist.getInt(1), blacklistData);
        }
    }

    public static String faction(Player player) {
        String uuid = player.getUniqueId().toString();
        PlayerData playerData = PlayerManager.playerDataMap.get(uuid);
        return playerData.getFaction();
    }

    public static Integer faction_grade(Player player) {
        String uuid = player.getUniqueId().toString();
        PlayerData playerData = PlayerManager.playerDataMap.get(uuid);
        return playerData.getFactionGrade();
    }
    public static void setPlayerInFrak(Player player, String frak, Integer rang) throws SQLException {
        String uuid = player.getUniqueId().toString();
        PlayerData playerData = PlayerManager.playerDataMap.get(uuid);
        playerData.setFaction(frak);
        playerData.setFactionGrade(rang);
        Statement statement = MySQL.getStatement();
        assert statement != null;
        statement.executeUpdate("UPDATE `players` SET `faction` = '" + frak + "', `faction_grade` = " + rang + " WHERE `uuid` = '" + uuid + "'");
        boolean found = false;
        for (FactionPlayerData factionPlayerData : ServerManager.factionPlayerDataMap.values()) {
            if (factionPlayerData.getUuid().equals(player.getUniqueId().toString())) {
                found = true;
                factionPlayerData.setFaction(frak);
                factionPlayerData.setFaction_grade(rang);
            }
        }
        if (!found) {
            FactionPlayerData factionPlayerData = new FactionPlayerData();
            factionPlayerData.setFaction_grade(rang);
            factionPlayerData.setUuid(player.getUniqueId().toString());
            factionPlayerData.setFaction(frak);
            factionPlayerData.setId(playerData.getId());
            ServerManager.factionPlayerDataMap.put(player.getUniqueId().toString(), factionPlayerData);
        }
        if (playerData.getTeamSpeakUID() != null) {
            Client client = TeamSpeak.getTeamSpeak().getAPI().getClientByUId(playerData.getTeamSpeakUID());
            TeamSpeak.getTeamSpeak().updateClientGroup(player, client);
        }
    }

    public static void removePlayerFromFrak(Player player) throws SQLException {
        String uuid = player.getUniqueId().toString();
        PlayerData playerData = PlayerManager.playerDataMap.get(uuid);
        playerData.setFaction(null);
        playerData.setFactionGrade(0);
        playerData.setDuty(false);
        if (playerData.getPermlevel() >= 60) {
            player.setDisplayName("§8[§7Team§8]§7 " + player.getName());
            player.setPlayerListName("§8[§7Team§8]§7 " + player.getName());
            player.setCustomName("§8[§7Team§8]§7 " + player.getName());
            player.setCustomNameVisible(true);
        } else {
            player.setDisplayName("§7" + player.getName());
            player.setPlayerListName("§7" + player.getName());
            player.setCustomName("§7" + player.getName());
            player.setCustomNameVisible(true);
        }
        Statement statement = MySQL.getStatement();
        assert statement != null;
        statement.executeUpdate("UPDATE `players` SET `faction` = NULL, `faction_grade` = 0, `isDuty` = false WHERE `uuid` = '" + uuid + "'");
        ServerManager.factionPlayerDataMap.remove(player.getUniqueId().toString());
        if (playerData.getTeamSpeakUID() != null) {
            Client client = TeamSpeak.getTeamSpeak().getAPI().getClientByUId(playerData.getTeamSpeakUID());
            TeamSpeak.getTeamSpeak().updateClientGroup(player, client);
        }
    }

    public static void removeOfflinePlayerFromFrak(OfflinePlayer player) throws SQLException {
        Statement statement = MySQL.getStatement();
        assert statement != null;
        ServerManager.factionPlayerDataMap.remove(player.getUniqueId().toString());
        statement.executeUpdate("UPDATE `players` SET `faction` = NULL, `faction_grade` = 0, `isDuty` = false WHERE `uuid` = '" + player.getUniqueId() + "'");
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
    }

    public static String faction_offlinePlayer(String playername) {
        String val = null;
        for (DBPlayerData dbPlayerData : ServerManager.dbPlayerDataMap.values()) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(dbPlayerData.getUuid()));
            if (player.getName().equalsIgnoreCase(playername)) {
                val = dbPlayerData.getFaction();
            }
        }
        return val;
    }

    public static Integer faction_grade_offlinePlayer(String playername) {
        int val = 0;
        for (DBPlayerData dbPlayerData : ServerManager.dbPlayerDataMap.values()) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(dbPlayerData.getUuid()));
            if (player.getName().equalsIgnoreCase(playername)) {
                val = dbPlayerData.getFaction_grade();
            }
        }
        return val;
    }

    public static String getFactionPrimaryColor(String faction) {
        FactionData factionData = factionDataMap.get(faction);
        return factionData.getPrimaryColor();
    }

    public static String getFactionSecondaryColor(String faction) {
        FactionData factionData = factionDataMap.get(faction);
        return factionData.getSecondaryColor();
    }

    public static String getFactionFullname(String faction) {
        FactionData factionData = factionDataMap.get(faction);
        return factionData.getFullname();
    }

    public static String getPlayerFactionRankName(Player p) {
        FactionGradeData factionGradeData = factionGradeDataMap.get(faction(p) + "_" + faction_grade(p));
        return factionGradeData.getName();
    }
    public static String getRankName(String faction, int rang) {
        FactionGradeData factionGradeData = factionGradeDataMap.get(faction + "_" + rang);
        return factionGradeData.getName();
    }
    public static Integer getPaydayFromFaction(String faction, Integer rank) {
        FactionGradeData factionGradeData = factionGradeDataMap.get(faction + "_" + rank);
        return factionGradeData.getPayday();
    }
    public static boolean isPlayerInGoodFaction(Player player) {
        return faction(player) == "FBI" || faction(player) == "Polizei" || faction(player) == "Medics";
    }

    public static Integer factionBank(String faction) {
        FactionData factionData = factionDataMap.get(faction);
        return factionData.getBank();
    }

    public static void addFactionMoney(String faction, Integer amount, String reason) throws SQLException {
        FactionData factionData = factionDataMap.get(faction);
        factionData.setBank(factionData.getBank() + amount);
        Statement statement = MySQL.getStatement();
        statement.execute("INSERT INTO `faction_bank_logs` (`type`, `faction`, `amount`, `reason`, `isPlus`) VALUES ('einzahlung', '" + faction + "', " + amount + ", '" + reason + "', true)");
        statement.execute("UPDATE `factions` SET `bank` = " + factionData.getBank() + " WHERE `name` = '" + faction + "'");
    }
    public static boolean removeFactionMoney(String faction, Integer amount, String reason) throws SQLException {
        boolean returnval = false;
        FactionData factionData = factionDataMap.get(faction);
        if (factionData.getBank() >= amount) {
            factionData.setBank(factionData.getBank() - amount);
            Statement statement = MySQL.getStatement();
            statement.execute("INSERT INTO `faction_bank_logs` (`type`, `faction`, `amount`, `reason`, `isPlus`) VALUES ('auszahlung', '" + faction + "', " + amount + ", '" + reason + "', false)");
            statement.execute("UPDATE `factions` SET `bank` = " + factionData.getBank() + " WHERE `name` = '" + faction + "'");
            returnval = true;
        }
        return returnval;
    }
    public static void sendMessageToFaction(String faction, String message) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (Objects.equals(faction(player), faction)) {
                player.sendMessage("§8[§" + getFactionPrimaryColor(faction) + faction + "§8]§" + getFactionSecondaryColor(faction) + " " + message);
            }
        }
    }
    public static boolean changeRankPayDay(String faction, int rank, int payday) throws SQLException {
        FactionGradeData factionGradeData = factionGradeDataMap.get(faction + "_" + rank);
        if (factionGradeData != null) {
            factionGradeData.setPayday(payday);
            Statement statement = MySQL.getStatement();
            statement.executeUpdate("UPDATE `faction_grades` SET `payday` = " + payday + " WHERE `faction` = '" + faction + "' AND `grade` = " + rank);
            return true;
        } else {
            return false;
        }
    }

    public static boolean changeRankName(String faction, int rank, String name) throws SQLException {
        FactionGradeData factionGradeData = factionGradeDataMap.get(faction + "_" + rank);
        if (factionGradeData != null) {
            factionGradeData.setName(name);
            Statement statement = MySQL.getStatement();
            statement.executeUpdate("UPDATE `faction_grades` SET `name` = '" + name + "' WHERE `faction` = '" + faction + "' AND `grade` = " + rank);
            return true;
        } else {
            return false;
        }
    }
    public static String getTitle(Player player) {
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        switch (playerData.getFaction()) {
            case "FBI":
                return "Agent";
            case "Polizei":
                return "Officer";
            case "Medic":
                return "Arzt";
        }
        return null;
    }

    public static void setDuty(Player player, boolean state) {
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        FactionData factionData = FactionManager.factionDataMap.get(playerData.getFaction());
        try {
            Statement statement = MySQL.getStatement();
            if (state) {
                statement.executeUpdate("UPDATE `players` SET `isDuty` = true WHERE `uuid` = '" + player.getUniqueId() + "'");
                if (playerData.getPermlevel() >= 60) {
                    player.setDisplayName("§" + factionData.getPrimaryColor() + "[Team] " + player.getName());
                    player.setPlayerListName("§" + factionData.getPrimaryColor() + "[Team] " + player.getName());
                    player.setCustomName("§" + factionData.getPrimaryColor() + "[Team] " + player.getName());
                    player.setCustomNameVisible(true);
                } else {
                    player.setDisplayName("§" + factionData.getPrimaryColor() + player.getName());
                    player.setPlayerListName("§" + factionData.getPrimaryColor() + player.getName());
                    player.setCustomName("§" + factionData.getPrimaryColor() + player.getName());
                    player.setCustomNameVisible(true);
                }
                playerData.setDuty(true);
            } else {
                statement.executeUpdate("UPDATE `players` SET `isDuty` = false WHERE `uuid` = '" + player.getUniqueId() + "'");
                if (playerData.getPermlevel() >= 60) {
                    player.setDisplayName("§8[§7Team§8]§7 " + player.getName());
                    player.setPlayerListName("§8[§7Team§8]§7 " + player.getName());
                    player.setCustomName("§8[§7Team§8]§7 " + player.getName());
                    player.setCustomNameVisible(true);
                } else {
                    player.setDisplayName("§7" + player.getName());
                    player.setPlayerListName("§7" + player.getName());
                    player.setCustomName("§7" + player.getName());
                    player.setCustomNameVisible(true);
                }
                playerData.setDuty(false);
            }
        } catch (SQLException e) {
            player.sendMessage(Main.error + "Fehler.");
            throw new RuntimeException(e);
        }
    }

    public static boolean isInBündnis(Player player) {
        return false;
    }

    public static boolean isInBündnisWith(Player player, String faction) {
        return false;
    }

    public static int getMemberCount(String faction) {
        int count = 0;
        for (FactionPlayerData factionPlayerData : ServerManager.factionPlayerDataMap.values()) {
            if (factionPlayerData.getFaction().equals(faction)) {
                count++;
            }
        }
        return count;
    }

    public static int getOnlineMemberCount(String faction) {
        int count = 0;
        for (FactionPlayerData factionPlayerData : ServerManager.factionPlayerDataMap.values()) {
            if (factionPlayerData.getFaction().equals(faction)) {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(factionPlayerData.getUuid()));
                if (offlinePlayer.isOnline()) {
                    count++;
                }
            }
        }
        return count;
    }
}
