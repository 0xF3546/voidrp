package de.polo.metropiacity.utils;

import de.polo.metropiacity.Main;
import de.polo.metropiacity.dataStorage.PlayerData;
import de.polo.metropiacity.dataStorage.RankData;
import de.polo.metropiacity.playerUtils.*;
import de.polo.metropiacity.utils.Game.GangwarUtils;
import de.polo.metropiacity.utils.Game.Housing;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.scoreboard.*;
import org.bukkit.scoreboard.Scoreboard;

import java.sql.*;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Calendar;

public class Utils {
    static int minutes = 1;
    public DeathUtils deathUtil;
    public StaatUtil staatUtil;
    public VertragUtil vertragUtil;
    public Housing housing;
    //public Shop shop;
    public Tutorial tutorial;
    public final Navigation navigation;
    public final PayDayUtils payDayUtils;
    public final BankingUtils bankingUtils;
    public final PhoneUtils phoneUtils;
    public final TabletUtils tabletUtils;
    public final FFAUtils ffaUtils;
    public final GangwarUtils gangwarUtils;

    public Utils(PlayerManager playerManager, AdminManager adminManager, FactionManager factionManager, LocationManager locationManager, Housing housing, Navigation navigation) {
        deathUtil = new DeathUtils(playerManager, adminManager, locationManager);
        vertragUtil = new VertragUtil(playerManager, factionManager, adminManager);
        staatUtil = new StaatUtil(playerManager, factionManager, locationManager, this);
        this.navigation = navigation;
        tutorial = new Tutorial(playerManager, navigation);
        this.housing = housing;
        payDayUtils = new PayDayUtils(playerManager, factionManager);
        bankingUtils = new BankingUtils(playerManager, factionManager);
        tabletUtils = new TabletUtils(playerManager, factionManager);
        phoneUtils = new PhoneUtils(playerManager, this);
        ffaUtils = new FFAUtils(playerManager, locationManager);
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

    public OfflinePlayer getOfflinePlayer(String player) {
        for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
            if (offlinePlayer.getName().equalsIgnoreCase(player)) {
                return offlinePlayer;
            }
        }
        return null;
    }

    public String stringArrayToString(String[] args) {
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

        static void setTablist(Player player, String suffix) {
            PlayerData playerData = Main.getInstance().playerManager.getPlayerData(player.getUniqueId());
            RankData rankData = Main.getInstance().serverManager.getRankData(playerData.getRang());
            String team = -playerData.getPermlevel() + "_" + rankData.getShortName();
            sb.getTeam(team).addPlayer(player);
            for (Player all : Bukkit.getOnlinePlayers()) {
                all.setScoreboard(sb);
            }
            if (suffix == null) {
                player.setDisplayName(Color.GRAY + player.getName());
                player.setPlayerListName(rankData.getColor() + rankData.getRang() + "§8 × §7" + player.getName());
                player.setCustomName(Color.GRAY + player.getName());
                player.setCustomNameVisible(true);
                return;
            }
            suffix = " " + suffix;
            player.setDisplayName(Color.GRAY + player.getName());
            player.setPlayerListName(rankData.getColor() + rankData.getRang() + "§8 × §7" + player.getName() + suffix);
            player.setCustomName(Color.GRAY + player.getName());
            player.setCustomNameVisible(true);
        }

        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Team team = scoreboard.registerNewTeam("a");
        Team team_offduty = scoreboard.registerNewTeam("b");
        Team FBI = scoreboard.registerNewTeam("c");
        Team Polizei = scoreboard.registerNewTeam("d");
        Team Medics = scoreboard.registerNewTeam("e");

        static void updatePlayer(Player player) {
            team.removeEntry(player.getName());
            team_offduty.removeEntry(player.getName());
            FBI.removeEntry(player.getName());
            Polizei.removeEntry(player.getName());
            Medics.removeEntry(player.getName());
            PlayerData playerData = Main.getInstance().playerManager.getPlayerData(player.getUniqueId());
            if (playerData.isAduty()) {
                team.addEntry(player.getName());
                return;
            }
            if (playerData.isDuty()) {
                switch (playerData.getFaction().toLowerCase()) {
                    case "polizei":
                        Polizei.addEntry(player.getName());
                        break;
                    case "fbi":
                        FBI.addEntry(player.getName());
                    case "medic":
                        Medics.addEntry(player.getName());
                        break;
                    default:
                        team_offduty.addEntry(player.getName());
                        break;
                }
            }
        }
    }

