package de.polo.api.faction;

import java.time.LocalDateTime;
import java.util.UUID;

public interface CharacterRecord {
    LocalDateTime getLastEdit();
    UUID getLastEditor();
    String getInfoText();

    void setLastEdit(LocalDateTime lastEdit);
    void setLastEditor(UUID lastEditor);
    void setInfoText(String infoText);
}
