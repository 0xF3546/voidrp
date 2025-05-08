package de.polo.core.faction.service;

import de.polo.api.faction.CharacterRecord;

import java.util.UUID;

public interface LawEnforcementService {
    CharacterRecord getCharacterRecord(UUID target);

    void setCharacterRecord(UUID target, CharacterRecord record);
}
