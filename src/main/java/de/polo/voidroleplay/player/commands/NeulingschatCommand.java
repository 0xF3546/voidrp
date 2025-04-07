package de.polo.voidroleplay.player.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.handler.CommandBase;
import de.polo.voidroleplay.player.entities.VoidPlayer;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.utils.Utils;
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
        for (PlayerData p : Main.getInstance().getPlayerManager().getPlayers()) {
            if (p == null) continue;
            if (p.getLevel() > 5 && p.getPermlevel() < 40) continue;
            p.getPlayer().sendMessage(PREFIX + (playerData.getPermlevel() >= 40 ? "§b" + player.getName() : player.getName()) + "§7: " + message);
        }
    }
}
