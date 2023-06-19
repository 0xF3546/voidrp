package de.polo.metropiacity.DataStorage;

public class FactionData {
    private int id;
    private String primaryColor;
    private String secondaryColor;
    private String fullname;
    private String name;
    private int bank;
    private int PayDay;
    private int maxMember;
    private boolean hasBlacklist;
    private int teamSpeakID;
    private int channelGroupID;
    private String current_gangwar;
    private boolean doGangwar;

    public String getPrimaryColor() {
        return primaryColor;
    }

    public void setPrimaryColor(String primaryColor) {
        this.primaryColor = primaryColor;
    }

    public String getSecondaryColor() {
        return secondaryColor;
    }

    public void setSecondaryColor(String secondaryColor) {
        this.secondaryColor = secondaryColor;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getBank() {
        return bank;
    }

    public void setBank(int bank) {
        this.bank = bank;
    }

    public boolean hasBlacklist() {
        return hasBlacklist;
    }

    public void setHasBlacklist(boolean hasBlacklist) {
        this.hasBlacklist = hasBlacklist;
    }

    public int getTeamSpeakID() {
        return teamSpeakID;
    }

    public void setTeamSpeakID(int teamSpeakID) {
        this.teamSpeakID = teamSpeakID;
    }

    public int getChannelGroupID() {
        return channelGroupID;
    }

    public void setChannelGroupID(int channelGroupID) {
        this.channelGroupID = channelGroupID;
    }

    public String getCurrent_gangwar() {
        return current_gangwar;
    }

    public void setCurrent_gangwar(String current_gangwar) {
        this.current_gangwar = current_gangwar;
    }

    public boolean canDoGangwar() {
        return doGangwar;
    }

    public void setDoGangwar(boolean doGangwar) {
        this.doGangwar = doGangwar;
    }

    public int getPayDay() {
        return PayDay;
    }

    public void setPayDay(int payDay) {
        PayDay = payDay;
    }

    public int getMaxMember() {
        return maxMember;
    }

    public void setMaxMember(int maxMember) {
        this.maxMember = maxMember;
    }
}
