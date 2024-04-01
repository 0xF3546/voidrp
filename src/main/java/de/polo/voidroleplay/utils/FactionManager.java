package de.polo.voidroleplay.utils;

import de.polo.voidroleplay.dataStorage.*;
import de.polo.voidroleplay.Main;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class FactionManager {
    private final Map<String, FactionData> factionDataMap = new HashMap<>();
    private final Map<String, FactionGradeData> factionGradeDataMap = new HashMap<>();
    private final Map<Integer, BlacklistData> blacklistDataMap = new HashMap<>();
    private final PlayerManager playerManager;
    public FactionManager(PlayerManager playerManager) {
        this.playerManager = playerManager;
        try {
            loadFactions();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Collection<FactionData> getFactions() {
        return factionDataMap.values();
    }
    public Collection<BlacklistData> getBlacklists()  {
        return blacklistDataMap.values();
    }

    public void addBlacklist(int factionId, BlacklistData data) {
        blacklistDataMap.put(factionId, data);
    }

    public void removeBlacklist(int blacklistDataId) {
        blacklistDataMap.remove(blacklistDataId);
    }

    public void loadFactions() throws SQLException {
        Statement statement = Main.getInstance().mySQL.getStatement();

        ResultSet locs = statement.executeQuery("SELECT f.*, fs.*, fu.*, fe.* FROM factions AS f " +
                "LEFT JOIN faction_storage AS fs ON f.id = fs.factionId " +
                "LEFT JOIN faction_upgrades AS fu ON f.id  = fu.factionId " +
                "LEFT JOIN faction_equip AS fe ON f.id = fe.factionId");
        while (locs.next()) {
            FactionData factionData = new FactionData();
            factionData.setId(locs.getInt("id"));
            factionData.setName(locs.getString("name"));
            factionData.setFullname(locs.getString("fullname"));
            factionData.setPrimaryColor(locs.getString("primaryColor"));
            factionData.setSecondaryColor(locs.getString("secondaryColor"));
            factionData.setBank(locs.getInt("bank"));
            factionData.setMaxMember(locs.getInt("maxMember"));
            factionData.setTeamSpeakID(locs.getInt("TeamSpeakID"));
            factionData.setChannelGroupID(locs.getInt("ChannelGroupID"));
            factionData.setHasBlacklist(locs.getBoolean("hasBlacklist"));
            factionData.setDoGangwar(locs.getBoolean("doGangwar"));
            factionData.setForumID(locs.getInt("forumID"));
            factionData.setForumID_Leader(locs.getInt("forumID_Leader"));
            factionData.setHasLaboratory(locs.getBoolean("hasLaboratory"));
            factionData.setJointsMade(locs.getInt("jointsMade"));
            factionData.setLaboratory(locs.getInt("laboratory"));
            factionData.storage.setJoint(locs.getInt("joint"));
            factionData.storage.setWeed(locs.getInt("weed"));
            factionData.storage.setCocaine(locs.getInt("cocaine"));
            factionData.storage.setKevlar(locs.getInt("kevlar"));
            factionData.storage.setNoble_joint(locs.getInt("noble_joint"));
            factionData.upgrades.setTaxLevel(locs.getInt("tax"));
            factionData.upgrades.setWeaponLevel(locs.getInt("weapon"));
            factionData.upgrades.setDrugEarningLevel(locs.getInt("drug_earning"));
            factionData.equip.setSturmgewehr(locs.getInt("sturmgewehr"));
            factionData.equip.setSturmgewehr_ammo(locs.getInt("sturmgewehr_ammo"));
            factionData.upgrades.calculate();
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

    public String faction(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        return playerData.getFaction();
    }

    public Integer faction_grade(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        return playerData.getFactionGrade();
    }
    public void setPlayerInFrak(Player player, String frak, Integer rang) throws SQLException {
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        playerData.setFaction(frak);
        playerData.setFactionGrade(rang);
        Statement statement = Main.getInstance().mySQL.getStatement();
        assert statement != null;
        statement.executeUpdate("UPDATE `players` SET `faction` = '" + frak + "', `faction_grade` = " + rang + " WHERE `uuid` = '" + player.getUniqueId() + "'");
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
        for (DBPlayerData dbPlayerData : ServerManager.dbPlayerDataMap.values()) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(dbPlayerData.getUuid()));
            if (offlinePlayer.getUniqueId() == player.getUniqueId()) {
                dbPlayerData.setFaction_grade(rang);
                dbPlayerData.setFaction(frak);
            }
        }
        TeamSpeak.reloadPlayer(player.getUniqueId());
    }

    public void removePlayerFromFrak(Player player) throws SQLException {
        String uuid = player.getUniqueId().toString();
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
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
        for (DBPlayerData dbPlayerData : ServerManager.dbPlayerDataMap.values()) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(dbPlayerData.getUuid()));
            if (offlinePlayer.getName() != null) {
                if (offlinePlayer.getName().equalsIgnoreCase(player.getName())) {
                    dbPlayerData.setFaction_grade(0);
                    dbPlayerData.setFaction(null);
                }
            }
        }
        Statement statement = Main.getInstance().mySQL.getStatement();
        assert statement != null;
        statement.executeUpdate("UPDATE `players` SET `faction` = NULL, `faction_grade` = 0, `isDuty` = false WHERE `uuid` = '" + uuid + "'");
        ServerManager.factionPlayerDataMap.remove(player.getUniqueId().toString());
        /*if (playerData.getTeamSpeakUID() != null) {
            Client client = TeamSpeak.getTeamSpeak().getAPI().getClientByUId(playerData.getTeamSpeakUID());
            TeamSpeak.getTeamSpeak().updateClientGroup(player, client);
        }*/
    }

    public void removeOfflinePlayerFromFrak(OfflinePlayer player) throws SQLException {
        Statement statement = Main.getInstance().mySQL.getStatement();
        assert statement != null;
        ServerManager.factionPlayerDataMap.remove(player.getUniqueId().toString());
        statement.executeUpdate("UPDATE `players` SET `faction` = NULL, `faction_grade` = 0, `isDuty` = false WHERE `uuid` = '" + player.getUniqueId() + "'");
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
    }

    public String faction_offlinePlayer(String playername) {
        String val = null;
        for (DBPlayerData dbPlayerData : ServerManager.dbPlayerDataMap.values()) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(dbPlayerData.getUuid()));
            if (player.getName().equalsIgnoreCase(playername)) {
                val = dbPlayerData.getFaction();
            }
        }
        return val;
    }

    public Integer faction_grade_offlinePlayer(String playername) {
        int val = 0;
        for (DBPlayerData dbPlayerData : ServerManager.dbPlayerDataMap.values()) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(dbPlayerData.getUuid()));
            if (player.getName().equalsIgnoreCase(playername)) {
                val = dbPlayerData.getFaction_grade();
            }
        }
        return val;
    }

    public String getFactionPrimaryColor(String faction) {
        FactionData factionData = factionDataMap.get(faction);
        return factionData.getPrimaryColor();
    }

    public String getFactionSecondaryColor(String faction) {
        FactionData factionData = factionDataMap.get(faction);
        return factionData.getSecondaryColor();
    }

    public String getFactionFullname(String faction) {
        FactionData factionData = factionDataMap.get(faction);
        return factionData.getFullname();
    }

    public String getPlayerFactionRankName(Player p) {
        FactionGradeData factionGradeData = factionGradeDataMap.get(faction(p) + "_" + faction_grade(p));
        return factionGradeData.getName();
    }
    public String getRankName(String faction, int rang) {
        FactionGradeData factionGradeData = factionGradeDataMap.get(faction + "_" + rang);
        return factionGradeData.getName();
    }
    public Integer getPaydayFromFaction(String faction, Integer rank) {
        FactionGradeData factionGradeData = factionGradeDataMap.get(faction + "_" + rank);
        return factionGradeData.getPayday();
    }
    public boolean isPlayerInGoodFaction(Player player) {
        return faction(player) == "FBI" || faction(player) == "Polizei" || faction(player) == "Medics";
    }

    public Integer factionBank(String faction) {
        FactionData factionData = factionDataMap.get(faction);
        return factionData.getBank();
    }

    public void addFactionMoney(String faction, Integer amount, String reason) throws SQLException {
        FactionData factionData = factionDataMap.get(faction);
        factionData.setBank(factionData.getBank() + amount);
        Statement statement = Main.getInstance().mySQL.getStatement();
        statement.execute("INSERT INTO `faction_bank_logs` (`type`, `faction`, `amount`, `reason`, `isPlus`) VALUES ('einzahlung', '" + faction + "', " + amount + ", '" + reason + "', true)");
        statement.execute("UPDATE `factions` SET `bank` = " + factionData.getBank() + " WHERE `name` = '" + faction + "'");
    }
    public boolean removeFactionMoney(String faction, Integer amount, String reason) throws SQLException {
        boolean returnval = false;
        FactionData factionData = factionDataMap.get(faction);
        if (factionData.getBank() >= amount) {
            factionData.setBank(factionData.getBank() - amount);
            Statement statement = Main.getInstance().mySQL.getStatement();
            statement.execute("INSERT INTO `faction_bank_logs` (`type`, `faction`, `amount`, `reason`, `isPlus`) VALUES ('auszahlung', '" + faction + "', " + amount + ", '" + reason + "', false)");
            statement.execute("UPDATE `factions` SET `bank` = " + factionData.getBank() + " WHERE `name` = '" + faction + "'");
            returnval = true;
        }
        return returnval;
    }
    public void sendMessageToFaction(String faction, String message) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (Objects.equals(faction(player), faction)) {
                player.sendMessage("§8[§" + getFactionPrimaryColor(faction) + faction + "§8]§" + getFactionSecondaryColor(faction) + " " + message);
            }
        }
    }
    public void sendCustomMessageToFactions(String message, String... factions) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            for (String faction : factions) {
                if (Objects.equals(faction(player), faction)) {
                    player.sendMessage(message);
                }
            }
        }
    }
    public void sendCustomMessageToFaction(String faction, String message) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (Objects.equals(faction(player), faction)) {
                player.sendMessage(message);
            }
        }
    }
    public boolean changeRankPayDay(String faction, int rank, int payday) throws SQLException {
        FactionGradeData factionGradeData = factionGradeDataMap.get(faction + "_" + rank);
        if (factionGradeData != null) {
            factionGradeData.setPayday(payday);
            Statement statement = Main.getInstance().mySQL.getStatement();
            statement.executeUpdate("UPDATE `faction_grades` SET `payday` = " + payday + " WHERE `faction` = '" + faction + "' AND `grade` = " + rank);
            return true;
        } else {
            return false;
        }
    }

    public boolean changeRankName(String faction, int rank, String name) throws SQLException {
        FactionGradeData factionGradeData = factionGradeDataMap.get(faction + "_" + rank);
        if (factionGradeData != null) {
            factionGradeData.setName(name);
            Statement statement = Main.getInstance().mySQL.getStatement();
            statement.executeUpdate("UPDATE `faction_grades` SET `name` = '" + name + "' WHERE `faction` = '" + faction + "' AND `grade` = " + rank);
            return true;
        } else {
            return false;
        }
    }
    public String getTitle(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        switch (playerData.getFaction()) {
            case "FBI":
                return "Agent";
            case "Polizei":
                return "Officer";
            case "Medic":
                return "Mediziner";
        }
        return null;
    }

    public void setDuty(Player player, boolean state) {
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        try {
            playerData.setDuty(state);
            Statement statement = Main.getInstance().mySQL.getStatement();
            if (state) {
                statement.executeUpdate("UPDATE `players` SET `isDuty` = true WHERE `uuid` = '" + player.getUniqueId() + "'");
            } else {
                statement.executeUpdate("UPDATE `players` SET `isDuty` = false WHERE `uuid` = '" + player.getUniqueId() + "'");
            }
            Utils.Tablist.updatePlayer(player);

        } catch (SQLException e) {
            player.sendMessage(Main.error + "Fehler.");
            throw new RuntimeException(e);
        }
    }

    public boolean isInBündnis(Player player) {
        return false;
    }

    public boolean isInBündnisWith(Player player, String faction) {
        return false;
    }

    public int getMemberCount(String faction) {
        int count = 0;
        for (FactionPlayerData factionPlayerData : ServerManager.factionPlayerDataMap.values()) {
            if (factionPlayerData.getFaction().equals(faction)) {
                count++;
            }
        }
        return count;
    }

    public int getOnlineMemberCount(String faction) {
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

    public FactionData getFactionData(int factionId) {
        for (FactionData data : factionDataMap.values()) {
            if (data.getId() == factionId) {
                return data;
            }
        }
        return null;
    }
    public FactionData getFactionData(String faction) {
        return factionDataMap.get(faction);
    }
}
