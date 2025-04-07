package de.polo.voidroleplay.jobs.commands;

import de.polo.voidroleplay.handler.CommandBase;
import de.polo.voidroleplay.jobs.enums.MiniJob;
import de.polo.voidroleplay.player.entities.VoidPlayer;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.utils.enums.Prefix;
import org.jetbrains.annotations.NotNull;

import static de.polo.voidroleplay.Main.locationService;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
@CommandBase.CommandMeta(
        name = "sewercleaner",
        usage = "/sewercleaner"
)

public class SewerCleanerCommand extends CommandBase {
    public SewerCleanerCommand(@NotNull CommandMeta meta) {
        super(meta);
    }

    @Override
    public void execute(@NotNull VoidPlayer player, @NotNull PlayerData playerData, @NotNull String[] args) throws Exception {
        if (locationService.getDistanceBetweenCoords(player, "sewercleaner") > 5) {
            player.sendMessage("Du bist nicht in der Nähe des Abwasserkanals.", Prefix.ERROR);
            return;
        }
        if (player.getMiniJob() != null) {
            player.sendMessage("Du hast bereits einen Minijob.", Prefix.ERROR);
            return;
        }
        player.setMiniJob(MiniJob.SEWER_CLEANER);
    }
}
