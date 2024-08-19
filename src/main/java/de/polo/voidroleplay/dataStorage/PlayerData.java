package de.polo.voidroleplay.dataStorage;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.game.base.extra.Seasonpass.PlayerQuest;
import de.polo.voidroleplay.game.base.farming.PlayerWorkstation;
import de.polo.voidroleplay.game.faction.laboratory.PlayerLaboratory;
import de.polo.voidroleplay.game.faction.staat.SubTeam;
import de.polo.voidroleplay.utils.PlayerPetManager;
import de.polo.voidroleplay.utils.Utils;
import de.polo.voidroleplay.utils.enums.EXPType;
import de.polo.voidroleplay.utils.enums.Gender;
import de.polo.voidroleplay.utils.enums.Workstation;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Date;

public class PlayerData {
    @Getter
    private PlayerPetManager playerPetManager;

    @Getter
    private Player player;
    private String spawn;

    private List<PlayerQuest> quests = new ArrayList<>();
    private List<de.polo.voidroleplay.game.base.extra.Beginnerpass.PlayerQuest> beginnerQuests = new ArrayList<>();

    public PlayerData(Player player) {
        this.player = player;
        this.playerPetManager = new PlayerPetManager(this, player);
    }

    private int id;
    private UUID uuid;
    private String firstname;
    private String lastname;
    private int bargeld;
    private int bank;
    private String rang;
    private int visum;
    private int permlevel;
    private String faction;
    private int faction_grade;
    private final HashMap<String, Object> variables = new HashMap<>();
    private final HashMap<String, Integer> integer_variables = new HashMap<>();
    private final HashMap<String, Location> locationVariables = new HashMap<>();
    private final HashMap<String, Inventory> inventoryVariables = new HashMap<>();
    private final HashMap<String, Integer> skillLevel = new HashMap<>();
    private final HashMap<String, Integer> skillExp = new HashMap<>();
    private final HashMap<String, Integer> skillNeeded_Exp = new HashMap<>();
    private boolean canInteract = true;
    private boolean isJailed;
    private int hafteinheiten = 0;
    @Getter
    @Setter
    private int jailParole = 0;
    private boolean isAduty = false;
    private int level;
    private int exp;
    private int needed_exp;
    private final HashMap<String, Scoreboard> scoreboards = new HashMap<>();
    private final HashMap<String, BossBar> bossBars = new HashMap<>();
    private boolean isDead = false;
    private boolean isStabilized = false;
    private boolean isHitmanDead = false;
    private int deathTime = 300;
    private int number = 0;
    private boolean isFlightmode = false;
    private boolean isDuty = false;
    private Gender gender;
    private Date birthday;
    private int houseSlot;
    private LocalDateTime rankDuration;
    private LocalDateTime boostDuration;
    @Getter
    @Setter
    private LocalDateTime lastContract;
    private Location deathLocation;
    private String secondaryTeam;
    private String teamSpeakUID;
    private String job;
    private int hours;
    private int minutes;
    private Integer business;
    private int business_grade;
    private int warns = 0;
    private Integer forumID;
    private HashMap<String, String> relationShip = new HashMap<>();
    private String bloodType;
    private boolean hasAnwalt;
    private boolean isAFK = false;
    private int Coins = 0;

    private boolean cuffed;
    private PlayerLaboratory laboratory;
    private Company company;
    private CompanyRole companyRole;
    private boolean hudEnabled;
    private LocalDateTime dailyBonusRedeemed;
    private LocalDateTime lastPayDay;

    @Getter
    @Setter
    private int payday;

    private int currentHours;
    private int atmBlown;

    private boolean receivedBonus;
    private int subGroupId;
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
    private List<PlayerWorkstation> workstations = new ArrayList<>();

    @Getter
    @Setter
    private LocalDateTime factionCooldown;

    @Getter
    @Setter
    private SubTeam subTeam = null;

    @Getter
    @Setter
    private int votes;

