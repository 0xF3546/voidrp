package de.polo.api.player;

import de.polo.api.player.enums.UniversalSkill;

public interface PlayerSkill {
    UniversalSkill getSkill();
    int getLevel();
    void addLevel(int skillPoints);
    void reset();
    int getSkillPoints();
    void addSkillPoints(int points);
    VoidPlayer getPlayer();
}
