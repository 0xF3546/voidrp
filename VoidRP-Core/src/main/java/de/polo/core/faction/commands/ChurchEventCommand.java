package de.polo.core.faction.commands;

import de.polo.core.Main;
import de.polo.core.faction.service.impl.FactionManager;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.utils.Prefix;
import de.polo.core.utils.Utils;
import org.bukkit.Bukkit;
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
public class ChurchEventCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final FactionManager factionManager;

    public ChurchEventCommand(PlayerManager playerManager, FactionManager factionManager) {
        this.playerManager = playerManager;
        this.factionManager = factionManager;
        Main.registerCommand("event", this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player);
        if (!playerData.getFaction().equalsIgnoreCase("Kirche")) {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
            return false;
        }
        if (playerData.getFactionGrade() < 2) {
            player.sendMessage(Prefix.ERROR + "Dieser Befehl geht erst ab Rang 2!");
            return false;
        }
        Bukkit.broadcastMessage("§8[§6Kirche§8]§e " + factionManager.getRankName(playerData.getFaction(), playerData.getFactionGrade()) + " " + playerData.getFirstname() + " " + playerData.getLastname() + ": " + Utils.stringArrayToString(args));
        return false;
    }
}
