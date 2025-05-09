package de.polo.core.utils;

import de.polo.api.VoidAPI;
import de.polo.api.player.VoidPlayer;
import de.polo.core.Main;
import de.polo.core.agreement.services.VertragUtil;
import de.polo.core.faction.service.impl.FactionManager;
import de.polo.core.game.base.housing.HouseManager;
import de.polo.core.game.faction.gangwar.GangwarUtils;
import de.polo.core.manager.*;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.storage.RankData;
import de.polo.core.utils.player.BankingUtils;
import de.polo.core.utils.player.DeathUtils;
import de.polo.core.utils.player.PayDayUtils;
import de.polo.core.utils.player.Tutorial;
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.sql.Date;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static de.polo.core.Main.gamePlay;

public class Utils {
    static Scoreboard sb;
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

    public Utils(PlayerManager playerManager, FactionManager factionManager, HouseManager houseManager, CompanyManager companyManager) {
        deathUtil = new DeathUtils(playerManager);
        vertragUtil = new VertragUtil(playerManager, factionManager);
        staatUtil = new StaatUtil(playerManager, factionManager, this);
        tutorial = new Tutorial(playerManager);
        this.houseManager = houseManager;
        payDayUtils = new PayDayUtils(playerManager, factionManager);
        bankingUtils = new BankingUtils(playerManager, factionManager);
        this.companyManager = companyManager;
        tabletUtils = new TabletUtils(playerManager, factionManager, this, companyManager);
        phoneUtils = new PhoneUtils(playerManager, this);
        gangwarUtils = new GangwarUtils(playerManager, factionManager);
    }

    public static String stringArrayToString(String[] args) {
        return String.join(" ", args);
    }

    public static void sendPlayerAchievementMessage(Player player, String message) {

    }

    public static String toDecimalFormat(int number) {
        return new DecimalFormat("#,###").format(number);
    }

    public static LocalDateTime toLocalDateTime(Date date) {
        long newDate = date.getTime();
        java.util.Date utilDate = new java.util.Date(newDate);

        return utilDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public static int roundUpToMultipleOfNine(int num) {
        return ((num + 8) / 9) * 9;
    }

    public static boolean isRandom(int chance) {
        int randomNumber = ThreadLocalRandom.current().nextInt(100 + 1);

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
            if (player.getName() == null) continue;
            if (player.getName().equalsIgnoreCase(name)) return player;
        }
        return null;
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

    public static String getTime(int seconds) {
        int minutes = seconds / 60;
        int sec = seconds % 60;
        return minutes + " Minuten & " + sec + " Sekunden";
    }

    public static int random(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    public static char getRandomChar(String characters) {
        Random random = new Random();
        int index = random.nextInt(characters.length());
        return characters.charAt(index);
    }

    public static void waitSeconds(int seconds, Runnable runnable) {
        new BukkitRunnable() {
            @Override
            public void run() {
                runnable.run();
            }
        }.runTaskLater(Main.getInstance(), seconds * 20L);
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

    public void loadTeams() {
        sb = Bukkit.getScoreboardManager().getMainScoreboard();
        for (RankData rankData : ServerManager.rankDataMap.values()) {
            if (sb.getTeam(-rankData.getPermlevel() + "_" + rankData.getShortName()) == null) {
                sb.registerNewTeam(-rankData.getPermlevel() + "_" + rankData.getShortName());
                sb.getTeam(-rankData.getPermlevel() + "_" + rankData.getShortName()).setPrefix(rankData.getColor() + rankData.getShortName() + "§8 × §7");
            }
        }
    }

    public void setAFK(Player player, boolean state) {
        PlayerData playerData = Main.getInstance().playerManager.getPlayerData(player.getUniqueId());
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Team afkTeam = scoreboard.getTeam("afk");

        if (afkTeam == null) {
            afkTeam = scoreboard.registerNewTeam("afk");
            afkTeam.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        }

        if (state) {
            player.sendMessage("§5Du bist nun abwesend.");
            playerData.setAFK(true);

            afkTeam.addPlayer(player);

        } else {
            player.sendMessage("§5Du bist nun wieder anwesend.");
            playerData.setAFK(false);
            playerData.setIntVariable("afk", 0);

            afkTeam.removePlayer(player);
        }

        Tablist.updatePlayer(player);
    }


    public LocalDateTime sqlDateToLocalDateTime(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public void summonCircle(Location location, int size, Particle particle) {
        for (int d = 0; d <= 90; d += 1) {
            Location particleLoc = new Location(location.getWorld(), location.getX(), location.getY(), location.getZ());
            particleLoc.setX(location.getX() + Math.cos(d) * size);
            particleLoc.setZ(location.getZ() + Math.sin(d) * size);
            location.getWorld().spawnParticle(particle, particleLoc, 1, new Particle.DustOptions(Color.WHITE, 5));
        }
    }

    public interface Tablist {

        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

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
            if (playerData.isDuty() && playerData.getFaction() != null) {
                switch (playerData.getFaction().toLowerCase()) {
                    case "polizei" -> name = "§9" + player.getName();
                    case "fbi" -> name = "§1" + player.getName();
                    case "medic" -> name = "§c" + player.getName();
                    default -> {
                    }
                }
            }
            player.setDisplayName(Color.GRAY + player.getName());
            player.setPlayerListName(prefix + "§7" + name + " " + suffix);
            player.setCustomName(Color.GRAY + player.getName());
            player.setCustomNameVisible(true);
        }

        static void updatePlayer(Player player) {
            PlayerData playerData = Main.getInstance().playerManager.getPlayerData(player.getUniqueId());
            String suffix = "";
            String prefix = "";
            VoidPlayer voidPlayer = VoidAPI.getPlayer(player);
            if (playerData.isAFK()) {
                prefix = "§8[§5AFK§8]";
            } else if (player.getGameMode().equals(GameMode.CREATIVE)) {
                prefix = "§8[§2GM§8]";
            } else if (player.getAllowFlight() && !player.getGameMode().equals(GameMode.SPECTATOR) && !voidPlayer.isAduty()) {
                prefix = "§8[§5Fly§8]";
            }

            if (playerData.getPermlevel() >= 40) {
                suffix = "§c◉";
            } else if (playerData.getPermlevel() >= 10) {
                suffix = "§d◈";
            }
            String color = "§7";
            if (playerData.isDuty() && playerData.getFaction() != null) {
                color = switch (playerData.getFaction().toLowerCase()) {
                    case "polizei" -> "§9";
                    case "fbi" -> "§1";
                    case "medic" -> "§c";
                    case "news" -> "§6";
                    default -> color;
                };
            }
            if (gamePlay.getMaskState(player) == null) {
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

    public static Block getPlayerFacingBlock(Player player, int range) {
        return player.getTargetBlock(null, range);
    }
}
