package de.polo.core.commands;

import de.polo.core.Main;
import de.polo.core.game.base.extra.drop.Drop;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.utils.Prefix;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class ForceDropCommand implements CommandExecutor {
    private final PlayerManager playerManager;

    public ForceDropCommand(PlayerManager playerManager) {
        this.playerManager = playerManager;
        Main.registerCommand("forcedrop", this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player);
        if (playerData.getPermlevel() < 100) {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
            return false;
        }

        Drop drop = Main.gamePlay.spawnDrop();
        if (drop == null) {
            player.sendMessage(Prefix.ERROR + "drop konnte nicht gespawnt werden.");
            return false;
        }
        player.teleport(drop.location);
        return false;
    }
}
