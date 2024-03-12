package de.polo.voidroleplay.dataStorage;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.utils.playerUtils.Scoreboard;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.UUID;

public class PlayerData {
    @Getter
    private Player player;
    private String spawn;
    public PlayerData(Player player) {
        this.player = player;
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
    private boolean isAduty = false;
    private int level;
    private int exp;
    private int needed_exp;
    private Scoreboard scoreboard;
    private final HashMap<String, Scoreboard> scoreboards = new HashMap<>();
    private boolean isDead;
    private int deathTime;
    private int number;
    private boolean isFlightmode;
    private boolean isDuty;
    private String gender;
    private String birthday;
    private int houseSlot;
    private LocalDateTime rankDuration;
    private int boostDuration;
    private Location deathLocation;
    private String secondaryTeam;
    private String teamSpeakUID;
    private String job;
    private int hours;
    private int minutes;
    private Integer business;
    private int business_grade;
    private int warns;
    private Integer forumID;
    private HashMap<String, String> relationShip = new HashMap<>();
    private String bloodType;
    private boolean hasAnwalt;
    private boolean isAFK;
    private int Coins;

    private boolean cuffed;
    private PlayerLaboratory laboratory;

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

    public Scoreboard getScoreboard() {
        return scoreboard;
    }

    public Scoreboard getScoreboard(String scoreboard) {
        return scoreboards.get(scoreboard);
    }

    public void setScoreboard(Scoreboard scoreboard) {
        this.scoreboard = scoreboard;
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

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
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


    public int getBoostDuration() {
        return boostDuration;
    }

    public void setBoostDuration(int boostDuration) {
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
                Main.waitSeconds(2, () -> {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 0, true, false));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, -12, true, false));
                });
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
    public void addMoney(int amount) {
        Statement statement = Main.getInstance().mySQL.getStatement();
        assert statement != null;
        setBargeld(getBargeld() + amount);
        ResultSet result = statement.executeQuery("SELECT `bargeld` FROM `players` WHERE `uuid` = '" + player.getUniqueId() + "'");
        if (result.next()) {
            int res = result.getInt(1);
            statement.executeUpdate("UPDATE `players` SET `bargeld` = " + getBargeld() + " WHERE `uuid` = '" + player.getUniqueId() + "'");
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
        }
    }

    @SneakyThrows
    public void addBankMoney(int amount, String reason)  {
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
    public void save() {
        PreparedStatement statement = Main.getInstance().mySQL.getConnection().prepareStatement("UPDATE players SET business = ?, deathTime = ?, isDead = ? WHERE id = ?");
        statement.setInt(1, getBusiness());
        statement.setInt(2, getDeathTime());
        statement.setBoolean(3, isDead());
        statement.setInt(4, getId());
        statement.executeUpdate();
    }

    public String getSpawn() {
        return spawn;
    }

    public void setSpawn(String spawn) {
        this.spawn = spawn;
    }

    public class AddonXP {
        private int fishingXP;

        private int fishingLevel;

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
    }
}
