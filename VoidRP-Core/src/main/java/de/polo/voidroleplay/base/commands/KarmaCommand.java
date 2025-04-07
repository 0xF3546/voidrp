package de.polo.voidroleplay.base.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.player.services.impl.PlayerManager;
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
public class KarmaCommand implements CommandExecutor {
    private final PlayerManager playerManager;

    public KarmaCommand(PlayerManager playerManager) {
        this.playerManager = playerManager;
        Main.registerCommand("karma", this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player);
        player.sendMessage("§8[§3Karma§8]§b Du hast " + playerData.getKarma() + " Karma.");
        return false;
    }
}
