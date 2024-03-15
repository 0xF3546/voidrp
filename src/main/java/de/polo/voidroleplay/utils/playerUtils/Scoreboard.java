package de.polo.voidroleplay.utils.playerUtils;

import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.utils.*;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

public class Scoreboard extends ScoreboardBuilder {

    private boolean isScore = false;
    private boolean isAdminScore = false;
    private boolean isMineScore = false;
    private boolean isLebensmittelLieferantScore = false;
    private boolean isFarmerScore = false;
    private boolean isCarScore = false;
    private final String uuid;
    private final Player player;
    private Vehicle vehicle;

    public Scoreboard(Player p) {
        super(p, "§6VoidRoleplay");
        uuid = p.getUniqueId().toString();
        player = p;
        run();
    }

    @Override
    public void createScoreboard() {

    }

    @Override
    public void createAdminScoreboard() {
        Runtime r = Runtime.getRuntime();
        setDisplayName(ChatColor.RED.toString() + ChatColor.BOLD + "  Administration  ");
        setScore("§6Tickets offen§8:", 5);
        setScore("§8 ➥ §e" + Main.getInstance().supportManager.getTickets().size(), 4);
        setScore("§6Auslastung§8:", 3);
        setScore("§8 ➥ §e" + (r.totalMemory() - r.freeMemory()) / 1048576, 2);
        setScore("§6Spieler Online§8:", 1);
        setScore("§8 ➥ §e" + Bukkit.getOnlinePlayers().size() + "§8/§6" + Bukkit.getMaxPlayers(), 0);
        isAdminScore = true;
        isScore = true;
    }
    public void createMineScoreboard() {
        PlayerData playerData = Main.getInstance().playerManager.getPlayerData(player.getUniqueId());
        setDisplayName("§8» §7§lMine §8«");
        setScore("§bDiamanterz§8:", 11);
        setScore("§8 ➥ §7" + ItemManager.getItem(player, Material.DIAMOND_ORE), 10);
        setScore("§aSmaragderz§8:", 9);
        setScore("§8 ➥ §7" + ItemManager.getItem(player, Material.EMERALD_ORE), 8);
        setScore("§6Golderz§8:", 7);
        setScore("§8 ➥ §7" + ItemManager.getItem(player, Material.GOLD_ORE), 6);
        setScore("§9Lapislazulierz§8:", 5);
        setScore("§8 ➥ §7" + ItemManager.getItem(player, Material.LAPIS_ORE), 4);
        setScore("§cRedstoneerz§8:", 3);
        setScore("§8 ➥ §7" + ItemManager.getItem(player, Material.REDSTONE_ORE), 2);
        setScore("§7Eisenerz§8:", 1);
        setScore("§8 ➥ §7" + ItemManager.getItem(player, Material.IRON_ORE), 0);
        isScore = true;
        isMineScore = true;
    }

    public void createFarmerScoreboard() {
        PlayerData playerData = Main.getInstance().playerManager.getPlayerData(player.getUniqueId());
        setDisplayName("§8» §eFarmer §8«");
        setScore("§eHeuballen abgebaut§8:", 3);
        setScore("§8 ➥ §7" + playerData.getIntVariable("heuballen"), 2);
        setScore("§eHeuballen abzubauen§8:", 1);
        setScore("§8 ➥ §7" + playerData.getIntVariable("heuballen_remaining"), 0);
        isScore = true;
        isFarmerScore = true;
    }
    public void updateFarmerScoreboard() {
        PlayerData playerData = Main.getInstance().playerManager.getPlayerData(player.getUniqueId());
        setDisplayName("§8» §eFarmer §8«");
        setScore("§eHeuballen abgebaut§8:", 3);
        setScore("§8 ➥ §7" + playerData.getIntVariable("heuballen"), 2);
        setScore("§eHeuballen abzubauen§8:", 1);
        setScore("§8 ➥ §7" + playerData.getIntVariable("heuballen_remaining"), 0);
    }
    public void createPostboteScoreboard() {
        PlayerData playerData = Main.getInstance().playerManager.getPlayerData(player.getUniqueId());
        setDisplayName("§8» §ePostbote §8«");
        setScore("§ePost verbleibend§8:", 1);
        setScore("§8 ➥ §7" + playerData.getIntVariable("post"), 0);
    }

