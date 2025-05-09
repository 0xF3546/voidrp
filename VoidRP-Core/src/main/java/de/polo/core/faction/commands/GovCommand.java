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

public class GovCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final FactionManager factionManager;
    private final Utils utils;

    public GovCommand(PlayerManager playerManager, FactionManager factionManager, Utils utils) {
        this.playerManager = playerManager;
        this.factionManager = factionManager;
        this.utils = utils;
        Main.registerCommand("gov", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        String playerfac = factionManager.faction(player);
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (!playerManager.isInStaatsFrak(player)) {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
            return false;
        }
        if (playerData.getFactionGrade() < 4) {
            player.sendMessage(Prefix.ERROR + "Du musst mindestens Rang 4+ sein.");
            return false;
        }
        if (args.length < 1) {
            player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /gov [Nachricht]");
            return false;
        }
        Bukkit.broadcastMessage(" ");
        Bukkit.broadcastMessage("§7§m====§8[§" + factionManager.getFactionPrimaryColor(playerfac) + "§l" + factionManager.getFactionFullname(playerfac) + "§8]§7§m====");
        Bukkit.broadcastMessage(" ");
        Bukkit.broadcastMessage("§8➥§" + factionManager.getFactionSecondaryColor(playerfac) + " " + player.getName() + "§8: §7" + Utils.stringArrayToString(args));
        Bukkit.broadcastMessage(" ");
        Bukkit.broadcastMessage("§7§m====§8[§" + factionManager.getFactionPrimaryColor(playerfac) + "§l" + factionManager.getFactionFullname(playerfac) + "§8]§7§m====");
        Bukkit.broadcastMessage(" ");
        return false;
    }
}
