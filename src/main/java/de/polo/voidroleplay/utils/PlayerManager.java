package de.polo.voidroleplay.utils;

import de.polo.api.faction.gangwar.IGangzone;
import de.polo.voidroleplay.dataStorage.*;
import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.Weapon;
import de.polo.voidroleplay.database.MySQL;
import de.polo.voidroleplay.game.base.extra.PlaytimeReward;
import de.polo.voidroleplay.game.base.farming.PlayerWorkstation;
import de.polo.voidroleplay.game.base.housing.House;
import de.polo.voidroleplay.game.base.housing.Housing;
import de.polo.voidroleplay.game.faction.SprayableBanner;
import de.polo.voidroleplay.game.faction.gangwar.Gangwar;
import de.polo.voidroleplay.game.faction.laboratory.PlayerLaboratory;
import de.polo.voidroleplay.game.faction.staat.SubTeam;
import de.polo.voidroleplay.utils.InventoryManager.CustomItem;
import de.polo.voidroleplay.utils.InventoryManager.InventoryManager;
import de.polo.voidroleplay.utils.enums.*;
import de.polo.voidroleplay.game.events.HourTickEvent;
import de.polo.voidroleplay.game.events.MinuteTickEvent;
import de.polo.voidroleplay.game.events.SubmitChatEvent;
import de.polo.voidroleplay.utils.playerUtils.ChatUtils;
import de.polo.voidroleplay.utils.playerUtils.PlayerTutorial;
import lombok.SneakyThrows;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.JSONObject;

import java.sql.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Date;
import java.util.stream.Collectors;

public class PlayerManager implements Listener, ServerTiming {

    private final Map<UUID, PlayerData> playerDataMap = new HashMap<>();
    private final HashMap<String, Boolean> playerMovement = new HashMap<>();
    public final HashMap<String, Integer> player_rent = new HashMap<>();

    private final List<PlaytimeReward> playtimeRewards = new ArrayList<>();

    private final MySQL mySQL;

    public PlayerManager(MySQL mySQL) {
        this.mySQL = mySQL;
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
        startTimeTracker();
        initPlaytimeRewards();
    }

    private void initPlaytimeRewards() {
        playtimeRewards.add(new PlaytimeReward(1, 2, "§a2000$", false, 2000, PlaytimeRewardType.MONEY));
        playtimeRewards.add(new PlaytimeReward(2, 2, "§a4000$", true, 4000, PlaytimeRewardType.MONEY));
        playtimeRewards.add(new PlaytimeReward(3, 6, "§e20 Crypto", true, 20, PlaytimeRewardType.CRYPTO));
        playtimeRewards.add(new PlaytimeReward(4, 6, "§e35 Crypto", true, 35, PlaytimeRewardType.CRYPTO));
    }

    private PlaytimeReward getRandomPlaytimeReward(PlayerData playerData) {
        List<PlaytimeReward> randomRewards = playtimeRewards;
        if (Main.random(1, 3) == 3 && playerData.getPermlevel() >= 20) {
           randomRewards = randomRewards.stream().filter(PlaytimeReward::isPremiumOnly).collect(Collectors.toList());
        }
        return randomRewards.get(Main.random(0, randomRewards.size() - 1));
    }

    public PlaytimeReward getPlaytimeReward(int id) {
        return playtimeRewards.stream().filter(x -> x.getId() == id).findFirst().orElse(null);
    }

