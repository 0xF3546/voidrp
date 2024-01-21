package de.polo.metropiacity.dataStorage;

import de.polo.metropiacity.Main;
import lombok.SneakyThrows;

import javax.swing.plaf.nimbus.State;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;

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
    private int forumID;
    private int forumID_Leader;
    private boolean hasLaboratory;
    private int jointsMade;
    public Storage storage = new Storage(this);
    public  Upgrades upgrades = new Upgrades(this);

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

    public int getForumID() {
        return forumID;
    }

    public void setForumID(int forumID) {
        this.forumID = forumID;
    }

    public int getForumID_Leader() {
        return forumID_Leader;
    }

    public void setForumID_Leader(int forumID_Leader) {
        this.forumID_Leader = forumID_Leader;
    }

    public boolean hasLaboratory() {
        return hasLaboratory;
    }

    public void setHasLaboratory(boolean hasLaboratory) {
        this.hasLaboratory = hasLaboratory;
    }

    public int getJointsMade() {
        return jointsMade;
    }

    public void setJointsMade(int jointsMade) {
        this.jointsMade = jointsMade;
    }

    @SneakyThrows
    public void addBankMoney(Integer amount, String reason) {
        setBank(getBank() + amount);
        Statement statement = Main.getInstance().mySQL.getStatement();
        statement.execute("INSERT INTO `faction_bank_logs` (`type`, `faction`, `amount`, `reason`, `isPlus`) VALUES ('einzahlung', '" + getName() + "', " + amount + ", '" + reason + "', true)");
        statement.execute("UPDATE `factions` SET `bank` = " + getBank() + " WHERE `name` = '" + getName() + "'");
    }
    @SneakyThrows
    public boolean removeFactionMoney(Integer amount, String reason)  {
        boolean returnval = false;
        if (getBank() >= amount) {
            setBank(getBank() - amount);
            Statement statement = Main.getInstance().mySQL.getStatement();
            statement.execute("INSERT INTO `faction_bank_logs` (`type`, `faction`, `amount`, `reason`, `isPlus`) VALUES ('auszahlung', '" + getName() + "', " + amount + ", '" + reason + "', false)");
            statement.execute("UPDATE `factions` SET `bank` = " + getBank() + " WHERE `name` = '" + getName() + "'");
            returnval = true;
        }
        return returnval;
    }

    public class Storage {
        private int weed;
        private int joint;
        private int cocaine;
        private int kevlar;
        private final FactionData factionData;
        private int noble_joint;
        private int proceedingAmount;
        private LocalDateTime proceedingStarted;
        public Storage(FactionData factionData) {
            this.factionData = factionData;
        }

        public int getWeed() {
            return weed;
        }

        public void setWeed(int weed) {
            this.weed = weed;
        }

        public int getJoint() {
            return joint;
        }

        public void setJoint(int joint) {
            this.joint = joint;
        }

        public int getCocaine() {
            return cocaine;
        }

        public void setCocaine(int cocaine) {
            this.cocaine = cocaine;
        }

        public int getKevlar() {
            return kevlar;
        }

        public void setKevlar(int kevlar) {
            this.kevlar = kevlar;
        }

        @SneakyThrows
        public void save() {
            Statement statement = Main.getInstance().mySQL.getStatement();
            statement.execute("UPDATE faction_storage SET weed = " + getWeed() + ", joint = " + getJoint() + ", cocaine = " + getCocaine() + ", kevlar = " + getKevlar() + ", noble_joint = " + getNoble_joint() + " WHERE factionId = " + factionData.getId());
        }

        public int getNoble_joint() {
            return noble_joint;
        }

        public void setNoble_joint(int noble_joint) {
            this.noble_joint = noble_joint;
        }
        public boolean proceedWeed(int amount) {
            if (getProceedingAmount() != 0) {
                return false;
            }
            setProceedingAmount(amount);
            setProceedingStarted(LocalDateTime.now());
            return true;
        }
        public int getProceedingAmount() {
            return proceedingAmount;
        }

        public void setProceedingAmount(int proceedingAmount) {
            this.proceedingAmount = proceedingAmount;
        }

        public LocalDateTime getProceedingStarted() {
            return proceedingStarted;
        }

        public void setProceedingStarted(LocalDateTime proceedingStarted) {
            this.proceedingStarted = proceedingStarted;
        }
    }

    public class Upgrades {
        private final FactionData factionData;
        public Upgrades(FactionData factionData) {
            this.factionData = factionData;
        }
        private int drugEarningLevel;
        private int taxLevel;
        private int weaponLevel;
        private float drugEarning;
        private int tax;
        private float weapon;

        public float getDrugEarning() {
            return drugEarning;
        }

        public void setDrugEarning(float drugEarning) {
            this.drugEarning = drugEarning;
        }

        public int getTax() {
            return tax;
        }

        public void setTax(int tax) {
            this.tax = tax;
        }

        public float getWeapon() {
            return weapon;
        }

        public void setWeapon(float weapon) {
            this.weapon = weapon;
        }

        @SneakyThrows
        public void save() {
            Statement statement = Main.getInstance().mySQL.getStatement();
            statement.executeUpdate("UPDATE faction_upgrades SET drug_earning = " + getDrugEarningLevel() + ", tax = " + getTaxLevel() + ", weapon = " + getWeaponLevel() + " WHERE factionId = " + factionData.getId());
            setDrugEarning(getDrugEarningLevel() * 0.1f);
            setTax(10000000 + (getTaxLevel() * 2000000));
            setWeapon(getWeaponLevel() * 2.5f);
        }

        public int getDrugEarningLevel() {
            return drugEarningLevel;
        }

        public void setDrugEarningLevel(int drugEarningLevel) {
            this.drugEarningLevel = drugEarningLevel;
        }

        public int getTaxLevel() {
            return taxLevel;
        }

        public void setTaxLevel(int taxLevel) {
            this.taxLevel = taxLevel;
        }

        public int getWeaponLevel() {
            return weaponLevel;
        }

        public void setWeaponLevel(int weaponLevel) {
            this.weaponLevel = weaponLevel;
        }
    }
}
