package de.polo.core.crew.services;

import de.polo.api.crew.Crew;
import de.polo.api.crew.CrewRank;
import de.polo.api.crew.enums.CrewPermission;
import de.polo.core.crew.dto.CreateCrewDto;
import de.polo.core.crew.dto.CreateCrewRankDto;
import de.polo.core.crew.dto.CrewMemberDto;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public interface CrewService {
    /**
     * @param id the id of the crew
     * @return the crew with the given id
     */
    @Nullable
    Crew getCrew(int id);

    /**
     * @param name the name of the crew
     * @return the crew with the given name
     */
    Crew getCrew(String name);

    /**
     * @param crew the crew to add
     */
    void createCrew(CreateCrewDto crew);

    /**
     * @param crew the crew to remove
     */
    void deleteCrew(Crew crew);

    /**
     * @param uuid the uuid of the player
     */
    void setPlayerCrew(UUID uuid, int crewId);
    void removePlayerFromCrew(UUID uuid);

    void setPlayerCrewRank(UUID uuid, int crewRankId);

    CompletableFuture<CrewRank> addCrewRank(CreateCrewRankDto createCrewRankDto);

    CrewRank getCrewRank(int id);
    void sendMessageToMembers(Crew crew, String message);
    List<CrewMemberDto> getCrewMembers(Crew crew);

    void addRankPermission(CrewRank crewRank, CrewPermission permission);
    void removeRankPermission(CrewRank crewRank, CrewPermission permission);
    void setRankName(CrewRank crewRank, String name);
    void setRankGrade(CrewRank crewRank, int grade);
}
