package de.polo.voidroleplay.dataStorage;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.game.base.extra.PlayerIllness;
import de.polo.voidroleplay.game.base.extra.Seasonpass.PlayerQuest;
import de.polo.voidroleplay.game.base.farming.PlayerWorkstation;
import de.polo.voidroleplay.game.faction.laboratory.PlayerLaboratory;
import de.polo.voidroleplay.game.faction.staat.SubTeam;
import de.polo.voidroleplay.manager.ItemManager;
import de.polo.voidroleplay.manager.PlayerPetManager;
import de.polo.voidroleplay.utils.enums.*;
import de.polo.voidroleplay.utils.enums.Weapon;
import de.polo.voidroleplay.utils.playerUtils.PlayerFFAStatsManager;
import de.polo.voidroleplay.utils.playerUtils.PlayerPowerUpManager;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

public class PlayerData {
    @Getter
    private PlayerPetManager playerPetManager;

    @Getter
    private PlayerFFAStatsManager playerFFAStatsManager;

    @Getter
    private PlayerPowerUpManager playerPowerUpManager;

    @Getter
    private Player player;
    @Setter
    @Getter
    private String spawn;

    @Getter
    @Setter
    private boolean sendAdminMessages = false;

    @Getter
    @Setter
    private int rewardId;

    @Getter
    @Setter
    private int rewardTime;

    private final List<PlayerQuest> quests = new ArrayList<>();
    private final List<de.polo.voidroleplay.game.base.extra.Beginnerpass.PlayerQuest> beginnerQuests = new ArrayList<>();
    private final List<PlayerIllness> illnesses = new ArrayList<>();

    private final List<PlayerWeapon> weapons = new ArrayList<>();

    private final List<ClickedEventBlock> clickedEventBlocks = new ArrayList<>();

    public PlayerData(Player player) {
        this.player = player;
        this.playerPetManager = new PlayerPetManager(this, player);
        this.playerFFAStatsManager = new PlayerFFAStatsManager(player);
        this.playerPowerUpManager = new PlayerPowerUpManager(player, this);
        loadIllnesses();
        loadClickedEventBlocks();
        loadWeapons();
        loadWanteds();
    }

    @Getter
    @Setter
    private float crypto;

    @Setter
    @Getter
    private int id;
    @Setter
    @Getter
    private UUID uuid;
    @Getter
    @Setter
    private String firstname;
    @Getter
    @Setter
    private String lastname;

    @Getter
    @Setter
    private int bargeld;
    private int bank;
    @Getter
    @Setter
    private String rang;
    @Getter
    @Setter
    private int visum;
    @Getter
    @Setter
    private int permlevel;
    @Getter
    @Setter
    private String faction;
    private int faction_grade;

    @Getter
    @Setter
    private int eventPoints;

    private final HashMap<String, Object> variables = new HashMap<>();
    private final HashMap<String, Integer> integer_variables = new HashMap<>();
    private final HashMap<String, Location> locationVariables = new HashMap<>();
    private final HashMap<String, Integer> skillLevel = new HashMap<>();
    private final HashMap<String, Integer> skillExp = new HashMap<>();
    private final HashMap<String, Integer> skillNeeded_Exp = new HashMap<>();
    @Setter
    private boolean canInteract = true;
    private boolean isJailed;
    @Setter
    @Getter
    private int hafteinheiten = 0;
    @Getter
    @Setter
    private int jailParole = 0;
    private boolean isAduty = false;
    @Setter
    @Getter
    private int level;
    @Setter
    @Getter
    private int exp;
    @Setter
    @Getter
    private int needed_exp;
    private final HashMap<String, Scoreboard> scoreboards = new HashMap<>();
    private final HashMap<String, BossBar> bossBars = new HashMap<>();
    private boolean isDead = false;
    private boolean isStabilized = false;
    private boolean isHitmanDead = false;
    @Setter
    @Getter
    private int deathTime = 300;
    @Getter
    @Setter
    private boolean isFFADead = false;
    @Setter
    @Getter
    private int number = 0;
    private boolean isFlightmode = false;
    private boolean isDuty = false;
    @Setter
    @Getter
    private Gender gender;
    @Setter
    @Getter
    private Date birthday;
    @Setter
    @Getter
    private int houseSlot;
    @Setter
    @Getter
    private LocalDateTime rankDuration;
    @Setter
    @Getter
    private LocalDateTime boostDuration;
    @Getter
    @Setter
    private LocalDateTime lastContract;
    @Setter
    @Getter
    private Location deathLocation;
    @Setter
    @Getter
    private String secondaryTeam;
    @Setter
    @Getter
    private String teamSpeakUID;
    @Setter
    @Getter
    private String job;
    @Setter
    @Getter
    private int hours;
    @Setter
    @Getter
    private int minutes;
    @Setter
    @Getter
    private Integer business;
    @Setter
    @Getter
    private int business_grade;
    @Setter
    @Getter
    private int warns = 0;
    @Setter
    @Getter
    private Integer forumID;
    @Setter
    @Getter
    private HashMap<String, String> relationShip = new HashMap<>();
    @Setter
    @Getter
    private String bloodType;
    @Setter
    private boolean hasAnwalt;
    private boolean isAFK = false;
    @Setter
    @Getter
    private int Coins = 0;