    public void updatePostboteScoreboard() {
        PlayerData playerData = Main.getInstance().playerManager.getPlayerData(player.getUniqueId());
        setDisplayName("§8» §ePostbote §8«");
        setScore("§ePost verbleibend§8:", 1);
        setScore("§8 ➥ §7" + playerData.getIntVariable("post"), 0);
    }

    public void createMuellmannScoreboard() {
        PlayerData playerData = Main.getInstance().playerManager.getPlayerData(player.getUniqueId());
        setDisplayName("§8» §9Müllmann §8«");
        setScore("§3Müll gesammelt§8:", 3);
        setScore("§8 ➥ §7" + playerData.getIntVariable("muellkg") + "kg", 2);
        setScore("§3Häuser verbleibend§8:", 1);
        setScore("§8 ➥ §7" + playerData.getIntVariable("muell"), 0);
    }

    public void updateMuellmannScoreboard() {
        PlayerData playerData = Main.getInstance().playerManager.getPlayerData(player.getUniqueId());
        setDisplayName("§8» §9Müllmann §8«");
        setScore("§3Müll gesammelt§8:", 3);
        setScore("§8 ➥ §7" + playerData.getIntVariable("muellkg") + "kg", 2);
        setScore("§3Häuser verbleibend§8:", 1);
        setScore("§8 ➥ §7" + playerData.getIntVariable("muell"), 0);
    }

    public void createLumberjackScoreboard() {
        PlayerData playerData = Main.getInstance().playerManager.getPlayerData(player.getUniqueId());
        setDisplayName("§8» §7Holzfäller §8«");
        setScore("§7Holz gesammelt§8:", 3);
        setScore("§8 ➥ §7" + playerData.getIntVariable("holzkg") + "kg", 2);
        setScore("§7Bäume verbleibend§8:", 1);
        setScore("§8 ➥ §7" + playerData.getIntVariable("holz"), 0);
    }

    public void createWinzerScoreboard() {
        PlayerData playerData = Main.getInstance().playerManager.getPlayerData(player.getUniqueId());
        setDisplayName("§8» §5Winzer §8«");
        setScore("§dWeintrauben gesammelt§8:", 3);
        setScore("§8 ➥ §7" + playerData.getIntVariable("winzer_harvested") + " Stück", 2);
        setScore("§dVerbleibende Weinreben§8:", 1);
        setScore("§8 ➥ §7" + playerData.getIntVariable("winzer"), 0);
    }

    public void updateLumberjackScoreboard() {
        PlayerData playerData = Main.getInstance().playerManager.getPlayerData(player.getUniqueId());
        setDisplayName("§8» §7Holzfäller §8«");
        setScore("§7Holz gesammelt§8:", 3);
        setScore("§8 ➥ §7" + playerData.getIntVariable("holzkg") + "kg", 2);
        setScore("§7Bäume verbleibend§8:", 1);
        setScore("§8 ➥ §7" + playerData.getIntVariable("holz"), 0);
    }

    public void createLebensmittelLieferantenScoreboard() {
        PlayerData playerData = Main.getInstance().playerManager.getPlayerData(player.getUniqueId());
        setDisplayName("8» §6Lieferant §8«");
        setScore("§6Snacks§8:", 3);
        setScore("§8 ➥ §7" + playerData.getIntVariable("snacks"), 2);
        setScore("§6Getränke§8:", 1);
        setScore("§8 ➥ §7" + playerData.getIntVariable("drinks"), 0);
        isScore = true;
        isLebensmittelLieferantScore = true;
    }

