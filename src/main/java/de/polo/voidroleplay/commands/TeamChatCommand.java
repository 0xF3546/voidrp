package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.manager.PlayerManager;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TeamChatCommand implements CommandExecutor, TabCompleter {
    private final PlayerManager playerManager;
    private final Utils utils;

    public TeamChatCommand(PlayerManager playerManager, Utils utils) {
        this.playerManager = playerManager;
        this.utils = utils;
        Main.registerCommand("teamchat", this);
        Main.addTabCompeter("teamchat", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        String uuid = player.getUniqueId().toString();
        if (playerManager.getPlayerData(player.getUniqueId()).getPermlevel() < 50) {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
            return false;
        }
        if (args.length < 1) {
            player.sendMessage(Prefix.ADMIN_ERROR + "Syntax-Error: /teamchat [Nachricht]");
            return false;
        }
        String msg = Utils.stringArrayToString(args);
        for (Player players : Bukkit.getOnlinePlayers()) {
            if (playerManager.getPlayerData(players.getUniqueId()).getPermlevel() >= 50) {
                players.sendMessage(Prefix.ADMIN + "§c" + playerManager.rang(player) + " " + player.getName() + "§8:§7 " + msg);
            }
        }
        return false;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return null;
    }
}
