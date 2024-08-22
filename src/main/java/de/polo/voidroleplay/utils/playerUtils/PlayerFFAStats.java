package de.polo.voidroleplay.utils.playerUtils;

import de.polo.voidroleplay.utils.enums.FFAStatsType;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class PlayerFFAStats {
    @Getter
    @Setter
    private int id;

    @Getter
    private String uuid;

    @Getter
    @Setter
    private int kills;

    @Getter
    @Setter
    private int deaths;

    @Getter
    @Setter
    private FFAStatsType ffaStatsType = FFAStatsType.ALL_TIME;

    public PlayerFFAStats(String uuid, int kills, int deaths) {
        this.uuid = uuid;
        this.kills = kills;
        this.deaths = deaths;
    }

    public float getKD() {
        return (float) kills / deaths;
    }

}
