package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.game.base.extra.Storage;
import de.polo.voidroleplay.manager.PlayerManager;
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
public class OpenInvCommand implements CommandExecutor {
    private final PlayerManager playerManager;

    public OpenInvCommand(PlayerManager playerManager) {
        this.playerManager = playerManager;
        Main.registerCommand("openinv", this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player);
        if (playerData.getPermlevel() < 80) {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
            return false;
        }
        if (args.length < 1) {
            player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /openinv [InvID]");
            return false;
        }
        try {
            int id = Integer.parseInt(args[0]);
            Storage s = Storage.getStorageById(id);
            if (s == null) {
                player.sendMessage(Prefix.ERROR + "Das Storage wurde nicht gefunden.");
                return false;
            }
            s.open(player);
        } catch (Exception ex) {
            player.sendMessage(Prefix.ERROR + "Die ID muss numerisch sein!");
        }
        return false;
    }
}
