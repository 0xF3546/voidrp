package de.polo.core.faction.service.impl;

import de.polo.api.faction.CharacterRecord;
import de.polo.api.player.PlayerWanted;
import de.polo.core.Main;
import de.polo.core.faction.entity.CoreCharacterRecord;
import de.polo.core.storage.CorePlayerWanted;
import de.polo.core.storage.WantedReason;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.SneakyThrows;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static de.polo.core.Main.database;

public class LawEnforcementRepository {
    @Getter
    private final List<CharacterRecord> characterRecords = new ObjectArrayList<>();
    @Getter
    private final List<WantedReason> wantedReasons = new ObjectArrayList<>();

    public LawEnforcementRepository() {
        loadWantedReasons();
    }

    @SneakyThrows
    private void loadWantedReasons() {
        Main.getInstance().getCoreDatabase().queryThreaded("SELECT * FROM wantedreasons")
                .thenAccept(result -> {
                    try {
                        while (result.next()) {
                            WantedReason reason = new WantedReason(result.resultSet().getInt("id"), result.resultSet().getString("reason"), result.resultSet().getInt("wanted"));
                            wantedReasons.add(reason);
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } finally {
                        result.close();
                    }
                });
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