    @Getter
    private boolean cuffed;
    @Setter
    @Getter
    private PlayerLaboratory laboratory;
    @Setter
    @Getter
    private Company company;
    @Setter
    @Getter
    private CompanyRole companyRole;
    @Setter
    @Getter
    private boolean hudEnabled;
    @Setter
    @Getter
    private LocalDateTime dailyBonusRedeemed;
    @Setter
    @Getter
    private LocalDateTime lastPayDay;

    @Getter
    @Setter
    private int payday;

    @Setter
    @Getter
    private int currentHours;
    @Setter
    @Getter
    private int atmBlown;

    @Setter
    private boolean receivedBonus;
    @Setter
    @Getter
    private int subGroupId;
    @Setter
    @Getter
    private int subGroupGrade;
    @Getter
    @Setter
    private boolean isChurch;

    @Getter
    @Setter
    private boolean isBaptized;

    @Getter
    @Setter
    private int karma;
    @Setter
    private List<PlayerWorkstation> workstations = new ArrayList<>();

    @Getter
    @Setter
    private LocalDateTime factionCooldown;

    @Getter
    @Setter
    private SubTeam subTeam = null;

    @Getter
    @Setter
    private int votes;

    @Getter
    private PlayerWanted wanted;

    @Getter
    @Setter
    private boolean isLeader;

    @Getter
    @Setter
    private int gwd;

    @Getter
    @Setter
    private int zd;

    public PlayerData() {
    }

    public void setBank(Integer bank) {
        this.bank = bank;
    }

    public Integer getBank() {
        return bank;
    }

    @SneakyThrows
    public void addCrypto(float amount, String reason, boolean silent) {
        setCrypto(crypto + amount);
        Main.getInstance().getMySQL().updateAsync("UPDATE players SET crypto = ? WHERE uuid = ?", crypto, player.getUniqueId().toString());
        Main.getInstance().getMySQL().insertAsync("INSERT INTO crypto_transactions (uuid, amount, reason) VALUES (?, ?, ?)", player.getUniqueId().toString(), amount, reason);

        if (!silent)
            player.sendMessage("§8[§eWallet§8]§7§l Neue Transaktion§7: +" + amount + " Coins (" + reason + ")");
    }

    @SneakyThrows
    public void removeCrypto(float amount, String reason, boolean silent) {
        setCrypto(crypto - amount);

        Main.getInstance().getMySQL().updateAsync("UPDATE players SET crypto = ? WHERE uuid = ?", crypto, player.getUniqueId().toString());

        /*float reversedAmount = 0;
        reversedAmount *= amount;*/
        Main.getInstance().getMySQL().insertAsync("INSERT INTO crypto_transactions (uuid, amount, reason) VALUES (?, ?, ?)", player.getUniqueId().toString(), -amount, reason);

        if (!silent)
            player.sendMessage("§8[§eWallet§8]§7§l Neue Transaktion§7: -" + amount + " Coins (" + reason + ")");
    }

