package de.polo.core.faction.service.impl;

import de.polo.core.player.entities.PlayerData;
import de.polo.core.Main;
import de.polo.core.database.impl.CoreDatabase;
import de.polo.core.faction.entity.Faction;
import de.polo.core.faction.entity.FactionGrade;
import de.polo.core.faction.entity.FactionPlayerData;
import de.polo.core.faction.enums.FactionType;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.manager.ServerManager;
import de.polo.core.storage.*;
import de.polo.core.game.faction.SprayableBanner;
import de.polo.core.game.faction.staat.SubTeam;
import de.polo.core.utils.SubGroups;
import de.polo.core.utils.TeamSpeak;
import de.polo.core.utils.Utils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
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

public class FactionManager {
    public final SubGroups subGroups;
    private final Map<String, Faction> factionDataMap = new HashMap<>();
    private final Map<String, FactionGrade> factionGradeDataMap = new HashMap<>();
    private final Map<Integer, BlacklistData> blacklistDataMap = new HashMap<>();
    private final PlayerManager playerManager;
    private final List<SubTeam> subTeams = new ObjectArrayList<>();
    private final HashMap<Block, LocalDateTime> bannerSprayed = new HashMap<>();
    private final List<SprayableBanner> sprayableBanners = new ObjectArrayList<>();

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

    public Collection<Faction> getFactions() {
        return factionDataMap.values();
    }

    public Collection<BlacklistData> getBlacklists() {
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
        sprayableBanners.clear();

        try (Connection connection = Main.getInstance().coreDatabase.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM faction_banner");
             ResultSet locs = statement.executeQuery()) {

            while (locs.next()) {
                SprayableBanner banner = new SprayableBanner(locs.getInt("registeredBlock"), locs.getInt("factionId"));
                sprayableBanners.add(banner);
            }
        }
    }

