package de.polo.core.base.commands;

import de.polo.api.utils.enums.Prefix;
import de.polo.api.player.VoidPlayer;
import de.polo.core.Main;
import de.polo.core.handler.CommandBase;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.utils.Utils;
import de.polo.core.utils.player.PlayerPacket;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static de.polo.core.Main.utils;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
@CommandBase.CommandMeta(
        name = "afk"
)
public class AFKCommand extends CommandBase {

    public AFKCommand(@NotNull CommandMeta meta) {
        super(meta);
    }

    @Override
    public void execute(@NotNull VoidPlayer player, @NotNull PlayerData playerData, @NotNull String[] args) throws Exception {
        if (player.isAFK()) {
            player.sendMessage("Du bist bereits AFK.", Prefix.ERROR);
            return;
        }
        utils.setAFK(player.getPlayer(), true);
        PlayerPacket packet = new PlayerPacket(player.getPlayer());
        packet.renewPacket();
    }
}
