package de.polo.voidroleplay.dataStorage;

import lombok.Getter;

public class WantedReason {
    @Getter
    private final int id;

    @Getter
    private final String reason;

    @Getter
    private final int wanted;

    public WantedReason(int id, String reason, int wanted) {
        this.id = id;
        this.reason = reason;
        this.wanted = wanted;
    }
}