    private void loadFactions() throws SQLException {
        // Versuche, alle Ressourcen mit try-with-resources zu verwalten
        try (Statement statement = Main.getInstance().coreDatabase.getStatement()) {

            // Abfrage 1: Factions
            try (ResultSet locs = statement.executeQuery("SELECT f.*, fs.*, fu.*, fe.* FROM factions AS f " +
                    "LEFT JOIN faction_storage AS fs ON f.id = fs.factionId " +
                    "LEFT JOIN faction_upgrades AS fu ON f.id  = fu.factionId " +
                    "LEFT JOIN faction_equip AS fe ON f.id = fe.factionId")) {
                while (locs.next()) {
                    Faction factionData = new Faction(FactionType.valueOf(locs.getString("type")));
                    factionData.setId(locs.getInt("id"));
                    factionData.setName(locs.getString("name"));
                    factionData.setChatColor(ChatColor.valueOf(locs.getString("chatColor")));
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
                    factionData.setEquipPoints(locs.getInt("equippoints"));
                    factionData.setActive(locs.getBoolean("isActive"));
                    if (locs.getString("banner") != null) {
                        String bannerData = locs.getString("banner");

                        JSONObject bannerObject = new JSONObject(bannerData);

                        String bannerTypeString = bannerObject.getString("baseColor");
                        factionData.setBannerColor(Material.valueOf(bannerTypeString));

                        JSONArray jsonArray = bannerObject.getJSONArray("patterns");
                        List<Pattern> patterns = new ObjectArrayList<>();

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
                    factionData.storage.setCrystal(locs.getInt("crystal"));
                    factionData.upgrades.setTaxLevel(locs.getInt("tax"));
                    factionData.upgrades.setWeaponLevel(locs.getInt("weapon"));
                    factionData.upgrades.setDrugEarningLevel(locs.getInt("drug_earning"));
                    factionData.equip.setSturmgewehr(locs.getInt("sturmgewehr"));
                    factionData.equip.setSturmgewehr_ammo(locs.getInt("sturmgewehr_ammo"));
                    factionData.upgrades.calculate();
                    factionDataMap.put(locs.getString(2), factionData);
                    factionData.loadReasons();
                }
            }

            // Abfrage 2: Faction Grades
            try (ResultSet grades = statement.executeQuery("SELECT * FROM faction_grades")) {
                while (grades.next()) {
                    FactionGrade factionGrade = new FactionGrade();
                    factionGrade.setId(grades.getInt(1));
                    factionGrade.setFaction(grades.getString(2));
                    factionGrade.setGrade(grades.getInt(3));
                    factionGrade.setName(grades.getString(4));
                    factionGrade.setPayday(grades.getInt(5));
                    factionGradeDataMap.put(grades.getString(2) + "_" + grades.getInt(3), factionGrade);
                }
            }

            // Abfrage 3: Blacklist
            try (ResultSet blacklist = statement.executeQuery("SELECT * FROM `blacklist`")) {
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

            // Abfrage 4: Subteams
            try (ResultSet subteams = statement.executeQuery("SELECT * FROM faction_subteams")) {
                while (subteams.next()) {
                    SubTeam subTeam = new SubTeam(subteams.getInt("faction"), subteams.getString("name"));
                    subTeam.setId(subteams.getInt("id"));
                    subTeams.add(subTeam);
                }
            }
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

    public void setPlayerInFrak(Player player, String frak, Integer rang) {
        setPlayerInFrak(player, frak, rang, false);
    }

    public void setPlayerInFrak(Player player, String frak, Integer rang, boolean updateFactionJoin) {
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        playerData.setFaction(frak);
        playerData.setFactionGrade(rang);
        if (updateFactionJoin) {
            playerData.setFactionJoin(Utils.getTime());
            Main.getInstance().getCoreDatabase().updateAsync("UPDATE players SET faction = ?, faction_grade = ?, factionJoin = ? WHERE uuid = ?", frak, rang, Utils.getTime().toString(), player.getUniqueId().toString());
        } else {
            Main.getInstance().getCoreDatabase().updateAsync("UPDATE players SET faction = ?, faction_grade = ? WHERE uuid = ?", frak, rang, player.getUniqueId().toString());
        }
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
            if (playerData.getFactionGrade() < 4) playerData.setFactionCooldown(cooldown);
            playerData.setFaction(null);
            playerData.setFactionGrade(0);
            playerData.setDuty(false);
            if (player != null) {
                if (playerData.getPermlevel() >= 60) {
                    Utils.Tablist.updatePlayer(player);
                    player.setCustomNameVisible(true);
                } else {
                    player.setDisplayName("§7" + player.getName());
                    player.setPlayerListName("§7" + player.getName());
                    player.setCustomName("§7" + player.getName());
                    player.setCustomNameVisible(true);
                }
            }
            for (DBPlayerData dbPlayerData : ServerManager.dbPlayerDataMap.values()) {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(dbPlayerData.getUuid()));
                if (offlinePlayer.getName() != null && player != null) {
                    if (offlinePlayer.getName().equalsIgnoreCase(player.getName())) {
                        dbPlayerData.setFaction_grade(0);
                        dbPlayerData.setFaction(null);
                    }
                }
            }
        }
        updateFactionCooldownIfApplicable(uuid);

        ServerManager.factionPlayerDataMap.remove(uuid.toString());
        TeamSpeak.reloadPlayer(uuid);
    }

    private void updateFactionCooldownIfApplicable(UUID uuid) {
        LocalDateTime cooldown = Utils.getTime().plusHours(6);
        CoreDatabase coreDatabase = Main.getInstance().coreDatabase;

        coreDatabase.executeQueryAsync("SELECT faction_grade FROM players WHERE `uuid` = ?", uuid.toString())
                .thenAccept(result -> {
                    if (result != null && !result.isEmpty()) {
                        Map<String, Object> playerData = result.get(0);
                        int factionGrade = (int) playerData.get("faction_grade");
                        if (factionGrade < 4) {
                            coreDatabase.updateAsync(
                                    "UPDATE `players` SET `faction` = NULL, `faction_grade` = 0, `isDuty` = false, `factionCooldown` = ? WHERE `uuid` = ?",
                                    cooldown.toString(), uuid.toString()
                            ).thenRun(() -> {
                                ServerManager.factionPlayerDataMap.remove(uuid.toString());
                                TeamSpeak.reloadPlayer(uuid);
                            }).exceptionally(e -> {
                                e.printStackTrace();
                                return null;
                            });
                        } else {
                            coreDatabase.updateAsync(
                                    "UPDATE `players` SET `faction` = NULL, `faction_grade` = 0, `isDuty` = false WHERE `uuid` = ?",
                                    uuid.toString()
                            ).thenRun(() -> {
                                ServerManager.factionPlayerDataMap.remove(uuid.toString());
                                TeamSpeak.reloadPlayer(uuid);
                            }).exceptionally(e -> {
                                e.printStackTrace();
                                return null;
                            });
                        }
                    }
                }).exceptionally(e -> {
                    e.printStackTrace();
                    return null;
                });
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
        Main.getInstance().getCoreDatabase().updateAsync("UPDATE players SET faction = NULL, faction_grade = 0, isDuty = false WHERE uuid = ?",
                uuid);
        ServerManager.factionPlayerDataMap.remove(player.getUniqueId().toString());
        TeamSpeak.reloadPlayer(player.getUniqueId());
    }

    public void removeOfflinePlayerFromFrak(OfflinePlayer player) throws SQLException {
        ServerManager.factionPlayerDataMap.remove(player.getUniqueId().toString());
        LocalDateTime cooldown = Utils.getTime().plusHours(18);
        Main.getInstance().getCoreDatabase().updateAsync("UPDATE players SET faction = NULL, faction_grade = 0, isDuty = false WHERE uuid = ?",
                player.getUniqueId().toString());
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        playerData.setFactionCooldown(cooldown);
        TeamSpeak.reloadPlayer(player.getUniqueId());
    }

    public String getFactionPrimaryColor(String faction) {
        Faction factionData = factionDataMap.get(faction);
        return factionData.getPrimaryColor();
    }

    public String getFactionSecondaryColor(String faction) {
        Faction factionData = factionDataMap.get(faction);
        return factionData.getSecondaryColor();
    }

    public String getFactionFullname(String faction) {
        Faction factionData = factionDataMap.get(faction);
        return factionData.getFullname();
    }

    public String getPlayerFactionRankName(Player p) {
        FactionGrade factionGrade = factionGradeDataMap.get(faction(p) + "_" + faction_grade(p));
        return factionGrade.getName();
    }

    public String getRankName(String faction, int rang) {
        FactionGrade factionGrade = factionGradeDataMap.get(faction + "_" + rang);
        return factionGrade.getName();
    }

    public Integer getPaydayFromFaction(String faction, Integer rank) {
        FactionGrade factionGrade = factionGradeDataMap.get(faction + "_" + rank);
        return factionGrade.getPayday();
    }

    public boolean isPlayerInGoodFaction(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player);
        if (playerData.getFaction() == null) return false;
        return playerData.getFaction().equalsIgnoreCase("FBI") || playerData.getFaction().equalsIgnoreCase("Medic") || playerData.getFaction().equalsIgnoreCase("Polizei");
    }

    public Integer factionBank(String faction) {
        Faction factionData = factionDataMap.get(faction);
        return factionData.getBank();
    }

    @SneakyThrows
    public void addFactionMoney(String faction, Integer amount, String reason) throws SQLException {
        Faction factionData = factionDataMap.get(faction);
        factionData.addBankMoney(amount, reason);
    }

    public boolean removeFactionMoney(String faction, Integer amount, String reason) throws SQLException {
        Faction factionData = factionDataMap.get(faction);
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
                if (!playerData.isLeader()) continue;
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
            if (faction.equalsIgnoreCase("Staat") || faction.equalsIgnoreCase("FBI") || faction.equalsIgnoreCase("Polizei")) {
                if (playerData.getFaction().equalsIgnoreCase("FBI") || playerData.getFaction().equalsIgnoreCase("Polizei")) {
                    player.sendMessage(message);
                }
            } else if (playerData.getFaction().equalsIgnoreCase(faction)) {
                player.sendMessage(message);
            }
        }
    }

