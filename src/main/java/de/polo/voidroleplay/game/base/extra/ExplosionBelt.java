package de.polo.voidroleplay.game.base.extra;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;

public class ExplosionBelt {

    @Getter
    private final Player player;

    @Getter
    @Setter
    private int seconds;

    public ExplosionBelt(Player player, int seconds) {
        this.player = player;
        this.seconds = seconds;
    }
}
