package de.polo.core.faction.entity;

import de.polo.api.faction.CharacterRecord;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

public class CoreCharacterRecord implements CharacterRecord {
    @Getter
    private final UUID criminal;

    @Getter
    @Setter
    private LocalDateTime lastEdit;

    @Getter
    @Setter
    private String infoText;

    @Getter
    @Setter
    private UUID lastEditor;

    public CoreCharacterRecord(UUID criminal, String infoText, UUID lastEditor, LocalDateTime lastEdit) {
        this.criminal = criminal;
        this.infoText = infoText;
        this.lastEditor = lastEditor;
        this.lastEdit = lastEdit;
    }
}
