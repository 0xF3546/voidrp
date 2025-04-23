package de.polo.core.player.entities;

import de.polo.api.VoidAPI;
import de.polo.api.crew.Crew;
import de.polo.api.crew.CrewRank;
import de.polo.api.jobs.enums.MiniJob;
import de.polo.api.player.JobSkill;
import de.polo.api.player.PlayerCharacter;
import de.polo.api.player.VoidPlayer;
import de.polo.api.player.enums.Gender;
import de.polo.api.player.enums.HealthInsurance;
import de.polo.api.player.enums.IllnessType;
import de.polo.api.player.enums.License;
import de.polo.core.Main;
import de.polo.core.game.base.extra.PlayerIllness;
import de.polo.core.game.base.extra.seasonpass.PlayerQuest;
import de.polo.core.game.base.farming.PlayerWorkstation;
import de.polo.core.game.faction.staat.SubTeam;
import de.polo.api.jobs.enums.LongTermJob;
import de.polo.core.manager.PlayerPetManager;
import de.polo.core.storage.*;
import de.polo.core.utils.Utils;
import de.polo.core.utils.enums.Weapon;
import de.polo.core.utils.enums.*;
import de.polo.core.utils.player.PlayerFFAStatsManager;
import de.polo.core.utils.player.PlayerInventoryManager;
import de.polo.core.utils.player.PlayerPowerUpManager;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static de.polo.core.Main.database;
import static de.polo.core.Main.playerManager;

public class PlayerData implements PlayerCharacter {
    public final AddonXP addonXP = new AddonXP();

    @Getter
    private PlayerInventoryManager inventory;
    private final List<PlayerQuest> quests = new ObjectArrayList<>();
    private final List<de.polo.core.game.base.extra.beginnerpass.PlayerQuest> beginnerQuests = new ObjectArrayList<>();
    private final List<PlayerIllness> illnesses = new ObjectArrayList<>();
    private final List<JobSkill> jobSkills = new ObjectArrayList<>();
    private final List<PlayerWeapon> weapons = new ObjectArrayList<>();
    private final List<ClickedEventBlock> clickedEventBlocks = new ObjectArrayList<>();
    private final HashMap<String, Object> variables = new HashMap<>();
    private final HashMap<String, Integer> integer_variables = new HashMap<>();
    private final HashMap<String, Location> locationVariables = new HashMap<>();
    private final HashMap<String, Integer> skillLevel = new HashMap<>();
    private final HashMap<String, Integer> skillExp = new HashMap<>();
    private final HashMap<String, Integer> skillNeeded_Exp = new HashMap<>();
    private final HashMap<String, Scoreboard> scoreboards = new HashMap<>();
    private final HashMap<String, BossBar> bossBars = new HashMap<>();
    @Getter
    private PlayerPetManager playerPetManager;
    @Getter
    private PlayerFFAStatsManager playerFFAStatsManager;
    @Getter
    private PlayerPowerUpManager playerPowerUpManager;
    @Getter
    private Player player;
    @Getter
    @Setter
    private HealthInsurance healthInsurance;
    @Getter
    @Setter
    private LongTermJob longTermJob;
    @Setter
    @Getter
    private String spawn;
    @Getter
    @Setter
    private boolean sendAdminMessages = false;
    @Getter
    @Setter
    private int rewardId;
    @Getter
    @Setter
    private int rewardTime;
    @Getter
    @Setter
    private float crypto;
    @Setter
    @Getter
    private int id;
    @Setter
    @Getter
    private UUID uuid;
    @Getter
    @Setter
    private String firstname;
    @Getter
    @Setter
    private String lastname;
    @Getter
    @Setter
    private int bargeld;
    private int bank;
    @Getter
    @Setter
    private String rang;
    @Getter
    @Setter
    private int visum;
    @Getter
    @Setter
    private int permlevel;
    @Getter
    @Setter
    private String faction;
    private int faction_grade;
    @Getter
    @Setter
    private int eventPoints;
    @Setter
    private boolean canInteract = true;
    private boolean isJailed;
    @Setter
    @Getter
    private int hafteinheiten = 0;
    @Getter
    @Setter
    private int jailParole = 0;
    private boolean isAduty = false;
    @Setter
    @Getter
    private int level;
    @Setter
    @Getter
    private int exp;
    @Setter
    private int needed_exp;
    private boolean isDead = false;
    private boolean isStabilized = false;
    private boolean isHitmanDead = false;
    @Setter
    @Getter
    private int deathTime = 300;
    @Getter
    @Setter
    private boolean isFFADead = false;
    @Setter
    @Getter
    private int number = 0;
    private boolean isFlightmode = false;
    private boolean isDuty = false;
    @Setter
    @Getter
    private Gender gender;
    @Setter
    @Getter
    private Date birthday;
    @Setter
    private int houseSlot;
    @Setter
    @Getter
    private LocalDateTime rankDuration;
    @Setter
    @Getter
    private LocalDateTime boostDuration;
    @Getter
    @Setter
    private LocalDateTime lastContract;
    @Getter
    @Setter
    private LocalDateTime lastPremiumBonus;

