package de.polo.voidroleplay.manager;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.*;
import de.polo.voidroleplay.game.base.shops.ShopData;
import de.polo.voidroleplay.game.base.shops.ShopItem;
import de.polo.voidroleplay.game.events.SecondTickEvent;
import de.polo.voidroleplay.game.faction.gangwar.Gangwar;
import de.polo.voidroleplay.utils.GamePlay.MilitaryDrop;
import de.polo.voidroleplay.utils.Utils;
import de.polo.voidroleplay.utils.enums.FFAStatsType;
import de.polo.voidroleplay.utils.enums.ShopType;
import de.polo.voidroleplay.utils.playerUtils.ScoreboardAPI;
import lombok.SneakyThrows;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.*;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.*;

public class ServerManager {
    public static final boolean canDoJobsBoolean = true;
    public static final String error_cantDoJobs = Main.error + "Der Job ist Serverseitig bis nach Restart gesperrt.";

    public static final Map<String, RankData> rankDataMap = new HashMap<>();
    public static final Map<String, DBPlayerData> dbPlayerDataMap = new HashMap<>();
    public static final Map<String, FactionPlayerData> factionPlayerDataMap = new HashMap<>();
    public static final Map<String, ContractData> contractDataMap = new HashMap<>();
    public static final Map<Integer, ShopData> shopDataMap = new HashMap<>();
    public static final Map<String, String> serverVariables = new HashMap<>();
    public static final List<UUID> factionStorageWeaponsTookout = new ArrayList<>();
    private static final Map<String, PayoutData> payoutDataMap = new HashMap<>();
    public static Object[][] faction_grades;

    private final PlayerManager playerManager;
    private final FactionManager factionManager;
    private final Utils utils;
    private final LocationManager locationManager;

