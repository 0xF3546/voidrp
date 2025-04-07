package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.faction.entity.Faction;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.faction.service.impl.FactionManager;
import de.polo.voidroleplay.player.services.impl.PlayerManager;
import de.polo.voidroleplay.utils.PhoneUtils;
import de.polo.voidroleplay.utils.Prefix;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DepartmentChatCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final FactionManager factionManager;

    public DepartmentChatCommand(PlayerManager playerManager, FactionManager factionManager) {
        this.playerManager = playerManager;
        this.factionManager = factionManager;

        Main.registerCommand("departmentchat", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (playerData.getFaction() == null) {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
            return false;
        }
        if (!PhoneUtils.hasPhone(player)) {
            player.sendMessage(PhoneUtils.ERROR_NO_PHONE);
            return false;
        }
        if (playerData.isFlightmode()) {
            player.sendMessage(PhoneUtils.ERROR_FLIGHTMODE);
            return false;
        }
        Faction factionData = factionManager.getFactionData(playerData.getFaction());
        if (args.length >= 1) {
            StringBuilder msg = new StringBuilder(args[0]);
            for (int i = 1; i < args.length; i++) {
                msg.append(" ").append(args[i]);
            }
            for (Player players : Bukkit.getOnlinePlayers()) {
                if (playerManager.getPlayerData(player).getFaction() != null) {
                    if (playerManager.isInStaatsFrak(player)) {
                        if (playerManager.isInStaatsFrak(players)) {
                            players.sendMessage("§c" + playerData.getFaction() + " " + player.getName() + "§8:§7 " + msg);
                        }
                    }
                }
            }
            Faction alliance = Main.getInstance().gamePlay.alliance.getAlliance(playerData.getFaction());
            if (alliance == null) return false;
            factionManager.sendCustomMessageToFactions("§c" + playerData.getFaction() + " " + player.getName() + "§8:§7 " + msg, alliance.getName(), playerData.getFaction());
        } else {
            player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /departmentchat [Nachricht]");
        }

        return false;
    }
}
