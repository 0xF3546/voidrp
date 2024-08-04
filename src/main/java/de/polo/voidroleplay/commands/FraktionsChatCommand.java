package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.FactionData;
import de.polo.voidroleplay.utils.*;
import de.polo.voidroleplay.utils.enums.RoleplayItem;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class FraktionsChatCommand implements CommandExecutor, TabCompleter {
    private final PlayerManager playerManager;
    private final FactionManager factionManager;
    private final Utils utils;
    public FraktionsChatCommand(PlayerManager playerManager, FactionManager factionManager, Utils utils) {
        this.playerManager = playerManager;
        this.factionManager = factionManager;
        this.utils = utils;
        Main.registerCommand("fraktionschat", this);
        Main.addTabCompeter("fraktionschat", this);
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        String uuid = player.getUniqueId().toString();
        if (ItemManager.getCustomItemCount(player, RoleplayItem.SMARTPHONE) < 1) {
            player.sendMessage(Prefix.ERROR + "Du hast kein Handy dabei!");
            return false;
        }
        if (factionManager.faction(player) != null) {
            if (args.length >= 1) {
                String msg = utils.stringArrayToString(args);
                String playerfac = factionManager.faction(player);
                FactionData factionData = factionManager.getFactionData(playerfac);
                for (Player players : Bukkit.getOnlinePlayers()) {
                    if (Objects.equals(factionManager.faction(players), playerfac)) {
                        players.sendMessage("§"+factionManager.getFactionPrimaryColor(playerfac) + factionManager.getPlayerFactionRankName(player) + " " + player.getName() + "§8:§7 " + msg);
                    }
                }
            } else {
                player.sendMessage(Main.error + "Syntax-Error: /fraktionschat [Nachricht]");
            }
        } else {
            player.sendMessage(Main.error_nopermission);
        }
        return false;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return null;
    }
}
