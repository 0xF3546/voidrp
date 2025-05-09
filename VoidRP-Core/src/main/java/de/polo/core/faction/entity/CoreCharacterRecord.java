package de.polo.core.faction.entity;

import de.polo.api.faction.CharacterRecord;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

public class CoreCharacterRecord implements CharacterRecord {
    @Getter
    @Setter
    private LocalDateTime lastEdit;

    @Getter
    @Setter
    private String infoText;

    @Getter
    @Setter
    private UUID lastEditor;

    public CoreCharacterRecord(String infoText, UUID lastEditor, LocalDateTime lastEdit) {
        this.infoText = infoText;
        this.lastEditor = lastEditor;
        this.lastEdit = lastEdit;
    }
}
