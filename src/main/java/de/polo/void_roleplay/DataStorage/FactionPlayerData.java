package de.polo.void_roleplay.DataStorage;

public class FactionPlayerData {
    private int id;
    private String uuid;
    private String faction;
    private int faction_grade;

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
}
