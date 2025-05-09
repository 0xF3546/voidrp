package de.polo.api.player;

import de.polo.api.jobs.enums.MiniJob;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public interface JobSkill {
    MiniJob getJob();

    int getLevel();

    int getExp();

    void addExp(int exp);

    VoidPlayer getPlayer();
}
