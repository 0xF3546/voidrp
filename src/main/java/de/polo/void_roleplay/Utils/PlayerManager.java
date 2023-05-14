package de.polo.void_roleplay.Utils;

import de.polo.void_roleplay.DataStorage.WeaponData;
import de.polo.void_roleplay.Main;
import de.polo.void_roleplay.MySQl.MySQL;
import de.polo.void_roleplay.PlayerUtils.PayDayUtil;
import de.polo.void_roleplay.DataStorage.PlayerData;
import de.polo.void_roleplay.PlayerUtils.Scoreboard;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class PlayerManager {

    public static Map<String, PlayerData> playerDataMap = new HashMap<>();
    public static HashMap<String, Boolean> onPlayer = new HashMap<String, Boolean>();
    public static HashMap<String, Integer> payday = new HashMap<>();
    public static HashMap<String, Boolean> playerMovement = new HashMap<>();
    public static HashMap<String, Integer> player_rent = new HashMap<String, Integer>();
    public static boolean isCreated(String uuid) {

        try {
            Statement statement = MySQL.getStatement();
            assert statement != null;
            ResultSet result = statement.executeQuery("SELECT `uuid` FROM `players` WHERE `uuid` = '" + uuid + "'");
            if (result.next()) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            Statement statement = MySQL.getStatement();
            SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
            Date date = new Date();
            String newDate = formatter.format(date);
            statement.execute("INSERT INTO `players` (`uuid`, `firstjoin`) VALUES ('" + uuid + "', '" + newDate + "')");
            statement.execute("INSERT INTO `player_ammo` (`uuid`) VALUES ('" + uuid + "')");
            Player player = Bukkit.getPlayer(UUID.fromString(uuid));
            assert player != null;
            loadPlayer(player);
            setPlayerMove(player, true);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false;
    }
    public static void updatePlayer(String uuid, String name, String adress) {
        try {
            Statement statement = MySQL.getStatement();
            statement.executeUpdate("UPDATE `players` SET `player_name` = '" + name + "', `adress` = '" + adress + "' WHERE uuid = '"+ uuid + "'");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static Serializable playerRang(Player player) {
        String uuid = player.getUniqueId().toString();
        try {
            Statement statement = MySQL.getStatement();
            assert statement != null;
            ResultSet result = statement.executeQuery("SELECT `player_rank` FROM `players` WHERE `uuid` = '" + uuid + "'");
            if (result.next()) {
                return result.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean loadPlayer(Player player) {
        String uuid = player.getUniqueId().toString();
        boolean returnval = false;
        try {
            Statement statement = MySQL.getStatement();
            assert statement != null;
            ResultSet name = statement.executeQuery("SELECT `firstname`, `lastname`, `bargeld`, `bank`, `visum`, `faction`, `faction_grade`, `player_permlevel`, `rent`, `player_rank`, `level`, `exp`, `needed_exp`, `isDead`, `deathTime`, `number`, `isDuty`, `gender`, `birthday`, `id`, `houseSlot`, `rankDuration`, `boostDuration`, `secondaryTeam` FROM `players` WHERE `uuid` = '" + uuid + "'");
            if (name.next()) {
                    PlayerData playerData = new PlayerData();
                    playerData.setFirstname(name.getString(1));
                    playerData.setLastname(name.getString(2));
                    playerData.setBargeld(name.getInt(3));
                    playerData.setBank(name.getInt(4));
                    playerData.setVisum(name.getInt(5));
                    playerData.setPermlevel(name.getInt(8));
                    playerData.setRang(name.getString(10));
                    playerData.setAduty(false);
                    playerData.setLevel(name.getInt(11));
                    playerData.setExp(name.getInt(12));
                    playerData.setNeeded_exp(name.getInt(13));
                    playerData.setScoreboard(new Scoreboard(player));
                    playerData.setDead(name.getBoolean(14));
                    if (name.getBoolean(14)) playerData.setDeathTime(name.getInt(15));
                    if (name.getInt(16) != 0) playerData.setNumber(name.getInt(16));
                    playerData.setDuty(name.getBoolean(17));
                    playerData.setGender(name.getString(18));
                    playerData.setBirthday(name.getString(19));
                    playerData.setId(name.getInt(20));
                    playerData.setHouseSlot(name.getInt(21));
                    playerData.setRankDuration(name.getInt(22));
                    playerData.setBoostDuration(name.getInt(23));
                    playerData.setSecondaryTeam(name.getString(24));

                    playerData.setCanInteract(true);
                    playerData.setFlightmode(false);

                    updatePlayer(player.getUniqueId().toString(), player.getName(), String.valueOf(player.getAddress()).replace("/", ""));
                    payday.put(player.getUniqueId().toString(), -1);
                    if (name.getInt(8) >= 60) {
                        onPlayer.put(player.getUniqueId().toString(), true);
                        player.setDisplayName("§8[§7Team§8]§7 " + player.getName());
                        player.setPlayerListName("§8[§7Team§8]§7 " + player.getName());
                        player.setCustomName("§8[§7Team§8]§7 " + player.getName());
                        player.setCustomNameVisible(true);
                    } else {
                        onPlayer.put(player.getUniqueId().toString(), false);
                        player.setDisplayName("§7" + player.getName());
                        player.setPlayerListName("§7" + player.getName());
                        player.setCustomName("§7" + player.getName());
                        player.setCustomNameVisible(true);
                    }

                    player_rent.put(player.getUniqueId().toString(), name.getInt(8));
                    player.setLevel(name.getInt(5));
                    if (name.getString(6) != null) {
                        playerData.setFaction(name.getString(6));
                        playerData.setFactionGrade(name.getInt(7));
                    }
                    player.setMaxHealth(30 + ((name.getInt(5) / 5) * 2));


                    ResultSet jail = statement.executeQuery("SELECT `hafteinheiten_verbleibend`, `reason` FROM `Jail` WHERE `uuid` = '" + uuid + "'");
                    if (jail.next()) {
                        playerData.setHafteinheiten(jail.getInt(1));
                        playerData.setVariable("jail_reason", jail.getString(2));
                        playerData.setJailed(true);
                    }
                    ResultSet skills = statement.executeQuery("SELECT `miner_level`, `miner_exp`, `miner_neededexp` FROM `player_skills` WHERE `uuid` = '" + uuid + "'");
                    if (skills.next()) {
                        playerData.setSkillLevel("miner", skills.getInt(1));
                        playerData.setSkillExp("miner", skills.getInt(2));
                        playerData.setSkillNeeded_Exp("miner", skills.getInt(3));
                    }
                    ResultSet ammo = statement.executeQuery("SELECT * FROM `player_ammo` WHERE `uuid` = '" + uuid + "'");
                    if (ammo.next()) {
                        //for (int i = 0; i < Weapons.weaponDataMap.size(); i++) playerData.getIntVariable("ammo_" + ammo.getRow(), ammo.getInt(i));
                    }
                    playerDataMap.put(uuid, playerData);
                    if (playerData.isDuty()) {
                        FactionManager.setDuty(player, true);
                    }
                    returnval = true;
            }
        } catch (SQLException e) {
            returnval = false;
            e.printStackTrace();
        }
        return returnval;
    }

    public static void savePlayer(Player player) throws SQLException {
        String uuid = player.getUniqueId().toString();
        Statement statement = MySQL.getStatement();
        PlayerData playerData = playerDataMap.get(uuid);
        if (playerData != null) {
            assert statement != null;
            onPlayer.remove(uuid);
            statement.executeUpdate("UPDATE `players` SET `player_rank` = '" + playerData.getRang() + "', `level` = " + playerData.getLevel() + ", `exp` = " + playerData.getExp() + ", `needed_exp` = " + playerData.getNeeded_exp() + ", `deathTime` = " + playerData.getDeathTime() + " WHERE `uuid` = '" + uuid + "'");
            if (playerData.isJailed()) {
                statement.executeUpdate("UPDATE `Jail` SET `hafteinheiten_verbleibend` = " + playerData.getHafteinheiten() + " WHERE `uuid` = '" + uuid + "'");
            }
            playerDataMap.remove(uuid);
        } else {
            System.out.println("Spieler " + player.getName() + "'s playerData konnte nicht gefunden werden.");
        }
    }

    public static Serializable createCpAccount(String uuid, String email, String password) {
        try {
            Statement statement = MySQL.getStatement();
            assert statement != null;
            String query = "UPDATE `players` SET `email` = '" + email + "', `password` = '" + password + "' WHERE `uuid` = '" + uuid + "'";
            PreparedStatement preparedStatement = MySQL.connection.prepareStatement(query);
            preparedStatement.setString(1, email);
            preparedStatement.setString(2, password);
            preparedStatement.setString(3, uuid);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static void add1MinutePlaytime(Player player) {
        try {
            String uuid = player.getUniqueId().toString();
            PlayerData playerData = playerDataMap.get(uuid);
            Statement statement = MySQL.getStatement();
            assert statement != null;
            if (playerData.isJailed()) {
                playerData.setHafteinheiten(playerData.getHafteinheiten() - 1);
                if (playerData.getHafteinheiten() <= 0) {
                    StaatUtil.unarrestPlayer(player);
                }
            }
            ResultSet result = statement.executeQuery("SELECT `playtime_hours`, `playtime_minutes`, `current_hours`, `needed_hours`, `visum` FROM `players` WHERE `uuid` = '" + uuid + "'");
            if (result.next()) {
                int hours = result.getInt(1) + 1;
                int minutes =  result.getInt(2);
                int newMinutes = result.getInt(2) + 1;
                int current_hours = result.getInt(3);
                int needed_hours = result.getInt(4);
                int visum = result.getInt(5) + 1;
                payday.replace(uuid, minutes);
                float value = (float) (needed_hours / player.getExpToLevel());
                player.setTotalExperience((int) (value * current_hours));
                if (minutes >= 60) {
                    if (current_hours >= needed_hours) {
                        needed_hours = needed_hours + 4;
                        PayDayUtil.givePayDay(player);
                        statement.executeUpdate("UPDATE `players` SET `playtime_hours` = " + hours + ", `playtime_minutes` = 1, `current_hours` = 0, `needed_hours` = " + needed_hours + ", `visum` = " + visum + " WHERE `uuid` = '" + uuid + "'");
                        player.sendMessage(Main.prefix + "Aufgrund deiner Spielzeit bist du nun Visumstufe §c" + visum + "§7!");
                        player.setLevel(visum);
                        playerData.setVisum(visum);
                        player.setMaxHealth(30 + (visum / 5) * 2);
                    } else {
                        PayDayUtil.givePayDay(player);
                        current_hours = current_hours + 1;
                        statement.executeUpdate("UPDATE `players` SET `playtime_hours` = " + hours + ", `playtime_minutes` = 1, `current_hours` = " + current_hours + " WHERE `uuid` = '" + uuid + "'");
                    }
                } else {
                    statement.executeUpdate("UPDATE `players` SET `playtime_minutes` = " + newMinutes + " WHERE `uuid` = '" + uuid + "'");
                    if (newMinutes == 56) {
                        player.sendMessage(Main.PayDay_prefix + "Du erhälst in 5 Minuten deinen PayDay.");
                    } else if (newMinutes == 58) {
                        player.sendMessage(Main.PayDay_prefix + "Du erhälst in 3 Minuten deinen PayDay.");
                    } else if (newMinutes == 60) {
                        player.sendMessage(Main.PayDay_prefix + "Du erhälst in 1 Minute deinen PayDay.");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static void addMoney(Player player, int amount) throws SQLException {
        Statement statement = MySQL.getStatement();
        assert statement != null;
        String uuid = player.getUniqueId().toString();
        PlayerData playerData = playerDataMap.get(uuid);
        playerData.setBargeld(playerData.getBargeld() + amount);
        ResultSet result = statement.executeQuery("SELECT `bargeld` FROM `players` WHERE `uuid` = '" + uuid + "'");
        if (result.next()) {
            int res =  result.getInt(1);
            statement.executeUpdate("UPDATE `players` SET `bargeld` = " + playerData.getBargeld() + " WHERE `uuid` = '" + uuid + "'");
        }
    }

    public static void removeMoney(Player player, int amount, String reason) throws SQLException {
        Statement statement = MySQL.getStatement();
        assert statement != null;
        String uuid = player.getUniqueId().toString();
        PlayerData playerData = playerDataMap.get(uuid);
        playerData.setBargeld(playerData.getBargeld() - amount);
        ResultSet result = statement.executeQuery("SELECT `bargeld` FROM `players` WHERE `uuid` = '" + uuid + "'");
        if (result.next()) {
            int res = result.getInt(1);
            statement.executeUpdate("UPDATE `players` SET `bargeld` = " + playerData.getBargeld() + " WHERE `uuid` = '" + uuid + "'");
        }
    }

    public static void addBankMoney(Player player, int amount) throws SQLException {
        Statement statement = MySQL.getStatement();
        assert statement != null;
        String uuid = player.getUniqueId().toString();
        PlayerData playerData = playerDataMap.get(uuid);
        playerData.setBank(playerData.getBank() + amount);
        ResultSet result = statement.executeQuery("SELECT `bank` FROM `players` WHERE `uuid` = '" + uuid + "'");
        if (result.next()) {
            int res = result.getInt(1);
            statement.executeUpdate("UPDATE `players` SET `bank` = " + playerData.getBank() + " WHERE `uuid` = '" + uuid + "'");
        }
    }
    public static void removeBankMoney(Player player, int amount, String reason) throws SQLException {
        Statement statement = MySQL.getStatement();
        assert statement != null;
        String uuid = player.getUniqueId().toString();
        PlayerData playerData = playerDataMap.get(uuid);
        playerData.setBank(playerData.getBank() - amount);
        ResultSet result = statement.executeQuery("SELECT `bank` FROM `players` WHERE `uuid` = '" + uuid + "'");
        if (result.next()) {
            int res = result.getInt(1);
            statement.executeUpdate("UPDATE `players` SET `bank` = " + playerData.getBank() + " WHERE `uuid` = '" + uuid + "'");
        }
    }


    public static void updatePlayerTeam(String uuid, String rank) {
        try {
            int permlevel = 0;
            switch (rank.toLowerCase()) {
                case "administrator":
                    permlevel = 100;
                case "moderator":
                    permlevel = 80;
                case "supporter":
                    permlevel = 70;
                case "assistent":
                    permlevel = 50;
                default:
                    permlevel = 0;
            }
            Statement statement = MySQL.getStatement();
            statement.executeUpdate("UPDATE `players` SET `player_rank` = '" + rank + "', `player_permlevel` = " + permlevel + " WHERE uuid = '"+ uuid + "'");
            PlayerData playerData = playerDataMap.get(uuid);
            playerData.setRang(rank);
            playerData.setPermlevel(permlevel);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static int money(Player player) {
        String uuid = player.getUniqueId().toString();
        PlayerData playerData = playerDataMap.get(uuid);
        return playerData.getBargeld();
    }

    public static int bank(Player player) {
        String uuid = player.getUniqueId().toString();
        PlayerData playerData = playerDataMap.get(uuid);
        return playerData.getBank();
    }

    public static String firstname(Player player) {
        String uuid = player.getUniqueId().toString();
        PlayerData playerData = playerDataMap.get(uuid);
        return playerData.getFirstname();
    }

    public static String lastname(Player player) {
        String uuid = player.getUniqueId().toString();
        PlayerData playerData = playerDataMap.get(uuid);
        return playerData.getLastname();
    }

    public static int visum(Player player) {
        String uuid = player.getUniqueId().toString();
        PlayerData playerData = playerDataMap.get(uuid);
        return playerData.getVisum();
    }

    public static int paydayDuration(Player player) {
        String uuid = player.getUniqueId().toString();
        return payday.get(uuid);
    }

    public static void setPlayerMove(Player player, Boolean state) {
        if (!state) {
            if (playerMovement.get(player.getUniqueId().toString()) == null) {
                playerMovement.put(player.getUniqueId().toString(), true);
                player.setWalkSpeed(0);
                player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0);
            }
        } else {
            playerMovement.remove(player.getUniqueId().toString());
            player.setWalkSpeed(0.2F);
            player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.1);
        }
    }

    public static boolean canPlayerMove(Player player) {
        return playerMovement.get(player.getUniqueId().toString()) != null;
    }

    public static boolean isTeam(Player player) {
        return onPlayer.get(player.getUniqueId().toString()) != null;
    }
    public static Integer perms(Player player) {
        String uuid = player.getUniqueId().toString();
        PlayerData playerData = playerDataMap.get(uuid);
        return playerData.getPermlevel();
    }
    public static String rang(Player player) {
        String uuid = player.getUniqueId().toString();
        PlayerData playerData = playerDataMap.get(uuid);
        return playerData.getRang();
    }

    public static void startTimeTracker() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    add1MinutePlaytime(player);
                }
            }
        }.runTaskTimer(Main.getInstance(), 20*2, 20*60);
    }

    public static void kickPlayer(Player player, String reason) {
        player.kickPlayer("§8• §6§lVoid Roleplay §8•\n\n§cDu wurdest vom Server geworfen.\nGrund§8:§7 " + reason + "\n\n§8• §6§lVoid Roleplay §8•");
    }

    public static void addExp(Player player, Integer exp) {
        String characters = "a0b1c2d3e4569";
        PlayerData playerData = playerDataMap.get(player.getUniqueId().toString());
        playerData.setExp(playerData.getExp() + exp);
        if (playerData.getExp() >= playerData.getNeeded_exp()) {
            player.sendMessage("§8[§6Level§8] §7Du bist im Level aufgestiegen! §a" + playerData.getLevel() + " §8➡ §2" + playerData.getLevel() + 1);
            playerData.setLevel(playerData.getLevel() + 1);
            playerData.setExp(playerData.getExp() - playerData.getNeeded_exp());
            playerData.setNeeded_exp(playerData.getNeeded_exp() + 1000);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 0);
        } else {
            player.sendMessage("§8[§6Level§8] §" + Main.getRandomChar(characters) + "+" + exp + " EXP");
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 0);
        }
    }

    public static void removeExp(Player player, Integer exp) {
        PlayerData playerData = playerDataMap.get(player.getUniqueId().toString());
        playerData.setExp(playerData.getExp() - exp);
        player.sendMessage("§8[§6Level§8] §c-+" + exp + " EXP");
    }

    public static void addEXPBoost(Player player, int hours) throws SQLException {
        PlayerData playerData = playerDataMap.get(player.getUniqueId().toString());
        playerData.setBoostDuration(playerData.getBoostDuration() + hours);
        Statement statement = MySQL.getStatement();
        statement.executeUpdate("UPDATE `players` SET `boostDuration` = " + playerData.getBoostDuration() + " WHERE `uuid` = '" + player.getUniqueId().toString() + "'");
    }

    public static void redeemRank(Player player, String type, int duration, String duration_type) throws SQLException {
        PlayerData playerData = playerDataMap.get(player.getUniqueId().toString());
        switch (type) {
            case "VIP":
                playerData.setRang("VIP");
                playerData.setPermlevel(30);
                playerData.setRankDuration(playerData.getRankDuration() + duration);
                break;
            case "Premium":
                playerData.setRang("Premium");
                playerData.setPermlevel(20);
                playerData.setRankDuration(playerData.getRankDuration() + duration);
                break;
            case "Gold":
                playerData.setRang("Gold");
                playerData.setPermlevel(10);
                playerData.setRankDuration(playerData.getRankDuration() + duration);
                break;
            default:
                player.sendMessage(Main.error + "§cFehler. Bitte einen Administratoren kontaktieren.");
                break;
        }
        Statement statement = MySQL.getStatement();
        statement.executeUpdate("UPDATE `players` SET `rankDuration` = " + playerData.getRankDuration() + ", `player_rank` = '" + playerData.getRang() + "', `player_permlevel` = " + playerData.getPermlevel() + " WHERE `uuid` = '" + player.getUniqueId().toString() + "'");
    }

}
