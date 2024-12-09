package de.polo.voidroleplay.storage;

import org.bukkit.ChatColor;

public class RankData {
    private int id;
    private String rang;
    private int permlevel;
    private int TeamSpeakID;
    private boolean isSecondary;
    private int forumID;
    private ChatColor color;
    private String shortName;

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

    public boolean isSecondary() {
        return isSecondary;
    }

    public void setSecondary(boolean secondary) {
        isSecondary = secondary;
    }

    public int getForumID() {
        return forumID;
    }

    public void setForumID(int forumID) {
        this.forumID = forumID;
    }

    public ChatColor getColor() {
        return color;
    }

    public void setColor(ChatColor color) {
        this.color = color;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }
}
