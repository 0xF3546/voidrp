package de.polo.voidroleplay.faction.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.faction.entity.Faction;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.faction.service.impl.FactionManager;
import de.polo.voidroleplay.player.services.impl.PlayerManager;
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
public class SetMOTDCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final FactionManager factionManager;

    public SetMOTDCommand(PlayerManager playerManager, FactionManager factionManager) {
        this.playerManager = playerManager;
        this.factionManager = factionManager;

        Main.registerCommand("setmotd", this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player);
        if (playerData.getFaction() == null) {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
            return false;
        }
        if (!playerData.isLeader()) {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
            return false;
        }
        if (args.length < 1) {
            player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /setmotd [MOTD]");
            return false;
        }
        Faction factionData = factionManager.getFactionData(playerData.getFaction());
        String motd = Utils.stringArrayToString(args);
        factionManager.setFactionMOTD(factionData.getId(), motd);
        factionManager.sendMessageToFaction(factionData.getName(), player.getName() + " hat die MOTD zu \"" + motd + "\" geändert.");
        return false;
    }
}
