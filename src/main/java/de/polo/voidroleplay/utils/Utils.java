package de.polo.voidroleplay.utils;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.dataStorage.RankData;
import de.polo.voidroleplay.game.faction.gangwar.GangwarUtils;
import de.polo.voidroleplay.game.base.housing.HouseManager;
import de.polo.voidroleplay.manager.*;
import de.polo.voidroleplay.utils.playerUtils.*;
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerSet;

import java.sql.*;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Random;

public class Utils {
    @Getter
    public DeathUtils deathUtil;
    @Getter
    public StaatUtil staatUtil;
    @Getter
    public VertragUtil vertragUtil;
    @Getter
    public HouseManager houseManager;
    //public Shop shop;
    @Getter
    public Tutorial tutorial;
    @Getter
    public final NavigationManager navigationManager;
    @Getter
    public final PayDayUtils payDayUtils;
    @Getter
    public final BankingUtils bankingUtils;
    @Getter
    public final PhoneUtils phoneUtils;
    @Getter
    public final TabletUtils tabletUtils;
    @Getter
    public final GangwarUtils gangwarUtils;
    @Getter
    private final CompanyManager companyManager;
    private static HashMap<String, AreaMarker> areaMarkers = new HashMap<>();
    private static HashMap<String, Marker> markers = new HashMap<>();

    public Utils(PlayerManager playerManager, AdminManager adminManager, FactionManager factionManager, LocationManager locationManager, HouseManager houseManager, NavigationManager navigationManager, CompanyManager companyManager) {
        deathUtil = new DeathUtils(playerManager, adminManager, locationManager);
        vertragUtil = new VertragUtil(playerManager, factionManager, adminManager);
        staatUtil = new StaatUtil(playerManager, factionManager, locationManager, this);
        this.navigationManager = navigationManager;
        tutorial = new Tutorial(playerManager, navigationManager);
        this.houseManager = houseManager;
        payDayUtils = new PayDayUtils(playerManager, factionManager);
        bankingUtils = new BankingUtils(playerManager, factionManager);
        this.companyManager = companyManager;
        tabletUtils = new TabletUtils(playerManager, factionManager, this, companyManager);
        phoneUtils = new PhoneUtils(playerManager, this);
        gangwarUtils = new GangwarUtils(playerManager, factionManager, locationManager);
    }

