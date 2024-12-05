package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.game.base.extra.Drop.Drop;
import de.polo.voidroleplay.utils.PlayerManager;
import de.polo.voidroleplay.utils.Prefix;
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

        Drop drop = Main.getInstance().gamePlay.spawnDrop();
        if (drop == null) {
            player.sendMessage(Prefix.ERROR + "Drop konnte nicht gespawnt werden.");
            return false;
        }
        player.teleport(drop.location);
        return false;
    }
}