    public void createWeizentransportScoreboard() {
        PlayerData playerData = Main.getInstance().playerManager.getPlayerData(player.getUniqueId());
        setDisplayName("§8» §eLieferant §8«");
        setScore("§eWeizen§8:", 1);
        setScore("§8 ➥ §7" + playerData.getIntVariable("weizen") + "kg", 0);
        isScore = true;
    }
    public void updateWeizentransportScoreboard() {
        PlayerData playerData = Main.getInstance().playerManager.getPlayerData(player.getUniqueId());
        setDisplayName("§8» §eLieferant §8«");
        setScore("§eWeizen§8:", 1);
        setScore("§8 ➥ §7" + playerData.getIntVariable("weizen") + "kg", 0);
    }

    public void createCarScoreboard(Vehicle minecart) {
        isCarScore = true;
        isScore = true;
        String type = minecart.getPersistentDataContainer().get(new NamespacedKey(Main.plugin, "type"), PersistentDataType.STRING);
        setDisplayName("§6" + type);
        setScore("§eKM/H§8:", 5);
        setScore("§8 ➥ §7Lädt...", 4);
        setScore("§eKM§8:", 3);
        setScore("§8 ➥ §7Lädt...", 2);
        setScore("§eTank§8:", 1);
        setScore("§8 ➥ §7Lädt...", 0);
        vehicle = minecart;
    }

    public void killScoreboard() {
        for (int i = 0; i < 15; i++) {
            removeScore(i);
        }
        clearScoreboard();
        Main.getInstance().playerManager.getPlayerData(player.getUniqueId()).removeScoreboard(this);
    }

    @Override
    public void update() {

    }

 private void run() {
        new BukkitRunnable() {
            @Override
            public void run() {
                PlayerData playerData = Main.getInstance().playerManager.getPlayerData(player.getUniqueId());

                if (!player.isOnline()) {
                    cancel();
                    return;
                }

                if (playerData.getScoreboard("admin") != null) {
                    Runtime r = Runtime.getRuntime();
                    setScore("§8 ➥ §e" + SupportManager.TicketCount, 4);
                    setScore("§8 ➥ §e" + (r.totalMemory() - r.freeMemory()) / 1048576, 2);
                    setScore("§8 ➥ §e" + Bukkit.getOnlinePlayers().size() + "§8/§6" + Bukkit.getMaxPlayers(), 0);
                } else if (playerData.getScoreboard("mine") != null) {
                    setScore("§8 ➥ §7" + ItemManager.getItem(player, Material.IRON_ORE), 10);
                    setScore("§8 ➥ §7" + ItemManager.getItem(player, Material.EMERALD_ORE), 8);
                    setScore("§8 ➥ §7" +ItemManager.getItem(player, Material.GOLD_ORE), 6);
                    setScore("§8 ➥ §7" + ItemManager.getItem(player, Material.LAPIS_ORE), 4);
                    setScore("§8 ➥ §7" + ItemManager.getItem(player, Material.REDSTONE_ORE), 2);
                    setScore("§8 ➥ §7" + ItemManager.getItem(player, Material.IRON_ORE), 0);
                } else if (playerData.getScoreboard("vehicle") != null) {
                    int km = vehicle.getPersistentDataContainer().get(new NamespacedKey(Main.plugin, "km"), PersistentDataType.INTEGER);
                    float fuel = vehicle.getPersistentDataContainer().get(new NamespacedKey(Main.plugin, "fuel"), PersistentDataType.FLOAT);
                    double speedMetersPerSecond = player.getVehicle().getVelocity().length();
                    double kmh = speedMetersPerSecond * 36;
                    setScore("§8 ➥ §7" + (int) kmh * 2, 4);
                    setScore("§8 ➥ §7" + km, 2);
                    setScore("§8 ➥ §7" + fuel + "l", 0);
                } else if (playerData.getScoreboard("winzer") != null) {
                    createWinzerScoreboard();
                } else {
                    cancel();
                }
            }
        }.runTaskTimer(Main.getInstance(), 20, 30);
    }
}