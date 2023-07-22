package de.polo.metropiacity.dataStorage;

import de.polo.metropiacity.playerUtils.Scoreboard;
import org.bukkit.Location;
import org.bukkit.inventory.Inventory;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.UUID;

public class PlayerData {
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
    private final HashMap<String, String> variables = new HashMap<>();
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
    private String business;
    private int business_grade;
    private int warns;
    private HashMap<String, String> relationShip = new HashMap<>();
    private String bloodType;

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

    public void setVariable(String variable, String value) {
        if (this.variables.get(variable) != null) {
            this.variables.replace(variable, value);
        } else {
            this.variables.put(variable, value);
        }
    }

    public String getVariable(String variable) {
        return variables.get(variable);
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

    public void setScoreboard(Scoreboard scoreboard) {
        this.scoreboard = scoreboard;
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

    public String getBusiness() {
        return business;
    }

    public void setBusiness(String business) {
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
}
