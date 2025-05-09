package de.polo.core.utils.player;

import de.polo.core.Main;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.manager.ItemManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

public class Scoreboard {

    private final ScoreboardAPI scoreboardAPI;
    private final Player player;
    private Vehicle vehicle;

    public Scoreboard(Player player, ScoreboardAPI scoreboardAPI) {
        this.player = player;
        this.scoreboardAPI = scoreboardAPI;
        run();
    }

    public void createAdminScoreboard() {
        scoreboardAPI.createScoreboard(player, "admin", "§cAdministration");
        updateAdminScoreboard();
    }

    public void createMineScoreboard() {
        scoreboardAPI.createScoreboard(player, "mine", "§8» §7§lMine §8«");
        updateMineScoreboard();
    }

    public void createFarmerScoreboard() {
        scoreboardAPI.createScoreboard(player, "farmer", "§8» §eFarmer §8«");
        updateFarmerScoreboard();
    }

    public void createPostboteScoreboard() {
        scoreboardAPI.createScoreboard(player, "postbote", "§8» §ePostbote §8«");
        updatePostboteScoreboard();
    }

    public void createMuellmannScoreboard() {
        scoreboardAPI.createScoreboard(player, "muellmann", "§8» §9Müllmann §8«");
        updateMuellmannScoreboard();
    }

    public void createLumberjackScoreboard() {
        scoreboardAPI.createScoreboard(player, "lumberjack", "§8» §7Holzfäller §8«");
        updateLumberjackScoreboard();
    }

    public void createWinzerScoreboard() {
        scoreboardAPI.createScoreboard(player, "winzer", "§8» §5Winzer §8«");
        updateWinzerScoreboard();
    }

    public void createLebensmittelLieferantenScoreboard() {
        scoreboardAPI.createScoreboard(player, "lebensmittel", "§8» §6Lieferant §8«");
        updateLebensmittelLieferantenScoreboard();
    }

    public void createWeizentransportScoreboard() {
        scoreboardAPI.createScoreboard(player, "weizen", "§8» §eLieferant §8«");
        updateWeizentransportScoreboard();
    }

    public void createCarScoreboard(Vehicle minecart) {
        scoreboardAPI.createScoreboard(player, "vehicle", "§6" + minecart.getPersistentDataContainer().get(new NamespacedKey(Main.getInstance(), "type"), PersistentDataType.STRING));
        updateCarScoreboard(minecart);
    }

    public void removeScoreboard(String scoreboardName) {
        scoreboardAPI.removeScoreboard(player, scoreboardName);
    }

    private void updateAdminScoreboard() {
        Runtime r = Runtime.getRuntime();
        scoreboardAPI.updateScoreboardTitle(player, "admin", "§cAdministration");
        scoreboardAPI.setScore(player, "admin", "§6Tickets offen§8:", Main.supportManager.getTickets().size());
        scoreboardAPI.setScore(player, "admin", "§6Auslastung§8:", (int) (r.totalMemory() - r.freeMemory()) / 1048576);
        scoreboardAPI.setScore(player, "admin", "§6Spieler Online§8:", Bukkit.getOnlinePlayers().size());
    }

    private void updateMineScoreboard() {
        PlayerData playerData = Main.playerManager.getPlayerData(player.getUniqueId());
        scoreboardAPI.updateScoreboardTitle(player, "mine", "§8» §7§lMine §8«");
        scoreboardAPI.setScore(player, "mine", "§bDiamanterz§8:", ItemManager.getItem(player, Material.DIAMOND_ORE));
        scoreboardAPI.setScore(player, "mine", "§aSmaragderz§8:", ItemManager.getItem(player, Material.EMERALD_ORE));
        scoreboardAPI.setScore(player, "mine", "§6Golderz§8:", ItemManager.getItem(player, Material.GOLD_ORE));
        scoreboardAPI.setScore(player, "mine", "§9Lapislazulierz§8:", ItemManager.getItem(player, Material.LAPIS_ORE));
        scoreboardAPI.setScore(player, "mine", "§cRedstoneerz§8:", ItemManager.getItem(player, Material.REDSTONE_ORE));
        scoreboardAPI.setScore(player, "mine", "§7Eisenerz§8:", ItemManager.getItem(player, Material.IRON_ORE));
    }

    private void updateFarmerScoreboard() {
        PlayerData playerData = Main.playerManager.getPlayerData(player.getUniqueId());
        scoreboardAPI.updateScoreboardTitle(player, "farmer", "§8» §eFarmer §8«");
        scoreboardAPI.setScore(player, "farmer", "§eHeuballen abgebaut§8:", playerData.getIntVariable("heuballen"));
        scoreboardAPI.setScore(player, "farmer", "§eHeuballen abzubauen§8:", playerData.getIntVariable("heuballen_remaining"));
    }

    private void updatePostboteScoreboard() {
        PlayerData playerData = Main.playerManager.getPlayerData(player.getUniqueId());
        scoreboardAPI.updateScoreboardTitle(player, "postbote", "§8» §ePostbote §8«");
        scoreboardAPI.setScore(player, "postbote", "§ePost verbleibend§8:", playerData.getIntVariable("post"));
    }

    private void updateMuellmannScoreboard() {
        PlayerData playerData = Main.playerManager.getPlayerData(player.getUniqueId());
        scoreboardAPI.updateScoreboardTitle(player, "muellmann", "§8» §9Müllmann §8«");
        scoreboardAPI.setScore(player, "muellmann", "§3Müll gesammelt§8:", playerData.getIntVariable("muellkg"));
        scoreboardAPI.setScore(player, "muellmann", "§3Häuser verbleibend§8:", playerData.getIntVariable("muell"));
    }

    private void updateLumberjackScoreboard() {
        // Implementiere diese Methode je nach Bedarf
    }

    private void updateWinzerScoreboard() {
        // Implementiere diese Methode je nach Bedarf
    }

    private void updateLebensmittelLieferantenScoreboard() {
        // Implementiere diese Methode je nach Bedarf
    }

    private void updateWeizentransportScoreboard() {
        // Implementiere diese Methode je nach Bedarf
    }

    private void updateCarScoreboard(Vehicle minecart) {
        // Implementiere diese Methode je nach Bedarf
    }

    private void run() {
        // Hier wird der Scoreboard-Updater gestartet
        new BukkitRunnable() {
            @Override
            public void run() {
                // Beispiel, alle 60 Sekunden aktualisieren
                updateAdminScoreboard();
            }
        }.runTaskTimer(Main.getInstance(), 0, 1200); // Alle 60 Sekunden aktualisieren
    }
}
