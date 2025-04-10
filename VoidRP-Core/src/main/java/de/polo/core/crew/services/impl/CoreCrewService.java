package de.polo.core.crew.services.impl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import de.polo.api.VoidAPI;
import de.polo.api.crew.Crew;
import de.polo.api.crew.CrewRank;
import de.polo.api.crew.enums.CrewPermission;
import de.polo.api.player.PlayerCharacter;
import de.polo.api.player.VoidPlayer;
import de.polo.core.Main;
import de.polo.core.crew.dto.CreateCrewDto;
import de.polo.core.crew.dto.CreateCrewRankDto;
import de.polo.core.crew.dto.CrewMemberDto;
import de.polo.core.crew.entities.CoreCrew;
import de.polo.core.crew.entities.CoreCrewRank;
import de.polo.core.crew.services.CrewService;
import de.polo.core.utils.Service;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.SneakyThrows;
import net.kyori.adventure.text.format.TextColor;
import org.json.simple.JSONArray;

import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static de.polo.core.Main.database;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
@Service
public class CoreCrewService implements CrewService {
    private final List<Crew> crews = new ObjectArrayList<>();

    public CoreCrewService() {
        loadCrews();
    }

    private void loadCrews() {
        try (final var connection = Main.getInstance().getCoreDatabase().getConnection();
             final var statement = connection.createStatement();
             final var resultSet = statement.executeQuery("SELECT * FROM crews")) {
            while (resultSet.next()) {
                final int id = resultSet.getInt("id");
                final String name = resultSet.getString("name");
                final UUID uuid = UUID.fromString(resultSet.getString("owner"));
                CoreCrew coreCrew = new CoreCrew(id, name, uuid);
                coreCrew.setBossGroup(resultSet.getInt("bossGroup"));
                coreCrew.setDefaultGroup(resultSet.getInt("defaultGroup"));
                crews.add(coreCrew);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        /// load crew ranks
        try (final var connection = Main.getInstance().getCoreDatabase().getConnection();
             final var statement = connection.createStatement();
             final var resultSet = statement.executeQuery("SELECT * FROM crew_ranks")) {
            while (resultSet.next()) {
                final int id = resultSet.getInt("id");
                final String name = resultSet.getString("name");
                final int crewId = resultSet.getInt("crewId");
                final TextColor color = TextColor.fromHexString(resultSet.getString("color"));
                final int rank = resultSet.getInt("rank");
                final boolean isDefault = resultSet.getBoolean("isDefault");
                final boolean isBoss = resultSet.getBoolean("isBoss");

                Type type = new TypeToken<List<CrewPermission>>() {}.getType();
                List<CrewPermission> permissions = new Gson().fromJson(resultSet.getString("permissions"), type);


                Crew crew = getCrew(crewId);
                if (crew != null) {
                    CrewRank crewRank = new CoreCrewRank(id, name, crewId, color, rank, isDefault, isBoss, permissions);
                    crew.addRank(crewRank);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Crew getCrew(int id) {
        return crews.stream()
                .filter(crew -> crew.getId() == id)
                .findFirst()
                .orElse(null);
    }

    @Override
    public Crew getCrew(String name) {
        return crews.stream()
                .filter(crew -> crew.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void createCrew(CreateCrewDto createCrewDto) {
        database.insertAndGetKeyAsync("INSERT INTO crews (owner, name) VALUES (?, ?)", createCrewDto.getOwner().toString(), createCrewDto.getName())
                .thenCompose(key -> {
                    if (key.isPresent()) {
                        final CoreCrew crew = new CoreCrew(key.get(), createCrewDto.getName(), createCrewDto.getOwner());
                        crews.add(crew);

                        // Erstelle die Standard-Ränge asynchron
                        CreateCrewRankDto defaultUserRank = new CreateCrewRankDto("Anfänger", TextColor.fromHexString("#FFFFFF"), 0, crew.getId(), true, false);
                        CreateCrewRankDto defaultBossRank = new CreateCrewRankDto("Boss", TextColor.fromHexString("#FFFFFF"), 0, crew.getId(), true, true);

                        CompletableFuture<CrewRank> userRankFuture = addCrewRank(defaultUserRank);
                        CompletableFuture<CrewRank> bossRankFuture = addCrewRank(defaultBossRank);

                        return CompletableFuture.allOf(userRankFuture, bossRankFuture)
                                .thenApplyAsync(v -> {
                                    try {
                                        CrewRank userRank = userRankFuture.get();
                                        CrewRank bossRank = bossRankFuture.get();
                                        addRankPermission(userRank, CrewPermission.BOSS);

                                        database.updateAsync("UPDATE crews SET defaultGroup = ? WHERE id = ?", userRank.getId(), crew.getId());
                                        database.updateAsync("UPDATE crews SET bossGroup = ? WHERE id = ?", bossRank.getId(), crew.getId());
                                        crew.setBossGroup(bossRank.getId());
                                        crew.setDefaultGroup(userRank.getId());
                                        database.updateAsync("UPDATE players SET crew = ?, crewRank = ? WHERE uuid = ?", crew.getId(), bossRank.getId(), createCrewDto.getOwner().toString())
                                                .thenRun(() -> {
                                                    VoidPlayer player = VoidAPI.getPlayer(createCrewDto.getOwner());
                                                    if (player != null) {
                                                        player.getData().setCrew(crew);
                                                        player.getData().setCrewRank(bossRank);
                                                    }
                                                });
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    return null;
                                });
                    }
                    return null;
                });
    }

    @Override
    public void deleteCrew(Crew crew) {
        database.deleteAsync("DELETE FROM crews WHERE id = ?", crew.getId())
                .thenRun(() -> {
                    crews.remove(crew);
                });
    }

    @Override
    public void setPlayerCrew(UUID uuid, int crewId) {
        Crew crew = getCrew(crewId);
        if (crew == null) {
            return;
        }
        database.updateAsync("UPDATE players SET crew = ? WHERE uuid = ?", crewId, uuid.toString())
                .thenRun(() -> {
                    VoidPlayer player = VoidAPI.getPlayer(uuid);
                    if (player == null) return;
                    player.getData().setCrew(crew);
                });
    }

    @Override
    public void removePlayerFromCrew(UUID uuid) {
        database.updateAsync("UPDATE players SET crew = NULL, crewRank = NULL WHERE uuid = ?", uuid.toString())
                .thenRun(() -> {
                    VoidPlayer player = VoidAPI.getPlayer(uuid);
                    if (player == null) return;
                    player.getData().setCrew(null);
                    player.getData().setCrewRank(null);
                });
    }

    @Override
    public void setPlayerCrewRank(UUID uuid, int crewRankId) {
        database.updateAsync("UPDATE players SET crewRank = ? WHERE uuid = ?", crewRankId, uuid.toString())
                .thenRun(() -> {
                    VoidPlayer player = VoidAPI.getPlayer(uuid);
                    if (player == null) return;
                    CrewRank crewRank = getCrewRank(crewRankId);
                    if (crewRank != null) {
                        player.getData().setCrewRank(crewRank);
                    }
                });
    }

    @Override
    public CompletableFuture<CrewRank> addCrewRank(CreateCrewRankDto createCrewRankDto) {
        return database.insertAndGetKeyAsync("INSERT INTO crew_ranks (crewId, name, color, rank, isDefault, isBoss) VALUES (?, ?, ?, ?, ?, ?)",
                        createCrewRankDto.getCrewId(), createCrewRankDto.getName(), createCrewRankDto.getColor().asHexString(),
                        createCrewRankDto.getRank(), createCrewRankDto.isDefault(), createCrewRankDto.isBoss())
                .thenApplyAsync(key -> {
                    if (key.isPresent()) {
                        final Crew crew = getCrew(createCrewRankDto.getCrewId());
                        if (crew != null) {
                            CrewRank crewRank = new CoreCrewRank(key.get(), createCrewRankDto.getName(),
                                    crew.getId(), createCrewRankDto.getColor(),
                                    createCrewRankDto.getRank(), createCrewRankDto.isDefault(),
                                    createCrewRankDto.isBoss(), new ObjectArrayList<>());
                            crew.addRank(crewRank);
                            return crewRank;
                        }
                    }
                    return null;
                });
    }


    @Override
    public CrewRank getCrewRank(int id) {
        return crews.stream()
                .flatMap(crew -> crew.getRanks().stream())
                .filter(crewRank -> crewRank.getId() == id)
                .findFirst()
                .orElse(null);
    }

    @Override
    public void sendMessageToMembers(Crew crew, String message) {
        for (VoidPlayer player : VoidAPI.getPlayers()) {
            if (player.getData().getCrew() != crew) continue;
            player.sendMessage("§c" + crew.getName() + " §8┃ §c" + message);
        }
    }

    @Override
    public List<CrewMemberDto> getCrewMembers(final Crew crew) {
        List<CrewMemberDto> crewMembers = new ObjectArrayList<>();

        String query = "SELECT * FROM players WHERE crew = ?";

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, crew.getId());

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    final UUID uuid = UUID.fromString(resultSet.getString("uuid"));
                    final String name = resultSet.getString("player_name");
                    final int crewRankId = resultSet.getInt("crewRank");
                    CrewRank crewRank = getCrewRank(crewRankId);

                    if (crewRank != null) {
                        crewMembers.add(new CrewMemberDto(uuid, name, crewRank));
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException("Fehler beim Verarbeiten der Ergebnisdaten", e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Fehler beim Abrufen der Crew-Mitglieder", e);
        }

        return crewMembers;
    }

    @Override
    public void addRankPermission(CrewRank crewRank, CrewPermission permission) {
        if (crewRank instanceof CoreCrewRank coreCrewRank) {
            coreCrewRank.addPermission(permission);
            updateCrewRankPermissions(coreCrewRank);
        }
    }

    @Override
    public void removeRankPermission(CrewRank crewRank, CrewPermission permission) {
        if (crewRank instanceof CoreCrewRank coreCrewRank) {
            coreCrewRank.removePermission(permission);
            updateCrewRankPermissions(coreCrewRank);
        }
    }

    private void updateCrewRankPermissions(CrewRank crewRank) {
        if (crewRank instanceof CoreCrewRank coreCrewRank) {
            String json = new Gson().toJson(coreCrewRank.getPermissions());
            database.updateAsync("UPDATE crew_ranks SET permissions = ? WHERE id = ?", json, crewRank.getId());
        }
    }
}
