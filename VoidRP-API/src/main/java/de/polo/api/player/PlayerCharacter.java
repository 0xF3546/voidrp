package de.polo.api.player;

import de.polo.api.crew.Crew;
import de.polo.api.crew.CrewRank;
import de.polo.api.jobs.enums.MiniJob;

import java.util.List;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public interface PlayerCharacter {
    <T> void setVariable(String key, T value);
    <T> T getVariable(String key);
    void addMoney(int amount, String reason);
    void addBankMoney(int amount, String reason);
    boolean removeMoney(int amount, String reason);
    boolean removeBankMoney(int amount, String reason);
    List<JobSkill> getJobSkills();
    JobSkill getJobSkill(MiniJob job);
    Crew getCrew();
    void setCrew(Crew crew);
    CrewRank getCrewRank();
    void setCrewRank(CrewRank rank);
}
