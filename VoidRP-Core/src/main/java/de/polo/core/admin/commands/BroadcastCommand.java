package de.polo.core.admin.commands;

import de.polo.core.Main;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.utils.Prefix;
import de.polo.core.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BroadcastCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final Utils utils;

    public BroadcastCommand(PlayerManager playerManager, Utils utils) {
        this.playerManager = playerManager;
        this.utils = utils;
        Main.registerCommand("announce", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (playerData.getPermlevel() < 70) {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
            return false;
        }
        if (!playerData.isAduty()) {
            player.sendMessage(Prefix.ERROR + "Du bist nicht im Admindienst!");
            return false;
        }
        if (args.length < 1) {
            player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /announce [Nachricht]");
            return false;
        }
        Bukkit.broadcastMessage(" ");
        Bukkit.broadcastMessage("§7§m====§8[§c§lAnkündigung§8]§7§m====");
        Bukkit.broadcastMessage(" ");
        Bukkit.broadcastMessage("§8➥§c " + player.getName() + "§8: §7" + Utils.stringArrayToString(args));
        Bukkit.broadcastMessage(" ");
        Bukkit.broadcastMessage("§7§m====§8[§c§lAnkündigung§8]§7§m====");
        Bukkit.broadcastMessage(" ");
        return false;
    }
}
