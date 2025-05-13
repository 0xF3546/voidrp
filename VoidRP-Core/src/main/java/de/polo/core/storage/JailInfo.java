package de.polo.core.storage;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

public class JailInfo {
    @Getter
    @Setter
    private UUID uuid;
    @Getter
    @Setter
    private int id;
    @Getter
    @Setter
    private int hafteinheiten;
    @Getter
    @Setter
    private int wantedId;

    public JailInfo(UUID uuid, int hafteinheiten, int wantedId) {
        this.uuid = uuid;
        this.hafteinheiten = hafteinheiten;
        this.wantedId = wantedId;
    }

    public JailInfo(int id, UUID uuid, int hafteinheiten, int wantedId) {
        this.id = id;
        this.uuid = uuid;
        this.hafteinheiten = hafteinheiten;
        this.wantedId = wantedId;
    }
}
