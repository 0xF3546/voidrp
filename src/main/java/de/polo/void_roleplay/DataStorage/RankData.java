package de.polo.void_roleplay.DataStorage;

public class RankData {
    private int id;
    private String rang;
    private int permlevel;
    private int TeamSpeakID;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRang() {
        return rang;
    }

    public void setRang(String rang) {
        this.rang = rang;
    }

    public int getPermlevel() {
        return permlevel;
    }

    public void setPermlevel(int permlevel) {
        this.permlevel = permlevel;
    }

    public int getTeamSpeakID() {
        return TeamSpeakID;
    }

    public void setTeamSpeakID(int teamSpeakID) {
        TeamSpeakID = teamSpeakID;
    }
}
