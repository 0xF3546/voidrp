package de.polo.voidroleplay.utils.player;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class PlayerScoreboard {
    @Getter
    @Setter
    private Scoreboard scoreboard;

    @Getter
    @Setter
    private Player player;

    @Getter
    @Setter
    private ScoreboardCallback scoreboardCallback;

    @Getter
    @Setter
    private String scoreboardName;

    @Getter
    @Setter
    private String displayName;

    public PlayerScoreboard(Player player, String scoreboardName, String displayName) {
        this.player = player;
        this.scoreboardName = scoreboardName;
        this.displayName = displayName;
    }
}
