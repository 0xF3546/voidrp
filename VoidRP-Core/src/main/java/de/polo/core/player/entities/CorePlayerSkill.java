package de.polo.core.player.entities;

import de.polo.api.player.PlayerSkill;
import de.polo.api.player.VoidPlayer;
import de.polo.api.player.enums.UniversalSkill;
import lombok.Getter;
import net.kyori.adventure.text.Component;

import static de.polo.core.Main.database;

public class CorePlayerSkill implements PlayerSkill{

    @Getter
    private final UniversalSkill skill;

    @Getter
    private int level;

    @Getter
    private int skillPoints;

    @Getter
    private double jobExtraXPPercentage;

    @Getter
    private double jobExtraMoneyPercentage;

    @Getter
    private double jobDoubleXPChance;

    @Getter
    private double jobReducedCooldownPercentage;

    @Getter
    private double finPaybackPercentage;

    @Getter
    private  double finInterestPercentage;

    @Getter
    private int finIntrestLimitInc;

    @Getter
    private double weapReducedSpreadPercentage;

    @Getter
    private double weapIncreasedSpeedPercentage;

    @Getter
    private double weapDecreaseReloadPercentage;

    @Getter
    private final VoidPlayer player;

    public CorePlayerSkill(VoidPlayer player, UniversalSkill skill, int level, int skillPoints) {
        this.player = player;
        this.skill = skill;
        this.level = level;
        this.skillPoints = skillPoints;
        updateSkillBoosts(0, level);
    }

    @Override
    public void addLevel(int skillPoints) {
        if (this.skillPoints >= skillPoints) {
            updateSkillBoosts(this.level, this.level + skillPoints);
            this.level += skillPoints;
            this.skillPoints -= skillPoints;
            database.updateAsync(
                    "UPDATE player_skills SET level = ? WHERE uuid = ? AND skill_type = ?",
                    this.level, player.getUuid().toString(), skill.name()
            ); //Update Level für einzelnen Skill
            database.updateAsync(
                    "UPDATE player_skills SET  skill_points = ? WHERE uuid = ?",
                    this.skillPoints, player.getUuid().toString()
            ); //Update Skillpoints für alle Skills

        } else {
            player.sendMessage("§cNicht genügend Skillpunkte!");
        }
    }

    @Override
    public void reset() {
        this.skillPoints = player.getPlayer().getLevel() * 2;
        updateSkillBoosts(this.level, 0);
        this.level = 0;
        database.updateAsync(
                "UPDATE player_skills SET level = 0 WHERE uuid = ?",
                player.getUuid().toString()
        );//Alle Skills auf Lvl 0 setzen
        database.updateAsync(
                "UPDATE player_skills SET skill_points = ? WHERE uuid = ?",
                this.skillPoints, player.getUuid().toString()
        );//Update Skillpoint für alle Skills
        player.sendMessage("§6Dein Skill " + skill.name() + " wurde zurückgesetzt!");
    }

    @Override
    public void addSkillPoints(int points) {
        this.skillPoints += points;
        player.getPlayer().sendActionBar(Component.text("§3+" + points + " §7Skillpunkte erhalten! §8[§7" + this.skillPoints + "§8]"));
        database.updateAsync(
                "UPDATE player_skills SET skill_points = ? WHERE uuid = ?",
                this.skillPoints, player.getUuid().toString()
        );//Skillpoint Update für Alle Skills
    }

    private void updateSkillBoosts(int from, int to) {
        if (to == 0) {
            if (skill.name().equalsIgnoreCase("Job")) {
                jobExtraXPPercentage = 0;
                jobExtraMoneyPercentage = 0;
                jobDoubleXPChance = 0;
                jobReducedCooldownPercentage = 0;
            } else if (skill.name().equalsIgnoreCase("Finanzen")) {
                finPaybackPercentage = 0;
                finInterestPercentage = 0;
                finIntrestLimitInc = 0;
            } else if (skill.name().equalsIgnoreCase("Waffen")) {
                weapReducedSpreadPercentage = 0;
                weapIncreasedSpeedPercentage = 0;
                weapDecreaseReloadPercentage = 0;
            }
            return;
        }
        if (skill.name().equalsIgnoreCase("Job")) {
            SkillBoostBase(from, to, 0.005, 0.005, 0.02, 0.05);
        } else if (skill.name().equalsIgnoreCase("Finanzen")) {
            SkillBoostBase(from, to, 0.002, 0.002, 0.02, 5000);
        } else if (skill.name().equalsIgnoreCase("Waffen")) {
            SkillBoostBase(from, to, 0.0125, 0.0125, 0.02, 1);
        }

    }

    private void SkillBoostBase(int from, int to, double step1, double step2, double step5, double step10) {
        if(step2 == 0){step2 = step1;}
        double end1 = 0, end2 = 0, end5 = 0, end10 = 0;
        for(int i = from; i <= to; i++){
            if(i%10 == 0){
                end10 += step10;
            }
            else if(i%5 == 0){
                end5 += step5;
            }
            else{
                int tmp = i;
                if(tmp%10 > 5){tmp -= 5;}
                if(tmp%2 == 1){
                    end1 += step1;
                }
                else{
                    end2 += step2;
                }
            }
        }
        if(skill.name().equalsIgnoreCase("Job")){
            jobExtraXPPercentage = end1;
            jobExtraMoneyPercentage = end2;
            jobDoubleXPChance = end5;
            jobReducedCooldownPercentage = end10;
        }
        else if(skill.name().equalsIgnoreCase("Finanzen")){
            finPaybackPercentage = end1 + end2;
            finInterestPercentage = end5;
            finIntrestLimitInc = (int)end5;
        }
        else if(skill.name().equalsIgnoreCase("Waffen")){
            weapReducedSpreadPercentage = end1 + end2;
            weapIncreasedSpeedPercentage = end5;
            weapDecreaseReloadPercentage = end5;
        }
    }
}