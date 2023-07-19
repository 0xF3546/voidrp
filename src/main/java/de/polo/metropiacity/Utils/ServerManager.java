package de.polo.metropiacity.Utils;

import de.polo.metropiacity.DataStorage.*;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.MySQl.MySQL;
import de.polo.metropiacity.PlayerUtils.DeathUtil;
import de.polo.metropiacity.PlayerUtils.Gangwar;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

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
    public static void loadRanks() throws SQLException {
        Statement statement = MySQL.getStatement();
        ResultSet locs = statement.executeQuery("SELECT * FROM ranks");
        while (locs.next()) {
            RankData rankData = new RankData();
            rankData.setId(locs.getInt(1));
            rankData.setRang(locs.getString(2));
            rankData.setPermlevel(locs.getInt(3));
            rankData.setTeamSpeakID(locs.getInt(4));
            rankData.setSecondary(locs.getBoolean(5));
            rankDataMap.put(locs.getString(2), rankData);
        }

        ResultSet res = statement.executeQuery("SELECT * FROM payouts");
        while (res.next()) {
            PayoutData payoutData = new PayoutData();
            payoutData.setId(res.getInt(1));
            payoutData.setType(res.getString(2));
            payoutData.setPayout(res.getInt(3));
            payoutDataMap.put(res.getString(2), payoutData);
        }
    }
    public static void loadDBPlayer() throws SQLException {
        Statement statement = MySQL.getStatement();
        ResultSet locs = statement.executeQuery("SELECT * FROM players");
        while (locs.next()) {
            DBPlayerData dbPlayerData = new DBPlayerData();
            dbPlayerData.setId(locs.getInt(1));
            dbPlayerData.setUuid(locs.getString(2));
            dbPlayerData.setPlayer_rank(locs.getString(6));
            dbPlayerData.setFaction(locs.getString(19));
            dbPlayerData.setFaction_grade(locs.getInt(20));
            dbPlayerData.setBusiness(locs.getString(41));
            dbPlayerData.setBusiness_grade(locs.getInt(42));
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
    }
    public static void loadContracts() throws SQLException {
        Statement statement = MySQL.getStatement();
        ResultSet locs = statement.executeQuery("SELECT * FROM contract");
        while (locs.next()) {
            ContractData contractData = new ContractData();
            contractData.setId(locs.getInt(1));
            contractData.setUuid(locs.getString(2));
            contractData.setAmount(locs.getInt(3));
            contractData.setSetter(locs.getString(4));
            contractDataMap.put(locs.getString(2), contractData);
        }
    }
    public static void loadShops() throws SQLException {
        Statement statement = MySQL.getStatement();
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
            shopData.setFaction(locs.getString(9));
            shopDataMap.put(locs.getInt(1), shopData);
        }
    }

    public static void everySecond() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (Utils.getCurrentHour() >= 0 && Utils.getCurrentHour() < 22) {
                    for (GangwarData gangwarData : Gangwar.gangwarDataMap.values()) {
                        if (gangwarData.getAttacker() != null) {
                            FactionData attackerData = FactionManager.factionDataMap.get(gangwarData.getAttacker());
                            FactionData defenderData = FactionManager.factionDataMap.get(gangwarData.getOwner());
                            for (Player players : Bukkit.getOnlinePlayers()) {
                                PlayerData playerData = PlayerManager.playerDataMap.get(players.getUniqueId().toString());
                                if (playerData.getFaction().equals(gangwarData.getAttacker()) || playerData.getFaction().equals(gangwarData.getOwner())) {
                                    if (playerData.getVariable("gangwar") != null) {
                                        if (!playerData.isDead()) {
                                            Utils.sendActionBar(players, "§5" + gangwarData.getZone() + "§8 | §5" + gangwarData.getMinutes() + "§8:§5" + gangwarData.getSeconds() + "§8 | §" + attackerData.getPrimaryColor() + gangwarData.getAttackerPoints() + "§8 - §" + defenderData.getPrimaryColor() + gangwarData.getDefenderPoints());
                                        } else {
                                            Utils.sendActionBar(players, "§cDu bist noch " + Main.getTime(playerData.getDeathTime()) + " Tot. §8[§" + attackerData.getPrimaryColor() + gangwarData.getAttackerPoints() + "§8 - §" + defenderData.getPrimaryColor() + gangwarData.getDefenderPoints() + "§8]");
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(Main.getInstance(), 20, 20);
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player players : Bukkit.getOnlinePlayers()) {
                    PlayerData playerData = PlayerManager.playerDataMap.get(players.getUniqueId().toString());
                    if (!playerData.isDead()) return;
                    playerData.setDeathTime(playerData.getDeathTime() - 1);
                    if (playerData.getVariable("gangwar") != null) {
                        GangwarData gangwarData = Gangwar.gangwarDataMap.get(playerData.getVariable("gangwar"));
                        FactionData attackerData = FactionManager.factionDataMap.get(gangwarData.getAttacker());
                        FactionData defenderData = FactionManager.factionDataMap.get(gangwarData.getOwner());
                        Utils.sendActionBar(players, "§cDu bist noch " + Main.getTime(playerData.getDeathTime()) + " Tot. §8[§" + attackerData.getPrimaryColor() + gangwarData.getAttackerPoints() + "§8 - §" + defenderData.getPrimaryColor() + gangwarData.getDefenderPoints() + "§8]");
                    } else {
                        Utils.sendActionBar(players, "§cDu bist noch " + Main.getTime(playerData.getDeathTime()) + " Tot.");
                    }
                    if (playerData.getDeathTime() <= 0) {
                        DeathUtil.despawnPlayer(players);
                    }
                }
            }
        }.runTaskTimer(Main.getInstance(), 20, 20);
    }

    public static boolean canDoJobs() {
        return canDoJobsBoolean;
    }

    public static void savePlayers() throws SQLException {
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerManager.savePlayer(player);
        }
    }

    public static void updateTablist(Player player) {
        if (player == null) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                PlayerData playerData = PlayerManager.playerDataMap.get(p.getUniqueId().toString());
                String loc = LocationManager.naviDataMap.get(LocationManager.getNearestLocationId(p)).getName().substring(2);
                p.setPlayerListHeader("§6§lMetropiaCity §8- §cV1.0\n\n§6Bargeld§8: §7" + playerData.getBargeld() + "$\n§6Ping§8:§7 " + p.getPing() + "ms\n");
                p.setPlayerListFooter("\n§6Nächser Ort§8:§7 " + loc + "\n§8» §e" + Bukkit.getOnlinePlayers().size() + "§8/§6" + Bukkit.getMaxPlayers() + "§8 «");
            }
        } else {
            PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
            String loc = LocationManager.naviDataMap.get(LocationManager.getNearestLocationId(player)).getName().substring(2);
            player.setPlayerListHeader("§6§lMetropiaCity §8- §cV1.0\n\n§6Bargeld§8: §7" + playerData.getBargeld() + "$\n§6Ping§8:§7 " + player.getPing() + "ms\n");
            player.setPlayerListFooter("\n§6Nächser Ort§8:§7 " + loc + "\n§8» §e" + Bukkit.getOnlinePlayers().size() + "§8/§6" + Bukkit.getMaxPlayers() + "§8 «");
        }
    }

    public static void startTabUpdateInterval() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    updateTablist(player);
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
}