    @Setter
    @Getter
    private Location deathLocation;
    @Setter
    @Getter
    private String secondaryTeam;
    @Setter
    @Getter
    private String teamSpeakUID;
    @Setter
    @Getter
    private String job;
    @Setter
    @Getter
    private int hours;
    @Setter
    @Getter
    private int minutes;
    @Setter
    @Getter
    private Integer business;
    @Setter
    @Getter
    private int business_grade;
    @Setter
    @Getter
    private int warns = 0;
    @Setter
    @Getter
    private Integer forumID;
    @Setter
    @Getter
    private HashMap<String, String> relationShip = new HashMap<>();
    @Setter
    @Getter
    private String bloodType;
    @Setter
    private boolean hasAnwalt;
    private boolean isAFK = false;
    @Setter
    @Getter
    private int Coins = 0;

    @Getter
    private boolean cuffed;
    @Setter
    @Getter
    private Company company;
    @Setter
    @Getter
    private CompanyRole companyRole;
    @Setter
    @Getter
    private boolean hudEnabled;
    @Setter
    @Getter
    private LocalDateTime dailyBonusRedeemed;
    @Setter
    @Getter
    private LocalDateTime lastPayDay;

    @Getter
    @Setter
    private int payday;

    @Setter
    @Getter
    private int currentHours;
    @Setter
    @Getter
    private int atmBlown;

    @Setter
    private boolean receivedBonus;
    @Setter
    @Getter
    private int subGroupId;
    @Setter
    @Getter
    private int subGroupGrade;
    @Getter
    @Setter
    private boolean isChurch;

    @Getter
    @Setter
    private boolean isBaptized;

    @Getter
    @Setter
    private int karma;
    @Setter
    private List<PlayerWorkstation> workstations = new ObjectArrayList<>();

    @Getter
    private final List<License> licenses = new ObjectArrayList<>();

    @Getter
    @Setter
    private LocalDateTime factionCooldown;

    @Getter
    @Setter
    private SubTeam subTeam = null;

    @Getter
    @Setter
    private int votes;

    @Getter
    private PlayerWanted wanted;

    @Getter
    @Setter
    private boolean isLeader;

    @Getter
    @Setter
    private int gwd;

    @Getter
    @Setter
    private int zd;

    @Getter
    @Setter
    private int loyaltyBonus;

    @Getter
    @Setter
    private LocalDateTime factionJoin;

    @Getter
    @Setter
    private Crew crew;

    @Getter
    @Setter
    private CrewRank crewRank;

    public PlayerData(Player player) {
        this.player = player;
        this.playerPetManager = new PlayerPetManager(this, player);
        this.playerFFAStatsManager = new PlayerFFAStatsManager(player);
        this.playerPowerUpManager = new PlayerPowerUpManager(player, this);
        inventory = new PlayerInventoryManager(this);
        loadIllnesses();
        loadClickedEventBlocks();
        loadWeapons();
        loadWanteds();
        loadLicenses();
        loadJobSkills();
    }

    public PlayerData() {
    }

    public Integer getBank() {
        return bank;
    }

