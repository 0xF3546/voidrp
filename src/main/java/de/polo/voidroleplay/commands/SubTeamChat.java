package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.game.faction.staat.SubTeam;
import de.polo.voidroleplay.manager.PlayerManager;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.utils.Utils;
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
public class SubTeamChat implements CommandExecutor {
    private final PlayerManager playerManager;

    public SubTeamChat(PlayerManager playerManager) {
        this.playerManager = playerManager;

        Main.registerCommand("subteamchat", this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player);
        if (playerData.getSubTeam() == null || playerData.getFaction() == null) {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
            return false;
        }
        if (args.length < 1) {
            player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /subteamchat [Nachricht]");
            return false;
        }
        SubTeam team = playerData.getSubTeam();
        team.sendMessage("§8[§3" + team.getName() + "§8]§7 " + player.getName() + ": " + Utils.stringArrayToString(args));
        return false;
    }
}
