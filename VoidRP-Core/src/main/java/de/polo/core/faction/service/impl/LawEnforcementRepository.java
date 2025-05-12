package de.polo.core.faction.service.impl;

import de.polo.api.faction.CharacterRecord;
import de.polo.api.player.PlayerWanted;
import de.polo.core.faction.entity.CoreCharacterRecord;
import de.polo.core.storage.CorePlayerWanted;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static de.polo.core.Main.database;

public class LawEnforcementRepository {
    private final List<CharacterRecord> characterRecords = new ObjectArrayList<>();

    public List<CharacterRecord> getCharacterRecords() {
        return characterRecords;
    }

    public CharacterRecord getCharacterRecord(UUID target) {
        for (CharacterRecord record : characterRecords) {
            if (record.getLastEditor().equals(target)) {
                return record;
            }
        }
        database.executeQueryAsync("SELECT * FROM player_records WHERE uuid = ?", target.toString())
                .thenApply(result -> {
                    for (Map<String, Object> res : result) {
                        CharacterRecord record = new CoreCharacterRecord(
                                (String) res.get("infoText"),
                                UUID.fromString((String) res.get("lastEditor")),
                                ((Timestamp) res.get("lastEdit")).toLocalDateTime()
                        );
                        characterRecords.add(record);
                        return record;
                    }
                    return null;
                });
        return null;
    }

    public void setCharacterRecord(UUID target, CharacterRecord record) {
        for (int i = 0; i < characterRecords.size(); i++) {
            if (characterRecords.get(i).getLastEditor().equals(target)) {
                characterRecords.set(i, record);
                database.updateAsync("UPDATE player_records SET info_text = ?, lastEditor = ?, lastEdit = ? WHERE uuid = ?",
                        record.getInfoText(), record.getLastEditor().toString(), record.getLastEdit().toString(), target.toString());
                return;
            }
        }
        characterRecords.add(record);
        database.insertAsync("INSERT INTO player_records (uuid, info_text, lastEditor, lastEdit) VALUES (?, ?, ?, ?)",
                target.toString(), record.getInfoText(), record.getLastEditor().toString(), record.getLastEdit().toString());
    }

    public void addWantedLog(UUID criminal, PlayerWanted playerWanted) {
        database.insertAsync("INSERT INTO player_wanted_logs (uuid, wantedId, issuer) VALUES (?, ?, ?)",
                criminal.toString(), playerWanted.getWantedId(), playerWanted.getIssuer().toString());
    }
}