    public PlayerData() {
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setBargeld(Integer bargeld) {
        this.bargeld = bargeld;
    }

    public Integer getBargeld() {
        return bargeld;
    }

    public void setBank(Integer bank) {
        this.bank = bank;
    }

    public Integer getBank() {
        return bank;
    }

    public void setRang(String rang) {
        this.rang = rang;
    }

    public String getRang() {
        return rang;
    }

    public void setVisum(int visum) {
        this.visum = visum;
    }

    public int getVisum() {
        return visum;
    }

    public void setPermlevel(int permlevel) {
        this.permlevel = permlevel;
    }

    public int getPermlevel() {
        return permlevel;
    }

    public void setFaction(String faction) {
        this.faction = faction;
    }

    public String getFaction() {
        return faction;
    }

    public void setFactionGrade(Integer faction_grade) {
        this.faction_grade = faction_grade;
    }

    public int getFactionGrade() {
        return faction_grade;
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

    public void setCanInteract(boolean canInteract) {
        this.canInteract = canInteract;
    }

    public boolean isJailed() {
        return isJailed;
    }

    public void setJailed(boolean isJailed) {
        this.isJailed = isJailed;
    }

    public int getHafteinheiten() {
        return hafteinheiten;
    }

    public void setHafteinheiten(int hafteinheiten) {
        this.hafteinheiten = hafteinheiten;
    }

    public boolean isAduty() {
        return isAduty;
    }

    public void setAduty(boolean aduty) {
        isAduty = aduty;
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

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getExp() {
        return exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public int getNeeded_exp() {
        return needed_exp;
    }

    public void setNeeded_exp(int needed_exp) {
        this.needed_exp = needed_exp;
    }

    public BossBar getBossBar(String identifier) {
        return bossBars.get(identifier);
    }

    public void setBossBar(String identifier, BossBar bossBar) {
        if (this.bossBars.get(identifier) != null) {
            this.bossBars.replace(identifier, bossBar);
        }
        this.bossBars.put(identifier, bossBar);
        bossBar.addPlayer(player);
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

    public Integer getSkillLevel(String type) {
        return skillLevel.get(type);
    }

    public void setSkillLevel(String type, Integer skillLevel) {
        if (this.skillLevel.get(type) != null) {
            this.skillLevel.replace(type, skillLevel);
        } else {
            this.skillLevel.put(type, skillLevel);
        }
    }

    public Integer getSkillExp(String type) {
        return skillExp.get(type);
    }

    public void setSkillExp(String type, Integer skillLevel) {
        if (this.skillExp.get(type) != null) {
            this.skillExp.replace(type, skillLevel);
        } else {
            this.skillExp.put(type, skillLevel);
        }
    }

    public Integer getSkillNeeded_Exp(String type) {
        return skillNeeded_Exp.get(type);
    }

    public void setSkillNeeded_Exp(String type, Integer skillLevel) {
        if (this.skillNeeded_Exp.get(type) != null) {
            this.skillNeeded_Exp.replace(type, skillLevel);
        } else {
            this.skillNeeded_Exp.put(type, skillLevel);
        }
    }

    public boolean isDead() {
        return isDead;
    }

    public void setDead(boolean dead) {
        isDead = dead;
    }

    public int getDeathTime() {
        return deathTime;
    }

    public void setDeathTime(int deathTime) {
        this.deathTime = deathTime;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
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


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getHouseSlot() {
        return houseSlot;
    }

    public void setHouseSlot(int houseSlot) {
        this.houseSlot = houseSlot;
    }


    public LocalDateTime getBoostDuration() {
        return boostDuration;
    }

    public void setBoostDuration(LocalDateTime boostDuration) {
        this.boostDuration = boostDuration;
    }

    public Location getDeathLocation() {
        return deathLocation;
    }

    public void setDeathLocation(Location deathLocation) {
        this.deathLocation = deathLocation;
    }

    public String getSecondaryTeam() {
        return secondaryTeam;
    }

    public void setSecondaryTeam(String secondaryTeam) {
        this.secondaryTeam = secondaryTeam;
    }

    public String getTeamSpeakUID() {
        return teamSpeakUID;
    }

    public void setTeamSpeakUID(String teamSpeakUID) {
        this.teamSpeakUID = teamSpeakUID;
    }

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public int getHours() {
        return hours;
    }

    public void setHours(int hours) {
        this.hours = hours;
    }

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    public Integer getBusiness() {
        return business;
    }

    public void setBusiness(Integer business) {
        this.business = business;
    }

    public int getBusiness_grade() {
        return business_grade;
    }

    public void setBusiness_grade(int business_grade) {
        this.business_grade = business_grade;
    }

    public HashMap<String, String> getRelationShip() {
        return relationShip;
    }

    public void setRelationShip(HashMap<String, String> relationShip) {
        this.relationShip = relationShip;
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

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public void setInventoryVariable(String variable, Inventory value) {
        if (this.locationVariables.get(variable) != null) {
            this.inventoryVariables.replace(variable, value);
        } else {
            this.inventoryVariables.put(variable, value);
        }
    }

    public Inventory getInventoryVariable(String variable) {
        return inventoryVariables.get(variable);
    }

    public int getWarns() {
        return warns;
    }

    public void setWarns(int warns) {
        this.warns = warns;
    }

    public String getBloodType() {
        return bloodType;
    }

    public void setBloodType(String bloodType) {
        this.bloodType = bloodType;
    }

    public LocalDateTime getRankDuration() {
        return rankDuration;
    }

    public void setRankDuration(LocalDateTime rankDuration) {
        this.rankDuration = rankDuration;
    }

    public Integer getForumID() {
        return forumID;
    }

    public void setForumID(Integer forumID) {
        this.forumID = forumID;
    }

    public boolean hasAnwalt() {
        return hasAnwalt;
    }

    public void setHasAnwalt(boolean hasAnwalt) {
        this.hasAnwalt = hasAnwalt;
    }

    public boolean isAFK() {
        return isAFK;
    }

    public void setAFK(boolean AFK) {
        isAFK = AFK;
    }

    public int getCoins() {
        return Coins;
    }

    public void setCoins(int coins) {
        Coins = coins;
    }

    public final AddonXP addonXP = new AddonXP();

    public boolean isCuffed() {
        return cuffed;
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

    public PlayerLaboratory getLaboratory() {
        return laboratory;
    }

    public void setLaboratory(PlayerLaboratory laboratory) {
        this.laboratory = laboratory;
    }

    @SneakyThrows
    public void addMoney(int amount, String reason) {
        Main.getInstance().beginnerpass.didQuest(player, 2, amount);
        Statement statement = Main.getInstance().mySQL.getStatement();
        assert statement != null;
        setBargeld(getBargeld() + amount);
        ResultSet result = statement.executeQuery("SELECT `bargeld` FROM `players` WHERE `uuid` = '" + player.getUniqueId() + "'");
        if (result.next()) {
            int res = result.getInt(1);
            statement.executeUpdate("UPDATE `players` SET `bargeld` = " + getBargeld() + " WHERE `uuid` = '" + player.getUniqueId() + "'");
            statement.execute("INSERT INTO `money_logs` (`isPlus`, `uuid`, `amount`, `reason`) VALUES (true, '" + player.getUniqueId() + "', " + amount + ", '" + reason + "')");
        }
    }

    @SneakyThrows
    public void removeMoney(int amount, String reason) {
        Statement statement = Main.getInstance().mySQL.getStatement();
        assert statement != null;
        setBargeld(getBargeld() - amount);
        ResultSet result = statement.executeQuery("SELECT `bargeld` FROM `players` WHERE `uuid` = '" + player.getUniqueId() + "'");
        if (result.next()) {
            int res = result.getInt(1);
            statement.executeUpdate("UPDATE `players` SET `bargeld` = " + getBargeld() + " WHERE `uuid` = '" + player.getUniqueId() + "'");
            statement.execute("INSERT INTO `money_logs` (`isPlus`, `uuid`, `amount`, `reason`) VALUES (false, '" + player.getUniqueId() + "', " + amount + ", '" + reason + "')");
        }
    }

    @SneakyThrows
    public void addBankMoney(int amount, String reason) {
        Main.getInstance().beginnerpass.didQuest(player, 2, amount);
        Statement statement = Main.getInstance().mySQL.getStatement();
        assert statement != null;
        setBank(getBank() + amount);
        ResultSet result = statement.executeQuery("SELECT `bank` FROM `players` WHERE `uuid` = '" + player.getUniqueId() + "'");
        if (result.next()) {
            int res = result.getInt(1);
            statement.executeUpdate("UPDATE `players` SET `bank` = " + getBank() + " WHERE `uuid` = '" + player.getUniqueId() + "'");
            statement.execute("INSERT INTO `bank_logs` (`isPlus`, `uuid`, `amount`, `reason`) VALUES (true, '" + player.getUniqueId() + "', " + amount + ", '" + reason + "')");
        }
    }

    @SneakyThrows
    public void removeBankMoney(int amount, String reason) {
        Statement statement = Main.getInstance().mySQL.getStatement();
        assert statement != null;
        setBank(getBank() - amount);
        ResultSet result = statement.executeQuery("SELECT `bank` FROM `players` WHERE `uuid` = '" + player.getUniqueId() + "'");
        if (result.next()) {
            int res = result.getInt(1);
            statement.executeUpdate("UPDATE `players` SET `bank` = " + getBank() + " WHERE `uuid` = '" + player.getUniqueId() + "'");
            statement.execute("INSERT INTO `bank_logs` (`isPlus`, `uuid`, `amount`, `reason`) VALUES (false, '" + player.getUniqueId() + "', " + amount + ", '" + reason + "')");
        }
    }

    @SneakyThrows
    public void addKarma(int amount, boolean silent) {
        if (!silent) {
            player.sendMessage("§8[§3Karma§8]§b +" + amount + " Karma.");
        }
        karma += amount;
        Connection connection = Main.getInstance().mySQL.getConnection();
        PreparedStatement statement = connection.prepareStatement("UPDATE players SET karma = ? WHERE uuid = ?");
        statement.setInt(1, karma);
        statement.setString(2, uuid.toString());
        statement.executeUpdate();
    }

    @SneakyThrows
    public void removeKarma(int amount, boolean silent) {
        if (!silent) {
            player.sendMessage("§8[§3Karma§8]§b -" + amount + " Karma.");
        }
        karma -= amount;
        Connection connection = Main.getInstance().mySQL.getConnection();
        PreparedStatement statement = connection.prepareStatement("UPDATE players SET karma = ? WHERE uuid = ?");
        statement.setInt(1, karma);
        statement.setString(2, uuid.toString());
        statement.executeUpdate();
    }

    @SneakyThrows
    public void save() {
        PreparedStatement statement = Main.getInstance().mySQL.getConnection().prepareStatement("UPDATE players SET business = ?, deathTime = ?, isDead = ?, company = ?, atmBlown = ?, subGroup = ?, subGroup_grade = ?, karma = ?, isChurch = ?, isBaptized = ?, lastContract = ?, votes = ?, factionCooldown = ?, subTeam = ? WHERE id = ?");
        statement.setInt(1, getBusiness());
        statement.setInt(2, getDeathTime());
        statement.setBoolean(3, isDead());
        int companyId = 0;
        if (company != null) {
            companyId = company.getId();
        }
        statement.setInt(4, companyId);
        statement.setInt(5, getAtmBlown());
        statement.setInt(6, subGroupId);
        statement.setInt(7, subGroupGrade);
        statement.setInt(8, karma);
        statement.setBoolean(9, isChurch);
        statement.setBoolean(10, isBaptized);
        Timestamp timestamp = Timestamp.valueOf(lastContract);
        statement.setTimestamp(11, timestamp);
        statement.setInt(12, votes);
        String formattedBoostDuration = factionCooldown.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        statement.setString(13, formattedBoostDuration);
        if (subTeam == null) {
            statement.setInt(14, -1);
        } else {
            statement.setInt(14, subTeam.getId());
        }
        statement.setInt(15, getId());
        statement.executeUpdate();
    }

    public String getSpawn() {
        return spawn;
    }

    public void setSpawn(String spawn) {
        this.spawn = spawn;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public boolean isHudEnabled() {
        return hudEnabled;
    }

    public void setHudEnabled(boolean hudEnabled) {
        this.hudEnabled = hudEnabled;
    }

    public LocalDateTime getDailyBonusRedeemed() {
        return dailyBonusRedeemed;
    }

    public void setDailyBonusRedeemed(LocalDateTime dailyBonusRedeemed) {
        this.dailyBonusRedeemed = dailyBonusRedeemed;
    }

    public LocalDateTime getLastPayDay() {
        return lastPayDay;
    }

    public void setLastPayDay(LocalDateTime lastPayDay) {
        this.lastPayDay = lastPayDay;
    }

    public int getCurrentHours() {
        return currentHours;
    }

    public void setCurrentHours(int currentHours) {
        this.currentHours = currentHours;
    }

    public int getAtmBlown() {
        return atmBlown;
    }

    public void setAtmBlown(int atmBlown) {
        this.atmBlown = atmBlown;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public CompanyRole getCompanyRole() {
        return companyRole;
    }

    public void setCompanyRole(CompanyRole companyRole) {
        this.companyRole = companyRole;
    }

    public boolean hasReceivedBonus() {
        return receivedBonus;
    }

    public void setReceivedBonus(boolean receivedBonus) {
        this.receivedBonus = receivedBonus;
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

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public int getSubGroupId() {
        return subGroupId;
    }

    public void setSubGroupId(int subGroupId) {
        this.subGroupId = subGroupId;
    }

    public int getSubGroupGrade() {
        return subGroupGrade;
    }

    public void setSubGroupGrade(int subGroupGrade) {
        this.subGroupGrade = subGroupGrade;
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

    public void setWorkstations(List<PlayerWorkstation> workstations) {
        this.workstations = workstations;
    }
    public void addWorkstation(PlayerWorkstation workstation) {
        workstations.add(workstation);
    }

    public void removeWorkstation(PlayerWorkstation playerWorkstation) {
        workstations.remove(playerWorkstation);
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

    public void addBeginnerQuest(de.polo.voidroleplay.game.base.extra.Beginnerpass.PlayerQuest playerQuest) {
        beginnerQuests.add(playerQuest);
    }

    public Collection<de.polo.voidroleplay.game.base.extra.Beginnerpass.PlayerQuest> getBeginnerQuests() {
        return beginnerQuests;
    }

    public class AddonXP {
        private int fishingXP;
        private int fishingLevel;
        private int lumberjackXP;
        private int lumberjackLevel;

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
            try {
                Statement statement = Main.getInstance().mySQL.getStatement();
                statement.executeUpdate("UPDATE player_addonxp SET fishingXP = " + fishingXP + " WHERE uuid = '" + player.getUniqueId() + "'");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        public int getFishingLevel() {
            return fishingLevel;
        }

        public void setFishingLevel(int fishingLevel) {
            this.fishingLevel = fishingLevel;
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
                Statement statement = Main.getInstance().mySQL.getStatement();
                statement.executeUpdate("UPDATE player_addonxp SET lumberjackXP = " + lumberjackXP + " WHERE uuid = '" + player.getUniqueId() + "'");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        public void addPopularity(int amount) {
            popularityXP += amount;
            if (popularityXP >= EXPType.POPULARITY.getLevelUpXp()) {
                setPopularityLevel(getPopularityLevel() + 1);
            }
            try {
                Statement statement = Main.getInstance().mySQL.getStatement();
                statement.executeUpdate("UPDATE player_addonxp SET popularityXP = " + popularityXP + ", popularityXP = " + popularityLevel + " WHERE uuid = '" + player.getUniqueId() + "'");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