    public void setBank(Integer bank) {
        this.bank = bank;
    }

    @SneakyThrows
    public void addCrypto(float amount, String reason, boolean silent) {
        setCrypto(crypto + amount);
        Main.getInstance().getCoreDatabase().updateAsync("UPDATE players SET crypto = ? WHERE uuid = ?", crypto, player.getUniqueId().toString());
        Main.getInstance().getCoreDatabase().insertAsync("INSERT INTO crypto_transactions (uuid, amount, reason) VALUES (?, ?, ?)", player.getUniqueId().toString(), amount, reason);

        if (!silent)
            player.sendMessage("§8[§eWallet§8]§7§l Neue Transaktion§7: +" + amount + " Coins (" + reason + ")");
    }

    @SneakyThrows
    public void removeCrypto(float amount, String reason, boolean silent) {
        setCrypto(crypto - amount);

        Main.getInstance().getCoreDatabase().updateAsync("UPDATE players SET crypto = ? WHERE uuid = ?", crypto, player.getUniqueId().toString());

        /*float reversedAmount = 0;
        reversedAmount *= amount;*/
        Main.getInstance().getCoreDatabase().insertAsync("INSERT INTO crypto_transactions (uuid, amount, reason) VALUES (?, ?, ?)", player.getUniqueId().toString(), -amount, reason);

        if (!silent)
            player.sendMessage("§8[§eWallet§8]§7§l Neue Transaktion§7: -" + amount + " Coins (" + reason + ")");
    }

    @SneakyThrows
    public void addIllness(PlayerIllness playerIllness, boolean save) {
        if (illnesses.stream().anyMatch(pi -> pi.getIllnessType().equals(playerIllness.getIllnessType()))) return;
        illnesses.add(playerIllness);
        if (save) {
            Main.getInstance().getCoreDatabase().insertAndGetKeyAsync("INSERT INTO player_illness (uuid, illness) VALUES (?, ?)", player.getUniqueId().toString(), playerIllness.getIllnessType().name())
                    .thenApply(key -> {
                        key.ifPresent(playerIllness::setId);
                        return null;
                    });
        }
    }

    @SneakyThrows
    public void removeIllness(PlayerIllness playerIllness, boolean save) {
        illnesses.remove(playerIllness);
        if (save) {
            Main.getInstance().getCoreDatabase().deleteAsync("DELETE FROM player_illness WHERE id = ?", playerIllness.getId());
        }
    }

    @SneakyThrows
    private void loadIllnesses() {
        illnesses.clear();
        database.executeQueryAsync("SELECT * FROM player_illness WHERE uuid = ?", player.getUniqueId().toString())
                .thenAccept(result -> {
                    for (Map<String, Object> row : result) {
                        PlayerIllness playerIllness = new PlayerIllness(IllnessType.valueOf((String) row.get("illness")));
                        playerIllness.setId((Integer) row.get("id"));
                        illnesses.add(playerIllness);
                    }
                });
    }

    private void loadJobSkills() {
        jobSkills.clear();
        database.executeQueryAsync("SELECT * FROM player_jobskills WHERE uuid = ?", player.getUniqueId().toString())
                .thenAccept(result -> {
                    for (Map<String, Object> row : result) {
                        MiniJob job = MiniJob.valueOf((String) row.get("job"));
                        int level = (Integer) row.get("level");
                        int exp = (Integer) row.get("exp");
                        JobSkill jobSkill = new CoreJobSkill(getVoidPlayer(), job, level, exp);
                        jobSkills.add(jobSkill);
                    }
                });
    }

/*    @SneakyThrows
    private void loadIllnesses() {
        illnesses.clear();
        try (Connection connection = Main.getInstance().mySQL.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM player_illness WHERE uuid = ?")) {
            statement.setString(1, player.getUniqueId().toString());
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    String illnessType = result.getString("illness");
                    int id = result.getInt("id");
                    PlayerIllness playerIllness = new PlayerIllness(IllnessType.valueOf(illnessType));
                    playerIllness.setId(id);
                    illnesses.add(playerIllness);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }*/

