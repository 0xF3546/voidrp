package de.polo.api.player;

import de.polo.api.player.enums.UniversalSkill;

public interface PlayerSkill {
    UniversalSkill getSkill();
    int getLevel();
    void addLevel(int skillPoints);
    void reset();
    int getSkillPoints();
    void addSkillPoints(int points);
    double getJobExtraXPPercentage();
    double getJobExtraMoneyPercentage();
    double getJobDoubleXPChance();
    double getJobReducedCooldownPercentage();
    double getFinPaybackPercentage();
    double getFinInterestPercentage();
    int getFinIntrestLimitInc();
    double getWeapReducedSpreadPercentage();
    double getWeapIncreasedSpeedPercentage();
    double getWeapDecreaseReloadPercentage();
    VoidPlayer getPlayer();
}
