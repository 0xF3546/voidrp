package de.polo.voidroleplay.utils;

import de.polo.voidroleplay.dataStorage.*;
import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.utils.Game.GangwarUtils;
import de.polo.voidroleplay.utils.enums.ShopType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.*;

public class ServerManager {
    public static final boolean canDoJobsBoolean = true;
    public static final String error_cantDoJobs = Main.error + "Der Job ist Serverseitig bis nach Restart gesperrt.";

    public static final Map<String, RankData> rankDataMap = new HashMap<>();
    public static final Map<String, PayoutData> payoutDataMap = new HashMap<>();
    public static final Map<String, DBPlayerData> dbPlayerDataMap = new HashMap<>();
    public static final Map<String, FactionPlayerData> factionPlayerDataMap = new HashMap<>();
    public static final Map<String, ContractData> contractDataMap = new HashMap<>();
    public static final Map<Integer, ShopData> shopDataMap = new HashMap<>();
    public static final Map<String, String> serverVariables = new HashMap<>();

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
            if (locs.getString(19) != null && !locs.getString(19).equals("Zivilist")) {
                FactionPlayerData factionPlayerData = new FactionPlayerData();
                factionPlayerData.setId(locs.getInt(1));
                factionPlayerData.setUuid(locs.getString(2));
                factionPlayerData.setFaction(locs.getString(19));
                factionPlayerData.setFaction_grade(locs.getInt(20));
                factionPlayerDataMap.put(locs.getString(2), factionPlayerData);
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
            @Override
            public void run() {
                if (utils.getCurrentHour() >= 0 && utils.getCurrentHour() < 22) {
                    for (GangwarData gangwarData : GangwarUtils.gangwarDataMap.values()) {
                        if (gangwarData.getAttacker() != null) {
                            FactionData attackerData = factionManager.getFactionData(gangwarData.getAttacker());
                            FactionData defenderData = factionManager.getFactionData(gangwarData.getOwner());
                            for (Player players : Bukkit.getOnlinePlayers()) {
                                PlayerData playerData = playerManager.getPlayerData(players.getUniqueId());
                                if (playerData.getFaction().equals(gangwarData.getAttacker()) || playerData.getFaction().equals(gangwarData.getOwner())) {
                                    if (playerData.getVariable("gangwar") != null) {
                                        if (!playerData.isDead()) {
                                            utils.sendActionBar(players, "§5" + gangwarData.getZone() + "§8 | §5" + gangwarData.getMinutes() + "§8:§5" + gangwarData.getSeconds() + "§8 | §" + attackerData.getPrimaryColor() + gangwarData.getAttackerPoints() + "§8 - §" + defenderData.getPrimaryColor() + gangwarData.getDefenderPoints());
                                        } else {
                                            utils.sendActionBar(players, "§cDu bist noch " + Main.getTime(playerData.getDeathTime()) + " Tot. §8[§" + attackerData.getPrimaryColor() + gangwarData.getAttackerPoints() + "§8 - §" + defenderData.getPrimaryColor() + gangwarData.getDefenderPoints() + "§8]");
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                for (Player players : Bukkit.getOnlinePlayers()) {
                    PlayerData playerData = playerManager.getPlayerData(players.getUniqueId());
                    if (playerData.isDead()) {
                        playerData.setDeathTime(playerData.getDeathTime() - 1);
                        if (playerData.getVariable("gangwar") != null) {
                            GangwarData gangwarData = GangwarUtils.gangwarDataMap.get(playerData.getVariable("gangwar"));
                            FactionData attackerData = factionManager.getFactionData(gangwarData.getAttacker());
                            FactionData defenderData = factionManager.getFactionData(gangwarData.getOwner());
                            utils.sendActionBar(players, "§cDu bist noch " + Main.getTime(playerData.getDeathTime()) + " Tot. §8[§" + attackerData.getPrimaryColor() + gangwarData.getAttackerPoints() + "§8 - §" + defenderData.getPrimaryColor() + gangwarData.getDefenderPoints() + "§8]");
                        } else {
                            utils.sendActionBar(players, "§cDu bist noch " + Main.getTime(playerData.getDeathTime()) + " Tot.");
                        }
                        if (playerData.getDeathTime() <= 0) {
                            Main.getInstance().utils.deathUtil.despawnPlayer(players);
                        }
                    }
                }
            }
        }.runTaskTimer(Main.getInstance(), 20, 20);
    }

    public static boolean canDoJobs() {
        boolean canDoJobs = true;
        if (LocalDateTime.now().getHour() == 1 && LocalDateTime.now().getMinute() >= 55) {
            canDoJobs = false;
        }
        return canDoJobs;
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
        PlayerData playerData = Main.getInstance().playerManager.getPlayerData(player.getUniqueId());
        String loc = LocationManager.naviDataMap.get(locationManager.getNearestLocationId(player)).getName().substring(2);
        player.setPlayerListHeader("\n§6§lVoidRoleplay §8- §cV1.0\n\n§7" + LocalDateTime.now().getHour() + ":" + LocalDateTime.now().getMinute() + " Uhr\n§6Ping§8:§7 " + player.getPing() + "ms\n§8__________________\n");
        player.setPlayerListFooter("§8__________________\n\n§6Nächster Ort§8:§7 " + loc + "\n§8» §e" + Bukkit.getOnlinePlayers().size() + "§8/§6" + Bukkit.getMaxPlayers() + "§8 «\n");

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
                            text.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://voidroleplay.de/forum/index.php?thread%2F4-ingame-regelwerk%2F=&postID=4#post4"));
                            text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§6§l§oRegelwerk öffnen")));
                            for (Player player : Bukkit.getOnlinePlayers()) {
                                player.spigot().sendMessage(text);
                            }
                            break;
                        case 2:
                            TextComponent forum = new TextComponent("§8[§6Forum§8]§e Bist du schon Mitglied in unserem Forum?");
                            forum.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://voidroleplay.de/forum/"));
                            forum.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§6§l§oForum öffnen")));
                            Bukkit.spigot().broadcast(forum);
                            TextComponent forum2 = new TextComponent("§8[§6Forum§8]§e Fraktionen, Informationen, Spiele - Werde Teil unserer Foren-Community!");
                            forum2.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://voidroleplay.de/forum/"));
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
        }.runTaskTimer(Main.getInstance(), 20*2, 20*60);
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

    public RankData getRankData(String rank) {
        return rankDataMap.get(rank);
    }
}