    public void sendActionBar(Player player, String message) {
        player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, net.md_5.bungee.api.chat.TextComponent.fromLegacyText(message));
    }

    public void sendBossBar(Player player, String text) {
    }

    public int getCurrentMinute() {
        return Calendar.getInstance().get(Calendar.MINUTE);
    }

    public int getCurrentHour() {
        return Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
    }

    public static String stringArrayToString(String[] args) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            stringBuilder.append(args[i]);
            if (i != args.length - 1) {
                stringBuilder.append(" ");
            }
        }
        return stringBuilder.toString();
    }

    public static void sendPlayerAchievementMessage(Player player, String message) {

    }

    public static String toDecimalFormat(int number) {
        return new DecimalFormat("#,###").format(number);
    }

    public static LocalDateTime toLocalDateTime(Date date) {
        long newDate = date.getTime();
        java.util.Date utilDate = new java.util.Date(newDate);

        LocalDateTime localDateTime = utilDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        return localDateTime;
    }

    static Scoreboard sb;

    public void loadTeams() {
        sb = Bukkit.getScoreboardManager().getMainScoreboard();
        for (RankData rankData : ServerManager.rankDataMap.values()) {
            if (sb.getTeam(-rankData.getPermlevel() + "_" + rankData.getShortName()) == null) {
                sb.registerNewTeam(-rankData.getPermlevel() + "_" + rankData.getShortName());
                sb.getTeam(-rankData.getPermlevel() + "_" + rankData.getShortName()).setPrefix(rankData.getColor() + rankData.getShortName() + "§8 × §7");
            }
        }
    }

    public interface Tablist {

        static void setTablist(Player player, String prefix) {
            PlayerData playerData = Main.getInstance().playerManager.getPlayerData(player.getUniqueId());
            RankData rankData = Main.getInstance().serverManager.getRankData(playerData.getRang());
            player.setDisplayName(Color.GRAY + player.getName());
            String suffix = "";
            if (rankData.getPermlevel() >= 40) {
                suffix = "§c◉";
            } else if (rankData.getPermlevel() >= 10) {
                suffix = "§d◈";
            }
            if (prefix != null) {
                prefix = " " + prefix;
            } else {
                prefix = "";
            }
            String name = "§7" + player.getName();
            if (playerData.isDuty()) {
                switch (playerData.getFaction().toLowerCase()) {
                    case "polizei":
                        name = "§9" + player.getName();
                        break;
                    case "fbi":
                        name = "§1" + player.getName();
                        break;
                    case "medic":
                        name = "§c" + player.getName();
                        break;
                    default:
                        break;
                }
            }
            player.setDisplayName(Color.GRAY + player.getName());
            player.setPlayerListName(prefix + "§7" + name + " " + suffix);
            player.setCustomName(Color.GRAY + player.getName());
            player.setCustomNameVisible(true);
        }

        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

        static void updatePlayer(Player player) {
            PlayerData playerData = Main.getInstance().playerManager.getPlayerData(player.getUniqueId());
            String suffix = "";
            String prefix = "";
            if (playerData.isAFK()) {
                prefix = "§8[§5AFK§8]";
            } else if (player.getGameMode().equals(GameMode.CREATIVE)) {
                prefix = "§8[§2GM§8]";
            } else if (player.getAllowFlight() && !player.getGameMode().equals(GameMode.SPECTATOR) && !playerData.isAduty()) {
                prefix = "§8[§5Fly§8]";
            }

            if (playerData.getPermlevel() >= 40) {
                suffix = "§c◉";
            } else if (playerData.getPermlevel() >= 10) {
                suffix = "§d◈";
            }
            String color = "§7";
            if (playerData.isDuty()) {
                switch (playerData.getFaction().toLowerCase()) {
                    case "polizei":
                        color = "§9";
                        break;
                    case "fbi":
                        color = "§1";
                        break;
                    case "medic":
                        color = "§c";
                        break;
                    case "news":
                        color = "§6";
                        break;
                }
            }
            if (Main.getInstance().gamePlay.getMaskState(player) == null) {
                player.setDisplayName(prefix + color + player.getName());
                player.setPlayerListName(prefix + color + player.getName() + " " + suffix);
                player.setCustomName(prefix + color + player.getName());
                player.setCustomNameVisible(true);
            } else {
                player.setCustomNameVisible(true);
                player.setCustomName("§k" + player.getName());
                player.setDisplayName("§k" + player.getName());
            }
        }
    }

    public static int roundUpToMultipleOfNine(int num) {
        return ((num + 8) / 9) * 9;
    }

    public void setAFK(Player player, boolean state) {
        PlayerData playerData = Main.getInstance().playerManager.getPlayerData(player.getUniqueId());
        if (state) {
            player.sendMessage("§5Du bist nun abwesend.");
            playerData.setAFK(true);
            player.setCollidable(false);
        } else {
            player.sendMessage("§5Du bist nicht mehr abwesend.");
            playerData.setAFK(false);
            playerData.setIntVariable("afk", 0);
            if (!playerData.isAduty()) {
                player.setCollidable(true);
            }
        }
        Tablist.updatePlayer(player);
    }

    public LocalDateTime sqlDateToLocalDateTime(Date date) {
        LocalDateTime localDateTime = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        return localDateTime;
    }

    public void summonCircle(Location location, int size, Particle particle) {
        for (int d = 0; d <= 90; d += 1) {
            Location particleLoc = new Location(location.getWorld(), location.getX(), location.getY(), location.getZ());
            particleLoc.setX(location.getX() + Math.cos(d) * size);
            particleLoc.setZ(location.getZ() + Math.sin(d) * size);
            location.getWorld().spawnParticle(particle, particleLoc, 1, new Particle.DustOptions(Color.WHITE, 5));
        }
    }

    public static boolean isRandom(int chance) {
        Random random = new Random();
        int randomNumber = random.nextInt(100);

        return randomNumber < chance;
    }

    public static String localDateTimeToString(LocalDateTime localDateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return localDateTime.format(formatter);
    }

    public static String localDateTimeToReadableString(LocalDateTime localDateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM | HH:mm");
        return localDateTime.format(formatter);
    }

    public static LocalDateTime getTime() {
        return LocalDateTime.now(ZoneId.of("Europe/Berlin"));
    }

    public static OfflinePlayer getOfflinePlayer(String name) {
        for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
            if (player == null) continue;
            if (player.getName() == null) continue;
            if (player.getName().equalsIgnoreCase(name)) return player;
        }
        return null;
    }

    public static void createMarker(String markerId, String markerLabel, String world, double x, double y, double z) {
        if (markers.get(markerLabel) != null) {
            System.out.println("REMOVING MARKER: " + markerLabel);
            removeMarker(markerLabel);
        };
        MarkerAPI markerAPI = Main.getInstance().getMarkerAPI();
        MarkerSet markerSet = markerAPI.getMarkerSet(markerLabel);
        if (markerSet == null) {
            markerSet = markerAPI.createMarkerSet(markerLabel, markerLabel, null, false);
        }


        Marker marker = markerSet.createMarker(markerId, markerLabel, world, x, y, z, markerAPI.getMarkerIcon("sign"), false);
        if (marker == null) {
            System.out.println("Error creating marker!");
            return;
        }

        // Optional: Weitere Einstellungen vornehmen
        // marker.setDescription("This is an example marker.");

        markers.put(markerLabel, marker);
    }

    public static void removeMarker(String markerLabel) {
        Marker marker = markers.get(markerLabel);
        if (marker == null) return;
        marker.deleteMarker();
        markers.remove(markerLabel);
    }

    public static void createWebAreaMarker(String markerId, String markerLabel, String world, double[] x, double[] z) {
        if (areaMarkers.get(markerLabel) != null) removeAreaMarker(markerLabel);
        MarkerAPI markerAPI = Main.getInstance().getMarkerAPI();
        MarkerSet markerSet = markerAPI.getMarkerSet("example.markerset");
        if (markerSet == null) {
            markerSet = markerAPI.createMarkerSet("example.markerset", "Example Marker Set", null, false);
        }

        AreaMarker areaMarker = markerSet.createAreaMarker(markerId, markerLabel, false, world, x, z, false);
        if (areaMarker == null) {
            System.out.println("Error creating area marker!");
            return;
        }

        // areaMarker.setDescription("This is an example area marker.");
        areaMarker.setLineStyle(3, 0.8, 0xFF0000);
        // areaMarker.setFillStyle(0.35, 0x00FF00);

        areaMarkers.put(markerLabel, areaMarker);
    }

    public static void removeAreaMarker(String markerLabel) {
        AreaMarker marker = areaMarkers.get(markerLabel);
        if (marker == null) return;
        marker.deleteMarker();
        areaMarkers.remove(markerLabel);
    }

    public static Location getLocation(int x, int y, int z) {
        return getLocation(x, y, z, Bukkit.getWorld("world"));
    }

    public static Location getLocation(int x, int y, int z, World world) {
        return getLocation(x, y, z, world, 0, 0);
    }

    public static Location getLocation(int x, int y, int z, World world, float yaw, float pitch) {
        return new Location(world, x, y, z, yaw, pitch);
    }
}