    public boolean changeRankPayDay(String faction, int rank, int payday) throws SQLException {
        FactionGrade factionGrade = factionGradeDataMap.get(faction + "_" + rank);
        if (factionGrade != null) {
            factionGrade.setPayday(payday);
            Main.getInstance().getCoreDatabase().updateAsync("UPDATE faction_grades SET payday = ? WHERE faction = ? AND grade = ?",
                    payday,
                    faction,
                    rank);
            return true;
        } else {
            return false;
        }
    }

    public boolean changeRankName(String faction, int rank, String name) throws SQLException {
        FactionGrade factionGrade = factionGradeDataMap.get(faction + "_" + rank);
        if (factionGrade != null) {
            factionGrade.setName(name);
            Main.getInstance().getCoreDatabase().updateAsync("UPDATE faction_grades SET name = ? WHERE faction = ? AND grade = ?",
                    name,
                    faction,
                    rank);
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
        playerData.setDuty(state);
        Main.getInstance().getCoreDatabase().updateAsync("UPDATE players SET isDuty = ? WHERE uuid = ?",
                state,
                player.getUniqueId().toString());
        Utils.Tablist.updatePlayer(player);
    }

    public boolean isInBündnis(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player);
        if (playerData.getFaction() == null) return false;
        if (playerManager.isInStaatsFrak(player)) return true;
        return Main.getInstance().gamePlay.alliance.getAlliance(playerData.getFaction()) != null;
    }