    public ServerManager(PlayerManager playerManager, FactionManager factionManager, Utils utils, LocationManager locationManager) {
        this.playerManager = playerManager;
        this.factionManager = factionManager;
        this.utils = utils;
        this.locationManager = locationManager;
        try {
            loadRanks();
            loadDBPlayer();
            startTabUpdateInterval();
            everySecond();
            loadShops();
            loadContracts();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean canDoJobs() {
        return Utils.getTime().getHour() != 1 || Utils.getTime().getMinute() < 55;
    }

    public static boolean canSpawnDrop() {
        return Utils.getTime().getHour() != 1;
    }

    public static int getPayout(String type) {
        return payoutDataMap.get(type).getPayout();
    }

    public static void setVariable(String variable, String value) {
        if (serverVariables.get(variable) != null) {
            serverVariables.replace(variable, value);
        } else {
            serverVariables.put(variable, value);
        }
    }

    public static String getVariable(String variable) {
        return serverVariables.get(variable);
    }

    private void loadRanks() throws SQLException {
        Statement statement = Main.getInstance().mySQL.getStatement();
        ResultSet locs = statement.executeQuery("SELECT * FROM ranks");
        while (locs.next()) {
            RankData rankData = new RankData();
            rankData.setId(locs.getInt(1));
            rankData.setRang(locs.getString(2));
            rankData.setPermlevel(locs.getInt(3));
            rankData.setTeamSpeakID(locs.getInt(4));
            rankData.setSecondary(locs.getBoolean(5));
            rankData.setForumID(locs.getInt("forumID"));
            rankData.setColor(ChatColor.valueOf(locs.getString("color")));
            rankData.setShortName(locs.getString("shortName"));
            rankDataMap.put(locs.getString(2), rankData);
        }
        utils.loadTeams();

        ResultSet res = statement.executeQuery("SELECT * FROM payouts");
        while (res.next()) {
            PayoutData payoutData = new PayoutData();
            payoutData.setId(res.getInt(1));
            payoutData.setType(res.getString(2));
            payoutData.setPayout(res.getInt(3));
            payoutDataMap.put(res.getString(2), payoutData);
        }
        statement.close();
    }

    private void loadDBPlayer() throws SQLException {
        Statement statement = Main.getInstance().mySQL.getStatement();
        ResultSet locs = statement.executeQuery("SELECT * FROM players");
        while (locs.next()) {
            DBPlayerData dbPlayerData = new DBPlayerData();
            dbPlayerData.setId(locs.getInt("id"));
            dbPlayerData.setUuid(locs.getString("uuid"));
            dbPlayerData.setPlayer_rank(locs.getString("player_rank"));
            dbPlayerData.setFaction(locs.getString("faction"));
            dbPlayerData.setFaction_grade(locs.getInt("faction_grade"));
            dbPlayerData.setBusiness(locs.getInt("business"));
            dbPlayerData.setBusiness_grade(locs.getInt("business_grade"));
            dbPlayerDataMap.put(locs.getString(2), dbPlayerData);
            if (locs.getString("faction") != null && !locs.getString("faction").equals("Zivilist")) {
                FactionPlayerData factionPlayerData = new FactionPlayerData();
                factionPlayerData.setId(locs.getInt("id"));
                factionPlayerData.setUuid(locs.getString("uuid"));
                factionPlayerData.setFaction(locs.getString("faction"));
                factionPlayerData.setFaction_grade(locs.getInt("faction_grade"));
                factionPlayerDataMap.put(locs.getString("uuid"), factionPlayerData);
            }
        }
        statement.close();
    }

    private void loadContracts() throws SQLException {
        Statement statement = Main.getInstance().mySQL.getStatement();
        ResultSet locs = statement.executeQuery("SELECT * FROM contract");
        while (locs.next()) {
            ContractData contractData = new ContractData();
            contractData.setId(locs.getInt(1));
            contractData.setUuid(locs.getString(2));
            contractData.setAmount(locs.getInt(3));
            contractData.setSetter(locs.getString(4));
            contractDataMap.put(locs.getString(2), contractData);
        }
        statement.close();
    }

    private void loadShops() throws SQLException {
        Statement statement = Main.getInstance().mySQL.getStatement();
        ResultSet locs = statement.executeQuery("SELECT * FROM shops");
        while (locs.next()) {
            ShopData shopData = new ShopData();
            shopData.setId(locs.getInt(1));
            shopData.setName(locs.getString(2));
            shopData.setX(locs.getInt(3));
            shopData.setY(locs.getInt(4));
            shopData.setZ(locs.getInt(5));
            shopData.setWelt(Bukkit.getWorld(locs.getString(6)));
            shopData.setYaw(locs.getFloat(7));
            shopData.setPitch(locs.getFloat(8));
            shopData.setBank(locs.getInt("bank"));
            if (locs.getInt("company") != 0) {
                shopData.setCompany(locs.getInt("company"));
            }

            String typeString = locs.getString(9);
            if (typeString != null) {
                try {
                    ShopType type = ShopType.valueOf(typeString.toUpperCase());
                    shopData.setType(type);
                } catch (IllegalArgumentException e) {
                    System.err.println("Invalid shop type: " + typeString);
                    continue;
                }
            } else {
                System.err.println("Null shop type retrieved from the database");
                continue;
            }
            shopDataMap.put(locs.getInt(1), shopData);
            Statement nStatement = Main.getInstance().mySQL.getStatement();
            ResultSet i = nStatement.executeQuery("SELECT * FROM shop_items WHERE shop = " + shopData.getId());
            while (i.next()) {
                ShopItem item = new ShopItem();
                item.setId(i.getInt("id"));
                item.setShop(i.getInt("shop"));
                item.setMaterial(Material.valueOf(i.getString("material")));
                item.setDisplayName(i.getString("name"));
                item.setPrice(i.getInt("price"));
                item.setType(i.getString("type"));
                item.setSecondType(i.getString("type2"));
                shopData.addItem(item);
            }
        }
        statement.close();
    }

    private void everySecond() {
        new BukkitRunnable() {
            @SneakyThrows
            @Override
            public void run() {
                LocalDateTime now = Utils.getTime();
                ScoreboardAPI scoreboardAPI = Main.getInstance().getScoreboardAPI();
                Bukkit.getPluginManager().callEvent(new SecondTickEvent(now.getSecond()));
                if (scoreboardAPI != null) scoreboardAPI.everySecond();
                if (now.getHour() == 0 && now.getMinute() == 1 && now.getSecond() == 0 && now.getDayOfWeek() == DayOfWeek.MONDAY) {
                    // clear everything
                    PreparedStatement statement = Main.getInstance().mySQL.getConnection().prepareStatement("DELETE FROM seasonpass_player_quests");
                    statement.execute();
                    for (PlayerData playerData : playerManager.getPlayers()) {
                        playerData.clearQuests();
                        if (playerData.getPlayerFFAStatsManager().getStats(FFAStatsType.WEEKLY) == null) continue;
                        playerData.getPlayerFFAStatsManager().clearStats(FFAStatsType.WEEKLY);
                        Main.getInstance().gamePlay.getFfa().clearStats(FFAStatsType.WEEKLY);
                    }
                    Bukkit.broadcastMessage("§8[§6Seasonpass§8]§7 Der Seasonpass wurde zurückgesetzt!");

                    PreparedStatement ffaStatement = Main.getInstance().mySQL.getConnection().prepareStatement("DELETE FROM player_ffa_stats WHERE statsType = ?");
                    ffaStatement.setString(1, FFAStatsType.WEEKLY.name());
                    ffaStatement.execute();
                    ffaStatement.close();

                    if (now.getDayOfMonth() == 1) {
                        for (PlayerData playerData : playerManager.getPlayers()) {
                            if (playerData.getPlayerFFAStatsManager().getStats(FFAStatsType.MONTHLY) == null) continue;
                            playerData.getPlayerFFAStatsManager().clearStats(FFAStatsType.MONTHLY);
                            Main.getInstance().gamePlay.getFfa().clearStats(FFAStatsType.MONTHLY);
                        }
                        PreparedStatement ffaMonStatement = Main.getInstance().mySQL.getConnection().prepareStatement("DELETE FROM player_ffa_stats WHERE statsType = ?");
                        ffaMonStatement.setString(1, FFAStatsType.MONTHLY.name());
                        ffaMonStatement.execute();
                        ffaMonStatement.close();
                    }
                }
                if (now.getMinute() == 45 && now.getHour() == 1 && now.getSecond() == 0) {
                    Bukkit.broadcastMessage("§8[§cAuto-Restart§8]§c Der Server startet in 15 Minuten neu!");
                }
                if (now.getMinute() == 55 && now.getHour() == 1 && now.getSecond() == 0) {
                    Bukkit.broadcastMessage("§8[§cAuto-Restart§8]§c Der Server startet in 5 Minuten neu!");
                }
                if (now.getMinute() == 57 && now.getHour() == 1 && now.getSecond() == 0) {
                    Bukkit.broadcastMessage("§8[§cAuto-Restart§8]§c Der Server startet in 3 Minuten neu!");
                }
                if (now.getMinute() == 59 && now.getHour() == 1 && now.getSecond() == 0) {
                    Bukkit.broadcastMessage("§8[§cAuto-Restart§8]§c Der Server startet in 1 Minute neu!");
                }
                if (now.getMinute() == 0 && now.getHour() == 2) {
                    Bukkit.spigot().restart();
                    return;
                }
                for (LocationData locationData : locationManager.getLocations()) {
                    if (locationData.getType() == null) continue;
                    if (locationData.getInfo() == null) continue;
                    if (!locationData.getType().equalsIgnoreCase("storage")) {
                        continue;
                    }
                    for (PlayerData playerData : playerManager.getPlayers()) {
                        if (playerData.getFaction() == null) continue;
                        if (!playerData.getFaction().equalsIgnoreCase(locationData.getInfo())) {
                            continue;
                        }
                        for (int d = 0; d <= 90; d += 1) {
                            Location particleLoc = new Location(playerData.getPlayer().getWorld(), locationData.getX(), locationData.getY(), locationData.getZ());
                            particleLoc.setX(locationData.getX() + Math.cos(d) * 1);
                            particleLoc.setZ(locationData.getZ() + Math.sin(d) * 1);
                            playerData.getPlayer().spawnParticle(Particle.REDSTONE, particleLoc, 1, new Particle.DustOptions(Color.WHITE, 2));
                        }
                    }
                }
                if (utils.getCurrentHour() >= 0 && utils.getCurrentHour() < 22) {
                    for (Gangwar gangwarData : Main.getInstance().utils.gangwarUtils.getGangwars()) {
                        if (gangwarData.getAttacker() != null) {
                            FactionData attackerData = factionManager.getFactionData(gangwarData.getAttacker());
                            FactionData defenderData = factionManager.getFactionData(gangwarData.getGangZone().getOwner());
                            for (Player players : Bukkit.getOnlinePlayers()) {
                                PlayerData playerData = playerManager.getPlayerData(players.getUniqueId());
                                if (playerData == null) continue;
                                if (playerData.getFaction() == null) continue;
                                if (playerData.getFaction().equals(gangwarData.getAttacker()) || playerData.getFaction().equals(gangwarData.getGangZone().getOwner())) {
                                    if (playerData.getVariable("gangwar") != null) {
                                        BossBar bossBar = playerData.getBossBar("gangwar");
                                        bossBar.setTitle("§5" + gangwarData.getGangZone().getName() + "§8 | §5" + gangwarData.getMinutes() + "§8:§5" + gangwarData.getSeconds() + "§8 | §" + attackerData.getPrimaryColor() + gangwarData.getAttackerPoints() + "§8 - §" + defenderData.getPrimaryColor() + gangwarData.getDefenderPoints());
                                    }
                                }
                            }
                        }
                    }
                }

                if (MilitaryDrop.ACTIVE) {
                    Main.getInstance().gamePlay.militaryDrop.everySecond();
                }
                for (Player players : Bukkit.getOnlinePlayers()) {
                    PlayerData playerData = playerManager.getPlayerData(players.getUniqueId());
                    if (playerData == null) continue;
                    if (playerData.isDead()) {
                        playerData.setDeathTime(playerData.getDeathTime() - 1);
                        utils.sendActionBar(players, "§cDu bist noch " + Main.getTime(playerData.getDeathTime()) + " Tot.");
                        if (playerData.getDeathTime() <= 0) {
                            Main.getInstance().utils.deathUtil.despawnPlayer(players);
                        }
                    }
                    playerData.getPlayerPetManager().everySecond();
                }
            }
        }.runTaskTimer(Main.getInstance(), 20, 20);
    }

    public void savePlayers() throws SQLException {
        for (Player player : Bukkit.getOnlinePlayers()) {
            playerManager.savePlayer(player);
        }
    }

    public void updateTablist(Player player) {
        if (player == null) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                setTablist(p);
            }
        } else {
            setTablist(player);
        }
    }

