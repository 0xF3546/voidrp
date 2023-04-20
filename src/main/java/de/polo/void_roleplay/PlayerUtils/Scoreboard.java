package de.polo.void_roleplay.PlayerUtils;

import de.polo.void_roleplay.DataStorage.PlayerData;
import de.polo.void_roleplay.Main;
import de.polo.void_roleplay.Utils.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.security.cert.TrustAnchor;

public class Scoreboard extends ScoreboardBuilder {

    private boolean isScore = false;
    private boolean isAdminScore = false;
    private boolean isMineScore = false;
    private boolean isLebensmittelLieferantScore = false;
    private final String uuid;
    private final Player player;

    public Scoreboard(Player p) {
        super(p, "§6§lVoid Roleplay");
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
        setScore("§8 ➥ §e" + SupportManager.TicketCount, 4);
        setScore("§6Auslastung§8:", 3);
        setScore("§8 ➥ §e" + (r.totalMemory() - r.freeMemory()) / 1048576, 2);
        setScore("§6Spieler Online§8:", 1);
        setScore("§8 ➥ §e" + Bukkit.getOnlinePlayers().size() + "§8/§6" + Bukkit.getMaxPlayers(), 0);
        isAdminScore = true;
        isScore = true;
    }
    public void createMineScoreboard() {
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        setDisplayName("§7§lMinenarbeiter");
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

    public void createLebensmittelLieferantenScoreboard() {
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        setScore("§6Snacks§8:", 3);
        setScore("§8 ➥ §7" + playerData.getIntVariable("snacks"), 2);
        setScore("§6Getränke§8:", 1);
        setScore("§8 ➥ §7" + playerData.getIntVariable("drinks"), 0);
        isScore = true;
        isLebensmittelLieferantScore = true;
    }

    public void killScoreboard() {
        for (int i = 0; i < 15; i++) {
            removeScore(i);
        }
        isScore = false;
        isAdminScore = false;
        isMineScore = false;
    }

    @Override
    public void update() {

    }

 private void run() {
        new BukkitRunnable() {
            @Override
            public void run() {
                PlayerData playerData = PlayerManager.playerDataMap.get(uuid);
                if (isAdminScore) {
                    Runtime r = Runtime.getRuntime();
                    setScore("§8 ➥ §e" + SupportManager.TicketCount, 4);
                    setScore("§8 ➥ §e" + (r.totalMemory() - r.freeMemory()) / 1048576, 2);
                    setScore("§8 ➥ §e" + Bukkit.getOnlinePlayers().size() + "§8/§6" + Bukkit.getMaxPlayers(), 0);
                } else if (isMineScore) {
                    setScore("§8 ➥ §7" + ItemManager.getItem(player, Material.IRON_ORE), 10);
                    setScore("§8 ➥ §7" + ItemManager.getItem(player, Material.EMERALD_ORE), 8);
                    setScore("§8 ➥ §7" +ItemManager.getItem(player, Material.GOLD_ORE), 6);
                    setScore("§8 ➥ §7" + ItemManager.getItem(player, Material.LAPIS_ORE), 4);
                    setScore("§8 ➥ §7" + ItemManager.getItem(player, Material.REDSTONE_ORE), 2);
                    setScore("§8 ➥ §7" + ItemManager.getItem(player, Material.IRON_ORE), 0);
                }
            }
        }.runTaskTimer(Main.getInstance(), 20, 30);
    }
}