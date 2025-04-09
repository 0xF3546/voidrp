package de.polo.api.player;

import de.polo.api.jobs.enums.MiniJob;
import de.polo.api.player.enums.UniversalSkill;

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
    List<JobSkill> getJobSkills();
    JobSkill getJobSkill(MiniJob job);
    List<PlayerSkill> getPlayerSkills();
    PlayerSkill getPlayerSkill(UniversalSkill skill);
}
