package de.polo.api.player;

import de.polo.api.player.enums.PlayerSkillBoostType;
import de.polo.api.player.enums.UniversalSkill;

import java.util.EnumMap;

public interface PlayerSkill {
    UniversalSkill getSkill();
    int getLevel();
    void addLevel(int skillPoints);
    void reset();
    int getSkillPoints();
    void addSkillPoints(int points);
    double getBoost(PlayerSkillBoostType type);
    VoidPlayer getPlayer();
}
