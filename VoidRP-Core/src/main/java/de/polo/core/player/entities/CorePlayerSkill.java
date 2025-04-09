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
    private final VoidPlayer player;

    public CorePlayerSkill(VoidPlayer player, UniversalSkill skill, int level, int skillPoints) {
        this.player = player;
        this.skill = skill;
        this.level = level;
        this.skillPoints = skillPoints;
    }

    @Override
    public UniversalSkill getSkill() {
        return skill;
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public void addLevel(int skillPoints) {
        if (this.skillPoints >= skillPoints) {
            this.level += skillPoints;
            this.skillPoints -= skillPoints;
            database.updateAsync(
                    "UPDATE player_skills SET level = ?, skill_points = ? WHERE uuid = ? AND skill_type = ?",
                    this.level, this.skillPoints, player.getUuid().toString(), skill.getName()
            );
        } else {
            player.getPlayer().sendMessage("§cNicht genügend Skillpunkte!");
        }
    }

    @Override
    public void reset() {
        this.skillPoints += this.level;
        this.level = 0;
        database.updateAsync(
                "UPDATE player_skills SET level = 0, skill_points = ? WHERE uuid = ? AND skill_type = ?",
                this.skillPoints, player.getUuid().toString(), skill.getName()
        );
        player.getPlayer().sendMessage("§6Dein Skill " + skill.getName() + " wurde zurückgesetzt!");
    }

    @Override
    public int getSkillPoints() {
        return skillPoints;
    }

    @Override
    public void addSkillPoints(int points) {
        this.skillPoints += points;
        player.getPlayer().sendActionBar(Component.text("§3+" + points + " §7Skillpunkte erhalten! §8[§7" + this.skillPoints + "§8]"));
        database.updateAsync(
                "UPDATE player_skills SET skill_points = ? WHERE uuid = ? AND skill = ?",
                this.skillPoints, player.getUuid().toString(), skill.getName()
        );
    }

    @Override
    public VoidPlayer getPlayer() {
        return player;
    }
}
