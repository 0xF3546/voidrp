package de.polo.core.faction.service;

import de.polo.api.faction.CharacterRecord;
import de.polo.api.player.VoidPlayer;
import de.polo.core.storage.CorePlayerWanted;

import java.util.UUID;

public interface LawEnforcementService {
    CharacterRecord getCharacterRecord(UUID target);

    void setCharacterRecord(UUID target, CharacterRecord record);

    void addWantedLog(UUID criminal, CorePlayerWanted corePlayerWanted);
    boolean arrestPlayer(VoidPlayer player, VoidPlayer target, boolean isDeathArrest);
}
