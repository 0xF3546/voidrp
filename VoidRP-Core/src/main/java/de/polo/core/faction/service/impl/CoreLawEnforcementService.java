package de.polo.core.faction.service.impl;

import de.polo.api.VoidAPI;
import de.polo.api.faction.CharacterRecord;
import de.polo.api.player.PlayerWanted;
import de.polo.api.player.VoidPlayer;
import de.polo.core.faction.service.LawEnforcementService;
import de.polo.core.location.services.LocationService;
import de.polo.core.storage.JailInfo;
import de.polo.core.storage.WantedReason;
import de.polo.core.utils.Service;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.List;
import java.util.UUID;

@Service
public class CoreLawEnforcementService implements LawEnforcementService {
    private final LawEnforcementRepository repository;

    public CoreLawEnforcementService() {
        this.repository = new LawEnforcementRepository();
    }

    @Override
    public CharacterRecord getCharacterRecord(UUID target) {
        return repository.getCharacterRecord(target);
    }

    @Override
    public void setCharacterRecord(UUID target, CharacterRecord record) {
        repository.setCharacterRecord(target, record);
    }

    @Override
    public void addWantedLog(UUID criminal, PlayerWanted playerWanted) {
        repository.addWantedLog(criminal, playerWanted);
    }

    @Override
    public boolean arrestPlayer(VoidPlayer player, VoidPlayer target, boolean isDeathArrest) {
        LocationService locationService = VoidAPI.getService(LocationService.class);
        if (target.getData().getWanted() == null) return false;
        target.getPlayer().teleport(locationService.getLocation("gefaengnis"));
        JailInfo jailInfo = new JailInfo();
        PlayerWanted wanted = target.getData().getWanted();
        return false;
    }
}
