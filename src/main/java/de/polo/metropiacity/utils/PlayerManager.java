package de.polo.metropiacity.utils;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;
import de.polo.metropiacity.dataStorage.FactionData;
import de.polo.metropiacity.dataStorage.GangwarData;
import de.polo.metropiacity.dataStorage.RankData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.database.MySQL;
import de.polo.metropiacity.playerUtils.*;
import de.polo.metropiacity.dataStorage.PlayerData;
import de.polo.metropiacity.utils.events.SubmitChatEvent;
import de.polo.metropiacity.commands.ADutyCommand;
import net.dv8tion.jda.api.events.session.SessionState;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.JSONObject;

import java.io.Serializable;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Date;

public class PlayerManager implements Listener {

    public static final Map<String, PlayerData> playerDataMap = new HashMap<>();
    public static final HashMap<String, Boolean> onPlayer = new HashMap<>();
    public static final HashMap<String, Boolean> playerMovement = new HashMap<>();
    public static final HashMap<String, Integer> player_rent = new HashMap<>();

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
            String[] adresse = adress.split(":");
            statement.executeUpdate("UPDATE `players` SET `player_name` = '" + name + "', `adress` = '" + adresse[0] + "' WHERE uuid = '" + uuid + "'");
            PlayerData playerData = playerDataMap.get(uuid);
            if (playerData.getForumID() != null) {
                Statement wcfStatement = MySQL.forum.getStatement();
                wcfStatement.execute("UPDATE wcf1_user SET username = '" + name + "' WHERE userID = " + playerData.getForumID());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void loadPlayer(Player player) {
        String uuid = player.getUniqueId().toString();
        boolean returnval = false;
        try {
            Statement statement = MySQL.getStatement();
            assert statement != null;
            ResultSet name = statement.executeQuery("SELECT `firstname`, `lastname`, `bargeld`, `bank`, `visum`, `faction`, `faction_grade`, `player_permlevel`, `rent`, `player_rank`, `level`, `exp`, `needed_exp`, `isDead`, `deathTime`, `number`, `isDuty`, `gender`, `birthday`, `id`, `houseSlot`, `rankDuration`, `boostDuration`, `secondaryTeam`, `teamSpeakUID`, `job`, `jugendschutz`, `tutorial`, `playtime_hours`, `playtime_minutes`, `relationShip`, `warns`, `business`, `business_grade`, `bloodtype`, `forumID`, `hasAnwalt` FROM `players` WHERE `uuid` = '" + uuid + "'");
            if (name.next()) {
                PlayerData playerData = new PlayerData();
                playerData.setUuid(player.getUniqueId());
                playerData.setFirstname(name.getString(1));
                playerData.setLastname(name.getString(2));
                playerData.setBargeld(name.getInt(3));
                playerData.setBank(name.getInt(4));
                playerData.setVisum(name.getInt(5));
                playerData.setPermlevel(name.getInt(8));
                playerData.setRang(name.getString(10));
                playerData.setAduty(false);
                playerData.setLevel(name.getInt("level"));
                playerData.setExp(name.getInt(12));
                playerData.setNeeded_exp(name.getInt(13));
                playerData.setScoreboard(new Scoreboard(player));
                playerData.setDead(name.getBoolean(14));
                if (name.getBoolean(14)) playerData.setDeathTime(name.getInt(15));
                playerData.setNumber(name.getInt(20));
                playerData.setDuty(name.getBoolean(17));
                playerData.setGender(name.getString(18));
                playerData.setBirthday(name.getString(19));
                playerData.setId(name.getInt(20));
                playerData.setHouseSlot(name.getInt(21));
                if (name.getDate(22) != null) {
                    java.util.Date utilDate = new java.util.Date(name.getDate(22).getTime());

                    // Konvertierung von java.util.Date zu LocalDateTime
                    LocalDateTime localDateTime = utilDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

                    playerData.setRankDuration(localDateTime);
                }
                playerData.setBoostDuration(name.getInt(23));
                playerData.setSecondaryTeam(name.getString(24));
                playerData.setTeamSpeakUID(name.getString(25));
                playerData.setJob(name.getString(26));
                player.setMaxHealth(32 + (((double) name.getInt("level") / 5) * 2));
                player.setExp((float) playerData.getExp() / playerData.getNeeded_exp());

                if (!name.getBoolean(27)) {
                    playerData.setVariable("current_inventory", "jugendschutz");
                    playerData.setVariable("jugendschutz", "muss");
                    Main.waitSeconds(1, () -> {
                        Inventory inv = Bukkit.createInventory(player, 27, "§c§lJugendschutz");
                        inv.setItem(11, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTkyZTMxZmZiNTljOTBhYjA4ZmM5ZGMxZmUyNjgwMjAzNWEzYTQ3YzQyZmVlNjM0MjNiY2RiNDI2MmVjYjliNiJ9fX0=", 1, 0, "§a§lIch bestäige", "Lädt..."));
                        ItemMeta meta = inv.getItem(11).getItemMeta();
                        meta.setLore(Arrays.asList("§7Void Roleplay simuliert das §fechte Leben§7, weshalb mit §7Gewalt§7,", " §fSexualität§7, §fvulgärer Sprache§7, §fDrogen§7", "§7 und §fAlkohol§7 gerechnet werden muss.", "\n", "§7Bitte bestätige, dass du mindestens §e18 Jahre§7", "§7 alt bist oder die §aErlaubnis§7 eines §fErziehungsberechtigten§7 hast.", "§7Das Void Roleplay Team behält sich vor", "§7 diesen Umstand ggf. unangekündigt zu prüfen", "\n", "§8 ➥ §7[§6Klick§7]§7 §a§lIch bin 18 Jahre alt oder", "§a§l habe die Erlaubnis meiner Eltern"));
                        inv.getItem(11).setItemMeta(meta);
                        inv.setItem(15, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmViNTg4YjIxYTZmOThhZDFmZjRlMDg1YzU1MmRjYjA1MGVmYzljYWI0MjdmNDYwNDhmMThmYzgwMzQ3NWY3In19fQ==", 1, 0, "§c§lIch bestätige nicht", "Lädt..."));
                        ItemMeta nmeta = inv.getItem(15).getItemMeta();
                        nmeta.setLore(Arrays.asList("§7Klicke hier, wenn du keine 18 Jahre alt bist", "§7 und nicht die §fZustimmung§7 eines §fErziehungsberechtigten§7", "§7hast, derartige Spiele zu Spielen", "\n", "§8 ➥ §7[§6Klick§7]§c§l Ich bin keine 18 Jahre alt", "§c§l und habe keine Erlaubnis meiner Eltern"));
                        inv.getItem(15).setItemMeta(nmeta);
                        for (int i = 0; i < 27; i++) {
                            if (inv.getItem(i) == null) {
                                inv.setItem(i, ItemManager.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, 0, "§8", null));
                            }
                        }
                        player.openInventory(inv);
                    });
                }
                if (name.getBoolean(28)) {
                    playerData.setVariable("tutorial", "muss");
                    Tutorial.start(player);
                }

                playerData.setHours(name.getInt(29));
                playerData.setMinutes(name.getInt(30));

                JSONObject object = new JSONObject(name.getString(31));
                HashMap<String, String> map = new HashMap<>();
                for (String key : object.keySet()) {
                    String value = (String) object.get(key);
                    map.put(key, value);
                }
                playerData.setRelationShip(map);

                playerData.setWarns(name.getInt(32));
                playerData.setBusiness(name.getString(33));
                playerData.setBusiness_grade(name.getInt(34));
                playerData.setBloodType(name.getString("bloodtype"));
                playerData.setForumID(name.getInt("forumID"));
                playerData.setHasAnwalt(name.getBoolean("hasAnwalt"));

                playerData.setCanInteract(true);
                playerData.setFlightmode(false);

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
                player.setLevel(name.getInt("level"));
                if (name.getString(6) != null) {
                    playerData.setFaction(name.getString(6));
                    playerData.setFactionGrade(name.getInt(7));
                }


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
                if (playerData.getRankDuration() != null) {
                    LocalDateTime date = playerData.getRankDuration().atZone(ZoneId.systemDefault()).toLocalDateTime();
                    if (date.isBefore(LocalDateTime.now())) {
                        player.sendMessage(Main.prefix + "Dein " + playerData.getRang() + " ist ausgelaufen.");
                        statement.executeUpdate("UPDATE players SET rankDuration = null WHERE uuid = '" + player.getUniqueId() + "'");
                        setRang(uuid, "Spieler");
                    }
                }
                if (playerData.isDuty()) {
                    FactionManager.setDuty(player, true);
                }
                returnval = true;
                updatePlayer(player.getUniqueId().toString(), player.getName(), String.valueOf(player.getAddress()).replace("/", ""));
                if (playerData.isDead()) DeathUtils.killPlayer(player);
            }
        } catch (SQLException e) {
            returnval = false;
            e.printStackTrace();
        }
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
                int minutes = result.getInt(2);
                int newMinutes = result.getInt(2) + 1;
                int current_hours = result.getInt(3);
                int needed_hours = result.getInt(4);
                int visum = result.getInt(5) + 1;
                playerData.setMinutes(newMinutes);
                float value = (float) (needed_hours / player.getExpToLevel());
                player.setTotalExperience((int) (value * current_hours));
                if (minutes >= 60) {
                    if (current_hours >= needed_hours) {
                        needed_hours = needed_hours + 4;
                        PayDayUtils.givePayDay(player);
                        statement.executeUpdate("UPDATE `players` SET `playtime_hours` = " + hours + ", `playtime_minutes` = 1, `current_hours` = 0, `needed_hours` = " + needed_hours + ", `visum` = " + visum + " WHERE `uuid` = '" + uuid + "'");
                        player.sendMessage(Main.prefix + "Aufgrund deiner Spielzeit bist du nun Visumstufe §c" + visum + "§7!");
                        playerData.setVisum(visum);
                        playerData.setHours(playerData.getHours() + 1);
                        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1, 0);
                    } else {
                        PayDayUtils.givePayDay(player);
                        current_hours = current_hours + 1;
                        playerData.setHours(playerData.getHours() + 1);
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
            int res = result.getInt(1);
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

    public static void addBankMoney(Player player, int amount, String reason) throws SQLException {
        Statement statement = MySQL.getStatement();
        assert statement != null;
        String uuid = player.getUniqueId().toString();
        PlayerData playerData = playerDataMap.get(uuid);
        playerData.setBank(playerData.getBank() + amount);
        ResultSet result = statement.executeQuery("SELECT `bank` FROM `players` WHERE `uuid` = '" + uuid + "'");
        if (result.next()) {
            int res = result.getInt(1);
            statement.executeUpdate("UPDATE `players` SET `bank` = " + playerData.getBank() + " WHERE `uuid` = '" + uuid + "'");
            statement.execute("INSERT INTO `bank_logs` (`isPlus`, `uuid`, `amount`, `reason`) VALUES (true, '" + player.getUniqueId() + "', " + amount + ", '" + reason + "')");
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
            statement.execute("INSERT INTO `bank_logs` (`isPlus`, `uuid`, `amount`, `reason`) VALUES (false, '" + player.getUniqueId() + "', " + amount + ", '" + reason + "')");
        }
    }


    public static void setRang(String uuid, String rank) {
        try {
            for (RankData rankData : ServerManager.rankDataMap.values()) {
                if (rankData.getRang().equalsIgnoreCase(rank)) {
                    Statement statement = MySQL.getStatement();
                    statement.executeUpdate("UPDATE `players` SET `player_rank` = '" + rankData.getRang() + "', `player_permlevel` = " + rankData.getPermlevel() + " WHERE uuid = '" + uuid + "'");
                    PlayerData playerData = playerDataMap.get(uuid);
                    playerData.setRang(rankData.getRang());
                    playerData.setPermlevel(rankData.getPermlevel());
                    Player player = Bukkit.getPlayer(UUID.fromString(uuid));
                    if (player == null) {
                        return;
                    }
                    if (playerData.getTeamSpeakUID() != null) {
                        Client client = TeamSpeak.getTeamSpeak().getAPI().getClientByUId(playerData.getTeamSpeakUID());
                        TeamSpeak.getTeamSpeak().updateClientGroup(player, client);
                    }
                }
            }
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
        return playerDataMap.get(player.getUniqueId().toString()).getMinutes();
    }

    public static void setPlayerMove(Player player, Boolean state) {
        if (!state) {
            if (playerMovement.get(player.getUniqueId().toString()) == null) {
                playerMovement.put(player.getUniqueId().toString(), true);
                player.setWalkSpeed(0);
                player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0);
                player.setFlying(false);
                Main.waitSeconds(2, () -> {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 0, true, false));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, -12, true, false));
                });
            }
        } else {
            playerMovement.remove(player.getUniqueId().toString());
            player.setWalkSpeed(0.2F);
            player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.1);
            player.removePotionEffect(PotionEffectType.JUMP);
            player.removePotionEffect(PotionEffectType.SLOW);
        }
    }

    public static boolean canPlayerMove(Player player) {
        return playerMovement.get(player.getUniqueId().toString()) != null;
    }

    public static boolean isTeam(Player player) {
        return playerDataMap.get(player.getUniqueId().toString()).getPermlevel() >= 40;
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
                if (Utils.getCurrentMinute() == 0) {
                    for (FactionData factionData : FactionManager.factionDataMap.values()) {
                        for (PlayerData playerData : playerDataMap.values()) {
                            if (playerData.getFactionGrade() >= 7 && playerData.getFaction().equals(factionData.getName())) {
                                Player player = Bukkit.getPlayer(playerData.getUuid());
                                player.sendMessage(" ");
                                player.sendMessage("§7   ===§8[§" + factionData.getPrimaryColor() + "KONTOAUSZUG (" + factionData.getName() + ")§8]§7===");
                                double plus = 0;
                                double zinsen = Math.round(factionData.getBank() * 0.00075);
                                double steuern = Math.round(factionData.getBank() * 0.00035);
                                plus += zinsen;
                                plus -= steuern;
                                player.sendMessage(" ");
                                player.sendMessage("§8 ➥ §6Zinsen§8:§a +" + (int) zinsen + "$");
                                player.sendMessage("§8 ➥ §6Steuern§8:§c -" + (int) steuern + "$");
                                player.sendMessage(" ");
                                for (GangwarData gangwarData : GangwarUtils.gangwarDataMap.values()) {
                                    if (gangwarData.getOwner().equals(factionData.getName())) {
                                        player.sendMessage("§8 ➥ §6Gebietseinnahmen (" + gangwarData.getZone() + ")§8:§a +" + 150 + "$");
                                        plus += 150;
                                    }
                                }
                                player.sendMessage(" ");
                                if (plus >= 0) {
                                    player.sendMessage("§8 ➥ §6Kontostand§8:§e " + new DecimalFormat("#,###").format(FactionManager.factionBank(factionData.getName())) + "$ §8(§a+" + (int) plus + "$§8)");
                                } else {
                                    player.sendMessage("§8 ➥ §6Kontostand§8:§e " + new DecimalFormat("#,###").format(FactionManager.factionBank(factionData.getName())) + "$ §8(§c" + (int) plus + "$§8)");
                                }
                                player.sendMessage(" ");
                                factionData.setPayDay(0);
                                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                            }
                        }
                        double plus = 0;
                        double zinsen = Math.round(factionData.getBank() * 0.0075);
                        double steuern = Math.round(factionData.getBank() * 0.0035);
                        plus += zinsen;
                        plus -= steuern;
                        for (GangwarData gangwarData : GangwarUtils.gangwarDataMap.values()) {
                            if (gangwarData.getOwner().equals(factionData.getName())) {
                                plus += 150;
                            }
                        }
                        factionData.setPayDay(0);
                        try {
                            FactionManager.addFactionMoney(factionData.getName(), (int) plus, "Fraktionspayday");
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }.runTaskTimer(Main.getInstance(), 20 * 2, 20 * 60);
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
            Utils.sendActionBar(player, "§6Du bist ein Level aufgestiegen!");
            playerData.setLevel(playerData.getLevel() + 1);
            player.setMaxHealth(32 + (((double) playerData.getLevel() / 5) * 2));
            playerData.setExp(playerData.getExp() - playerData.getNeeded_exp());
            playerData.setNeeded_exp(playerData.getNeeded_exp() + 1000);
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1, 0);
        } else {
            player.sendMessage("§" + Main.getRandomChar(characters) + "+" + exp + " EXP");
            Utils.sendActionBar(player, "§" + Main.getRandomChar(characters) + "+" + exp + " EXP");
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
        }
        player.setExp((float) playerData.getExp() / playerData.getNeeded_exp());
    }

    public static void removeExp(Player player, Integer exp) {
        PlayerData playerData = playerDataMap.get(player.getUniqueId().toString());
        playerData.setExp(playerData.getExp() - exp);
        player.sendMessage("§c-+" + exp + " EXP");
    }

    public static void addEXPBoost(Player player, int hours) throws SQLException {
        PlayerData playerData = playerDataMap.get(player.getUniqueId().toString());
        playerData.setBoostDuration(playerData.getBoostDuration() + hours);
        Statement statement = MySQL.getStatement();
        statement.executeUpdate("UPDATE `players` SET `boostDuration` = " + playerData.getBoostDuration() + " WHERE `uuid` = '" + player.getUniqueId() + "'");
    }

    public static void redeemRank(Player player, String type, int duration, String duration_type) throws SQLException {
        PlayerData playerData = playerDataMap.get(player.getUniqueId().toString());
        switch (type.toLowerCase()) {
            case "vip":
                if (playerData.getRang().equals("VIP") || playerData.getRang().equals("Spieler")) {
                    if (playerData.getRankDuration() != null) {
                        playerData.setRankDuration(playerData.getRankDuration().plusDays(duration));
                    } else {
                        playerData.setRankDuration(LocalDateTime.now().plusDays(duration));
                    }
                } else {
                    playerData.setRankDuration(LocalDateTime.now().plusDays(duration));
                    player.sendMessage("§b   Info§8:§f Da du vom Rang " + playerData.getRang() + " auf VIP gestiegen bist, ist der alte Rang verloren gegangen.");
                }
                playerData.setPermlevel(30);
                playerData.setRang("VIP");
                break;
            case "premium":
                if (playerData.getRang().equals("Premium") || playerData.getRang().equals("Spieler")) {
                    if (playerData.getRankDuration() != null) {
                        playerData.setRankDuration(playerData.getRankDuration().plusDays(duration));
                    } else {
                        playerData.setRankDuration(LocalDateTime.now().plusDays(duration));
                    }
                } else {
                    playerData.setRankDuration(LocalDateTime.now().plusDays(duration));
                    player.sendMessage("§b   Info§8:§f Da du vom Rang " + playerData.getRang() + " auf Premium gestiegen bist, ist der alte Rang verloren gegangen.");
                }
                playerData.setRang("Premium");
                playerData.setPermlevel(20);
                break;
            case "gold":
                if (playerData.getRang().equals("Gold") || playerData.getRang().equals("Spieler")) {
                    if (playerData.getRankDuration() != null) {
                        playerData.setRankDuration(playerData.getRankDuration().plusDays(duration));
                    } else {
                        playerData.setRankDuration(LocalDateTime.now().plusDays(duration));
                    }
                } else {
                    playerData.setRankDuration(LocalDateTime.now().plusDays(duration));
                    player.sendMessage("§b   Info§8:§f Da du vom Rang " + playerData.getRang() + " auf Gold gestiegen bist, ist der alte Rang verloren gegangen.");
                }
                playerData.setRang("Gold");
                playerData.setPermlevel(10);
                break;
            default:
                player.sendMessage(Main.error + "§cFehler. Bitte einen Administratoren kontaktieren.");
                break;
        }
        Statement statement = MySQL.getStatement();
        System.out.println(playerData.getRankDuration());
        LocalDateTime localDateTime = playerData.getRankDuration();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDateTime = localDateTime.format(formatter);
        statement.executeUpdate("UPDATE `players` SET `rankDuration` = '" + playerData.getRankDuration() + "', `player_rank` = '" + playerData.getRang() + "', `player_permlevel` = " + playerData.getPermlevel() + " WHERE `uuid` = '" + player.getUniqueId() + "'");
    }

    public static void setJob(Player player, String job) {
        PlayerData playerData = playerDataMap.get(player.getUniqueId().toString());
        playerData.setJob(job);
        try {
            Statement statement = MySQL.getStatement();
            statement.executeUpdate("UPDATE `players` SET `job` = '" + job + "' WHERE `uuid` = '" + player.getUniqueId() + "'");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void resetJob(Player player) {
        PlayerData playerData = playerDataMap.get(player.getUniqueId().toString());
        playerData.setJob(null);
        try {
            Statement statement = MySQL.getStatement();
            statement.executeUpdate("UPDATE `players` SET `job` = null WHERE `uuid` = '" + player.getUniqueId() + "'");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isInStaatsFrak(Player player) {
        PlayerData playerData = playerDataMap.get(player.getUniqueId().toString());
        return playerData.getFaction().equals("FBI") || playerData.getFaction().equals("Medic") || playerData.getFaction().equals("Polizei");
    }

    public static void openInterActionMenu(Player player, Player targetplayer) {
        Inventory inv = Bukkit.createInventory(player, 54, "§8 » §6Interaktionsmenü");
        PlayerData playerData = playerDataMap.get(player.getUniqueId().toString());
        PlayerData targetplayerData = playerDataMap.get(targetplayer.getUniqueId().toString());
        playerData.setVariable("current_inventory", "interaktionsmenü");
        playerData.setVariable("current_player", targetplayer.getUniqueId().toString());
        inv.setItem(13, ItemManager.createItemHead(targetplayer.getUniqueId().toString(), 1, 0, "§6" + targetplayer.getName(), null));
        inv.setItem(20, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjg4OWNmY2JhY2JlNTk4ZThhMWNkODYxMGI0OWZjYjYyNjQ0ZThjYmE5ZDQ5MTFkMTIxMTM0NTA2ZDhlYTFiNyJ9fX0=", 1, 0, "§aGeld geben", null));
        inv.setItem(24, ItemManager.createItem(Material.PAPER, 1, 0, "§6Personalausweis zeigen", null));
        inv.setItem(38, ItemManager.createItem(Material.IRON_BARS, 1, 0, "§7Durchsuchen", null));
        inv.setItem(40, ItemManager.createItem(Material.POPPY, 1, 0, "§cKüssen", null));
        if (playerData.getFaction() != null) {
            FactionData factionData = FactionManager.factionDataMap.get(playerData.getFaction());
            inv.setItem(53, ItemManager.createItem(Material.GOLD_NUGGET, 1, 0, "§8[§" + factionData.getPrimaryColor() + factionData.getName() + "§8]§7 Interaktionsmenü", null));
        }
        for (int i = 0; i < 54; i++) {
            if (inv.getItem(i) == null) {
                inv.setItem(i, ItemManager.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, 0, "§8", null));
            }
        }
        player.openInventory(inv);
    }

    public static void openFactionInteractionMenu(Player player, String faction) {
        Inventory inv = Bukkit.createInventory(player, 54, "§8 » §6Interaktionsmenü");
        PlayerData playerData = playerDataMap.get(player.getUniqueId().toString());
        Player targetplayer = Bukkit.getPlayer(UUID.fromString(playerData.getVariable("current_player")));
        if (targetplayer == null) return;
        PlayerData targetplayerData = playerDataMap.get(targetplayer.getUniqueId().toString());
        playerData.setVariable("current_inventory", "interaktionsmenü_" + faction);
        playerData.setVariable("current_player", targetplayer.getUniqueId().toString());
        inv.setItem(13, ItemManager.createItemHead(targetplayer.getUniqueId().toString(), 1, 0, "§6" + targetplayer.getName(), null));
        switch (faction.toLowerCase()) {
            case "medic":
                inv.setItem(20, ItemManager.createItem(Material.REDSTONE, 1, 0, "§cBlutgruppe testen", null));
                break;
        }
        inv.setItem(53, ItemManager.createItem(Material.GOLD_NUGGET, 1, 0, "§7Interaktionsmenü", null));
        for (int i = 0; i < 54; i++) {
            if (inv.getItem(i) == null) {
                inv.setItem(i, ItemManager.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, 0, "§8", null));
            }
        }
        player.openInventory(inv);
    }

    @EventHandler
    public void onChatSubmit(SubmitChatEvent event) {
        if (event.getSubmitTo().equals("givemoney")) {
            if (event.isCancel()) {
                event.sendCancelMessage();
                event.end();
                return;
            }
            Player targetplayer = Bukkit.getPlayer(UUID.fromString(event.getPlayerData().getVariable("current_player")));
            if (targetplayer == null) {
                event.end();
                return;
            }
            if (event.getPlayer().getLocation().distance(targetplayer.getLocation()) < 5) {
                int amount = Integer.parseInt(event.getMessage());
                if (amount >= 1) {
                    if (event.getPlayerData().getBargeld() >= amount) {
                        try {
                            PlayerManager.removeMoney(event.getPlayer(), amount, "Geld an " + targetplayer.getName() + " übergeben.");
                            PlayerManager.addMoney(targetplayer, amount);
                            event.getPlayer().sendMessage("§2Du hast " + targetplayer.getName() + " " + amount + "$ zugesteckt.");
                            targetplayer.sendMessage("§2" + event.getPlayer().getName() + " hat dir " + amount + "$ zugesteckt.");
                            ChatUtils.sendMeMessageAtPlayer(event.getPlayer(), "§o" + event.getPlayer().getName() + " gibt " + targetplayer.getName() + " Bargeld.");
                            ADutyCommand.send_message(event.getPlayer().getName() + " hat " + targetplayer.getName() + " " + amount + "$ gegeben.", ChatColor.GOLD);
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        event.getPlayer().sendMessage(Main.error + "Du hast nicht genug Geld dabei.");
                    }
                } else {
                    event.getPlayer().sendMessage(Main.error + "Der Betrag muss >= 1 sein.");
                }
            } else {
                event.getPlayer().sendMessage(Main.error + "Der Spieler ist nicht in deiner nähe.");
            }
            event.end();
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!canPlayerMove(player)) return;
        player.setFlying(false);
        //event.setCancelled(true);
    }

    public static PlayerData getPlayerData(Player player) {
        return playerDataMap.get(player.getUniqueId().toString());
    }
}
