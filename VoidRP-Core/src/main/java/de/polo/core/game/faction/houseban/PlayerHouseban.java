package de.polo.core.game.faction.houseban;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class PlayerHouseban {
    @Getter
    @Setter
    private int id;

    @Getter
    @Setter
    private UUID uuid;

    @Getter
    @Setter
    private String reason;

    @Getter
    @Setter
    private LocalDateTime until;

    @Getter
    @Setter
    private UUID punisher;

    public PlayerHouseban(UUID uuid, String reason, LocalDateTime until) {
        this.uuid = uuid;
        this.reason = reason;
        this.until = until;
    }
}
