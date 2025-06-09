package de.polo.api.faction;

import java.time.LocalDateTime;
import java.util.UUID;

public interface CharacterRecord {
    UUID getCriminal();
    LocalDateTime getLastEdit();

    void setLastEdit(LocalDateTime lastEdit);

    UUID getLastEditor();

    void setLastEditor(UUID lastEditor);

    String getInfoText();

    void setInfoText(String infoText);
}
