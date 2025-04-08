package de.polo.core.web.dto;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class StatsDto {
    public final int factionCount;
    public final int companyCount;
    public final int playerCount;

    public StatsDto(final int factionCount, final int companyCount, final int playerCount) {
        this.factionCount = factionCount;
        this.companyCount = companyCount;
        this.playerCount = playerCount;
    }
}
