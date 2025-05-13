package de.polo.core.faction.service;

import de.polo.api.faction.CharacterRecord;
import de.polo.api.player.PlayerWanted;
import de.polo.api.player.VoidPlayer;
import de.polo.core.storage.CorePlayerWanted;
import de.polo.core.storage.WantedReason;

import java.util.List;
import java.util.UUID;

public interface LawEnforcementService {
    CharacterRecord getCharacterRecord(UUID target);

    void setCharacterRecord(UUID target, CharacterRecord record);

    void addWantedLog(UUID criminal, PlayerWanted playerWanted);
    boolean arrestPlayer(VoidPlayer player, VoidPlayer target, boolean isDeathArrest);
    void unarrestPlayer(VoidPlayer player);
    List<WantedReason> getWantedReasons();
    WantedReason getWantedReason(int id);
    WantedReason getWantedReason(String reason);
    void addWantedReason(WantedReason reason);
}