    @SneakyThrows
    public void addIllness(PlayerIllness playerIllness, boolean save) {
        if (illnesses.stream().anyMatch(pi -> pi.getIllnessType().equals(playerIllness.getIllnessType()))) return;
        illnesses.add(playerIllness);
        if (save) {
            Main.getInstance().getMySQL().insertAndGetKeyAsync("INSERT INTO player_illness (uuid, illness) VALUES (?, ?)", player.getUniqueId().toString(), playerIllness.getIllnessType().name())
                    .thenAccept(key -> key.ifPresent(playerIllness::setId));
        }
    }

    @SneakyThrows
    public void removeIllness(PlayerIllness playerIllness, boolean save) {
        illnesses.remove(playerIllness);
        if (save) {
            Main.getInstance().getMySQL().deleteAsync("DELETE FROM player_illness WHERE id = ?", playerIllness.getId());
        }
    }

    @SneakyThrows
    private void loadIllnesses() {
        illnesses.clear();
        Connection connection = Main.getInstance().mySQL.getConnection();
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM player_illness WHERE uuid = ?");
        statement.setString(1, player.getUniqueId().toString());
        ResultSet result = statement.executeQuery();
        while (result.next()) {
            PlayerIllness playerIllness = new PlayerIllness(IllnessType.valueOf(result.getString("illness")));
            playerIllness.setId(result.getInt(id));
            illnesses.add(playerIllness);
        }
    }

    @SneakyThrows
    private void loadClickedEventBlocks() {
        clickedEventBlocks.clear();
        Connection connection = Main.getInstance().mySQL.getConnection();
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM player_eventblocks_clicked WHERE uuid = ?");
        statement.setString(1, player.getUniqueId().toString());
        ResultSet result = statement.executeQuery();
        while (result.next()) {
            ClickedEventBlock block = new ClickedEventBlock(result.getInt("blockId"));
            clickedEventBlocks.add(block);
        }
    }

    @SneakyThrows
    private void loadWeapons() {
        Connection connection = Main.getInstance().mySQL.getConnection();
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM player_gun_cabinet WHERE uuid = ?");
        statement.setString(1, player.getUniqueId().toString());
        ResultSet result = statement.executeQuery();
        while (result.next()) {
            weapons.add(new PlayerWeapon(result.getInt("id"), Weapon.valueOf(result.getString("weapon")),
                    result.getInt("wear"),
                    result.getInt("ammo"),
                    WeaponType.NORMAL));
        }
    }

    @SneakyThrows
    private void loadWanteds() {
        Connection connection = Main.getInstance().mySQL.getConnection();
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM player_wanteds WHERE uuid = ?");
        statement.setString(1, player.getUniqueId().toString());
        ResultSet result = statement.executeQuery();
        if (result.next()) {
            wanted = new PlayerWanted(
                    result.getInt("id"),
                    result.getInt("wantedId"),
                    UUID.fromString(result.getString("issuer")),
                    result.getTimestamp("issued").toLocalDateTime()
            );
        }
    }