    public interface Display {
        static void setNameBelow(Player player, String name) {
            PlayerData playerData = Main.getInstance().playerManager.getPlayerData(player.getUniqueId());
            Scoreboard scoreboard = playerData.getScoreboard().scoreboard;
            if (scoreboard.getObjective(name) != null) scoreboard.getObjective(name).unregister();
            Objective objective = scoreboard.registerNewObjective(name, "name");
            objective.setDisplaySlot(DisplaySlot.BELOW_NAME);
            player.setScoreboard(scoreboard);
        }

        static void deleteNameBelow(Player player, String name) {
            PlayerData playerData = Main.getInstance().playerManager.getPlayerData(player.getUniqueId());
            Scoreboard scoreboard = playerData.getScoreboard().scoreboard;
            Objective objective = scoreboard.getObjective(name);
            if (objective != null) objective.unregister();
        }

        static void adminMode(Player player, boolean state) {
            PlayerData playerData = Main.getInstance().playerManager.getPlayerData(player.getUniqueId());
            Scoreboard scoreboard = playerData.getScoreboard().scoreboard;
            if (!state) {
                Objective objective = scoreboard.getObjective("showhealth");
                if (objective != null) objective.unregister();
            } else {
                if (scoreboard.getObjective("admin") != null) scoreboard.getObjective("admin").unregister();
                Objective objective = playerData.getScoreboard().scoreboard.registerNewObjective("showhealth", "health");
                objective.setDisplaySlot(DisplaySlot.BELOW_NAME);
                objective.setDisplayName("/ 20");

                for (Player online : Bukkit.getOnlinePlayers()) {

                    online.setScoreboard(playerData.getScoreboard().scoreboard);
                    online.setHealth(online.getHealth());
                }
            }
        }
    }

    /*public interface Skin {
        static boolean saveOutfit(Player player, String name) {
            PlayerProfile profile = player.getPlayerProfile();
            try {
                MySQL mySQL = Main.getInstance().mySQL;
                Statement statement = Main.getInstance().mySQL.getStatement();
                ResultSet res = statement.executeQuery("SELECT * FROM player_wardrobe WHERE uuid = '" + player.getUniqueId() + "' AND name = '" + name + "'");
                if (res.next()) {
                    return false;
                }
                String query = "INSERT INTO player_wardrobe (uuid, texture, name) VALUES (?, ?, ?)";
                try (PreparedStatement preparedStatement = mySQL.getConnection().prepareStatement(query)) {
                    preparedStatement.setString(1, player.getUniqueId().toString());
                    preparedStatement.setString(2, profile.getTextures().toString());
                    preparedStatement.setString(3, name);
                    return true;
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        static boolean loadOutfit(Player player, String name) {
            try {
                Statement statement = Main.getInstance().mySQL.getStatement();
                ResultSet res = statement.executeQuery("SELECT * FROM player_wardrobe WHERE uuid = '" + player.getUniqueId() + "' and name = '" + name + "'");
                if (!res.next()) {
                    return false;
                }
                PlayerProfile profile = player.getPlayerProfile();
                profile.setTextures(profile.getTextures());
                return true;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }*/

    public void setAFK(Player player, boolean state) {
        PlayerData playerData = Main.getInstance().playerManager.getPlayerData(player.getUniqueId());
        if (state) {
            player.sendMessage("§5Du bist nun abwesend.");
            playerData.setAFK(true);
            player.setCollidable(false);
            if (playerData.isAduty()) return;
            Tablist.setTablist(player, "§8[§5AFK§8]");
        } else {
            player.sendMessage("§5Du bist nicht mehr abwesend.");
            playerData.setAFK(false);
            playerData.setIntVariable("afk", 0);
            if (!playerData.isAduty()) {
                player.setCollidable(true);
                Tablist.setTablist(player, null);
            }
        }
    }

    public final Shop shop = new Shop();

