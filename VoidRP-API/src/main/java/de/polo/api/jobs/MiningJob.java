package de.polo.api.jobs;

import de.polo.api.player.VoidPlayer;
import org.bukkit.event.block.BlockBreakEvent;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public interface MiningJob extends Job {
    void handleBlockBreak(VoidPlayer player, BlockBreakEvent event);
}
