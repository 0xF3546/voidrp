package de.polo.void_roleplay.DataStorage;

import de.polo.void_roleplay.PlayerUtils.Scoreboard;

import java.util.HashMap;

public class PlayerData {
    private String firstname;
    private String lastname;
    private int bargeld;
    private int bank;
    private String rang;
    private int visum;
    private int permlevel;
    private String faction;
    private int faction_grade;
    private HashMap<String, String> variables = new HashMap<>();
    private HashMap<String, Integer> integer_variables = new HashMap<>();
    private boolean canInteract = true;
    private boolean isJailed;
    private int hafteinheiten = 0;
    private boolean isAduty = false;
    private int level;
    private int exp;
    private int needed_exp;
    private Scoreboard scoreboard;

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

    public void setIntVariable(String variable, int value) {
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
}
