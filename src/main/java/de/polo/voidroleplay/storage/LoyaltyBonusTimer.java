package de.polo.voidroleplay.storage;

import de.polo.voidroleplay.utils.Utils;
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
