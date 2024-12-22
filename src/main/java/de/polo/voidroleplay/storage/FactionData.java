package de.polo.voidroleplay.storage;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.game.faction.blacklist.BlacklistReason;
import de.polo.voidroleplay.utils.Utils;
import de.polo.voidroleplay.utils.enums.RoleplayItem;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.banner.Pattern;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;

public class FactionData {
    public Storage storage = new Storage(this);
    public Upgrades upgrades = new Upgrades(this);
    public factionEquip equip = new factionEquip(this);
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
    private int laboratory;
    private boolean isBadFrak;
    private int subGroupId;
    private int cooperationPartner;

    @Getter
    @Setter
    private int tookOut;
    @Getter
    @Setter
    private ChatColor chatColor = ChatColor.GRAY;
    @Getter
    @Setter
    private List<Pattern> bannerPattern = null;
    @Getter
    @Setter
    private Material bannerColor = null;
    @Getter
    @Setter
    private int allianceFaction;
    @Getter
    @Setter
    private String motd;
    @Getter
    @Setter
    private List<BlacklistReason> blacklistReasons = new ObjectArrayList<>();
    @Getter
    @Setter
    private boolean active;

    @Getter
    @Setter
    private int equipPoints;

    @SneakyThrows
    public void loadReasons() {
        Connection connection = Main.getInstance().mySQL.getConnection();
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM blacklistreasons WHERE faction = ?");
        statement.setInt(1, id);
        ResultSet result = statement.executeQuery();
        while (result.next()) {
            BlacklistReason reason = new BlacklistReason(result.getString("reason"), result.getInt("price"), result.getInt("kills"));
            reason.setId(result.getInt("id"));
            blacklistReasons.add(reason);
        }
    }

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
        Main.getInstance().getMySQL().updateAsync("UPDATE factions SET bank = ? WHERE id = ?", getBank(), getId());
        Main.getInstance().getMySQL().insertAsync("INSERT INTO faction_bank_logs (type, faction, amount, reason, isPlus) VALUES ('einzahlung', ?, ?, ?, true)",
                getName(),
                amount,
                reason);
    }

    @SneakyThrows
    public boolean removeFactionMoney(Integer amount, String reason) {
        if (getBank() >= amount) {
            setBank(getBank() - amount);
            Main.getInstance().getMySQL().updateAsync("UPDATE factions SET bank = ? WHERE id = ?", getBank(), getId());
            Main.getInstance().getMySQL().insertAsync("INSERT INTO faction_bank_logs (type, faction, amount, reason, isPlus) VALUES ('auszahlung', ?, ?, ?, false)",
                    getName(),
                    amount,
                    reason);
            return true;
        }
        return false;
    }

    public int getLaboratory() {
        return laboratory;
    }

    public void setLaboratory(int laboratory) {
        this.laboratory = laboratory;
    }

    public boolean isBadFrak() {
        return isBadFrak;
    }

    public void setBadFrak(boolean badFrak) {
        isBadFrak = badFrak;
    }

    public int getSubGroupId() {
        return subGroupId;
    }

    public void setSubGroupId(int subGroupId) {
        this.subGroupId = subGroupId;
    }

    public SubGroup getSubGroup() {
        return Main.getInstance().factionManager.subGroups.getSubGroup(subGroupId);
    }

    @SneakyThrows
    public void save() {
        Main.getInstance().getMySQL().updateAsync("UPDATE factions SET subGroup = ?, alliance = ?, equipPoints = ? WHERE id = ?",
                subGroupId,
                allianceFaction,
                equipPoints,
                id);
    }

    @SneakyThrows
    public void addBlacklistReason(BlacklistReason blacklistReason, boolean save) {
        blacklistReasons.add(blacklistReason);
        if (save) {
            Connection connection = Main.getInstance().mySQL.getConnection();
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO blacklistreasons (kills, price, reason, faction) VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            statement.setInt(1, blacklistReason.getKills());
            statement.setInt(2, blacklistReason.getPrice());
            statement.setString(3, blacklistReason.getReason());
            statement.setInt(4, blacklistReason.getFactionId());

            statement.executeUpdate();
            ResultSet generatedKeys = statement.getGeneratedKeys();

            if (generatedKeys.next()) {
                int generatedId = generatedKeys.getInt(1);
                blacklistReason.setId(generatedId);
            }

            generatedKeys.close();
            statement.close();
            connection.close();
        }
    }

    @SneakyThrows
    public void removeBlacklistReason(BlacklistReason blacklistReason, boolean save) {
        blacklistReasons.remove(blacklistReason);
        if (save) {
            Main.getInstance().getMySQL().deleteAsync("DELETE FROM blacklistreasons WHERE id = ?",
                    blacklistReason.getId());
        }
    }

    public BlacklistReason getBlacklistReasonById(int id) {
        for (BlacklistReason reason : getBlacklistReasons()) {
            if (reason.getId() == id) return reason;
        }
        return null;
    }


    public int getCooperationPartner() {
        return cooperationPartner;
    }

    public void setCooperationPartner(int cooperationPartner) {
        this.cooperationPartner = cooperationPartner;
    }

    public class Storage {
        private final FactionData factionData;
        private int weed;
        private int joint;
        private int cocaine;
        private int kevlar;
        private int noble_joint;
        private int proceedingAmount;
        @Getter
        @Setter
        private int crystal;
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
            Main.getInstance().getMySQL().updateAsync("UPDATE faction_storage SET weed = ?, joint = ?, cocaine = ?, kevlar = ?, noble_joint = ?, crystal = ? WHERE factionId = ?",
                    getWeed(),
                    getJoint(),
                    getCocaine(),
                    getKevlar(),
                    getNoble_joint(),
                    getCrystal(),
                    factionData.getId());
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
            setProceedingStarted(Utils.getTime());
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

        public int getAmount(RoleplayItem item) {
            int amount = 0;
            switch (item) {
                case SNUFF:
                    amount = cocaine;
                    break;
                case PIPE_TOBACCO:
                    amount = weed;
                    break;
                case CIGAR:
                    amount = noble_joint;
                    break;
                case PIPE:
                case FACTION_PIPE:
                    amount = joint;
                    break;
                case CRYSTAL:
                    amount = crystal;
                    break;
            }
            return amount;
        }

        public void removeItem(RoleplayItem item, int amount) {
            switch (item) {
                case SNUFF:
                    setCocaine(cocaine - amount);
                    break;
                case PIPE_TOBACCO:
                    setWeed(weed - amount);
                    break;
                case CIGAR:
                    setNoble_joint(noble_joint - amount);
                    break;
                case FACTION_PIPE:
                case PIPE:
                    setJoint(joint - amount);
                    break;
                case CRYSTAL:
                    setCrystal(crystal - amount);
                    break;
            }
        }

        public void addItem(RoleplayItem item, int amount) {
            switch (item) {
                case SNUFF:
                    setCocaine(cocaine + amount);
                    break;
                case PIPE_TOBACCO:
                    setWeed(weed + amount);
                    break;
                case CIGAR:
                    setNoble_joint(noble_joint + amount);
                    break;
                case FACTION_PIPE:
                case PIPE:
                    setJoint(joint + amount);
                    break;
                case CRYSTAL:
                    setCrystal(crystal + amount);
                    break;
            }
        }
    }

    public class Upgrades {
        private final FactionData factionData;
        private int drugEarningLevel;
        private int taxLevel;
        private int weaponLevel;
        private float drugEarning;
        private int tax;
        private float weapon;

        public Upgrades(FactionData factionData) {
            this.factionData = factionData;
        }

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

        public void calculate() {
            setDrugEarning(getDrugEarningLevel() * 0.1f);
            setTax(10000000 + (getTaxLevel() * 2000000));
            setWeapon(getWeaponLevel() * 2.5f);
        }

        @SneakyThrows
        public void save() {
            Main.getInstance().getMySQL().updateAsync("UPDATE faction_upgrades SET drug_earning = ?, tax = ?, weapon = ? WHERE factionId = ?", getDrugEarningLevel(), getTaxLevel(), getWeaponLevel(), factionData.getId());
            calculate();
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

    public class factionEquip {
        private final FactionData factionData;
        private int sturmgewehr;
        private int sturmgewehr_ammo;

        public factionEquip(FactionData factionData) {
            this.factionData = factionData;
        }

        public int getSturmgewehr() {
            return sturmgewehr;
        }

        public void setSturmgewehr(int sturmgewehr) {
            this.sturmgewehr = sturmgewehr;
        }

        public int getSturmgewehr_ammo() {
            return sturmgewehr_ammo;
        }

        public void setSturmgewehr_ammo(int sturmgewehr_ammo) {
            this.sturmgewehr_ammo = sturmgewehr_ammo;
        }
    }
}