    @SneakyThrows
    private void loadClickedEventBlocks() {
        clickedEventBlocks.clear();
        String sql = "SELECT * FROM player_eventblocks_clicked WHERE uuid = ?";

        try (Connection connection = Main.getInstance().coreDatabase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, player.getUniqueId().toString());

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    ClickedEventBlock block = new ClickedEventBlock(result.getInt("blockId"));
                    clickedEventBlocks.add(block);
                }
            }
        }
    }

    @SneakyThrows
    private void loadWeapons() {
        String sql = "SELECT * FROM player_gun_cabinet WHERE uuid = ?";
        try (Connection connection = Main.getInstance().coreDatabase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, player.getUniqueId().toString());

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    weapons.add(new PlayerWeapon(
                            result.getInt("id"),
                            Weapon.valueOf(result.getString("weapon")),
                            result.getInt("wear"),
                            result.getInt("ammo"),
                            WeaponType.NORMAL
                    ));
                }
            }
        }
    }

    @SneakyThrows
    private void loadWanteds() {
        String sql = "SELECT * FROM player_wanteds WHERE uuid = ?";
        try (Connection connection = Main.getInstance().coreDatabase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, player.getUniqueId().toString());
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    wanted = new PlayerWanted(
                            result.getInt("id"),
                            result.getInt("wantedId"),
                            UUID.fromString(result.getString("issuer")),
                            result.getTimestamp("issued").toLocalDateTime(),
                            result.getString("variations")
                    );
                }
            }
        }
    }

    @SneakyThrows
    private void loadLicenses() {
        String sql = "SELECT * FROM player_licenses WHERE uuid = ?";
        try (Connection connection = Main.getInstance().coreDatabase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, player.getUniqueId().toString());
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    License license = License.valueOf(result.getString("license"));
                    licenses.add(license);
                }
            }
        }
    }


    @SneakyThrows
    public boolean addClickedBlock(RegisteredBlock block) {
        if (clickedEventBlocks.stream().anyMatch(b -> b.getBlockId() == block.getId())) return false;
        if (clickedEventBlocks.size() == 0) setVariable("event::startTime", Utils.getTime());
        playerManager.addExp(player, Utils.random(10, 20));
        ClickedEventBlock clickedEventBlock = new ClickedEventBlock(block.getId());
        clickedEventBlocks.add(clickedEventBlock);
        Connection connection = Main.getInstance().coreDatabase.getConnection();
        PreparedStatement statement = connection.prepareStatement("INSERT INTO player_eventblocks_clicked (uuid, blockId) VALUES (?, ?)");
        statement.setString(1, player.getUniqueId().toString());
        statement.setInt(2, block.getId());
        statement.execute();
        statement.close();
        connection.close();
        return true;
    }

    public Collection<ClickedEventBlock> getClickedEventBlocks() {
        return clickedEventBlocks;
    }

    public PlayerIllness getIllness(IllnessType illnessType) {
        return illnesses.stream().filter(i -> i.getIllnessType().equals(illnessType)).findFirst().orElse(null);
    }

    public int getFactionGrade() {
        return faction_grade;
    }

    public void setFactionGrade(Integer faction_grade) {
        this.faction_grade = faction_grade;
    }

    public <T> void setVariable(String variable, T value) {
        if (this.variables.get(variable) != null) {
            this.variables.replace(variable, value);
        } else {
            this.variables.put(variable, value);
        }
    }

    public <T> T getVariable(String variable) {
        return (T) variables.get(variable);
    }

    public boolean canInteract() {
        return canInteract;
    }

    public boolean isJailed() {
        return isJailed;
    }

    public void setJailed(boolean isJailed) {
        this.isJailed = isJailed;
    }

    public void setIntVariable(String variable, Integer value) {
        if (this.integer_variables.get(variable) != null) {
            this.integer_variables.replace(variable, value);
        } else {
            this.integer_variables.put(variable, value);
        }
    }

    public int getIntVariable(String variable) {
        return integer_variables.get(variable);
    }

    public BossBar getBossBar(String identifier) {
        return bossBars.get(identifier);
    }

    public void setBossBar(String identifier, BossBar bossBar) {
        if (this.bossBars.get(identifier) != null) {
            this.bossBars.replace(identifier, bossBar);
            bossBar.removePlayer(player);
            bossBar.addPlayer(player);
        } else {
            this.bossBars.put(identifier, bossBar);
            bossBar.addPlayer(player);
        }
    }

    public void removeBossBar(String identifier) {
        BossBar bb = getBossBar(identifier);
        if (bb == null) return;
        bb.removeAll();
        bossBars.remove(identifier);
    }

    public Scoreboard getScoreboard(String scoreboard) {
        return scoreboards.get(scoreboard);
    }

    public void setScoreboard(String scoreboardName, Scoreboard scoreboard) {
        if (this.scoreboards.get(scoreboardName) != null) {
            this.scoreboards.replace(scoreboardName, scoreboard);
            return;
        }
        this.scoreboards.put(scoreboardName, scoreboard);
    }

    public void removeScoreboard(Scoreboard scoreboard) {
        for (String key : scoreboards.keySet()) {
            if (scoreboards.get(key).equals(scoreboard)) {
                scoreboards.remove(key);
                return;
            }
        }
    }

    public void removeScoreboard(String scoreboardName) {
        scoreboards.remove(scoreboardName);
    }

    public boolean isDead() {
        return isDead;
    }

    public void setDead(boolean dead) {
        isDead = dead;
    }

    public boolean isFlightmode() {
        return isFlightmode;
    }

    public void setFlightmode(boolean flightmode) {
        isFlightmode = flightmode;
    }

    public boolean isDuty() {
        return isDuty;
    }

    public void setDuty(boolean duty) {
        isDuty = duty;
    }

    public void setLocationVariable(String variable, Location value) {
        if (this.locationVariables.get(variable) != null) {
            this.locationVariables.replace(variable, value);
        } else {
            this.locationVariables.put(variable, value);
        }
    }

    public Location getLocationVariable(String variable) {
        return locationVariables.get(variable);
    }

    public boolean hasAnwalt() {
        return hasAnwalt;
    }

    public boolean isAFK() {
        return isAFK;
    }

    public void setAFK(boolean AFK) {
        isAFK = AFK;
    }

    public void setCuffed(boolean cuffed) {
        this.cuffed = cuffed;
        if (cuffed) {
            player.setWalkSpeed(0);
            player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0);
            player.setFlying(false);
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 0, true, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, -12, true, false));
        } else {
            player.setWalkSpeed(0.2F);
            player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.1);
            player.removePotionEffect(PotionEffectType.JUMP);
            player.removePotionEffect(PotionEffectType.SLOW);
        }
    }

    @SneakyThrows
    public void addMoney(int amount, String reason) {
        Main.getInstance().beginnerpass.didQuest(player, 2, amount);
        setBargeld(getBargeld() + amount);
        Main.getInstance().coreDatabase.updateAsync("UPDATE players SET bargeld = ? WHERE uuid = ?", getBargeld(), player.getUniqueId().toString());
        Main.getInstance().coreDatabase.insertAsync("INSERT INTO money_logs (isPlus, uuid, amount, reason) VALUES (true, ?, ?, ?)", player.getUniqueId().toString(), amount, reason);
    }

    @Override
    public List<JobSkill> getJobSkills() {
        return jobSkills;
    }

    public VoidPlayer getVoidPlayer() {
        return VoidAPI.getPlayer(player);
    }

    @Override
    public JobSkill getJobSkill(MiniJob job) {
        JobSkill skill = jobSkills.stream().filter(js -> js.getJob().equals(job)).findFirst().orElse(null);
        if (skill == null) {
            skill = new CoreJobSkill(getVoidPlayer(), job, 1, 0);
            database.insertAsync("INSERT INTO player_jobskills (uuid, job, level, exp) VALUES (?, ?, ?, ?)", player.getUniqueId().toString(), job.name(), 1, 0);
            jobSkills.add(skill);
        }
        return skill;
    }

    @SneakyThrows
    public boolean removeMoney(int amount, String reason) {
        setBargeld(getBargeld() - amount);
        Main.getInstance().coreDatabase.updateAsync("UPDATE players SET bargeld = ? WHERE uuid = ?", getBargeld(), player.getUniqueId().toString());
        Main.getInstance().coreDatabase.insertAsync("INSERT INTO money_logs (isPlus, uuid, amount, reason) VALUES (false, ?, ?, ?)", player.getUniqueId().toString(), amount, reason);
        return true;
    }

    @SneakyThrows
    public void addBankMoney(int amount, String reason) {
        Main.getInstance().beginnerpass.didQuest(player, 2, amount);
        setBank(getBank() + amount);
        Main.getInstance().coreDatabase.updateAsync("UPDATE players SET bank = ? WHERE uuid = ?", getBank(), player.getUniqueId().toString());
        Main.getInstance().coreDatabase.insertAsync("INSERT INTO bank_logs (isPlus, uuid, amount, reason) VALUES (true, ?, ?, ?)", player.getUniqueId().toString(), amount, reason);

    }

    @SneakyThrows
    public boolean removeBankMoney(int amount, String reason) {
        setBank(getBank() - amount);
        Main.getInstance().coreDatabase.updateAsync("UPDATE players SET bank = ? WHERE uuid = ?", getBank(), player.getUniqueId().toString());
        Main.getInstance().coreDatabase.insertAsync("INSERT INTO bank_logs (isPlus, uuid, amount, reason) VALUES (false, ?, ?, ?)", player.getUniqueId().toString(), amount, reason);
        return true;
    }

    @SneakyThrows
    public void addKarma(int amount, boolean silent) {
        if (!silent) {
            player.sendMessage("§8[§3Karma§8]§b +" + amount + " Karma.");
        }
        karma += amount;
        Main.getInstance().getCoreDatabase().updateAsync("UPDATE players SET karma = ? WHERE uuid = ?",
                karma,
                uuid.toString());
    }

    @SneakyThrows
    public void removeKarma(int amount, boolean silent) {
        if (!silent) {
            player.sendMessage("§8[§3Karma§8]§b -" + amount + " Karma.");
        }
        karma -= amount;
        Main.getInstance().getCoreDatabase().updateAsync("UPDATE players SET karma = ? WHERE uuid = ?",
                karma,
                uuid.toString());
    }

    @SneakyThrows
    public void save() {
        Main.getInstance().getCoreDatabase().updateAsync("UPDATE players SET business = ?, deathTime = ?, isDead = ?, company = ?, atmBlown = ?, subGroup = ?, subGroup_grade = ?, karma = ?, isChurch = ?, isBaptized = ?, lastContract = ?, votes = ?, factionCooldown = ?, subTeam = ?, rewardId = ?, rewardTime = ? WHERE id = ?",
                getBusiness(),
                getDeathTime(),
                isDead(),
                (company != null) ? company.getId() : 0,
                getAtmBlown(),
                subGroupId,
                subGroupGrade,
                karma,
                isChurch,
                isBaptized,
                Timestamp.valueOf(lastContract).toString(),
                votes,
                (factionCooldown == null) ? null : factionCooldown.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                (subTeam != null) ? subTeam.getId() : -1,
                rewardId,
                rewardTime,
                getId());
    }

    public boolean isExecutiveFaction() {
        if (faction == null) return false;
        return faction.equalsIgnoreCase("FBI") || faction.equalsIgnoreCase("Polizei");
    }

    public boolean hasReceivedBonus() {
        return receivedBonus;
    }

    public boolean isHitmanDead() {
        return isHitmanDead;
    }

    public void setHitmanDead(boolean hitmanDead) {
        isHitmanDead = hitmanDead;
    }

    public boolean isStabilized() {
        return isStabilized;
    }

    public void setStabilized(boolean stabilized) {
        isStabilized = stabilized;
    }

    public SubGroup getSubGroup() {
        return Main.getInstance().factionManager.subGroups.getSubGroup(subGroupId);
    }

    public Collection<PlayerWorkstation> getWorkstations() {
        return workstations;
    }

    public PlayerWorkstation getWorkStation(Workstation workstation) {
        for (PlayerWorkstation w : workstations) {
            if (w.Workstation == workstation) {
                return w;
            }
        }
        return null;
    }

    public void addWorkstation(PlayerWorkstation workstation) {
        workstations.add(workstation);
    }

    public void removeWorkstation(PlayerWorkstation playerWorkstation) {
        workstations.remove(playerWorkstation);
    }

    public void addLicense(License license) {
        licenses.add(license);
    }

    public void addLicenseToDatabase(License license) {
        addLicense(license);
        Main.getInstance().getCoreDatabase().insertAsync("INSERT INTO player_licenses (uuid, license) VALUES (?, ?)", player.getUniqueId().toString(), license.name());
    }

    public void removeLicenseFromDatabase(License license) {
        addLicense(license);
        Main.getInstance().getCoreDatabase().deleteAsync("DELETE FROM player_licenses WHERE uuid ? AND license = ?", player.getUniqueId().toString(), license.name());
    }

    public void removeLicense(License license) {
        licenses.remove(license);
    }

    public boolean hasLicense(License license) {
        return licenses.contains(license);
    }

    public void addQuest(PlayerQuest playerQuest) {
        quests.add(playerQuest);
    }

    public void clearQuests() {
        quests.clear();
    }

    public Collection<PlayerQuest> getQuests() {
        return quests;
    }

    public void addBeginnerQuest(de.polo.core.game.base.extra.beginnerpass.PlayerQuest playerQuest) {
        beginnerQuests.add(playerQuest);
    }

    public Collection<de.polo.core.game.base.extra.beginnerpass.PlayerQuest> getBeginnerQuests() {
        return beginnerQuests;
    }

    public CompletableFuture<Boolean> setWanted(PlayerWanted playerWanted, boolean silent) {
        System.out.println("MOIN");
        return CompletableFuture.supplyAsync(() -> {
            System.out.println("www");
            if (getWanted() != null) {
                WantedReason reason = Main.getInstance().utils.getStaatUtil().getWantedReason(getWanted().getWantedId());
                WantedReason newReason = Main.getInstance().utils.getStaatUtil().getWantedReason(playerWanted.getWantedId());
                return reason.getWanted() <= newReason.getWanted();
            }
            System.out.println("yo");
            return true;
        }).thenCompose(result -> {
            System.out.println("hhh");
            if (!result) return CompletableFuture.completedFuture(false);
            System.out.println("h#ä");
            WantedReason wantedReason = Main.getInstance().utils.getStaatUtil().getWantedReason(playerWanted.getWantedId());

            System.out.println("Executing DELETE query...");
            String deleteQuery = "DELETE FROM player_wanteds WHERE uuid = ?";
            return Main.getInstance().getCoreDatabase()
                    .queryThreaded(deleteQuery, player.getUniqueId().toString())
                    .thenCompose(deleteResult -> {
                        System.out.println("DELETE query completed.");
                        String insertQuery = "INSERT INTO player_wanteds (uuid, issuer, wantedId) VALUES (?, ?, ?)";
                        System.out.println("Preparing to execute INSERT query...");
                        return Main.getInstance().getCoreDatabase().insertAndGetKeyAsync(insertQuery,
                                player.getUniqueId().toString(),
                                playerWanted.getIssuer().toString(),
                                playerWanted.getWantedId());
                    })
                    .thenApply(generatedKey -> {
                        System.out.println("INSERT query completed: Key = " + generatedKey);
                        if (generatedKey.isPresent()) {
                            playerWanted.setId(generatedKey.get());
                            this.wanted = playerWanted;
                            OfflinePlayer issuer = Bukkit.getOfflinePlayer(wanted.getIssuer());
                            player.sendMessage("§cDu hast ein Verbrechen begangen ( " + wantedReason.getReason() + " ). Beamter: " + issuer.getName());
                            player.sendMessage("§eDeine momentanen Wantedpunkte: " + wantedReason.getWanted());
                            return true;
                        }
                        System.out.println("No key was generated.");
                        return false;
                    });
        });
    }

    public void clearWanted() {
        wanted = null;
        Main.getInstance().getCoreDatabase().queryThreaded("DELETE FROM player_wanteds WHERE uuid = ?", uuid.toString());
    }

    public Collection<PlayerWeapon> getWeapons() {
        return weapons;
    }

    public PlayerWeapon getWeapon(Weapon weapon) {
        return weapons.stream().filter(x -> x.getWeapon() == weapon).findFirst().orElse(null);
    }

    public void giveWeapon(PlayerWeapon playerWeapon) {
        this.weapons.add(playerWeapon);
    }

    public int getHouseSlot() {
        int slots = houseSlot;
        if (getPermlevel() >= 20) slots++;
        return slots;
    }

    public int getNeeded_exp() {
        return (level + 1) * 1000;
    }

    public class AddonXP {
        private int fishingXP;
        private int fishingLevel;
        private int lumberjackXP;
        private int lumberjackLevel;
        private int minerXP;
        private int minerLevel;

        @Getter
        @Setter
        private int popularityXP;

        @Getter
        @Setter
        private int popularityLevel;

        public int getFishingXP() {
            return fishingXP;
        }

        public void setFishingXP(int fishingXP) {
            this.fishingXP = fishingXP;
        }

        public void addFishingXP(int amount) {
            fishingXP += amount;
            if (fishingXP >= EXPType.SKILL_FISHING.getLevelUpXp()) {
                setFishingLevel(getFishingLevel() + 1);
                setFishingLevel(getFishingLevel() - EXPType.SKILL_FISHING.getLevelUpXp());
            }
            Main.getInstance().getCoreDatabase().updateAsync("UPDATE player_addonxp SET fishingXP = ?, fishingLevel = ? WHERE uuid = ?", fishingXP, fishingLevel, player.getUniqueId().toString());
        }

        public int getFishingLevel() {
            return fishingLevel;
        }

        public void setFishingLevel(int fishingLevel) {
            this.fishingLevel = fishingLevel;
        }

        public int getMinerXP() {
            return minerXP;
        }

        public void setMinerXP(int minerXP) {
            this.minerXP = minerXP;
        }

        public void addMinerXP(int amount) {
            minerXP += amount;
            if (minerXP >= ((minerLevel + 1) * EXPType.SKILL_MINER.getLevelUpXp())) {
                setMinerLevel(getMinerLevel() + 1);
                setMinerXP(0);
            }
            Main.getInstance().getCoreDatabase().updateAsync("UPDATE player_addonxp SET minerXP = ?, miningLevel = ? WHERE uuid = ?", minerXP, minerLevel, player.getUniqueId().toString());
        }

        public int getMinerLevel() {
            return minerLevel;
        }

        public void setMinerLevel(int minerLevel) {
            this.minerLevel = minerLevel;
        }

        public int getLumberjackLevel() {
            return lumberjackLevel;
        }

        public void setLumberjackLevel(int lumberjackLevel) {
            this.lumberjackLevel = lumberjackLevel;
        }

        public int getLumberjackXP() {
            return lumberjackXP;
        }

        public void setLumberjackXP(int lumberjackXP) {
            this.lumberjackXP = lumberjackXP;
        }

        public void addLumberjackXP(int amount) {
            lumberjackXP += amount;
            try {
                Statement statement = Main.getInstance().coreDatabase.getStatement();
                statement.executeUpdate("UPDATE player_addonxp SET lumberjackXP = " + lumberjackXP + " WHERE uuid = '" + player.getUniqueId() + "'");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        public void addPopularity(int amount) {
            popularityXP += amount;
            if (popularityXP >= EXPType.POPULARITY.getLevelUpXp()) {
                setPopularityLevel(getPopularityLevel() + 1);
                setPopularityXP(getPopularityXP() - EXPType.POPULARITY.getLevelUpXp());
            }
            try {
                Statement statement = Main.getInstance().coreDatabase.getStatement();
                statement.executeUpdate("UPDATE player_addonxp SET popularityXP = " + popularityXP + ", popularityXP = " + popularityLevel + " WHERE uuid = '" + player.getUniqueId() + "'");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
