package de.polo.voidroleplay.utils;

import de.polo.voidroleplay.commands.SubTeamCommand;
import de.polo.voidroleplay.dataStorage.*;
import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.game.faction.SprayableBanner;
import de.polo.voidroleplay.game.faction.staat.SubTeam;
import lombok.SneakyThrows;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.entity.Player;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Date;

public class FactionManager {
    private final Map<String, FactionData> factionDataMap = new HashMap<>();
    private final Map<String, FactionGradeData> factionGradeDataMap = new HashMap<>();
    private final Map<Integer, BlacklistData> blacklistDataMap = new HashMap<>();
    private final PlayerManager playerManager;
    public final SubGroups subGroups;
    private final List<SubTeam> subTeams = new ArrayList<>();
    private final HashMap<Block, LocalDateTime> bannerSprayed = new HashMap<>();
    private final List<SprayableBanner> sprayableBanners = new ArrayList<>();
    public FactionManager(PlayerManager playerManager) {
        this.playerManager = playerManager;
        try {
            loadFactions();
            loadBanners();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        subGroups = new SubGroups(this);
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

    @SneakyThrows
    private void loadBanners() {
        Statement statement = Main.getInstance().mySQL.getStatement();

        ResultSet locs = statement.executeQuery("SELECT * FROM faction_banner");
        while (locs.next()) {
            SprayableBanner banner = new SprayableBanner(locs.getInt("registeredBlock"), locs.getInt("factionId"));
            sprayableBanners.add(banner);
        }
    }

    private void loadFactions() throws SQLException {
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
            factionData.setBadFrak(locs.getBoolean("isBadFrak"));
            factionData.setAllianceFaction(locs.getInt("alliance"));
            factionData.setMotd(locs.getString("motd"));
            factionData.setActive(locs.getBoolean("isActive"));
            if (locs.getString("banner") != null) {
                String bannerData = locs.getString("banner");

                JSONObject bannerObject = new JSONObject(bannerData);

                String bannerTypeString = bannerObject.getString("baseColor");
                factionData.setBannerColor(Material.valueOf(bannerTypeString));

                JSONArray jsonArray = bannerObject.getJSONArray("patterns");
                List<Pattern> patterns = new ArrayList<>();

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject patternObject = jsonArray.getJSONObject(i);
                    DyeColor color = DyeColor.valueOf(patternObject.getString("color"));
                    PatternType type = PatternType.valueOf(patternObject.getString("type"));
                    patterns.add(new Pattern(color, type));
                }

                factionData.setBannerPattern(patterns);
            }
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
            factionData.loadReasons();
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

        ResultSet subteams = statement.executeQuery("SELECT * FROM faction_subteams");
        while (subteams.next()) {
            SubTeam subTeam = new SubTeam(subteams.getInt("faction"), subteams.getString("name"));
            subTeam.setId(subteams.getInt("id"));
            subTeams.add(subTeam);
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
        Main.getInstance().gamePlay.displayNameManager.reloadDisplayNames(player);
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

    @SneakyThrows
    public void removePlayerFromFrak(UUID uuid) {
        PlayerData playerData = playerManager.getPlayerData(uuid);
        LocalDateTime cooldown = Utils.getTime().plusHours(6);
        if (playerData != null) {
            Player player = Bukkit.getPlayer(uuid);
            playerData.setFaction(null);
            playerData.setFactionGrade(0);
            playerData.setDuty(false);
            Main.getInstance().gamePlay.displayNameManager.reloadDisplayNames(player);
            if (playerData.getPermlevel() >= 60) {
                Utils.Tablist.updatePlayer(player);
                player.setCustomNameVisible(true);
            } else {
                player.setDisplayName("§7" + player.getName());
                player.setPlayerListName("§7" + player.getName());
                player.setCustomName("§7" + player.getName());
                player.setCustomNameVisible(true);
            }
            playerData.setFactionCooldown(cooldown);
            for (DBPlayerData dbPlayerData : ServerManager.dbPlayerDataMap.values()) {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(dbPlayerData.getUuid()));
                if (offlinePlayer.getName() != null) {
                    if (offlinePlayer.getName().equalsIgnoreCase(player.getName())) {
                        dbPlayerData.setFaction_grade(0);
                        dbPlayerData.setFaction(null);
                    }
                }
            }
        }
        Statement statement = Main.getInstance().mySQL.getStatement();
        assert statement != null;
        statement.executeUpdate("UPDATE `players` SET `faction` = NULL, `faction_grade` = 0, `isDuty` = false, `factionCooldown` = '" + cooldown + "' WHERE `uuid` = '" + uuid + "'");
        ServerManager.factionPlayerDataMap.remove(uuid.toString());
        TeamSpeak.reloadPlayer(uuid);
    }

    public void removePlayerFromFrak(Player player) throws SQLException {
        String uuid = player.getUniqueId().toString();
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        playerData.setFaction(null);
        playerData.setFactionGrade(0);
        playerData.setDuty(false);
        Main.getInstance().gamePlay.displayNameManager.reloadDisplayNames(player);
        if (playerData.getPermlevel() >= 60) {
            Utils.Tablist.updatePlayer(player);
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
        TeamSpeak.reloadPlayer(player.getUniqueId());
    }

    public void removeOfflinePlayerFromFrak(OfflinePlayer player) throws SQLException {
        Statement statement = Main.getInstance().mySQL.getStatement();
        assert statement != null;
        ServerManager.factionPlayerDataMap.remove(player.getUniqueId().toString());
        LocalDateTime cooldown = Utils.getTime().plusHours(6);
        statement.executeUpdate("UPDATE `players` SET `faction` = NULL, `faction_grade` = 0, `isDuty` = false WHERE `uuid` = '" + player.getUniqueId() + "'");
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        playerData.setFactionCooldown(cooldown);
        TeamSpeak.reloadPlayer(player.getUniqueId());
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
        PlayerData playerData = playerManager.getPlayerData(player);
        return playerData.getFaction().equalsIgnoreCase("FBI") || playerData.getFaction().equalsIgnoreCase("Medic") || playerData.getFaction().equalsIgnoreCase("Polizei");
    }

    public Integer factionBank(String faction) {
        FactionData factionData = factionDataMap.get(faction);
        return factionData.getBank();
    }

    @SneakyThrows
    public void addFactionMoney(String faction, Integer amount, String reason) throws SQLException {
        FactionData factionData = factionDataMap.get(faction);
        factionData.addBankMoney(amount, reason);
    }
    public boolean removeFactionMoney(String faction, Integer amount, String reason) throws SQLException {
        FactionData factionData = factionDataMap.get(faction);
        return factionData.removeFactionMoney(amount, reason);
    }
    public void sendMessageToFaction(String faction, String message) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerData playerData = playerManager.getPlayerData(player);
            if (playerData.getFaction() == null) continue;
            if (playerData.getFaction().equalsIgnoreCase(faction)) {
                player.sendMessage("§8[§" + getFactionPrimaryColor(faction) + faction + "§8]§" + getFactionSecondaryColor(faction) + " " + message);
            }
        }
    }
    public void sendCustomMessageToFactions(String message, String... factions) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            for (String faction : factions) {
                PlayerData playerData = playerManager.getPlayerData(player);
                if (playerData.getFaction() == null) continue;
                if (playerData.getFaction().equalsIgnoreCase(faction)) {
                    player.sendMessage(message);
                }
            }
        }
    }

    public void sendCustomLeaderMessageToFactions(String message, String... factions) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            for (String faction : factions) {
                PlayerData playerData = playerManager.getPlayerData(player);
                if (playerData.getFaction() == null) continue;
                if (playerData.getFactionGrade() < 7) continue;
                if (playerData.getFaction().equalsIgnoreCase(faction)) {
                    player.sendMessage(message);
                }
            }
        }
    }

    public void sendCustomMessageToFaction(String faction, String message) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerData playerData = playerManager.getPlayerData(player);
            if (playerData.getFaction() == null) continue;
            if (faction.equalsIgnoreCase("Staat")) {
                if (playerData.getFaction().equalsIgnoreCase("FBI") || playerData.getFaction().equalsIgnoreCase("Polizei")) {
                    player.sendMessage(message);
                }
            } else if (playerData.getFaction().equalsIgnoreCase(faction)) {
                player.sendMessage(message);
            }
        }
    }
    public boolean changeRankPayDay(String faction, int rank, int payday) throws SQLException {
        FactionGradeData factionGradeData = factionGradeDataMap.get(faction + "_" + rank);
        if (factionGradeData != null) {
            factionGradeData.setPayday(payday);
            PreparedStatement statement = Main.getInstance().mySQL.getConnection().prepareStatement("UPDATE faction_grades SET payday = ? WHERE faction = ? AND grade = ?");
            statement.setInt(1, payday);
            statement.setString(2, faction);
            statement.setInt(3, rank);
            statement.executeUpdate();
            return true;
        } else {
            return false;
        }
    }

    public boolean changeRankName(String faction, int rank, String name) throws SQLException {
        FactionGradeData factionGradeData = factionGradeDataMap.get(faction + "_" + rank);
        if (factionGradeData != null) {
            factionGradeData.setName(name);
            PreparedStatement statement = Main.getInstance().mySQL.getConnection().prepareStatement("UPDATE faction_grades SET name = ? WHERE faction = ? AND grade = ?");
            statement.setString(1, name);
            statement.setString(2, faction);
            statement.setInt(3, rank);
            statement.executeUpdate();
            statement.close();
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
        PlayerData playerData = playerManager.getPlayerData(player);
        if (playerData.getFaction() == null) return false;
        if (playerManager.isInStaatsFrak(player)) return true;
        if (Main.getInstance().gamePlay.alliance.getAlliance(playerData.getFaction()) != null) return true;
        return false;
    }

    public boolean isInBündnisWith(Player player, String faction) {
        PlayerData playerData = playerManager.getPlayerData(player);
        FactionData factionData = Main.getInstance().gamePlay.alliance.getAlliance(playerData.getFaction());
        if (factionData == null) return false;
        FactionData val = getFactionData(faction);
        System.out.println("VAL: " + val.getName());
        System.out.println("FACTIONDATA: " + factionData.getName());
        return val.getId() == factionData.getAllianceFaction() || val.getId() == factionData.getId();
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
        for (FactionData factionData : factionDataMap.values()) {
            if (factionData.getName().equalsIgnoreCase(faction)) return factionData;
        }
        return null;
    }

    public boolean isFactionMemberInRange(String faction, Location location, int range, boolean ignoreDeath) {
        for (PlayerData playerData : playerManager.getPlayers()) {
            if (playerData.getFaction() == null) continue;
            if (playerData.getFaction().equalsIgnoreCase(faction)) {
                if (playerData.getPlayer().getLocation().distance(location) <= range) {
                    if (!playerData.isDead() || ignoreDeath) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public Collection<PlayerData> getFactionMemberInRange(String faction, Location location, int range, boolean ignoreDeath) {
        List<PlayerData> players = new ArrayList<>();
        for (PlayerData playerData : playerManager.getPlayers()) {
            if (playerData.getFaction() == null) continue;
            if (playerData.getFaction().equalsIgnoreCase(faction)) {
                if (playerData.getPlayer().getLocation().distance(location) <= range) {
                    if (!playerData.isDead() || ignoreDeath) {
                        players.add(playerData);
                    }
                }
            }
        }
        return players;
    }

    @SneakyThrows
    public Collection<FactionPlayerData> getFactionMember(String faction) {
        FactionData factionData = getFactionData(faction);
        if (factionData == null) return null;

        List<FactionPlayerData> factionPlayers = new ArrayList<>();
        PreparedStatement statement = Main.getInstance().mySQL.getConnection().prepareStatement("SELECT * FROM players WHERE faction = ?");
        statement.setString(1, factionData.getName());
        ResultSet result = statement.executeQuery();
        while (result.next()) {
            FactionPlayerData fpd = new FactionPlayerData();
            fpd.setFaction(factionData.getName());
            fpd.setFaction_grade(result.getInt("faction_grade"));
            fpd.setId(result.getInt("id"));
            fpd.setUuid(result.getString("uuid"));
            fpd.setLastLogin(result.getTimestamp("lastLogin").toLocalDateTime());
            factionPlayers.add(fpd);
        }

        return factionPlayers;
    }

    @SneakyThrows
    public PlayerData getFactionOfPlayer(UUID uuid) {
        PlayerData playerData = playerManager.getPlayerData(uuid);
        if (playerData != null) {
            return playerData;
        }
        Connection connection = Main.getInstance().mySQL.getConnection();
        PreparedStatement statement = connection.prepareStatement("SELECT faction, faction_grade FROM players WHERE uuid = ?");
        statement.setString(1, uuid.toString());
        ResultSet result = statement.executeQuery();
        if (result.next()) {
            PlayerData pData = new PlayerData();
            pData.setFaction(result.getString("faction"));
            pData.setFactionGrade(result.getInt("faction_grade"));
            return pData;
        }
        return null;
    }

    @SneakyThrows
    public void createSubTeam(SubTeam subTeam) {
        Connection connection = Main.getInstance().mySQL.getConnection();
        PreparedStatement statement = connection.prepareStatement("INSERT INTO faction_subteams (faction, name) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS);
        statement.setInt(1, subTeam.getFactionId());
        statement.setString(2, subTeam.getName());
        statement.execute();
        ResultSet generatedKeys = statement.getGeneratedKeys();
        if (generatedKeys.next()) {
            int key = generatedKeys.getInt(1);
            subTeam.setId(key);
        }
        statement.close();
        connection.close();
        subTeams.add(subTeam);
    }

    @SneakyThrows
    public void deleteSubTeam(SubTeam subTeam) {
        Connection connection = Main.getInstance().mySQL.getConnection();
        PreparedStatement statement = connection.prepareStatement("DELETE FROM faction_subteams WHERE id = ?");
        statement.setInt(1, subTeam.getId());
        statement.execute();
        statement.close();
        connection.close();
        subTeams.remove(subTeam);
    }

    public Collection<SubTeam> getSubTeams(int factionId) {
        List<SubTeam> teams = new ArrayList<>();
        for (SubTeam team : subTeams) {
            if (team.getFactionId() == factionId) teams.add(team);
        }
        return teams;
    }

    @SneakyThrows
    public void setFactionMOTD(int factionId, String motd) {
        FactionData factionData = getFactionData(factionId);
        factionData.setMotd(motd);
        Connection connection = Main.getInstance().mySQL.getConnection();
        PreparedStatement statement = connection.prepareStatement("UPDATE factions SET motd = ? WHERE id = ?");
        statement.setString(1, motd);
        statement.setInt(2, factionId);
        statement.executeUpdate();
        statement.close();
        connection.close();
    }

    @SneakyThrows
    public void updateBanner(RegisteredBlock block, FactionData faction) {
        SprayableBanner banner = getSprayAbleBannerByBlockId(block.getId());
        if (banner == null) {
            SprayableBanner b = new SprayableBanner(block.getId(), faction.getId());
            Connection connection = Main.getInstance().mySQL.getConnection();
            PreparedStatement statement = connection.prepareStatement("INSERT INTO faction_banner (registeredBlock, factionId) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS);
            statement.setInt(1, block.getId());
            statement.setInt(2, faction.getId());
            statement.execute();
            ResultSet res = statement.getGeneratedKeys();
            if (res.next()) {
                b.setId(res.getInt(1));
            }
            sprayableBanners.add(b);
            statement.close();
            connection.close();
            return;
        }
        banner.setLastSpray(Utils.getTime());
        banner.setFaction(faction.getId());
    }

    private SprayableBanner getSprayAbleBannerByBlockId(int id) {
        return sprayableBanners.stream().filter(b -> b.getRegisteredBlock() == id).findFirst().orElse(null);
    }
    public boolean canSprayBanner(RegisteredBlock block) {
        SprayableBanner banner = getSprayAbleBannerByBlockId(block.getId());
        if (banner == null) return false;
        return Utils.getTime().isAfter(banner.getLastSpray().plusMinutes(10));
    }

    public boolean isBannerRegistered(RegisteredBlock block) {
        return getSprayAbleBannerByBlockId(block.getId()) != null;
    }

    public Collection<SprayableBanner> getBanner()
    {
        return sprayableBanners;
    }
}
