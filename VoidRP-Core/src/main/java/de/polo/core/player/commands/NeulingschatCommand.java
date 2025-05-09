package de.polo.core.player.commands;

import de.polo.core.Main;
import de.polo.core.handler.CommandBase;
import de.polo.api.player.VoidPlayer;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.utils.Utils;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */

@CommandBase.CommandMeta(name = "neulingschat", usage = "/neulingschat [Nachricht]")
public class NeulingschatCommand extends CommandBase {
    public NeulingschatCommand(@NotNull CommandMeta meta) {
        super(meta);
    }

    @Override
    public void execute(@NotNull VoidPlayer player, @NotNull PlayerData playerData, @NotNull String[] args) throws Exception {
        String PREFIX = "§8[§3Neulingschat§8]§7 ";
        if (playerData.getLevel() > 5 && playerData.getPermlevel() < 40) {
            player.sendMessage(Component.text(PREFIX + "Der Neulingschat ist nur bis Level 5 verfügbar."));
            return;
        }
        if (args.length < 1) {
            showSyntax(player);
            return;
        }
        String message = Utils.stringArrayToString(args);
        for (PlayerData p : Main.getPlayerManager().getPlayers()) {
            if (p == null) continue;
            if (p.getLevel() > 5 && p.getPermlevel() < 40) continue;
            p.getPlayer().sendMessage(PREFIX + (playerData.getPermlevel() >= 40 ? "§b" + player.getName() : player.getName()) + "§7: " + message);
        }
    }
}
