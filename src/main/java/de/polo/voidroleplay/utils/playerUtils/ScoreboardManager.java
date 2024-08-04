package de.polo.voidroleplay.utils.playerUtils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScoreboardManager implements Listener {

    private final List<PlayerScoreboard> playerScoreboards = new ArrayList<>();
    private final Map<Player, PlayerScoreboard> activeScoreboards = new HashMap<>();

    public void createScoreboard(PlayerScoreboard playerScoreboard) {
        Player player = playerScoreboard.getPlayer();
        System.out.println("Erstelle neues Scoreboard: " + playerScoreboard.getScoreboardName() + " für Spieler: " + player.getName());

        Scoreboard newScoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = newScoreboard.registerNewObjective(playerScoreboard.getScoreboardName(), "dummy", playerScoreboard.getDisplayName());
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        playerScoreboard.setScoreboard(newScoreboard);
        playerScoreboards.add(playerScoreboard);

        System.out.println("Scoreboard hinzugefügt: " + playerScoreboard.getScoreboardName() + " für Spieler: " + player.getName());

        // Setze das neue Scoreboard als aktives Scoreboard
        setActiveScoreboard(player, playerScoreboard.getScoreboardName());
        if (playerScoreboard.getScoreboardCallback() != null) playerScoreboard.getScoreboardCallback().onUpdate();
    }

    public void setActiveScoreboard(Player player, String scoreboardName) {
        for (PlayerScoreboard playerScoreboard : playerScoreboards) {
            if (playerScoreboard.getPlayer().equals(player) && playerScoreboard.getScoreboardName().equals(scoreboardName)) {
                player.setScoreboard(playerScoreboard.getScoreboard());
                activeScoreboards.put(player, playerScoreboard);
                System.out.println("Aktives Scoreboard gesetzt: " + scoreboardName + " für Spieler: " + player.getName());
                return;
            }
        }
    }

    public void removeScoreboard(Player player, String scoreboardName) {
        PlayerScoreboard toRemove = null;

        for (PlayerScoreboard playerScoreboard : playerScoreboards) {
            if (playerScoreboard.getPlayer().equals(player) && playerScoreboard.getScoreboardName().equals(scoreboardName)) {
                toRemove = playerScoreboard;
                break;
            }
        }

        if (toRemove != null) {
            playerScoreboards.remove(toRemove);
            System.out.println("Scoreboard entfernt: " + scoreboardName + " für Spieler: " + player.getName());

            // Wenn das entfernte Scoreboard das aktive Scoreboard war, setze das Haupt-Scoreboard
            if (toRemove.equals(activeScoreboards.get(player))) {
                player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
                activeScoreboards.remove(player);
                System.out.println("Hauptscoreboard gesetzt für Spieler: " + player.getName());

                // Wenn noch andere Scoreboards vorhanden sind, setze eines davon als aktiv
                for (PlayerScoreboard playerScoreboard : playerScoreboards) {
                    if (playerScoreboard.getPlayer().equals(player)) {
                        setActiveScoreboard(player, playerScoreboard.getScoreboardName());
                        break;
                    }
                }
            }

            if (playerScoreboards.stream().noneMatch(ps -> ps.getPlayer().equals(player))) {
                activeScoreboards.remove(player);
                System.out.println("Alle Scoreboards entfernt für Spieler: " + player.getName());
            }
        }
    }

    public void removeAllScoreboards(Player player) {
        playerScoreboards.removeIf(playerScoreboard -> playerScoreboard.getPlayer().equals(player));
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        activeScoreboards.remove(player);
        System.out.println("Alle Scoreboards entfernt und Hauptscoreboard gesetzt für Spieler: " + player.getName());
    }

    public void updateScoreboard(Player player, String scoreboardName, String displayName) {
        for (PlayerScoreboard playerScoreboard : playerScoreboards) {
            if (playerScoreboard.getPlayer().equals(player) && playerScoreboard.getScoreboardName().equals(scoreboardName)) {
                Objective objective = playerScoreboard.getScoreboard().getObjective(scoreboardName);
                if (objective != null) {
                    objective.setDisplayName(displayName);
                    playerScoreboard.setDisplayName(displayName);
                    System.out.println("Scoreboard aktualisiert: " + scoreboardName + " für Spieler: " + player.getName());
                }
                break;
            }
        }
    }

    public Scoreboard getScoreboard(Player player, String scoreboardName) {
        for (PlayerScoreboard playerScoreboard : playerScoreboards) {
            if (playerScoreboard.getPlayer().equals(player) && playerScoreboard.getScoreboardName().equals(scoreboardName)) {
                return playerScoreboard.getScoreboard();
            }
        }
        return null;
    }

    public void everySecond() {
        for (PlayerScoreboard playerScoreboard : activeScoreboards.values()) {
            if (playerScoreboard.getScoreboardCallback() != null) {
                playerScoreboard.getScoreboardCallback().onUpdate();
            }
        }
    }
}
