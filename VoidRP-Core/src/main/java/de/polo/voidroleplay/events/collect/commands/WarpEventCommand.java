package de.polo.voidroleplay.events.collect.commands;

import de.polo.voidroleplay.handler.CommandBase;
import de.polo.voidroleplay.player.entities.VoidPlayer;
import de.polo.voidroleplay.storage.PlayerData;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static de.polo.voidroleplay.Main.locationManager;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */

@CommandBase.CommandMeta(name = "warpevent")
public class WarpEventCommand extends CommandBase {
    public WarpEventCommand(@NotNull CommandMeta meta) {
        super(meta);
    }

    @Override
    public void execute(@NotNull VoidPlayer player, @NotNull PlayerData playerData, @NotNull String[] args) throws Exception {
        if (locationManager.getDistanceBetweenCoords(player, "event") < 5) {
            locationManager.useLocation(player.getPlayer(), "toevent");
        } else if (locationManager.getDistanceBetweenCoords(player, "toevent") < 5) {
            locationManager.useLocation(player.getPlayer(), "event");
        }
    }
}
