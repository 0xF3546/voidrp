package de.polo.core.player.entities;

import de.polo.api.player.PlayerSkill;
import de.polo.api.player.VoidPlayer;
import de.polo.api.player.enums.PlayerSkillBoostType;
import de.polo.api.player.enums.UniversalSkill;
import lombok.Getter;
import net.kyori.adventure.text.Component;

import java.util.EnumMap;

import static de.polo.core.Main.database;

public class CorePlayerSkill implements PlayerSkill{

    @Getter
    private final UniversalSkill skill;

    @Getter
    private int level;

    @Getter
    private int skillPoints;

    @Getter
    private final EnumMap<PlayerSkillBoostType, Double> boosts = new EnumMap<>(PlayerSkillBoostType.class);

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
            if (skill == UniversalSkill.JOB) {
                boosts.put(PlayerSkillBoostType.EXTRA_XP, 0.0);
                boosts.put(PlayerSkillBoostType.EXTRA_MONEY, 0.0);
                boosts.put(PlayerSkillBoostType.DOUBLE_XP_CHANCE, 0.0);
                boosts.put(PlayerSkillBoostType.REDUCED_COOLDOWN, 0.0);
            } else if (skill == UniversalSkill.FINANZEN) {
                boosts.put(PlayerSkillBoostType.PAYBACK, 0.0);
                boosts.put(PlayerSkillBoostType.INTEREST, 0.0);
                boosts.put(PlayerSkillBoostType.INTEREST_LIMIT, 0.0);
            } else if (skill == UniversalSkill.WAFFEN) {
                boosts.put(PlayerSkillBoostType.REDUCED_SPREAD, 0.0);
                boosts.put(PlayerSkillBoostType.INCREASED_SPEED, 0.0);
                boosts.put(PlayerSkillBoostType.DECREASE_RELOAD, 0.0);
            }
            return;
        }
        SkillBoostBase(from, to);
    }

    private void SkillBoostBase(int from, int to) {
        double end1 = 0, end2 = 0, end5 = 0, end10 = 0;
        for(int i = from; i <= to; i++){
            if(i%10 == 0){
                end10++;
            }
            else if(i%5 == 0){
                end5++;
            }
            else{
                int tmp = i;
                if(tmp%10 > 5){tmp -= 5;}
                if(tmp%2 == 1){
                    end1++;
                }
                else{
                    end2++;
                }
            }
        }
        if(skill == UniversalSkill.JOB){
            boosts.put(PlayerSkillBoostType.EXTRA_XP, end1 * 0.005);
            boosts.put(PlayerSkillBoostType.EXTRA_MONEY, end2 * 0.005);
            boosts.put(PlayerSkillBoostType.DOUBLE_XP_CHANCE, end5 * 0.02);
            boosts.put(PlayerSkillBoostType.REDUCED_COOLDOWN, end10 * 0.05);
        }
        else if(skill == UniversalSkill.FINANZEN){
            boosts.put(PlayerSkillBoostType.PAYBACK, (end1 + end2) * 0.002);
            boosts.put(PlayerSkillBoostType.INTEREST, end5 * 0.02);
            boosts.put(PlayerSkillBoostType.INTEREST_LIMIT, end10 * 5000);
        }
        else if(skill == UniversalSkill.WAFFEN){
            boosts.put(PlayerSkillBoostType.REDUCED_SPREAD, end1 + end2 * 0.0125);
            boosts.put(PlayerSkillBoostType.INCREASED_SPEED, end5 * 0.02);
            boosts.put(PlayerSkillBoostType.DECREASE_RELOAD, end10 * 3);
        }
    }
}