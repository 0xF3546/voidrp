package de.polo.api.crew;

import java.util.List;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public interface Crew {
    int getId();
    String getName();
    List<CrewRank> getRanks();
    CrewRank getRank(int grade);
    void addRank(CrewRank rank);
    CrewRank getDefaultRank();
    CrewRank getBossRank();
    int getLevel();
    int getExp();
    void addExp(int exp, String reason);
}
