package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.handler.CommandBase;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.utils.Prefix;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static de.polo.voidroleplay.Main.locationManager;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */

@CommandBase.CommandMeta(name = "uranmine")
public class UranMineCommand extends CommandBase {
    public UranMineCommand(@NotNull CommandMeta meta) {
        super(meta);
    }

    @Override
    public void execute(@NotNull Player player, @NotNull PlayerData playerData, @NotNull String[] args) throws Exception {
        if (locationManager.getDistanceBetweenCoords(player, "uranmine") > 5) {
            player.sendMessage(Component.text(Prefix.ERROR + "Du bist nicht in der nähe der Uranmine."));
            return;
        }
    }
}
