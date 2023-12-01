package de.polo.metropiacity.commands;

import de.polo.metropiacity.Main;
import de.polo.metropiacity.dataStorage.FactionData;
import de.polo.metropiacity.utils.FactionManager;
import de.polo.metropiacity.utils.PlayerManager;
import de.polo.metropiacity.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;

public class FraktionsChatCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final FactionManager factionManager;
    private final Utils utils;
    public FraktionsChatCommand(PlayerManager playerManager, FactionManager factionManager, Utils utils) {
        this.playerManager = playerManager;
        this.factionManager = factionManager;
        this.utils = utils;
        Main.registerCommand("fraktionschat", this);
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        String uuid = player.getUniqueId().toString();
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
}