    private void setTablist(Player player) {
        LocalDateTime time = Utils.getTime();
        player.setPlayerListHeader("\n§8▍ §6§lVoidRoleplay §8× §eReallife & Roleplay §8▍\n\n§7" + time.getHour() + ":" + time.getMinute() + " Uhr\n§6Ping§8:§7 " + player.getPing() + "ms\n§8__________________\n");
        player.setPlayerListFooter("§8__________________\n\n§8» §e" + Bukkit.getOnlinePlayers().size() + "§8/§6" + Bukkit.getMaxPlayers() + "§8 «\n§8» §9discord.gg/void-roleplay §8«");

    }

    private void startTabUpdateInterval() {
        new BukkitRunnable() {
            int announceTick = 5;
            int announceType = 1;

            @Override
            public void run() {
                //Bukkit.getServer().getWorlds().forEach(world -> world.setFullTime(LocalTime.now().toSecondOfDay()));
                for (Player player : Bukkit.getOnlinePlayers()) {
                    updateTablist(player);
                }
                if (announceTick == 0) {
                    announceTick = 5;
                    switch (announceType) {
                        case 1:
                            TextComponent text = new TextComponent("§8[§6Regelwerk§8]§e Unwissenheit schützt vor Strafe nicht!");
                            text.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://voidroleplay.de/rules"));
                            text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§6§l§oRegelwerk öffnen")));
                            for (Player player : Bukkit.getOnlinePlayers()) {
                                player.spigot().sendMessage(text);
                            }
                            break;
                        case 2:
                            TextComponent forum = new TextComponent("§8[§6Forum§8]§e Bist du schon Mitglied in unserem Forum?");
                            forum.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://forum.voidroleplay.de"));
                            forum.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§6§l§oForum öffnen")));
                            Bukkit.spigot().broadcast(forum);
                            TextComponent forum2 = new TextComponent("§8[§6Forum§8]§e Fraktionen, Informationen, Spiele - Werde Teil unserer Foren-Community!");
                            forum2.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://forum.voidroleplay.de"));
                            forum2.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§6§l§oForum öffnen")));
                            for (Player player : Bukkit.getOnlinePlayers()) {
                                player.spigot().sendMessage(forum2);
                            }
                            break;
                        case 3:
                            Bukkit.broadcastMessage("§8[§9TeamSpeak§8]§3 Warst du bereits auf unserem TeamSpeak?");
                            Bukkit.broadcastMessage("§8[§9TeamSpeak§8]§3 Betritt noch heute unseren TeamSpeak unter §lvoidroleplay.de§3!");
                            break;
                    }
                    announceType++;
                    if (announceType == 4) {
                        announceType = 1;
                    }
                } else {
                    announceTick--;
                }
            }
        }.runTaskTimer(Main.getInstance(), 20 * 2, 20 * 60);
    }

    public RankData getRankData(String rank) {
        return rankDataMap.get(rank);
    }
}
