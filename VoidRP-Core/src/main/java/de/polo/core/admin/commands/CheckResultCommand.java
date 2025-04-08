package de.polo.core.admin.commands;

import de.polo.core.handler.CommandBase;
import de.polo.api.player.VoidPlayer;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.utils.Utils;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

import static de.polo.core.Main.playerManager;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */

@CommandBase.CommandMeta(name = "checkresult")
public class CheckResultCommand extends CommandBase {
    public CheckResultCommand(@NotNull CommandMeta meta) {
        super(meta);
    }

    @Override
    public void execute(@NotNull VoidPlayer player, @NotNull PlayerData playerData, @NotNull String[] args) throws Exception {
        if (playerData.getSecondaryTeam() == null || !playerData.getSecondaryTeam().equalsIgnoreCase("Event-Team")) {
            return;
        }
        for (PlayerData playerData1 : playerManager.getPlayers()) {
            if (playerData1.getClickedEventBlocks().isEmpty()) continue;
            if (playerData1.getVariable("event::startTime") == null) continue;
            if (playerData1.getVariable("event::endTime") == null) {
                long diff = Duration.between(playerData1.getVariable("event::startTime"), Utils.getTime()).toSeconds();
                player.sendMessage(Component.text("§8 ➥ §7" + playerData1.getPlayer().getName() + " | " + playerData1.getClickedEventBlocks().size() + " Blöcke | " + Utils.getTime((int) diff)));
            } else {
                long diff = Duration.between(playerData1.getVariable("event::startTime"), playerData1.getVariable("event::endTime")).toSeconds();
                player.sendMessage(Component.text("§8 ➥ §7" + playerData1.getPlayer().getName() + " | " + Utils.getTime((int) diff)));
            }
        }
    }
}
