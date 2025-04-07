package de.polo.voidroleplay.game.base.extra;

import de.polo.voidroleplay.utils.enums.PlaytimeRewardType;
import lombok.Getter;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class PlaytimeReward {
    @Getter
    private final int id;

    @Getter
    private final int hour;

    @Getter
    private final String displayName;

    @Getter
    private final boolean premiumOnly;

    @Getter
    private final float amount;

    @Getter
    private final PlaytimeRewardType playtimeRewardType;

    public PlaytimeReward(int id, int hour, String displayName, boolean premiumOnly, float amount, PlaytimeRewardType playtimeRewardType) {
        this.id = id;
        this.hour = hour;
        this.displayName = displayName;
        this.premiumOnly = premiumOnly;
        this.amount = amount;
        this.playtimeRewardType = playtimeRewardType;
    }
}
