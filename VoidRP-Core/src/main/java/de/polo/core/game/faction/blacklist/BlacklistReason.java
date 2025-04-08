package de.polo.core.game.faction.blacklist;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class BlacklistReason {
    @Getter
    @Setter
    private int id;

    @Getter
    @Setter
    private String reason;

    @Getter
    @Setter
    private int price;

    @Getter
    @Setter
    private int kills;

    @Getter
    @Setter
    private int factionId;

    public BlacklistReason(String reason, int price, int kills) {
        this.reason = reason;
        this.price = price;
        this.kills = kills;
    }
}