    public boolean isInBündnisWith(Player player, String faction) {
        PlayerData playerData = playerManager.getPlayerData(player);
        Faction factionData = Main.getInstance().gamePlay.alliance.getAlliance(playerData.getFaction());
        if (factionData == null) return false;
        Faction val = getFactionData(faction);
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

    public Faction getFactionData(int factionId) {
        for (Faction data : factionDataMap.values()) {
            if (data.getId() == factionId) {
                return data;
            }
        }
        return null;
    }

    public Faction getFactionData(String faction) {
        for (Faction factionData : factionDataMap.values()) {
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
        List<PlayerData> players = new ObjectArrayList<>();
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
        Faction factionData = getFactionData(faction);
        if (factionData == null) return null;

        List<FactionPlayerData> factionPlayers = new ObjectArrayList<>();
        PreparedStatement statement = Main.getInstance().coreDatabase.getConnection().prepareStatement("SELECT * FROM players WHERE faction = ?");
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
        Connection connection = Main.getInstance().coreDatabase.getConnection();
        PreparedStatement statement = connection.prepareStatement("SELECT faction, faction_grade , factionJoin FROM players WHERE uuid = ?");
        statement.setString(1, uuid.toString());
        ResultSet result = statement.executeQuery();
        if (result.next()) {
            PlayerData pData = new PlayerData();
            pData.setFaction(result.getString("faction"));
            pData.setFactionGrade(result.getInt("faction_grade"));
            pData.setFactionJoin(Utils.toLocalDateTime(result.getDate("factionJoin")));
            return pData;
        }
        return null;
    }

    @SneakyThrows
    public void createSubTeam(SubTeam subTeam) {
        Connection connection = Main.getInstance().coreDatabase.getConnection();
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
        Main.getInstance().getCoreDatabase().updateAsync("DELETE FROM faction_subteams WHERE id = ?",
                subTeam.getId());
        subTeams.remove(subTeam);
    }

    public Collection<SubTeam> getSubTeams(int factionId) {
        List<SubTeam> teams = new ObjectArrayList<>();
        for (SubTeam team : subTeams) {
            if (team.getFactionId() == factionId) teams.add(team);
        }
        return teams;
    }

    @SneakyThrows
    public void setFactionMOTD(int factionId, String motd) {
        Faction factionData = getFactionData(factionId);
        factionData.setMotd(motd);
        Main.getInstance().getCoreDatabase().updateAsync("UPDATE factions SET motd = ? WHERE id = ?",
                motd, factionId);
    }

    @SneakyThrows
    public void setFactionChatColor(int factionId, ChatColor color) {
        Faction factionData = getFactionData(factionId);
        factionData.setChatColor(color);
        Main.getInstance().getCoreDatabase().updateAsync("UPDATE factions SET chatColor = ? WHERE id = ?",
                color.name(), factionId);
    }

    @SneakyThrows
    public void updateBanner(RegisteredBlock block, Faction faction) {
        SprayableBanner banner = getSprayAbleBannerByBlockId(block.getId());
        if (banner == null) {
            SprayableBanner b = new SprayableBanner(block.getId(), faction.getId());
            Main.getInstance().getCoreDatabase().insertAndGetKeyAsync("INSERT INTO faction_banner (registeredBlock, factionId) VALUES (?, ?)", block.getId(), faction.getId())
                    .thenApply(key -> {
                        if (key.isPresent()) {
                            b.setId(key.get());
                            sprayableBanners.add(b);
                        }
                        return null;
                    });
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

    public Collection<SprayableBanner> getBanner() {
        return sprayableBanners;
    }

    public void setLeader(OfflinePlayer offlinePlayer, boolean leader) {
        if (offlinePlayer.isOnline() && offlinePlayer.getPlayer() != null) {
            PlayerData playerData = playerManager.getPlayerData(offlinePlayer.getPlayer());
            playerData.setLeader(leader);
        }
        Main.getInstance().getCoreDatabase().updateAsync("UPDATE players SET isLeader = ? WHERE uuid = ?", leader, offlinePlayer.getUniqueId().toString());
    }
}
