package de.polo.voidroleplay.utils.playerUtils;

import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

public class ScoreboardAPI {

    private final ScoreboardManager scoreboardManager;

    public ScoreboardAPI(ScoreboardManager scoreboardManager) {
        this.scoreboardManager = scoreboardManager;
    }

    public void createScoreboard(Player player, String scoreboardName, String displayName) {
        if (scoreboardManager == null) {
            throw new IllegalStateException("ScoreboardManager nicht initialisiert.");
        }
        PlayerScoreboard playerScoreboard = new PlayerScoreboard(player, scoreboardName, displayName);
        scoreboardManager.createScoreboard(playerScoreboard);
    }

    public void createScoreboard(Player player, String scoreboardName, String displayName, ScoreboardCallback scoreboardCallback) {
        if (scoreboardManager == null) {
            throw new IllegalStateException("ScoreboardManager nicht initialisiert.");
        }

        PlayerScoreboard playerScoreboard = new PlayerScoreboard(player, scoreboardName, displayName);
        playerScoreboard.setScoreboardCallback(scoreboardCallback);
        scoreboardManager.createScoreboard(playerScoreboard);
    }

    public void removeScoreboard(Player player, String scoreboardName) {
        scoreboardManager.removeScoreboard(player, scoreboardName);
    }

    public void removeAllScoreboards(Player player) {
        scoreboardManager.removeAllScoreboards(player);
    }

    public void updateScoreboard(Player player, String scoreboardName, String displayName) {
        scoreboardManager.updateScoreboard(player, scoreboardName, displayName);
    }

    public Scoreboard getScoreboard(Player player, String scoreboardName) {
        return scoreboardManager.getScoreboard(player, scoreboardName);
    }

    public void setScore(Player player, String scoreboardName, String entry, int score) {
        Scoreboard scoreboard = player.getScoreboard();
        if (scoreboard == null) {
            return;
        }
        Objective objective = scoreboard.getObjective(scoreboardName);
        if (objective == null) {
            return;
        }
        Score scoreEntry = objective.getScore(entry);
        scoreEntry.setScore(score);
    }

    public void updateScoreboardTitle(Player player, String scoreboardName, String newTitle) {
        Scoreboard scoreboard = scoreboardManager.getScoreboard(player, scoreboardName);
        if (scoreboard != null) {
            Objective objective = scoreboard.getObjective(scoreboardName);
            if (objective != null) {
                objective.setDisplayName(newTitle);
            }
        }
    }

    public void everySecond() {
        scoreboardManager.everySecond();
    }
}
