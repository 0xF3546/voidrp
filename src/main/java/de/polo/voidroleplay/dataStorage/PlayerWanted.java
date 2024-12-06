package de.polo.voidroleplay.dataStorage;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

public class PlayerWanted {
    @Getter
    private final int wantedId;
    @Getter
    private final UUID issuer;
    @Getter
    private final LocalDateTime issued;
    @Getter
    @Setter
    private int id;

    public PlayerWanted(int wantedId, UUID issuer, LocalDateTime issued) {
        this.wantedId = wantedId;
        this.issuer = issuer;
        this.issued = issued;
    }

    public PlayerWanted(int id, int wantedId, UUID issuer, LocalDateTime issued) {
        this.id = id;
        this.wantedId = wantedId;
        this.issuer = issuer;
        this.issued = issued;
    }
}
