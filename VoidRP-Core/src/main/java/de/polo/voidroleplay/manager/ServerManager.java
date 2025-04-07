package de.polo.voidroleplay.manager;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.faction.entity.Faction;
import de.polo.voidroleplay.faction.entity.FactionPlayerData;
import de.polo.voidroleplay.faction.service.impl.FactionManager;
import de.polo.voidroleplay.game.base.shops.ShopData;
import de.polo.voidroleplay.game.base.shops.ShopItem;
import de.polo.voidroleplay.game.events.SecondTickEvent;
import de.polo.voidroleplay.game.faction.gangwar.Gangwar;
import de.polo.voidroleplay.location.services.impl.LocationManager;
import de.polo.voidroleplay.player.services.impl.PlayerManager;
import de.polo.voidroleplay.storage.*;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.utils.Utils;
import de.polo.voidroleplay.utils.enums.FFAStatsType;
import de.polo.voidroleplay.utils.enums.ShopType;
import de.polo.voidroleplay.utils.gameplay.MilitaryDrop;
import de.polo.voidroleplay.utils.player.ScoreboardAPI;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ServerManager {
    public static final boolean canDoJobsBoolean = true;
    public static final String error_cantDoJobs = Prefix.ERROR + "Der Job ist Serverseitig bis nach Restart gesperrt.";

    public static final Map<String, RankData> rankDataMap = new HashMap<>();
    public static final Map<String, DBPlayerData> dbPlayerDataMap = new HashMap<>();
    public static final Map<String, FactionPlayerData> factionPlayerDataMap = new HashMap<>();
    public static final Map<String, ContractData> contractDataMap = new HashMap<>();
    public static final Map<Integer, ShopData> shopDataMap = new HashMap<>();
    public static final Map<String, String> serverVariables = new HashMap<>();
    public static final List<UUID> factionStorageWeaponsTookout = new ObjectArrayList<>();
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
        // return payoutDataMap.get(type).getPayout();
        // ISSUE VPR-10003: Null check for payoutDataMap.get(type)
        PayoutData payoutData = payoutDataMap.get(type);
        return payoutData != null ? payoutData.getPayout() : 0;
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
        Statement statement = Main.getInstance().coreDatabase.getStatement();
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
        Statement statement = Main.getInstance().coreDatabase.getStatement();
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
        Statement statement = Main.getInstance().coreDatabase.getStatement();
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
        Statement statement = Main.getInstance().coreDatabase.getStatement();
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
            try {
                Statement nStatement = Main.getInstance().coreDatabase.getStatement();
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
            } catch (SQLException e1) {
                e1.printStackTrace();
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
                    PreparedStatement statement = Main.getInstance().coreDatabase.getConnection().prepareStatement("DELETE FROM seasonpass_player_quests");
                    statement.execute();
                    for (PlayerData playerData : playerManager.getPlayers()) {
                        playerData.clearQuests();
                        if (playerData.getPlayerFFAStatsManager().getStats(FFAStatsType.WEEKLY) == null) continue;
                        playerData.getPlayerFFAStatsManager().clearStats(FFAStatsType.WEEKLY);
                        Main.getInstance().gamePlay.getFfa().clearStats(FFAStatsType.WEEKLY);
                    }
                    Bukkit.broadcastMessage("§8[§6Seasonpass§8]§7 Der seasonpass wurde zurückgesetzt!");

                    PreparedStatement ffaStatement = Main.getInstance().coreDatabase.getConnection().prepareStatement("DELETE FROM player_ffa_stats WHERE statsType = ?");
                    ffaStatement.setString(1, FFAStatsType.WEEKLY.name());
                    ffaStatement.execute();
                    ffaStatement.close();

                    if (now.getDayOfMonth() == 1) {
                        for (PlayerData playerData : playerManager.getPlayers()) {
                            if (playerData.getPlayerFFAStatsManager().getStats(FFAStatsType.MONTHLY) == null) continue;
                            playerData.getPlayerFFAStatsManager().clearStats(FFAStatsType.MONTHLY);
                            Main.getInstance().gamePlay.getFfa().clearStats(FFAStatsType.MONTHLY);
                        }
                        PreparedStatement ffaMonStatement = Main.getInstance().coreDatabase.getConnection().prepareStatement("DELETE FROM player_ffa_stats WHERE statsType = ?");
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
                            Faction attackerData = factionManager.getFactionData(gangwarData.getAttacker());
                            Faction defenderData = factionManager.getFactionData(gangwarData.getGangZone().getOwner());
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
                        utils.sendActionBar(players, "§cDu bist noch " + Utils.getTime(playerData.getDeathTime()) + " bewusstlos.");
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

        // Farbverlauf-Header (VoidRoleplay in einem Gradient-Stil)
        String header = "\n" +
                "§7»" + gradient(" VoidRoleplay V2 × Reallife & Roleplay ", new String[]{"#FF4500", "#FF8C00", "#FFD700"}) + "§7«" +
                "\n\n§6Uhrzeit§8: §7" +
                (time.getHour() < 10 ? "0" + time.getHour() : time.getHour()) + ":" +
                (time.getMinute() < 10 ? "0" + time.getMinute() : time.getMinute()) + " Uhr\n" +
                "§6Ping§8: §7" + player.getPing() + "ms\n" +
                "§8__________________\n";

        // Footer mit Farbverlauf und klaren Informationen
        String footer = "§8__________________\n" +
                "\n§e" + Bukkit.getOnlinePlayers().size() + "§8/§6" + Bukkit.getMaxPlayers() + "\n" +
                "§9discord.gg/void-roleplay";

        player.setPlayerListHeader(header);
        player.setPlayerListFooter(footer);
    }

    /**
     * Erstellt einen Farbverlauf über einen Text hinweg.
     *
     * @param text Der Text, auf den der Verlauf angewendet werden soll.
     * @param colors Array der Hex-Farben als Strings (z.B. {"#FF4500", "#FFD700"}).
     * @return Der Text mit angewendetem Farbverlauf in Minecraft-Format.
     */
    private String gradient(String text, String[] colors) {
        StringBuilder gradientText = new StringBuilder();
        int length = text.length();
        for (int i = 0; i < length; i++) {
            // Berechne die Position im Farbverlauf
            float ratio = (float) i / (length - 1);
            String color = interpolateColor(colors, ratio);
            gradientText.append(color).append(text.charAt(i));
        }
        return gradientText.toString();
    }

    /**
     * Interpoliert eine Farbe basierend auf einem Verhältnis zwischen mehreren Farben.
     *
     * @param colors Array der Hex-Farben als Strings (z.B. {"#FF4500", "#FFD700"}).
     * @param ratio Verhältnis zwischen 0.0 (Anfang) und 1.0 (Ende).
     * @return Die interpolierte Minecraft-Farbsequenz (z.B. §x§R§R§G§G§B§B).
     */
    private String interpolateColor(String[] colors, float ratio) {
        int startIndex = (int) Math.floor(ratio * (colors.length - 1));
        int endIndex = Math.min(startIndex + 1, colors.length - 1);
        float localRatio = (ratio * (colors.length - 1)) - startIndex;

        // Hex-Farben zu RGB konvertieren
        int[] startColor = hexToRgb(colors[startIndex]);
        int[] endColor = hexToRgb(colors[endIndex]);

        int r = (int) (startColor[0] + localRatio * (endColor[0] - startColor[0]));
        int g = (int) (startColor[1] + localRatio * (endColor[1] - startColor[1]));
        int b = (int) (startColor[2] + localRatio * (endColor[2] - startColor[2]));

        // RGB zurück zu Hex und Minecraft-Farbsystem
        return String.format("§x§%1$X§%2$X§%3$X§%4$X§%5$X§%6$X", (r >> 4), (r & 0xF), (g >> 4), (g & 0xF), (b >> 4), (b & 0xF));
    }

    /**
     * Konvertiert eine Hex-Farbe (#RRGGBB) in ein RGB-Array.
     *
     * @param hex Hexadezimale Farbangabe (z.B. "#FF4500").
     * @return Ein Array mit den RGB-Werten {R, G, B}.
     */
    private int[] hexToRgb(String hex) {
        hex = hex.replace("#", "");
        int r = Integer.parseInt(hex.substring(0, 2), 16);
        int g = Integer.parseInt(hex.substring(2, 4), 16);
        int b = Integer.parseInt(hex.substring(4, 6), 16);
        return new int[]{r, g, b};
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
                            TextComponent forum = new TextComponent("§8[§9Discord§8]§3 Bist du schon auf unserem Discord?");
                            forum.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://discord.gg/void-roleplay"));
                            forum.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§9§l§oDiscord beitreten")));
                            Bukkit.spigot().broadcast(forum);
                            TextComponent forum2 = new TextComponent("§8[§9Discord§8]§3 Fraktionen, Ankündigungen, Changelogs uvm.!");
                            forum.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://discord.gg/void-roleplay"));
                            forum.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§9§l§oDiscord beitreten")));
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
