package de.polo.core.faction.service.impl;

import de.polo.api.faction.CharacterRecord;
import de.polo.core.faction.service.LawEnforcementService;
import de.polo.core.storage.PlayerWanted;
import de.polo.core.utils.Service;

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
}
