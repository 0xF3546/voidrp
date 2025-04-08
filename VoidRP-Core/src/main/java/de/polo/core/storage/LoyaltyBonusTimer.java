package de.polo.core.storage;

import de.polo.core.utils.Utils;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

public class LoyaltyBonusTimer {
    @Getter
    private final UUID uuid;

    @Getter
    @Setter
    private LocalDateTime started;

    @Getter
    @Setter
    private LocalDateTime stopped;
    public LoyaltyBonusTimer(UUID uuid) {
        this.uuid = uuid;
        started = Utils.getTime();
    }
}