    @SneakyThrows
    public boolean addClickedBlock(RegisteredBlock block) {
        if (clickedEventBlocks.stream().anyMatch(b -> b.getBlockId() == block.getId())) return false;
        Main.getInstance().playerManager.addExp(player, Main.random(10, 20));
        int money = Main.random(50, 100);
        player.sendMessage("§a+" + money + "$");
        addMoney(money, "Event-Block");
        ClickedEventBlock clickedEventBlock = new ClickedEventBlock(block.getId());
        clickedEventBlocks.add(clickedEventBlock);
        switch (clickedEventBlocks.size()) {
            case 10:
                addMoney(10000, "Gewinn 10 Köpfe");
                player.sendMessage("§8[§6Cookies§8]§a+10.000$ für 10 Köpfe");
                break;
            case 20:
                player.sendMessage("§8[§6Cookies§8]§a+5 Daily Chests für 20 Köpfe");
                player.getInventory().addItem(ItemManager.createItem(Material.CHEST, 10, 0, CaseType.DAILY.getDisplayName()));
                break;
            case 30:
                player.sendMessage("§8[§6Cookies§8]§a+6h Gameboost für 30 Köpfe");
                Main.getInstance().playerManager.addEXPBoost(player, 6);
                break;
            case 40:
                player.sendMessage("§8[§6Cookies§8]§a+1.000 EXP für 40 Köpfe");
                Main.getInstance().playerManager.addExp(player, 1000);
                break;
            case 50:
                player.sendMessage("§8[§6Cookies§8]§a+20 Daily Cases für 50 Köpfe");
                player.getInventory().addItem(ItemManager.createItem(Material.CHEST, 20, 0, CaseType.DAILY.getDisplayName()));
                break;
            case 60:
                addMoney(25000, "Gewinn 60 Köpfe");
                player.sendMessage("§8[§6Cookies§8]§a+25.000$ für 60 Köpfe");
                break;
            case 70:
                player.sendMessage("§8[§6Cookies§8]§a+10 Daily Cases & 10 XP Cases für 70 Köpfe");
                player.getInventory().addItem(ItemManager.createItem(Material.CHEST, 10, 0, "§bXP-Case"));
                player.getInventory().addItem(ItemManager.createItem(Material.CHEST, 10, 0, CaseType.DAILY.getDisplayName()));
                break;
            case 80:
                player.sendMessage("§8[§6Cookies§8]§a+25 Daily Cases für 80 Köpfe");
                player.getInventory().addItem(ItemManager.createItem(Material.CHEST, 25, 0, CaseType.DAILY.getDisplayName()));
                break;
        }
        Connection connection = Main.getInstance().mySQL.getConnection();
        PreparedStatement statement = connection.prepareStatement("INSERT INTO player_eventblocks_clicked (uuid, blockId) VALUES (?, ?)");
        statement.setString(1, player.getUniqueId().toString());
        statement.setInt(2, block.getId());
        statement.execute();
        statement.close();
        connection.close();
        return true;
    }

    public Collection<ClickedEventBlock> getClickedEventBlocks() {
        return clickedEventBlocks;
    }

