package de.polo.voidroleplay.storage;

public class DBPlayerData {
    private int id;
    private String uuid;
    private String player_rank;
    private String faction;
    private int faction_grade;
    private Integer business;
    private int business_grade;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getPlayer_rank() {
        return player_rank;
    }

    public void setPlayer_rank(String player_rank) {
        this.player_rank = player_rank;
    }

    public String getFaction() {
        return faction;
    }

    public void setFaction(String faction) {
        this.faction = faction;
    }

    public int getFaction_grade() {
        return faction_grade;
    }

    public void setFaction_grade(int faction_grade) {
        this.faction_grade = faction_grade;
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
}
