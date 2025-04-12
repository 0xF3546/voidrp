package de.polo.core.player.entities;

import de.polo.api.jobs.enums.MiniJob;
import de.polo.api.player.JobSkill;
import de.polo.api.player.VoidPlayer;
import lombok.Getter;
import net.kyori.adventure.text.Component;

import static de.polo.core.Main.database;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class CoreJobSkill implements JobSkill {
    @Getter
    private final MiniJob job;

    @Getter
    private int level;

    @Getter
    private int exp;

    @Getter
    private final VoidPlayer player;

    public CoreJobSkill(VoidPlayer player, MiniJob job, int level, int exp) {
        this.player = player;
        this.job = job;
        this.level = level;
        this.exp = exp;
    }

    @Override
    public void addExp(int exp) {
        this.exp += exp;
        if (this.exp >= getLevel() * 2250) {
            this.level++;
            this.exp = 0;
            player.getPlayer().sendActionBar(Component.text("§6§lDu bist im Job auf Level " + level + "aufgestiegen!"));
        } else {
            player.getPlayer().sendActionBar(Component.text("§3+" + exp + " Job Erfahrung! §8[§7" + this.exp + "§8/§7" + (getLevel() * 2250) + "§8]"));
        }
        database.updateAsync(
                "UPDATE player_jobskills SET level = ?, exp = ? WHERE uuid = ? AND job = ?",
                this.level, this.exp, player.getUuid().toString(), job.getName()
        );
    }
}