    public PlayerIllness getIllness(IllnessType illnessType) {
        return illnesses.stream().filter(i -> i.getIllnessType().equals(illnessType)).findFirst().orElse(null);
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

    public boolean isJailed() {
        return isJailed;
    }

    public void setJailed(boolean isJailed) {
        this.isJailed = isJailed;
    }

    public boolean isAduty() {
        return isAduty;
    }

    public void setAduty(boolean aduty) {
        isAduty = aduty;
        if (aduty) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 0, true, false));

        } else {
            player.removePotionEffect(PotionEffectType.GLOWING);

        }
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

    public BossBar getBossBar(String identifier) {
        return bossBars.get(identifier);
    }

    public void setBossBar(String identifier, BossBar bossBar) {
        if (this.bossBars.get(identifier) != null) {
            this.bossBars.replace(identifier, bossBar);
        }
        this.bossBars.put(identifier, bossBar);
        bossBar.addPlayer(player);
    }

    public void removeBossBar(String identifier) {
        BossBar bb = getBossBar(identifier);
        if (bb == null) return;
        bb.removeAll();
        bossBars.remove(identifier);
    }

    public Scoreboard getScoreboard(String scoreboard) {
        return scoreboards.get(scoreboard);
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

    public boolean isDead() {
        return isDead;
    }

    public void setDead(boolean dead) {
        isDead = dead;
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


    public boolean hasAnwalt() {
        return hasAnwalt;
    }

    public boolean isAFK() {
        return isAFK;
    }

    public void setAFK(boolean AFK) {
        isAFK = AFK;
    }

    public final AddonXP addonXP = new AddonXP();

    public void setCuffed(boolean cuffed) {
        this.cuffed = cuffed;
        if (cuffed) {
            player.setWalkSpeed(0);
            player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0);
            player.setFlying(false);
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 0, true, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, -12, true, false));
        } else {
            player.setWalkSpeed(0.2F);
            player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.1);
            player.removePotionEffect(PotionEffectType.JUMP);
            player.removePotionEffect(PotionEffectType.SLOW);
        }
    }

    @SneakyThrows
    public void addMoney(int amount, String reason) {
        Main.getInstance().beginnerpass.didQuest(player, 2, amount);
        setBargeld(getBargeld() + amount);
        Main.getInstance().mySQL.updateAsync("UPDATE players SET bargeld = ? WHERE uuid = ?", getBargeld(), uuid.toString());
        Main.getInstance().mySQL.insertAsync("INSERT INTO money_logs (isPlus, uuid, amount, reason) VALUES (true, ?, ?, ?)", uuid.toString(), amount, reason);
    }

    @SneakyThrows
    public void removeMoney(int amount, String reason) {
        setBargeld(getBargeld() - amount);
        Main.getInstance().mySQL.updateAsync("UPDATE players SET bargeld = ? WHERE uuid = ?", getBargeld(), uuid.toString());
        Main.getInstance().mySQL.insertAsync("INSERT INTO money_logs (isPlus, uuid, amount, reason) VALUES (false, ?, ?, ?)", uuid.toString(), amount, reason);

    }

    @SneakyThrows
    public void addBankMoney(int amount, String reason) {
        Main.getInstance().beginnerpass.didQuest(player, 2, amount);
        setBank(getBank() + amount);
        Main.getInstance().mySQL.updateAsync("UPDATE players SET bank = ? WHERE uuid = ?", getBank(), uuid.toString());
        Main.getInstance().mySQL.insertAsync("INSERT INTO bank_logs (isPlus, uuid, amount, reason) VALUES (true, ?, ?, ?)", uuid.toString(), amount, reason);

    }

    @SneakyThrows
    public void removeBankMoney(int amount, String reason) {
        setBank(getBank() - amount);
        Main.getInstance().mySQL.updateAsync("UPDATE players SET bank = ? WHERE uuid = ?", getBank(), uuid.toString());
        Main.getInstance().mySQL.insertAsync("INSERT INTO bank_logs (isPlus, uuid, amount, reason) VALUES (false, ?, ?, ?)", uuid.toString(), amount, reason);
    }

    @SneakyThrows
    public void addKarma(int amount, boolean silent) {
        if (!silent) {
            player.sendMessage("§8[§3Karma§8]§b +" + amount + " Karma.");
        }
        karma += amount;
        Main.getInstance().getMySQL().updateAsync("UPDATE players SET karma = ? WHERE uuid = ?",
                karma,
                uuid);
    }

    @SneakyThrows
    public void removeKarma(int amount, boolean silent) {
        if (!silent) {
            player.sendMessage("§8[§3Karma§8]§b -" + amount + " Karma.");
        }
        karma -= amount;
        Main.getInstance().getMySQL().updateAsync("UPDATE players SET karma = ? WHERE uuid = ?",
                karma,
                uuid);
    }

    @SneakyThrows
    public void save() {
        PreparedStatement statement = Main.getInstance().mySQL.getConnection().prepareStatement("UPDATE players SET business = ?, deathTime = ?, isDead = ?, company = ?, atmBlown = ?, subGroup = ?, subGroup_grade = ?, karma = ?, isChurch = ?, isBaptized = ?, lastContract = ?, votes = ?, factionCooldown = ?, subTeam = ?, rewardId = ?, rewardTime = ? WHERE id = ?");
        statement.setInt(1, getBusiness());
        statement.setInt(2, getDeathTime());
        statement.setBoolean(3, isDead());
        int companyId = 0;
        if (company != null) {
            companyId = company.getId();
        }
        statement.setInt(4, companyId);
        statement.setInt(5, getAtmBlown());
        statement.setInt(6, subGroupId);
        statement.setInt(7, subGroupGrade);
        statement.setInt(8, karma);
        statement.setBoolean(9, isChurch);
        statement.setBoolean(10, isBaptized);
        Timestamp timestamp = Timestamp.valueOf(lastContract);
        statement.setTimestamp(11, timestamp);
        statement.setInt(12, votes);
        String formattedBoostDuration = factionCooldown.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        statement.setString(13, formattedBoostDuration);
        if (subTeam == null) {
            statement.setInt(14, -1);
        } else {
            statement.setInt(14, subTeam.getId());
        }
        statement.setInt(15, rewardId);
        statement.setInt(16, rewardTime);
        statement.setInt(17, getId());
        statement.executeUpdate();
        statement.close();
    }

    public boolean isExecutiveFaction() {
        return faction.equalsIgnoreCase("FBI") || faction.equalsIgnoreCase("Polizei");
    }

    public boolean hasReceivedBonus() {
        return receivedBonus;
    }

    public boolean isHitmanDead() {
        return isHitmanDead;
    }

    public void setHitmanDead(boolean hitmanDead) {
        isHitmanDead = hitmanDead;
    }

    public boolean isStabilized() {
        return isStabilized;
    }

    public void setStabilized(boolean stabilized) {
        isStabilized = stabilized;
    }

    public SubGroup getSubGroup() {
        return Main.getInstance().factionManager.subGroups.getSubGroup(subGroupId);
    }

    public Collection<PlayerWorkstation> getWorkstations() {
        return workstations;
    }

    public PlayerWorkstation getWorkStation(Workstation workstation) {
        for (PlayerWorkstation w : workstations) {
            if (w.Workstation == workstation) {
                return w;
            }
        }
        return null;
    }

    public void addWorkstation(PlayerWorkstation workstation) {
        workstations.add(workstation);
    }

    public void removeWorkstation(PlayerWorkstation playerWorkstation) {
        workstations.remove(playerWorkstation);
    }

    public void addQuest(PlayerQuest playerQuest) {
        quests.add(playerQuest);
    }

    public void clearQuests() {
        quests.clear();
    }

    public Collection<PlayerQuest> getQuests() {
        return quests;
    }

    public void addBeginnerQuest(de.polo.voidroleplay.game.base.extra.Beginnerpass.PlayerQuest playerQuest) {
        beginnerQuests.add(playerQuest);
    }

    public Collection<de.polo.voidroleplay.game.base.extra.Beginnerpass.PlayerQuest> getBeginnerQuests() {
        return beginnerQuests;
    }

    public CompletableFuture<Boolean> setWanted(PlayerWanted playerWanted, boolean silent) {
        System.out.println("MOIN");
        return CompletableFuture.supplyAsync(() -> {
            System.out.println("www");
            if (playerWanted != null) {
                WantedReason reason = Main.getInstance().utils.getStaatUtil().getWantedReason(playerWanted.getWantedId());
                WantedReason newReason = Main.getInstance().utils.getStaatUtil().getWantedReason(playerWanted.getWantedId());
                return reason.getWanted() <= newReason.getWanted();
            }
            System.out.println("yo");
            return true;
        }).thenCompose(result -> {
            System.out.println("hhh");
            if (!result) return CompletableFuture.completedFuture(false);
            System.out.println("h#ä");
            WantedReason wantedReason = Main.getInstance().utils.getStaatUtil().getWantedReason(playerWanted.getWantedId());

            System.out.println("Executing DELETE query...");
            String deleteQuery = "DELETE FROM player_wanteds WHERE uuid = ?";
            return Main.getInstance().getMySQL()
                    .queryThreaded(deleteQuery, player.getUniqueId().toString())
                    .thenCompose(deleteResult -> {
                        System.out.println("DELETE query completed.");
                        String insertQuery = "INSERT INTO player_wanteds (uuid, issuer, wantedId) VALUES (?, ?, ?)";
                        System.out.println("Preparing to execute INSERT query...");
                        return Main.getInstance().getMySQL().queryThreadedWithGeneratedKeys(insertQuery,
                                player.getUniqueId().toString(),
                                playerWanted.getIssuer().toString(),
                                playerWanted.getWantedId());
                    })
                    .thenApply(generatedKey -> {
                        System.out.println("INSERT query completed: Key = " + generatedKey);
                        if (generatedKey.isPresent()) {
                            playerWanted.setId(generatedKey.get());
                            this.wanted = playerWanted;
                            OfflinePlayer issuer = Bukkit.getOfflinePlayer(wanted.getIssuer());
                            player.sendMessage("§cDu hast ein Verbrechen begangen ( " + wantedReason.getReason() + " ). Beamter: " + issuer.getName());
                            player.sendMessage("§eDeine momentanen Wantedpunkte: " + wantedReason.getWanted());
                            return true;
                        }
                        System.out.println("No key was generated.");
                        return false;
                    });
        });
    }

    public void clearWanted() {
        wanted = null;
        Main.getInstance().getMySQL().queryThreaded("DELETE FROM player_wanteds WHERE uuid = ?", uuid.toString());
    }

    public Collection<PlayerWeapon> getWeapons() {
        return weapons;
    }

    public PlayerWeapon getWeapon(Weapon weapon) {
        return weapons.stream().filter(x -> x.getWeapon() == weapon).findFirst().orElse(null);
    }

    public void giveWeapon(PlayerWeapon playerWeapon) {
        this.weapons.add(playerWeapon);
    }

    public class AddonXP {
        private int fishingXP;
        private int fishingLevel;
        private int lumberjackXP;
        private int lumberjackLevel;
        private int minerXP;
        private int minerLevel;

        @Getter
        @Setter
        private int popularityXP;

        @Getter
        @Setter
        private int popularityLevel;

        public int getFishingXP() {
            return fishingXP;
        }

        public void setFishingXP(int fishingXP) {
            this.fishingXP = fishingXP;
        }

        public void addFishingXP(int amount) {
            fishingXP += amount;
            if (fishingXP >= EXPType.SKILL_FISHING.getLevelUpXp()) {
                setFishingLevel(getFishingLevel() + 1);
                setFishingLevel(getFishingLevel() - EXPType.SKILL_FISHING.getLevelUpXp());
            }
            Main.getInstance().getMySQL().updateAsync("UPDATE player_addonxp SET fishingXP = ?, fishingLevel = ? WHERE uuid = ?", fishingXP, fishingLevel, player.getUniqueId().toString());
        }

        public int getFishingLevel() {
            return fishingLevel;
        }

        public void setFishingLevel(int fishingLevel) {
            this.fishingLevel = fishingLevel;
        }

        public int getMinerXP() {
            return minerXP;
        }

        public void setMinerXP(int minerXP) {
            this.minerXP = minerXP;
        }

        public void addMinerXP(int amount) {
            minerXP += amount;
            if (minerXP >= ((minerLevel + 1) * EXPType.SKILL_MINER.getLevelUpXp())) {
                setMinerLevel(getMinerLevel() + 1);
                setMinerXP(0);
            }
            Main.getInstance().getMySQL().updateAsync("UPDATE player_addonxp SET minerXP = ?, miningLevel = ? WHERE uuid = ?", minerXP, minerLevel, player.getUniqueId().toString());
        }

        public int getMinerLevel() {
            return minerLevel;
        }

        public void setMinerLevel(int minerLevel) {
            this.minerLevel = minerLevel;
        }

        public int getLumberjackLevel() {
            return lumberjackLevel;
        }

        public void setLumberjackLevel(int lumberjackLevel) {
            this.lumberjackLevel = lumberjackLevel;
        }

        public int getLumberjackXP() {
            return lumberjackXP;
        }

        public void setLumberjackXP(int lumberjackXP) {
            this.lumberjackXP = lumberjackXP;
        }

        public void addLumberjackXP(int amount) {
            lumberjackXP += amount;
            try {
                Statement statement = Main.getInstance().mySQL.getStatement();
                statement.executeUpdate("UPDATE player_addonxp SET lumberjackXP = " + lumberjackXP + " WHERE uuid = '" + player.getUniqueId() + "'");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        public void addPopularity(int amount) {
            popularityXP += amount;
            if (popularityXP >= EXPType.POPULARITY.getLevelUpXp()) {
                setPopularityLevel(getPopularityLevel() + 1);
                setPopularityXP(getPopularityXP() - EXPType.POPULARITY.getLevelUpXp());
            }
            try {
                Statement statement = Main.getInstance().mySQL.getStatement();
                statement.executeUpdate("UPDATE player_addonxp SET popularityXP = " + popularityXP + ", popularityXP = " + popularityLevel + " WHERE uuid = '" + player.getUniqueId() + "'");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