    public class Shop {
        public void openShop(Player player) {
            PlayerData playerData = Main.getInstance().playerManager.getPlayerData(player.getUniqueId());
            playerData.setVariable("current_inventory", "coinshop");
            Inventory inv = Bukkit.createInventory(player, 27, "§8 » §eCoin-Shop");
            inv.setItem(4, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWZhNzU5OTVjZTUzYmQzNjllZDczNjE1YmYzMjNlMTRhOWNkNzc4OGNhNWFjYjY1YjBiMWFmNTY0NWRkZDA5MSJ9fX0=", 1, 0, "§6Guthaben", Arrays.asList("§8 ➥ §e" + toDecimalFormat(playerData.getCoins()) + " Coins")));
            inv.setItem(11, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWUzZDM2YmE4YTI5NjYzZGZkYmVmMTFmOWIyZDExY2FlMzg4Yzc1Nzg0Y2FiYzcwNmRjNjY4OWE4Y2IwYjM1MSJ9fX0=", 1, 0, "§eRänge", null));
            inv.setItem(13, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTQ4MGQ1N2IwZDFkNDMyZTA3NDg3OGM2YWVjNWY0NWEyY2U5OGQ5YzQ4MWZiOGNjODM4MmNmZjE3MWY4MzY5OSJ9fX0=", 1, 0, "§5Cosmetics", null));
            inv.setItem(15, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjE2ZjI3MTQ0ZDhjMmU2NDlhNzZmYjU5NzU3Yzk0ZTQyNTFmMTQ5ZGNhYWFhNzIwZjZmZDZhYTgxY2RlY2MxYSJ9fX0=", 1, 0, "§2Extras", null));
            inv.setItem(18, ItemManager.createItem(Material.NETHER_WART, 1, 0, "§cZurück", null));
            for (int i = 0; i < 27; i++) {
                if (inv.getItem(i) == null) {
                    inv.setItem(i, ItemManager.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, 0, "§c", null));
                }
            }
            player.openInventory(inv);
        }

        public void openRankShop(Player player) {
            PlayerData playerData = Main.getInstance().playerManager.getPlayerData(player.getUniqueId());
            playerData.setVariable("current_inventory", "coinshop_ranks");
            Inventory inv = Bukkit.createInventory(player, 27, "§8 » §eRänge");
            inv.setItem(4, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWZhNzU5OTVjZTUzYmQzNjllZDczNjE1YmYzMjNlMTRhOWNkNzc4OGNhNWFjYjY1YjBiMWFmNTY0NWRkZDA5MSJ9fX0=", 1, 0, "§6Guthaben", Arrays.asList("§8 ➥ §e" + toDecimalFormat(playerData.getCoins()) + " Coins")));
            inv.setItem(11, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWUzZDM2YmE4YTI5NjYzZGZkYmVmMTFmOWIyZDExY2FlMzg4Yzc1Nzg0Y2FiYzcwNmRjNjY4OWE4Y2IwYjM1MSJ9fX0=", 1, 0, "§6VIP", Arrays.asList("§8 » §e30 Tage", "§8 » §e20.000 Coins")));
            inv.setItem(13, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWUzZDM2YmE4YTI5NjYzZGZkYmVmMTFmOWIyZDExY2FlMzg4Yzc1Nzg0Y2FiYzcwNmRjNjY4OWE4Y2IwYjM1MSJ9fX0=", 1, 0, "§bPremium", Arrays.asList("§8 » §e30 Tage", "§8 » §e10.000 Coins")));
            inv.setItem(15, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWUzZDM2YmE4YTI5NjYzZGZkYmVmMTFmOWIyZDExY2FlMzg4Yzc1Nzg0Y2FiYzcwNmRjNjY4OWE4Y2IwYjM1MSJ9fX0=", 1, 0, "§eGold", Arrays.asList("§8 » §e30 Tage", "§8 » §e5.000 Coins")));
            inv.setItem(18, ItemManager.createItem(Material.NETHER_WART, 1, 0, "§cZurück", null));
            for (int i = 0; i < 27; i++) {
                if (inv.getItem(i) == null) {
                    inv.setItem(i, ItemManager.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, 0, "§c", null));
                }
            }
            player.openInventory(inv);
        }

        public void openCosmeticShop(Player player) {

        }

        public void openExtraShop(Player player) {
            PlayerData playerData = Main.getInstance().playerManager.getPlayerData(player.getUniqueId());
            playerData.setVariable("current_inventory", "coinshop_extras");
            Inventory inv = Bukkit.createInventory(player, 27, "§8 » §2Extras");
            inv.setItem(4, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWZhNzU5OTVjZTUzYmQzNjllZDczNjE1YmYzMjNlMTRhOWNkNzc4OGNhNWFjYjY1YjBiMWFmNTY0NWRkZDA5MSJ9fX0=", 1, 0, "§6Guthaben", Arrays.asList("§8 ➥ §e" + toDecimalFormat(playerData.getCoins()) + " Coins")));
            inv.setItem(11, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmY4MTIxMTJkZDE4N2U3YzhkZGI1YzNiOGU4NTRlODJmMTkxOTc0MTRhOGNkYjU0MjAyMWYxYTQ5MTg5N2U1MyJ9fX0=", 1, 0, "§bHausslot", Arrays.asList("§8 » §e4.000 Coins")));
            inv.setItem(13, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzkyNzRhMmFjNTQxZTQwNGMwYWE4ODg3OWIwYzhiMTBmNTAyYmMyZDdlOWE2MWIzYjRiZjMzNjBiYzE1OTdhMiJ9fX0=", 1, 0, "§3EXP-Boost", Arrays.asList("§8 » §e3 Stunden", "§8 » §e2.000 Coins")));
            inv.setItem(18, ItemManager.createItem(Material.NETHER_WART, 1, 0, "§cZurück", null));
            for (int i = 0; i < 27; i++) {
                if (inv.getItem(i) == null) {
                    inv.setItem(i, ItemManager.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, 0, "§c", null));
                }
            }
            player.openInventory(inv);
        }


        public void buy(Player player, String type) {
            PlayerData playerData = Main.getInstance().playerManager.getPlayerData(player.getUniqueId());
            switch (type) {
                case "vip_30":
                    if (playerData.getCoins() < 20000) {
                        player.sendMessage(Main.error + "Du hast nicht genug Coins (20.000).");
                        player.closeInventory();
                        return;
                    }
                    try {
                        Main.getInstance().playerManager.removeCoins(player, 20000);
                        Main.getInstance().playerManager.redeemRank(player, "vip", 30, "days");
                        player.closeInventory();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                case "premium_30":
                    if (playerData.getCoins() < 10000) {
                        player.sendMessage(Main.error + "Du hast nicht genug Coins (10.000).");
                        player.closeInventory();
                        return;
                    }
                    try {
                        Main.getInstance().playerManager.removeCoins(player, 10000);
                        Main.getInstance().playerManager.redeemRank(player, "premium", 30, "days");
                        player.closeInventory();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                case "gold_30":
                    if (playerData.getCoins() < 5000) {
                        player.sendMessage(Main.error + "Du hast nicht genug Coins (5.000).");
                        player.closeInventory();
                        return;
                    }
                    try {
                        Main.getInstance().playerManager.removeCoins(player, 5000);
                        Main.getInstance().playerManager.redeemRank(player, "gold", 30, "days");
                        player.closeInventory();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                case "hausslot":
                    if (playerData.getCoins() < 4000) {
                        player.sendMessage(Main.error + "Du hast nicht genug Coins (4.000).");
                        player.closeInventory();
                        return;
                    }
                    try {
                        Main.getInstance().playerManager.removeCoins(player, 4000);
                        housing.addHausSlot(player);
                        player.closeInventory();
                        player.sendMessage("§8[§eCoin-Shop§8]§a Du hast einen Hausslot eingelöst!");
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    break;
            }
        }
    }

    public interface GUI {
        interface Tasche {
            static void openMainInventory(Player player) {
                PlayerData playerData = Main.getInstance().playerManager.getPlayerData(player.getUniqueId());
                playerData.setVariable("current_inventory", "tasche");
                Inventory inv = Bukkit.createInventory(player, 27, "§8 » §6Deine Tasche");
                inv.setItem(11, ItemManager.createItem(Material.BOOK, 1, 0, "§ePortmonee", "§8 ➥ §7" + playerData.getBargeld() + "$"));
                inv.setItem(22, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWZhNzU5OTVjZTUzYmQzNjllZDczNjE1YmYzMjNlMTRhOWNkNzc4OGNhNWFjYjY1YjBiMWFmNTY0NWRkZDA5MSJ9fX0=", 1, 0, "§eCoin-Shop", Arrays.asList("§8 ➥ §7Ränge, Cosmetics und vieles mehr!")));
                for (int i = 0; i < 27; i++) {
                    if (inv.getItem(i) == null) {
                        inv.setItem(i, ItemManager.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, 0, "§c", null));
                    }
                }
                player.openInventory(inv);
            }
        }
    }
}
