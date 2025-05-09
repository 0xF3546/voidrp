package de.polo.core.faction.commands;

import de.polo.core.Main;
import de.polo.core.faction.entity.Faction;
import de.polo.core.faction.service.impl.FactionManager;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.utils.PhoneUtils;
import de.polo.core.utils.Prefix;
import de.polo.core.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class FraktionsChatCommand implements CommandExecutor, TabCompleter {
    private final PlayerManager playerManager;
    private final FactionManager factionManager;
    private final Utils utils;

    public FraktionsChatCommand(PlayerManager playerManager, FactionManager factionManager, Utils utils) {
        this.playerManager = playerManager;
        this.factionManager = factionManager;
        this.utils = utils;
        Main.registerCommand("fraktionschat", this);
        Main.addTabCompleter("fraktionschat", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        String uuid = player.getUniqueId().toString();
        if (!PhoneUtils.hasPhone(player)) {
            player.sendMessage(PhoneUtils.ERROR_NO_PHONE);
            return false;
        }
        PlayerData playerData = playerManager.getPlayerData(player);
        if (playerData.isFlightmode()) {
            player.sendMessage(PhoneUtils.ERROR_FLIGHTMODE);
            return false;
        }
        if (factionManager.faction(player) != null) {
            if (args.length >= 1) {
                String msg = Utils.stringArrayToString(args);
                String playerfac = factionManager.faction(player);
                Faction factionData = factionManager.getFactionData(playerfac);
                for (Player players : Bukkit.getOnlinePlayers()) {
                    if (Objects.equals(factionManager.faction(players), playerfac)) {
                        players.sendMessage(factionData.getChatColor() + factionManager.getPlayerFactionRankName(player) + " " + player.getName() + "§8:§7 " + msg);
                    }
                }
            } else {
                player.sendMessage(Prefix.ERROR + "Syntax-Error: /fraktionschat [Nachricht]");
            }
        } else {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
        }
        return false;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return null;
    }
}
