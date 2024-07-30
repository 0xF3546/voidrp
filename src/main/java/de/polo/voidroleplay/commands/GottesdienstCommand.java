package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.utils.FactionManager;
import de.polo.voidroleplay.utils.PlayerManager;
import de.polo.voidroleplay.utils.Prefix;
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
public class GottesdienstCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final FactionManager factionManager;
    public GottesdienstCommand(PlayerManager playerManager, FactionManager factionManager) {
        this.playerManager = playerManager;
        this.factionManager = factionManager;
        Main.registerCommand("gottesdienst", this);
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player);
        if (!playerData.getFaction().equalsIgnoreCase("Kirche")) {
            player.sendMessage(Prefix.error_nopermission);
            return false;
        }
        if (playerData.getFactionGrade() < 4) {
            player.sendMessage(Prefix.ERROR + "Dieser Befehl geht erst ab Rang 4!");
            return false;
        }
        Bukkit.broadcastMessage("§8[§6Gottesdienst§8]§e " + factionManager.getRankName(playerData.getFaction(), playerData.getFactionGrade()) + " " + playerData.getFirstname() + " " + playerData.getLastname() + " ruft zum Gottesdienst in der Kirche auf!");
        return false;
    }
}