    public boolean isCreated(UUID uuid) {

        try {
            Statement statement = Main.getInstance().mySQL.getStatement();
            assert statement != null;
            ResultSet result = statement.executeQuery("SELECT `uuid` FROM `players` WHERE `uuid` = '" + uuid + "'");
            if (result.next()) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            Statement statement = Main.getInstance().mySQL.getStatement();
            SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
            Date date = new Date();
            String newDate = formatter.format(date);
            Player player = Bukkit.getPlayer(uuid);
            statement.execute("INSERT INTO `players` (`uuid`, `player_name`, `adress`) VALUES ('" + uuid + "', '" + player.getName() + "', '" + player.getAddress() + "')");
            statement.execute("INSERT INTO `player_ammo` (`uuid`) VALUES ('" + uuid + "')");
            statement.execute("INSERT INTO `player_addonxp` (`uuid`) VALUES ('" + uuid + "')");
            loadPlayer(player);
            setPlayerMove(player, true);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    public void updatePlayer(String uuid, String name, String adress) {
        try {
            Statement statement = Main.getInstance().mySQL.getStatement();
            String[] adresse = adress.split(":");
            statement.executeUpdate("UPDATE `players` SET `player_name` = '" + name + "', `adress` = '" + adresse[0] + "' WHERE uuid = '" + uuid + "'");
            PlayerData playerData = playerDataMap.get(uuid);
            /*if (playerData.getForumID() != null) {
                Statement wcfStatement = MySQL.forum.getStatement();
                wcfStatement.execute("UPDATE wcf1_user SET username = '" + name + "' WHERE userID = " + playerData.getForumID());
            }*/
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void loadPlayer(Player player) {
        UUID uuid = player.getUniqueId();
        try {
            Statement statement = Main.getInstance().mySQL.getStatement();
            assert statement != null;
            String query = "SELECT players.*, player_addonxp.* " +
                    "FROM players " +
                    "LEFT JOIN player_addonxp ON players.uuid = player_addonxp.uuid " +
                    "WHERE players.uuid = '" + uuid + "'";

            ResultSet result = statement.executeQuery(query);
            if (result.next()) {
                if (result.getString("player_name") != null) {
                    if (!result.getString("player_name").equalsIgnoreCase(player.getName())) {
                        List<Integer> houses = new ArrayList<>();
                        for (House house : Housing.houseDataMap.values()) {
                            if (house.getOwner() == null) continue;
                            if (house.getOwner().equalsIgnoreCase(player.getUniqueId().toString())) {
                                houses.add(house.getId());
                            }
                        }
                        for (int house : houses) {
                            for (RegisteredBlock block : Main.getInstance().blockManager.getBlocks()) {
                                if (block.getInfo() == null) continue;
                                if (block.getInfoValue() == null) continue;
                                try {
                                    if (block.getInfo().equalsIgnoreCase("house") && Integer.parseInt(block.getInfoValue()) == house) {
                                        Block b = block.getLocation().getBlock();
                                        Sign sign = (Sign) b.getState();
                                        sign.setLine(2, "§0" + player.getName());
                                        sign.update();
                                    }
                                } catch (Exception ex) {
                                    ex.printStackTrace();  // Oder eine andere Form der Fehlermeldung
                                }
                            }
                        }
                    }
                }
                PlayerData playerData = new PlayerData(player);
                playerData.setFirstname(result.getString("firstname"));
                playerData.setLastname(result.getString("lastname"));
                playerData.setBargeld(result.getInt("bargeld"));
                playerData.setBank(result.getInt("bank"));
                playerData.setVisum(result.getInt("visum"));
                playerData.setPermlevel(result.getInt("player_permlevel"));
                playerData.setRang(result.getString("player_rank"));
                playerData.setAduty(false);
                playerData.setLevel(result.getInt("level"));
                playerData.setExp(result.getInt("exp"));
                playerData.setNeeded_exp(result.getInt("needed_exp"));
                playerData.setDead(result.getBoolean("isDead"));
                if (result.getBoolean("isDead")) {
                    playerData.setDeathTime(result.getInt("deathTime"));
                    playerData.setHitmanDead(result.getBoolean("isHitmanDead"));
                    playerData.setStabilized(result.getBoolean("isStabilized"));
                }
                playerData.setNumber(result.getInt("number"));
                playerData.setDuty(result.getBoolean("isDuty"));
                if (result.getString("gender") != null) {
                    playerData.setGender(Gender.valueOf(result.getString("gender")));
                }
                playerData.setBirthday(result.getDate("birthday"));
                playerData.setId(result.getInt("id"));
                playerData.setHouseSlot(result.getInt("houseSlot"));
                playerData.setCurrentHours(result.getInt("current_hours"));
                if (result.getDate("rankDuration") != null) {
                    Date utilDate = new Date(result.getDate("rankDuration").getTime());
                    LocalDateTime localDateTime = utilDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                    playerData.setRankDuration(localDateTime);
                }
                if (result.getDate("dailyBonusRedeemed") != null) {
                    Date utilDate = new Date(result.getDate("dailyBonusRedeemed").getTime());
                    playerData.setDailyBonusRedeemed(utilDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
                }
                if (result.getDate("lastPayDay") != null) {
                    Date utilDate = new Date(result.getDate("lastPayDay").getTime());
                    playerData.setLastPayDay(utilDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
                }
                if (result.getDate("boostDuration") != null)
                    playerData.setBoostDuration(result.getTimestamp("boostDuration").toLocalDateTime());
                if (result.getDate("lastContract") != null)
                    playerData.setLastContract(result.getTimestamp("lastContract").toLocalDateTime());
                playerData.setSecondaryTeam(result.getString("secondaryTeam"));
                playerData.setTeamSpeakUID(result.getString("teamSpeakUID"));
                playerData.setSpawn(result.getString("spawn"));
                playerData.setJob(result.getString("job"));
                player.setMaxHealth(32 + (((double) result.getInt("level") / 5) * 2));
                player.setExp((float) playerData.getExp() / playerData.getNeeded_exp());

                playerData.setAtmBlown(result.getInt("atmBlown"));
                playerData.setSubGroupGrade(result.getInt("subGroup_grade"));
                playerData.setSubGroupId(result.getInt("subGroup"));
                playerData.setKarma(result.getInt("karma"));
                playerData.setVotes(result.getInt("votes"));
                playerData.setChurch(result.getBoolean("isChurch"));
                playerData.setBaptized(result.getBoolean("isBaptized"));
                playerData.setFactionCooldown(Utils.toLocalDateTime(result.getDate("factionCooldown")));
                playerData.setEventPoints(result.getInt("eventPoints"));
                playerData.setCrypto(result.getFloat("crypto"));
                playerData.setRewardTime(result.getInt("rewardTime"));
                playerData.setRewardId(result.getInt("rewardId"));
                if (!result.getBoolean("tpNewmap")) {
                    Main.getInstance().locationManager.useLocation(player, "stadthalle");
                    player.sendMessage("§8 ✈ §aWillkommen auf der neuen Map!");
                    Connection connection = Main.getInstance().mySQL.getConnection();
                    PreparedStatement ps = connection.prepareStatement("UPDATE players SET tpNewmap = true WHERE uuid = ?");
                    ps.setString(1, uuid.toString());
                    ps.execute();
                    ps.close();
                    connection.close();
                }

                if (result.getString("faction") != null) {
                    playerData.setFaction(result.getString("faction"));
                    playerData.setFactionGrade(result.getInt("faction_grade"));
                }

                if (result.getInt("subTeam") != -1 && playerData.getFaction() != null) {
                    FactionData factionData = Main.getInstance().factionManager.getFactionData(playerData.getFaction());
                    for (SubTeam subTeam : Main.getInstance().factionManager.getSubTeams(factionData.getId())) {
                        if (!(subTeam.getId() == result.getInt("subTeam"))) continue;
                        playerData.setSubTeam(subTeam);
                    }
                }

                if (!result.getBoolean("jugendschutz")) {
                    playerData.setVariable("jugendschutz", "muss");
                    Main.waitSeconds(1, () -> {
                        InventoryManager inventory = new InventoryManager(player, 27, "§c§lJugendschutz", true, false);
                        playerData.setVariable("originClass", this);
                        inventory.setItem(new CustomItem(11, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTkyZTMxZmZiNTljOTBhYjA4ZmM5ZGMxZmUyNjgwMjAzNWEzYTQ3YzQyZmVlNjM0MjNiY2RiNDI2MmVjYjliNiJ9fX0=", 1, 0, "§a§lIch bestäige", Arrays.asList("§VoidRoleplay simuliert das §fechte Leben§7, weshalb mit §7Gewalt§7,", " §fSexualität§7, §fvulgärer Sprache§7, §fDrogen§7", "§7 und §fAlkohol§7 gerechnet werden muss.", "\n", "§7Bitte bestätige, dass du mindestens §e18 Jahre§7", "§7 alt bist oder die §aErlaubnis§7 eines §fErziehungsberechtigten§7 hast.", "§7Das VoidRoleplay Team behält sich vor", "§7 diesen Umstand ggf. unangekündigt zu prüfen", "\n", "§8 ➥ §7[§6Klick§7]§7 §a§lIch bin 18 Jahre alt oder", "§a§l habe die Erlaubnis meiner Eltern"))) {
                            @Override
                            public void onClick(InventoryClickEvent event) {
                                playerData.setVariable("jugendschutz", null);
                                player.closeInventory();
                                player.sendMessage("§8[§c§lJugendschutz§8]§a Du hast den Jugendschutz aktzeptiert.");
                                Statement statement = null;
                                try {
                                    statement = Main.getInstance().mySQL.getStatement();
                                    statement.executeUpdate("UPDATE `players` SET `jugendschutz` = true, `jugendschutz_accepted` = NOW() WHERE `uuid` = '" + player.getUniqueId() + "'");
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }
                                player.closeInventory();
                            }
                        });
                        inventory.setItem(new CustomItem(15, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmViNTg4YjIxYTZmOThhZDFmZjRlMDg1YzU1MmRjYjA1MGVmYzljYWI0MjdmNDYwNDhmMThmYzgwMzQ3NWY3In19fQ==", 1, 0, "§c§lIch bestätige nicht", Arrays.asList("§7Klicke hier, wenn du keine 18 Jahre alt bist", "§7 und nicht die §fZustimmung§7 eines §fErziehungsberechtigten§7", "§7hast, derartige Spiele zu Spielen", "\n", "§8 ➥ §7[§6Klick§7]§c§l Ich bin keine 18 Jahre alt", "§c§l und habe keine Erlaubnis meiner Eltern"))) {
                            @Override
                            public void onClick(InventoryClickEvent event) {
                                player.closeInventory();
                                player.kickPlayer("§cDa du den Jugendschutz nicht aktzeptieren konntest, kannst du auf dem Server §lnicht§c Spielen.\n§cBitte deine Erziehungsberechtigten um Erlabunis oder warte bis du 18 bist.");
                            }
                        });
                        for (int i = 0; i < 27; i++) {
                            if (i != 15 && i != 11) {
                                inventory.setItem(new CustomItem(i, ItemManager.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, 0, "§8")) {
                                    @Override
                                    public void onClick(InventoryClickEvent event) {

                                    }
                                });
                            }
                        }
                    });
                }
                int tutorial = result.getInt("tutorial");
                playerData.setHours(result.getInt("playtime_hours"));
                playerData.setMinutes(result.getInt("playtime_minutes"));
                playerData.setAFK(false);

                JSONObject object = new JSONObject(result.getString("relationShip"));
                HashMap<String, String> map = new HashMap<>();
                for (String key : object.keySet()) {
                    String value = (String) object.get(key);
                    map.put(key, value);
                }
                playerData.setRelationShip(map);

                playerData.setWarns(result.getInt("warns"));
                playerData.setBusiness(result.getInt("business"));
                playerData.setBusiness_grade(result.getInt("business_grade"));
                playerData.setBloodType(result.getString("bloodtype"));
                playerData.setForumID(result.getInt("forumID"));
                playerData.setHasAnwalt(result.getBoolean("hasAnwalt"));

                playerData.setReceivedBonus(result.getBoolean("bonusReceived"));

                playerData.setCanInteract(true);
                playerData.setFlightmode(false);
                playerData.setCoins(result.getInt("coins"));
                playerData.setCompany(null);
                if (result.getInt("company") != 0) {
                    Company company = Main.getInstance().companyManager.getCompanyById(result.getInt("company"));
                    if (company != null) {
                        playerData.setCompany(company);
                        if (company.getRoles() != null) {
                            for (CompanyRole role : company.getRoles()) {
                                if (role == null) continue;
                                if (role.getId() == result.getInt("companyRole")) {
                                    playerData.setCompanyRole(role);
                                }
                            }
                        }
                    }
                }

                for (PlayerWorkstation workstation : PlayerWorkstation.getPlayerWorkstationsFromDatabase(uuid)) {
                    playerData.addWorkstation(workstation);
                }

                player_rent.put(player.getUniqueId().toString(), result.getInt("rent"));
                player.setLevel(result.getInt("level"));

                playerData.addonXP.setFishingXP(result.getInt("fishingXP"));
                playerData.addonXP.setFishingLevel(result.getInt("fishingLevel"));
                playerData.addonXP.setLumberjackLevel(result.getInt("lumberjackLevel"));
                playerData.addonXP.setLumberjackXP(result.getInt("lumberjackXP"));
                playerData.addonXP.setPopularityLevel(result.getInt("popularityLevel"));
                playerData.addonXP.setPopularityXP(result.getInt("popularityXP"));
                playerData.addonXP.setMinerLevel(result.getInt("miningLevel"));
                playerData.addonXP.setMinerXP(result.getInt("minerXP"));

                ResultSet jail = statement.executeQuery("SELECT `hafteinheiten_verbleibend`, `reason` FROM `Jail` WHERE `uuid` = '" + uuid + "'");
                if (jail.next()) {
                    playerData.setHafteinheiten(jail.getInt(1));
                    playerData.setVariable("jail_reason", jail.getString(2));
                    playerData.setJailed(true);
                }
                playerData.setIntVariable("afk", 0);


                playerDataMap.put(uuid, playerData);
                if (playerData.getRewardId() == 0) {
                    PlaytimeReward playtimeReward = getRandomPlaytimeReward(playerData);
                    playerData.setRewardId(playtimeReward.getId());
                    playerData.setRewardTime(playtimeReward.getHour());
                    playerData.save();
                }
                if (tutorial != 0) {
                    playerData.setVariable("tutorial", new PlayerTutorial(player, playerData, tutorial));
                    Main.getInstance().utils.tutorial.start(player);
                }
                if (playerData.getRankDuration() != null) {
                    LocalDateTime date = playerData.getRankDuration().atZone(ZoneId.systemDefault()).toLocalDateTime();
                    if (date.isBefore(LocalDateTime.now())) {
                        player.sendMessage(Main.prefix + "Dein " + playerData.getRang() + " ist ausgelaufen.");
                        statement.executeUpdate("UPDATE players SET rankDuration = null WHERE uuid = '" + player.getUniqueId() + "'");
                        if (playerData.getBusiness() != null) {
                            BusinessData business = Main.getInstance().businessManager.getBusinessData(playerData.getBusiness());
                            if (business != null) {
                                if (business.getOwner().equals(player.getUniqueId())) {
                                    business.setActive(false);
                                    business.save();
                                }
                            }
                        }
                        setRang(uuid, "Spieler");
                    }
                }
                if (playerData.isDuty()) {
                    Main.getInstance().factionManager.setDuty(player, true);
                }
                updatePlayer(player.getUniqueId().toString(), player.getName(), String.valueOf(player.getAddress()).replace("/", ""));
                if (playerData.isDead()) Main.getInstance().utils.deathUtil.killPlayer(player);

                /*
                Labor: deaktiviert
                 */
                /*if (playerData.getPermlevel() >= 20) {
                    PlayerLaboratory laboratory = new PlayerLaboratory(Main.getInstance().laboratory);
                    laboratory.create(player.getUniqueId());
                    playerData.setLaboratory(laboratory);
                    laboratory.start();
                }*/

                Main.getInstance().seasonpass.loadPlayerQuests(player.getUniqueId());
                Main.getInstance().beginnerpass.loadPlayerQuests(player.getUniqueId());
                Main.getInstance().utils.staatUtil.loadParole(player);
                Main.getInstance().gamePlay.displayNameManager.reloadDisplayNames(player);

                if (playerData.getFaction() != null) {
                    for (PlayerData pData : getPlayers()) {
                        if (pData.getFaction() == null) continue;
                        if (pData.getFaction().equalsIgnoreCase(playerData.getFaction())) {
                            Main.getInstance().gamePlay.displayNameManager.reloadDisplayNames(pData.getPlayer());
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void savePlayer(Player player) throws SQLException {
        UUID uuid = player.getUniqueId();
        Statement statement = Main.getInstance().mySQL.getStatement();
        PlayerData playerData = playerDataMap.get(uuid);
        if (playerData != null) {
            assert statement != null;
            if (playerData.isDead()) {
                Item skull = Main.getInstance().utils.deathUtil.getDeathSkull(player.getUniqueId().toString());
                if (skull != null) {
                    skull.remove();
                    Main.getInstance().utils.deathUtil.removeDeathSkull(player.getUniqueId().toString());
                }
            }
            statement.executeUpdate("UPDATE `players` SET `player_rank` = '" + playerData.getRang() + "', `level` = " + playerData.getLevel() + ", `exp` = " + playerData.getExp() + ", `needed_exp` = " + playerData.getNeeded_exp() + ", `deathTime` = " + playerData.getDeathTime() + ", `isDead` = " + playerData.isDead() + ", `lastLogin` = NOW(), playtime_minutes = " + playerData.getMinutes() + " WHERE `uuid` = '" + uuid + "'");
            if (playerData.isJailed()) {
                statement.executeUpdate("UPDATE `Jail` SET `hafteinheiten_verbleibend` = " + playerData.getHafteinheiten() + " WHERE `uuid` = '" + uuid + "'");
            }

            for (Weapon weapon : Main.getInstance().weapons.getWeapons().values()) {
                if (weapon.getOwner().equals(player.getUniqueId())) {
                    Statement statement1 = mySQL.getStatement();
                    statement1.executeUpdate("UPDATE player_weapons SET ammo = " + weapon.getAmmo() + ", current_ammo = " + weapon.getCurrentAmmo() + " WHERE id = " + weapon.getId());
                    statement1.close();
                }
            }

            if (playerData.getLaboratory() != null) {
                playerData.getLaboratory().save();
            }

            playerData.getWorkstations().forEach(PlayerWorkstation::save);
            playerDataMap.remove(uuid);
            statement.close();
        } else {
            System.out.println("Spieler " + player.getName() + "'s playerData konnte nicht gefunden werden.");
        }
    }

    private void giveReward(PlayerData playerData) {
        PlaytimeReward playtimeReward = getPlaytimeReward(playerData.getRewardId());
        switch (playtimeReward.getPlaytimeRewardType()) {
            case MONEY:
                playerData.addBankMoney((int) playtimeReward.getAmount(), "Spielzeitbelhnung");
                break;
            case CRYPTO:
                playerData.addCrypto(playtimeReward.getAmount(), "Spielzeitbelohnung" , false);
                break;
        }
    }

    @SneakyThrows
    public void add1MinutePlaytime(Player player) {
        UUID uuid = player.getUniqueId();
        PlayerData playerData = playerDataMap.get(uuid);
        Statement statement = Main.getInstance().mySQL.getStatement();
        assert statement != null;
        if (playerData.isJailed()) {
            playerData.setHafteinheiten(playerData.getHafteinheiten() - 1);
            if (playerData.getHafteinheiten() <= 0) {
                Main.getInstance().utils.staatUtil.unarrestPlayer(player);
            }
        }
        if (playerData.isAFK()) return;
        int hours = playerData.getHours() + 1;
        int minutes = playerData.getMinutes();
        int newMinutes = minutes + 1;
        int current_hours = playerData.getCurrentHours();
        int needed_hours = playerData.getVisum() * 4;
        int visum = playerData.getVisum() + 1;
        playerData.setMinutes(newMinutes);
        float value = (float) (needed_hours / player.getExpToLevel());
        player.setTotalExperience((int) (value * current_hours));
        if (minutes >= 60) {
            Main.getInstance().beginnerpass.didQuest(player, 7);
            Main.getInstance().seasonpass.didQuest(player, 7);
            playerData.setHours(playerData.getHours() + 1);
            playerData.setMinutes(0);
            playerData.setRewardTime(playerData.getRewardTime() - 1);
            Main.getInstance().utils.payDayUtils.givePayDay(player);
            if (playerData.getRewardTime() <= 0) {
                giveReward(playerData);
                PlaytimeReward playtimeReward = getRandomPlaytimeReward(playerData);
                playerData.setRewardId(playtimeReward.getId());
                playerData.setRewardTime(playtimeReward.getHour());
                playerData.save();
            }
            if (current_hours >= needed_hours) {
                needed_hours = needed_hours + 4;
                statement.executeUpdate("UPDATE `players` SET `playtime_hours` = " + hours + ", `playtime_minutes` = 1, `current_hours` = 0, `needed_hours` = " + needed_hours + ", `visum` = " + visum + " WHERE `uuid` = '" + uuid + "'");
                player.sendMessage(Main.prefix + "Aufgrund deiner Spielzeit bist du nun Visumstufe §c" + visum + "§7!");
                playerData.setVisum(visum);
                Main.getInstance().beginnerpass.didQuest(player, 4);
                player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1, 0);
                playerData.setCurrentHours(0);
                if (visum == 2) {

                }
            } else {
                current_hours = current_hours + 1;
                playerData.setCurrentHours(current_hours);
                statement.executeUpdate("UPDATE `players` SET `playtime_hours` = " + hours + ", `playtime_minutes` = 1, `current_hours` = " + current_hours + " WHERE `uuid` = '" + uuid + "'");
            }
        } else {
            if (newMinutes == 56) {
                player.sendMessage(Main.PayDay_prefix + "Du erhälst in 5 Minuten deinen PayDay.");
            } else if (newMinutes == 58) {
                player.sendMessage(Main.PayDay_prefix + "Du erhälst in 3 Minuten deinen PayDay.");
            } else if (newMinutes == 60) {
                player.sendMessage(Main.PayDay_prefix + "Du erhälst in 1 Minute deinen PayDay.");
            }
        }
    }

    private void registerBonus(Player player) {

    }

    public void addMoney(Player player, int amount, String reason) throws SQLException {
        Main.getInstance().beginnerpass.didQuest(player, 2, amount);
        Statement statement = Main.getInstance().mySQL.getStatement();
        assert statement != null;
        PlayerData playerData = playerDataMap.get(player.getUniqueId());
        playerData.setBargeld(playerData.getBargeld() + amount);
        ResultSet result = statement.executeQuery("SELECT `bargeld` FROM `players` WHERE `uuid` = '" + player.getUniqueId() + "'");
        if (result.next()) {
            int res = result.getInt(1);
            statement.executeUpdate("UPDATE `players` SET `bargeld` = " + playerData.getBargeld() + " WHERE `uuid` = '" + player.getUniqueId() + "'");
            statement.execute("INSERT INTO `money_logs` (`isPlus`, `uuid`, `amount`, `reason`) VALUES (true, '" + player.getUniqueId() + "', " + amount + ", '" + reason + "')");
        }
    }

    public void removeMoney(Player player, int amount, String reason) throws SQLException {
        Statement statement = Main.getInstance().mySQL.getStatement();
        assert statement != null;
        PlayerData playerData = playerDataMap.get(player.getUniqueId());
        playerData.setBargeld(playerData.getBargeld() - amount);
        ResultSet result = statement.executeQuery("SELECT `bargeld` FROM `players` WHERE `uuid` = '" + player.getUniqueId() + "'");
        if (result.next()) {
            int res = result.getInt(1);
            statement.executeUpdate("UPDATE `players` SET `bargeld` = " + playerData.getBargeld() + " WHERE `uuid` = '" + player.getUniqueId() + "'");
            statement.execute("INSERT INTO `money_logs` (`isPlus`, `uuid`, `amount`, `reason`) VALUES (false, '" + player.getUniqueId() + "', " + amount + ", '" + reason + "')");
        }
    }

    public void addBankMoney(Player player, int amount, String reason) throws SQLException {
        Main.getInstance().beginnerpass.didQuest(player, 2, amount);
        Statement statement = Main.getInstance().mySQL.getStatement();
        assert statement != null;
        PlayerData playerData = playerDataMap.get(player.getUniqueId());
        playerData.setBank(playerData.getBank() + amount);
        ResultSet result = statement.executeQuery("SELECT `bank` FROM `players` WHERE `uuid` = '" + player.getUniqueId() + "'");
        if (result.next()) {
            int res = result.getInt(1);
            statement.executeUpdate("UPDATE `players` SET `bank` = " + playerData.getBank() + " WHERE `uuid` = '" + player.getUniqueId() + "'");
            statement.execute("INSERT INTO `bank_logs` (`isPlus`, `uuid`, `amount`, `reason`) VALUES (true, '" + player.getUniqueId() + "', " + amount + ", '" + reason + "')");
        }
    }

    public void removeBankMoney(Player player, int amount, String reason) throws SQLException {
        Statement statement = Main.getInstance().mySQL.getStatement();
        assert statement != null;
        PlayerData playerData = playerDataMap.get(player.getUniqueId());
        playerData.setBank(playerData.getBank() - amount);
        ResultSet result = statement.executeQuery("SELECT `bank` FROM `players` WHERE `uuid` = '" + player.getUniqueId() + "'");
        if (result.next()) {
            int res = result.getInt(1);
            statement.executeUpdate("UPDATE `players` SET `bank` = " + playerData.getBank() + " WHERE `uuid` = '" + player.getUniqueId() + "'");
            statement.execute("INSERT INTO `bank_logs` (`isPlus`, `uuid`, `amount`, `reason`) VALUES (false, '" + player.getUniqueId() + "', " + amount + ", '" + reason + "')");
        }
    }


    public void setRang(UUID uuid, String rank) {
        try {
            for (RankData rankData : ServerManager.rankDataMap.values()) {
                if (rankData.getRang().equalsIgnoreCase(rank)) {
                    Statement statement = Main.getInstance().mySQL.getStatement();
                    statement.executeUpdate("UPDATE `players` SET `player_rank` = '" + rankData.getRang() + "', `player_permlevel` = " + rankData.getPermlevel() + " WHERE uuid = '" + uuid + "'");
                    PlayerData playerData = playerDataMap.get(uuid);
                    playerData.setRang(rankData.getRang());
                    playerData.setPermlevel(rankData.getPermlevel());
                    playerData.setAduty(false);
                    Player player = Bukkit.getPlayer(uuid);
                    if (player == null) {
                        return;
                    }
                    Utils.Tablist.setTablist(player, null);
                    TeamSpeak.reloadPlayer(player.getUniqueId());
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int money(Player player) {
        PlayerData playerData = getPlayerData(player.getUniqueId());
        return playerData.getBargeld();
    }

    public int bank(Player player) {
        PlayerData playerData = getPlayerData(player.getUniqueId());
        return playerData.getBank();
    }

    public String firstname(Player player) {
        PlayerData playerData = getPlayerData(player.getUniqueId());
        return playerData.getFirstname();
    }

    public String lastname(Player player) {
        PlayerData playerData = getPlayerData(player.getUniqueId());
        return playerData.getLastname();
    }

    public int visum(Player player) {
        PlayerData playerData = getPlayerData(player.getUniqueId());
        return playerData.getVisum();
    }

    public int paydayDuration(Player player) {
        return getPlayerData(player.getUniqueId()).getMinutes();
    }

    public void setPlayerMove(Player player, Boolean state) {
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

    public boolean canPlayerMove(Player player) {
        return playerMovement.get(player.getUniqueId().toString()) != null;
    }

    public boolean isTeam(Player player) {
        return getPlayerData(player.getUniqueId()).getPermlevel() >= 40;
    }

    public Integer perms(Player player) {
        PlayerData playerData = getPlayerData(player.getUniqueId());
        return playerData.getPermlevel();
    }

    public String rang(Player player) {
        PlayerData playerData = getPlayerData(player.getUniqueId());
        return playerData.getRang();
    }

    public void startTimeTracker() {
        new BukkitRunnable() {
            @SneakyThrows
            @Override
            public void run() {
                int currentMinute = Utils.getTime().getMinute();
                Bukkit.getPluginManager().callEvent(new MinuteTickEvent(currentMinute));

                // Batch-Operation für Spielerdaten-Update
                Map<UUID, PlayerData> playerDataMap = new HashMap<>();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    PlayerData playerData = getPlayerData(player.getUniqueId());
                    playerDataMap.put(player.getUniqueId(), playerData);
                    if (playerData.getBoostDuration() != null) {
                        if (Utils.getTime().isAfter(playerData.getBoostDuration())) {
                            clearExpBoost(player);
                        }
                    }
                    add1MinutePlaytime(player);
                    if (!playerData.isAFK()) {
                        int afkCounter = playerData.getIntVariable("afk") + 1;
                        playerData.setIntVariable("afk", afkCounter);
                        if (afkCounter >= 2) {
                            Main.getInstance().utils.setAFK(player, true);
                        }
                    }
                    if (playerData.getJailParole() > 0) {
                        playerData.setJailParole(playerData.getJailParole() - 1);
                        PreparedStatement preparedStatement;
                        if (playerData.getJailParole() == 0) {
                            preparedStatement = Main.getInstance().mySQL.getConnection().prepareStatement("UPDATE Jail_Parole SET minutes_remaining = ? WHERE uuid = ?");
                            preparedStatement.setInt(1, playerData.getJailParole());
                            preparedStatement.setString(2, player.getUniqueId().toString());
                            preparedStatement.executeUpdate();
                        } else {
                            preparedStatement = Main.getInstance().mySQL.getConnection().prepareStatement("DELETE FROM Jail_Parole WHERE uuid = ?");
                            preparedStatement.setString(1, player.getUniqueId().toString());
                            preparedStatement.execute();
                            player.sendMessage("§8[§cGefängnis§8]§7 Deine Bewährung ist abgelaufen.");
                        }
                        preparedStatement.close();
                    }
                }

                if (currentMinute % 5 == 0) {
                    for (PlayerData playerData : getPlayers()) {
                        for (PlayerWorkstation playerWorkstation : playerData.getWorkstations()) {
                            playerWorkstation.doTick();
                        }
                    }
                }

                if (Utils.getTime().getDayOfWeek().equals(DayOfWeek.SATURDAY)) {
                    if (Utils.getTime().getMinute() == 30 && Utils.getTime().getHour() == 17) {
                        String[] factions = Main.getInstance().factionManager.getFactions().stream().map(FactionData::getName).toArray(String[]::new);
                        Main.getInstance().factionManager.sendCustomLeaderMessageToFactions("§8[§3Bank§8]§a In 90 Minuten ist die Auktion beendet!", factions);
                    }
                    if (Utils.getTime().getMinute() == 30 && Utils.getTime().getHour() == 18) {
                        String[] factions = Main.getInstance().factionManager.getFactions().stream().map(FactionData::getName).toArray(String[]::new);
                        Main.getInstance().factionManager.sendCustomLeaderMessageToFactions("§8[§3Bank§8]§a In 30 Minuten ist die Auktion beendet!", factions);
                    }

                    if (Utils.getTime().getMinute() == 45 && Utils.getTime().getHour() == 18) {
                        String[] factions = Main.getInstance().factionManager.getFactions().stream().map(FactionData::getName).toArray(String[]::new);
                        Main.getInstance().factionManager.sendCustomLeaderMessageToFactions("§8[§3Bank§8]§a In 15 Minuten ist die Auktion beendet!", factions);
                    }

                    if (Utils.getTime().getMinute() == 55 && Utils.getTime().getHour() == 18) {
                        String[] factions = Main.getInstance().factionManager.getFactions().stream().map(FactionData::getName).toArray(String[]::new);
                        Main.getInstance().factionManager.sendCustomLeaderMessageToFactions("§8[§3Bank§8]§a In 5 Minuten ist die Auktion beendet!", factions);
                    }

                    if (Utils.getTime().getMinute() == 0 && Utils.getTime().getHour() == 19 && Utils.getTime().getDayOfWeek().equals(DayOfWeek.SATURDAY)) {
                        Main.getInstance().commands.auktionCommand.rollAuction();
                    }
                }

                if (currentMinute == 0) {
                    int currentHour = Main.getInstance().utils.getCurrentHour();
                    Bukkit.getPluginManager().callEvent(new HourTickEvent(currentHour));

                    // Batch-Operation für Fraktionsdaten-Update
                    for (FactionData factionData : Main.getInstance().factionManager.getFactions()) {
                        double zinsen = Math.round(factionData.getBank() * 0.00075);
                        double steuern = Math.round(factionData.getBank() * 0.00035);
                        if (factionData.getBank() >= factionData.upgrades.getTax()) {
                            steuern += Math.round(factionData.getBank() * 0.015);
                        }
                        double plus = zinsen - steuern;

                        // Berechnung der Gebietseinnahmen
                        for (Gangwar gangwarData : Main.getInstance().utils.gangwarUtils.getGangwars()) {
                            if (gangwarData.getGangZone().getOwner().equals(factionData.getName())) {
                                plus = plus + 150;
                            }
                        }
                        int auction = 0;
                        try {
                            if (Integer.parseInt(GlobalStats.getValue("auction")) == factionData.getId()) {
                                auction = Main.random(500, 1000);
                                plus = plus + auction;
                            }
                        } catch (Exception ignored) {

                        }

                        int banner = 0;
                        for (SprayableBanner sprayableBanner : Main.getInstance().factionManager.getBanner()) {
                            if (sprayableBanner.getFaction() == factionData.getId()) {
                                banner++;
                            }
                        }
                        plus += (banner * 30);

                        // Batch-Operation für Fraktionsmitglieder
                        for (PlayerData playerData : playerDataMap.values()) {
                            if (playerData.getFaction() != null) {
                                if (playerData.getFactionGrade() >= 5 && playerData.getFaction().equals(factionData.getName())) {
                                    Player player = Bukkit.getPlayer(playerData.getUuid());
                                    if (player == null) continue;
                                    sendFactionPaydayMessage(player, factionData, zinsen, steuern, plus, auction, (banner * 30));
                                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                                }
                            }
                        }

                        // Datenbank- und Fraktionsaktualisierungen
                        try {
                            Main.getInstance().factionManager.addFactionMoney(factionData.getName(), (int) plus, "Fraktionspayday");
                            Statement statement = mySQL.getStatement();
                            statement.execute("UPDATE factions SET jointsMade = 0 WHERE id = " + factionData.getId());
                            factionData.setJointsMade(0);
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }

                if (currentMinute % 15 == 0) {
                    if (Main.getInstance().laboratory != null) Main.getInstance().laboratory.pushTick();
                }
            }
        }.runTaskTimer(Main.getInstance(), 20 * 2, 20 * 60);
    }

    private void sendFactionPaydayMessage(Player player, FactionData factionData, double zinsen, double steuern, double plus, int auction, int banner) {
        player.sendMessage(" ");
        player.sendMessage("§7   ===§8[§" + factionData.getPrimaryColor() + "KONTOAUSZUG (" + factionData.getName() + ")§8]§7===");
        player.sendMessage(" ");
        player.sendMessage("§8 ➥ §6Zinsen§8:§a +" + (int) zinsen + "$");
        player.sendMessage("§8 ➥ §6Steuern§8:§c -" + (int) steuern + "$");
        List<IGangzone> gangZones = new ArrayList<>();
        for (IGangzone gangzone : Main.getInstance().utils.gangwarUtils.getGangzones()) {
            if (gangzone.getOwner().equals(factionData.getName())) {
                gangZones.add(gangzone);
            }
        }
        if (gangZones.size() >= 1) {
            player.sendMessage(" ");
        }
        for (IGangzone gangzone : gangZones) {
            if (gangzone.getOwner().equals(factionData.getName())) {
                plus += 150;
                player.sendMessage("§8 ➥ §6Gebietseinnahmen (" + gangzone.getName() + ")§8:§a +" + 150 + "$");
            }
        }
        if (factionData.hasLaboratory()) {
            player.sendMessage(" ");
            player.sendMessage("§8 ➥ §7Zigarren§8:§a +" + factionData.getJointsMade() + " Stück");
        }
        if (auction != 0) {
            player.sendMessage("§8 ➥ §3Bank§8:§a +" + auction + "$");
        }
        player.sendMessage("§8 ➥ §bBanner§8:§a +" + banner + "$");
        player.sendMessage(" ");
        if (plus >= 0) {
            player.sendMessage("§8 ➥ §6Kontostand§8:§e " + new DecimalFormat("#,###").format(Main.getInstance().factionManager.factionBank(factionData.getName())) + "$ §8(§a+" + (int) plus + "$§8)");
        } else {
            player.sendMessage("§8 ➥ §6Kontostand§8:§e " + new DecimalFormat("#,###").format(Main.getInstance().factionManager.factionBank(factionData.getName())) + "$ §8(§c" + (int) plus + "$§8)");
        }
        player.sendMessage(" ");
    }


    public void kickPlayer(Player player, String reason) {
        player.kickPlayer("§8• §6§lVoidRoleplay §8•\n\n§cDu wurdest vom Server geworfen.\nGrund§8:§7 " + reason + "\n\n§8• §6§lVoidRoleplay §8•");
    }

    @SneakyThrows
    public void clearExpBoost(Player player) {
        PlayerData playerData = getPlayerData(player);
        player.sendMessage("§8[§bEXP-Boost§8]§c Dein EXP-Boost ist ausgelaufen!");
        playerData.setBoostDuration(null);
        PreparedStatement statement = Main.getInstance().mySQL.getConnection().prepareStatement("UPDATE players SET boostDuration = NULL WHERE uuid = ?");
        statement.setString(1, player.getUniqueId().toString());
        statement.executeUpdate();
        statement.close();
    }

    public void addExp(Player player, Integer exp) {
        String characters = "a0b1c2d3e4569";
        PlayerData playerData = playerDataMap.get(player.getUniqueId());
        exp = exp + ( exp * (playerData.getPlayerPowerUpManager().getPowerUp(Powerup.EXP).getAmount() / 100));
        if (playerData.getBoostDuration() != null) {
            if (Utils.getTime().isAfter(playerData.getBoostDuration())) {
                clearExpBoost(player);
            } else {
                exp = exp * 2;
            }
        }
        playerData.setExp(playerData.getExp() + exp);
        if (playerData.getExp() >= playerData.getNeeded_exp()) {
            player.sendMessage("§8[§6Level§8] §7Du bist im Level aufgestiegen! §a" + playerData.getLevel() + " §8➡ §2" + (playerData.getLevel() + 1));
            Main.getInstance().utils.sendActionBar(player, "§6Du bist ein Level aufgestiegen!");
            playerData.setLevel(playerData.getLevel() + 1);
            Main.getInstance().beginnerpass.didQuest(player, 3);
            player.setMaxHealth(32 + (((double) playerData.getLevel() / 5) * 2));
            playerData.setExp(playerData.getExp() - playerData.getNeeded_exp());
            playerData.setNeeded_exp(playerData.getNeeded_exp() + 1000);
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1, 0);
        } else {
            if (playerData.getBoostDuration() == null) {
                player.sendMessage("§" + Main.getRandomChar(characters) + "+" + exp + " EXP");
                Main.getInstance().utils.sendActionBar(player, "§" + Main.getRandomChar(characters) + "+" + exp + " EXP");
            } else {
                player.sendMessage("§" + Main.getRandomChar(characters) + "§l+" + exp + " EXP (2x)");
                Main.getInstance().utils.sendActionBar(player, "§l+" + exp + " EXP (2x)");
            }
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
        }
        player.setExp((float) playerData.getExp() / playerData.getNeeded_exp());
    }

    public void addExp(Player player, EXPType expType, Integer amount) {
        PlayerData playerData = getPlayerData(player.getUniqueId());
        switch (expType.getSkillType()) {
            case FISHING:
                playerData.addonXP.addFishingXP(amount);
                Main.getInstance().utils.sendActionBar(player, expType.getColor() + "+" + amount + " " + expType.getDisplayName() + "-XP (" + playerData.addonXP.getFishingXP() + "/" + expType.getLevelUpXp() + ")");
                break;
            case LUMBERJACK:
                playerData.addonXP.addLumberjackXP(amount);
                Main.getInstance().utils.sendActionBar(player, expType.getColor() + "+" + amount + " " + expType.getDisplayName() + "-XP (" + playerData.addonXP.getLumberjackXP() + "/" + expType.getLevelUpXp() + ")");
                break;
            case POPULARITY:
                playerData.addonXP.addPopularity(amount);
                Main.getInstance().utils.sendActionBar(player, expType.getColor() + "+" + amount + " " + expType.getDisplayName() + "-XP (" + playerData.addonXP.getPopularityXP() + "/" + expType.getLevelUpXp() + ")");
                break;
            case MINER:
                playerData.addonXP.addMinerXP(amount);
                Main.getInstance().utils.sendActionBar(player, expType.getColor() + "+" + amount + " " + expType.getDisplayName() + "-XP (" + playerData.addonXP.getMinerXP() + "/" + ((playerData.addonXP.getMinerLevel() + 1) * expType.getLevelUpXp()) + ")");
                break;
        }
    }

    public void addEXPBoost(Player player, int hours) throws SQLException {
        PlayerData playerData = playerDataMap.get(player.getUniqueId());

        if (playerData.getBoostDuration() == null) {
            playerData.setBoostDuration(Utils.getTime().plusHours(hours));
        } else {
            playerData.setBoostDuration(playerData.getBoostDuration().plusHours(hours));
        }

        // Format the boostDuration as a string
        String formattedBoostDuration = playerData.getBoostDuration().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // Use PreparedStatement to prevent SQL injection and handle SQL syntax properly
        String query = "UPDATE `players` SET `boostDuration` = ? WHERE `uuid` = ?";
        try (PreparedStatement preparedStatement = Main.getInstance().mySQL.getConnection().prepareStatement(query)) {
            preparedStatement.setString(1, formattedBoostDuration);
            preparedStatement.setString(2, player.getUniqueId().toString());
            preparedStatement.executeUpdate();
        }
    }

    @SneakyThrows
    public void redeemRank(Player player, String type, int duration, String duration_type) {
        PlayerData playerData = playerDataMap.get(player.getUniqueId());
        switch (type.toLowerCase()) {
            case "vip":
                if (playerData.getRang().equals("VIP") || playerData.getRang().equals("Spieler")) {
                    if (playerData.getRankDuration() != null) {
                        playerData.setRankDuration(playerData.getRankDuration().plusDays(duration));
                    } else {
                        playerData.setRankDuration(Utils.getTime().plusDays(duration));
                    }
                } else {
                    playerData.setRankDuration(Utils.getTime().plusDays(duration));
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
                        playerData.setRankDuration(Utils.getTime().plusDays(duration));
                    }
                } else {
                    playerData.setRankDuration(Utils.getTime().plusDays(duration));
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
                        playerData.setRankDuration(Utils.getTime().plusDays(duration));
                    }
                } else {
                    playerData.setRankDuration(Utils.getTime().plusDays(duration));
                    player.sendMessage("§b   Info§8:§f Da du vom Rang " + playerData.getRang() + " auf Gold gestiegen bist, ist der alte Rang verloren gegangen.");
                }
                playerData.setRang("Gold");
                playerData.setPermlevel(10);
                break;
            default:
                player.sendMessage(Main.error + "§cFehler. Bitte einen Administratoren kontaktieren.");
                break;
        }
        Statement statement = Main.getInstance().mySQL.getStatement();
        System.out.println(playerData.getRankDuration());
        statement.executeUpdate("UPDATE `players` SET `rankDuration` = '" + playerData.getRankDuration() + "', `player_rank` = '" + playerData.getRang() + "', `player_permlevel` = " + playerData.getPermlevel() + " WHERE `uuid` = '" + player.getUniqueId() + "'");
        TeamSpeak.reloadPlayer(player.getUniqueId());
    }

    public void setJob(Player player, String job) {
        PlayerData playerData = playerDataMap.get(player.getUniqueId());
        playerData.setJob(job);
        try {
            Statement statement = Main.getInstance().mySQL.getStatement();
            statement.executeUpdate("UPDATE `players` SET `job` = '" + job + "' WHERE `uuid` = '" + player.getUniqueId() + "'");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void resetJob(Player player) {
        PlayerData playerData = playerDataMap.get(player.getUniqueId());
        playerData.setJob(null);
        try {
            Statement statement = Main.getInstance().mySQL.getStatement();
            statement.executeUpdate("UPDATE `players` SET `job` = null WHERE `uuid` = '" + player.getUniqueId() + "'");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isInStaatsFrak(Player player) {
        PlayerData playerData = playerDataMap.get(player.getUniqueId());
        if (playerData.getFaction() == null) return false;
        return playerData.getFaction().equals("FBI") || playerData.getFaction().equals("Medic") || playerData.getFaction().equals("Polizei");
    }

    public void openInterActionMenu(Player player, Player targetplayer) {
        PlayerData targetData = getPlayerData(targetplayer);
        if (targetData.isAFK()) return;
        Main.getInstance().beginnerpass.didQuest(player, 12);
        PlayerData playerData = getPlayerData(player);
        InventoryManager inventoryManager = new InventoryManager(player, 54, "§8 » §6Interaktionsmenü");
        playerData.setVariable("current_player", targetplayer.getUniqueId().toString());
        inventoryManager.setItem(new CustomItem(13, ItemManager.createItemHead(targetplayer.getUniqueId().toString(), 1, 0, "§6" + targetplayer.getName())) {
            @Override
            public void onClick(InventoryClickEvent event) {

            }
        });
        inventoryManager.setItem(new CustomItem(20, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjg4OWNmY2JhY2JlNTk4ZThhMWNkODYxMGI0OWZjYjYyNjQ0ZThjYmE5ZDQ5MTFkMTIxMTM0NTA2ZDhlYTFiNyJ9fX0=", 1, 0, "§aGeld geben", null)) {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (player.getLocation().distance(targetplayer.getLocation()) > 5) {
                    player.sendMessage(Prefix.ERROR + targetplayer.getName() + " ist nicht in der nähe");
                    return;
                }
                playerData.setVariable("chatblock", "givemoney");
                player.sendMessage("§8[§6Interaktion§8]§7 Gib nun einen Wert ein.");
                player.closeInventory();
            }
        });
        inventoryManager.setItem(new CustomItem(24, ItemManager.createItem(Material.PAPER, 1, 0, "§6Personalausweis zeigen")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (player.getLocation().distance(targetplayer.getLocation()) > 5) {
                    player.sendMessage(Prefix.ERROR + targetplayer.getName() + " ist nicht in der nähe");
                    return;
                }
                player.performCommand("personalausweis show " + targetplayer.getName());
                if (playerData.getGender().equals(Gender.MALE)) {
                    ChatUtils.sendMeMessageAtPlayer(player, "§o" + player.getName() + " zeigt " + targetplayer.getName() + " seinen Personalausweis.");
                } else {
                    ChatUtils.sendMeMessageAtPlayer(player, "§o" + player.getName() + " zeigt " + targetplayer.getName() + " ihren Personalausweis.");
                }
                player.closeInventory();
            }
        });
        inventoryManager.setItem(new CustomItem(25, ItemManager.createItem(Material.PAPER, 1, 0, "§6Finanzen zeigen")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (player.getLocation().distance(targetplayer.getLocation()) > 5) {
                    player.sendMessage(Prefix.ERROR + targetplayer.getName() + " ist nicht in der nähe");
                    return;
                }
                player.sendMessage(Prefix.MAIN + "Du hast " + targetplayer.getName() + " deine Finanzen gezeigt.");
                player.closeInventory();
                targetplayer.sendMessage("§7   ===§8[§6" + player.getName() + "'s Finanzen§8]§7===");
                targetplayer.sendMessage("§8 ➥§eBargeld§8: §a" + playerData.getBargeld() + "$");
                targetplayer.sendMessage("§8 ➥§eBank§8: §a" + playerData.getBank() + "$");
            }
        });
        inventoryManager.setItem(new CustomItem(38, ItemManager.createItem(Material.POPPY, 1, 0, "§cKüssen")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (player.getLocation().distance(targetplayer.getLocation()) > 5) {
                    player.sendMessage(Prefix.ERROR + targetplayer.getName() + " ist nicht in der nähe");
                    return;
                }
                Server.Utils.kissPlayer(player, targetplayer);
                player.closeInventory();
            }
        });
        inventoryManager.setItem(new CustomItem(42, ItemManager.createItem(Material.POPPY, 1, 0, "§eTragen")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (player.getLocation().distance(targetplayer.getLocation()) > 5) {
                    player.sendMessage(Prefix.ERROR + targetplayer.getName() + " ist nicht in der nähe");
                    return;
                }
                carryPlayer(player, targetplayer);
                player.closeInventory();
            }
        });
        if (playerData.getFaction() != null) {
            FactionData factionData = Main.getInstance().factionManager.getFactionData(playerData.getFaction());
            inventoryManager.setItem(new CustomItem(53, ItemManager.createItem(Material.GOLD_NUGGET, 1, 0, "§8[§" + factionData.getPrimaryColor() + factionData.getName() + "§8]§7 Interaktionsmenü")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    Main.getInstance().getCooldownManager().setCooldown(player, "interaction_cooldown", 1);
                    openFactionInteractionMenu(player, targetplayer, playerData.getFaction());
                }
            });
        }
    }

    public void openFactionInteractionMenu(Player player, Player targetplayer, String faction) {
        InventoryManager inventoryManager = new InventoryManager(player, 54, "§8 » §6Interaktionsmenü");
        PlayerData playerData = getPlayerData(player.getUniqueId());
        if (targetplayer == null) return;
        PlayerData targetplayerData = getPlayerData(targetplayer.getUniqueId());
        playerData.setVariable("current_inventory", "interaktionsmenü_" + faction);
        playerData.setVariable("current_player", targetplayer.getUniqueId().toString());
        inventoryManager.setItem(new CustomItem(13, ItemManager.createItemHead(targetplayer.getUniqueId().toString(), 1, 0, "§6" + targetplayer.getName())) {
            @Override
            public void onClick(InventoryClickEvent event) {

            }
        });
        if (ItemManager.getCustomItemCount(targetplayer, RoleplayItem.SMARTPHONE) >= 1) {
            inventoryManager.setItem(new CustomItem(22, ItemManager.createItem(RoleplayItem.SMARTPHONE.getMaterial(), 1, 0, "§eHandy abnehmen")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    if (player.getLocation().distance(targetplayer.getLocation()) > 5) {
                        player.sendMessage(Prefix.ERROR + targetplayer.getName() + " ist nicht in der nähe");
                        return;
                    }
                    if (ItemManager.getCustomItemCount(targetplayer, RoleplayItem.SMARTPHONE) < 1) {
                        return;
                    }
                    ItemManager.removeCustomItem(targetplayer, RoleplayItem.SMARTPHONE, 1);
                    ItemManager.addCustomItem(player, RoleplayItem.SMARTPHONE, 1);
                    ChatUtils.sendGrayMessageAtPlayer(player, player.getName() + " nimmt " + targetplayer.getName() + " das Handy ab");
                    player.closeInventory();
                }
            });
        } else if (ItemManager.getCustomItemCount(player, RoleplayItem.SMARTPHONE) >= 1) {
            inventoryManager.setItem(new CustomItem(22, ItemManager.createItem(RoleplayItem.SMARTPHONE.getMaterial(), 1, 0, "§eHandy geben")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    if (player.getLocation().distance(targetplayer.getLocation()) > 5) {
                        player.sendMessage(Prefix.ERROR + targetplayer.getName() + " ist nicht in der nähe");
                        return;
                    }
                    if (ItemManager.getCustomItemCount(player, RoleplayItem.SMARTPHONE) < 1) {
                        return;
                    }
                    ItemManager.removeCustomItem(player, RoleplayItem.SMARTPHONE, 1);
                    ItemManager.addCustomItem(targetplayer, RoleplayItem.SMARTPHONE, 1);
                    ChatUtils.sendGrayMessageAtPlayer(player, player.getName() + " gibt " + targetplayer.getName() + " ein Handy");
                    player.closeInventory();
                }
            });
        } else {
            inventoryManager.setItem(new CustomItem(22, ItemManager.createItem(RoleplayItem.SMARTPHONE.getMaterial(), 1, 0, "§e§mHandy abnehmen")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                }
            });
        }
        switch (faction.toLowerCase()) {
            case "medic":
                inventoryManager.setItem(new CustomItem(20, ItemManager.createItem(Material.REDSTONE, 1, 0, "§cBlutgruppe testen")) {
                    @Override
                    public void onClick(InventoryClickEvent event) {
                        if (player.getLocation().distance(targetplayer.getLocation()) > 5) {
                            player.sendMessage(Prefix.ERROR + targetplayer.getName() + " ist nicht in der nähe");
                            return;
                        }
                        Main.getInstance().utils.staatUtil.checkBloodGroup(player, targetplayer);
                        player.closeInventory();
                    }
                });
                inventoryManager.setItem(new CustomItem(21, ItemManager.createItem(Material.PAPER, 1, 0, "§cUntersuchen")) {
                    @Override
                    public void onClick(InventoryClickEvent event) {
                        if (player.getLocation().distance(targetplayer.getLocation()) > 5) {
                            player.sendMessage(Prefix.ERROR + targetplayer.getName() + " ist nicht in der nähe");
                            return;
                        }
                        player.closeInventory();
                        player.sendMessage("§7   ===§8[§cUntersuchung§8]§7===");
                        for (IllnessType illnessType : IllnessType.values()) {
                            if (targetplayerData.getIllness(illnessType) != null) {
                                player.sendMessage("§8 ➦ §6" + illnessType.getName() + "§8: §cPositiv");
                            } else {
                                player.sendMessage("§8 ➦ §6" + illnessType.getName() + "§8: §aNegativ");
                            }
                        }

                        targetplayer.sendMessage(Prefix.MAIN + player.getName() + " hat dich untersucht.");
                    }
                });
                break;
            case "fbi":
            case "polizei":
                if (targetplayerData.isCuffed()) {
                    inventoryManager.setItem(new CustomItem(20, ItemManager.createItem(Material.LEAD, 1, 0, "§3Handschellen abnehmen")) {
                        @Override
                        public void onClick(InventoryClickEvent event) {
                            if (player.getLocation().distance(targetplayer.getLocation()) > 5) {
                                player.sendMessage(Prefix.ERROR + targetplayer.getName() + " ist nicht in der nähe");
                                return;
                            }
                            targetplayerData.setCuffed(false);
                            ItemManager.addCustomItem(player, RoleplayItem.CUFF, 1);
                            ChatUtils.sendGrayMessageAtPlayer(player, player.getName() + " hat " + targetplayer.getName() + " Handschellen abgenommen.");
                            player.closeInventory();
                        }
                    });
                } else {
                    inventoryManager.setItem(new CustomItem(20, ItemManager.createItem(Material.LEAD, 1, 0, "§3§mHandschellen abnehmen")) {
                        @Override
                        public void onClick(InventoryClickEvent event) {
                        }
                    });
                }
                inventoryManager.setItem(new CustomItem(21, ItemManager.createItem(Material.LEAD, 1, 0, "§3Dienstmarke zeigen")) {
                    @Override
                    public void onClick(InventoryClickEvent event) {
                        if (player.getLocation().distance(targetplayer.getLocation()) > 5) {
                            player.sendMessage(Prefix.ERROR + targetplayer.getName() + " ist nicht in der nähe");
                            return;
                        }
                        targetplayer.sendMessage("§7   ===§8[§3Dienstmarke§8]§7===");
                        targetplayer.sendMessage("§8 ➥ §bRang§8: §7" + Main.getInstance().factionManager.getRankName(playerData.getFaction(), playerData.getFactionGrade()));
                        player.sendMessage(Prefix.MAIN + "Du hast " + targetplayer.getName() + " deinen Dienstausweis gezeigt.");
                    }
                });
                break;
        }
        inventoryManager.setItem(new CustomItem(53, ItemManager.createItem(Material.GOLD_NUGGET, 1, 0, "§7Interaktionsmenü")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openInterActionMenu(player, targetplayer);
            }
        });
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
                            removeMoney(event.getPlayer(), amount, "Geld an " + targetplayer.getName() + " übergeben.");
                            addMoney(targetplayer, amount, event.getPlayer().getName() + " gab " + targetplayer.getName() + " Geld");
                            event.getPlayer().sendMessage("§2Du hast " + targetplayer.getName() + " " + amount + "$ zugesteckt.");
                            targetplayer.sendMessage("§2" + event.getPlayer().getName() + " hat dir " + amount + "$ zugesteckt.");
                            ChatUtils.sendMeMessageAtPlayer(event.getPlayer(), "§o" + event.getPlayer().getName() + " gibt " + targetplayer.getName() + " Bargeld.");
                            Main.getInstance().adminManager.send_message(event.getPlayer().getName() + " hat " + targetplayer.getName() + " " + amount + "$ gegeben.", ChatColor.GOLD);
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
        PlayerData playerData = getPlayerData(player);
        if (playerData.isAFK() && event.getTo() != null && event.getFrom().distance(event.getTo()) > 0) {
            event.getPlayer().setCollidable(false);
        }
        if (!canPlayerMove(player)) return;
        player.setFlying(false);
    }

    public PlayerData getPlayerData(UUID uuid) {
        return playerDataMap.get(uuid);
    }

    public PlayerData getPlayerData(Player player) {
        return playerDataMap.get(player.getUniqueId());
    }

    public Collection<PlayerData> getPlayers() {
        return playerDataMap.values();
    }

    public void addCoins(Player player, int amount) {
        PlayerData playerData = getPlayerData(player.getUniqueId());
        playerData.setCoins(playerData.getCoins() + amount);
        player.sendMessage("§e+" + amount + " Coins");
        try {
            Statement statement = Main.getInstance().mySQL.getStatement();
            statement.executeUpdate("UPDATE players SET coins = coins + " + amount + " WHERE uuid = '" + player.getUniqueId() + "'");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void removeCoins(Player player, int amount) {
        PlayerData playerData = getPlayerData(player.getUniqueId());
        playerData.setCoins(playerData.getCoins() - amount);
        try {
            Statement statement = Main.getInstance().mySQL.getStatement();
            statement.executeUpdate("UPDATE players SET coins = coins - " + amount + " WHERE uuid = '" + player.getUniqueId() + "'");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void PushMinuteTick() {

    }

    @Override
    public void PushHourTick() {

    }

    @SneakyThrows
    public void setPlayerSpawn(PlayerData playerData, String spawn) {
        PreparedStatement statement = Main.getInstance().mySQL.getConnection().prepareStatement("UPDATE players SET spawn = ? WHERE uuid = ?");
        if (spawn.equalsIgnoreCase("krankenhaus")) {
            spawn = null;
        }
        statement.setString(1, spawn);
        statement.setString(2, playerData.getUuid().toString());
        statement.executeUpdate();
        statement.close();
        playerData.setSpawn(spawn);
    }

    public void carryPlayer(Player player, Player target) {
        // Überprüfen, ob die Spieler null sind
        if (player == null || target == null) {
            return; // Beende die Methode, wenn einer der Spieler null ist
        }

        ArmorStand armorStand = (ArmorStand) player.getWorld().spawnEntity(player.getLocation(), EntityType.ARMOR_STAND);

        armorStand.setVisible(false);
        armorStand.setGravity(false);
        armorStand.setBasePlate(false);
        armorStand.setArms(false);
        armorStand.setSmall(true);
        armorStand.getPersistentDataContainer().set(new NamespacedKey(Main.plugin, "id"), PersistentDataType.INTEGER, 0);
        armorStand.setCustomName("CarryStand_" + player.getUniqueId()); // Füge einen benutzerdefinierten Namen hinzu

        Location playerLocation = player.getLocation();
        Location armorStandLocation = playerLocation.clone().add(0, 1.8, 0); // 1.8 für den Kopfbereich
        armorStand.teleport(armorStandLocation);

        player.addPassenger(armorStand);
        armorStand.addPassenger(target);
    }

    private ArmorStand getArmorStand(Player player) {
        for (Entity entity : player.getWorld().getEntities()) {
            if (entity instanceof ArmorStand) {
                ArmorStand armorStand = (ArmorStand) entity;
                if (armorStand.getCustomName() != null && armorStand.getCustomName().equals("CarryStand_" + player.getUniqueId())) {
                    return armorStand;
                }
            }
        }
        return null;
    }

    public void removeTargetFromArmorStand(Player player) {
        ArmorStand armorStand = getArmorStand(player);
        if (armorStand != null) {
            armorStand.remove();
        }
    }


    public boolean isCarrying(Entity entity) {
        if (entity == null) {
            return false;
        }

        return !entity.getPassengers().isEmpty();
    }

    @SneakyThrows
    public int getGeworbenCount(Player player) {
        Connection connection = Main.getInstance().mySQL.getConnection();
        PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM players WHERE geworben = ? AND visum >= 2");
        statement.setString(1, player.getUniqueId().toString());
        ResultSet result = statement.executeQuery();

        int count = 0;
        if (result.next()) {
            count = result.getInt(1);
        }

        result.close();
        statement.close();
        connection.close();

        return count;
    }


}
