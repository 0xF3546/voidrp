package de.polo.voidroleplay.faction.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.storage.ServiceData;
import de.polo.voidroleplay.player.services.impl.PlayerManager;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.utils.StaatUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CloseServiceCommand implements CommandExecutor {
    private final PlayerManager playerManager;

    public CloseServiceCommand(PlayerManager playerManager) {
        this.playerManager = playerManager;
        Main.registerCommand("closeservice", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (playerData.getFaction().equals("Medic") || playerData.getFaction().equals("Polizei")) {
            if (args.length >= 1) {
                Player targetplayer = Bukkit.getPlayer(args[0]);
                if (targetplayer != null) {
                    ServiceData serviceData = StaatUtil.serviceDataMap.get(targetplayer.getUniqueId().toString());
                    if (serviceData != null) {
                        if (serviceData.getAcceptedByUuid().equals(player.getUniqueId().toString())) {
                            for (Player p : Bukkit.getOnlinePlayers()) {
                                PlayerData playersData = playerManager.getPlayerData(p);
                                if (playersData.getFaction() == null) continue;
                                if (playersData.getFaction().equals(playerData.getFaction())) {
                                    p.sendMessage("§8[§9Zentrale§8]§3 " + player.getName() + " hat den Service von " + targetplayer.getName() + " geschlossen.");
                                }
                            }
                            targetplayer.sendMessage("§8[§6Notruf§8]§c Dein Notruf wurde von " + player.getName() + " geschlossen.");
                            StaatUtil.serviceDataMap.remove(targetplayer.getUniqueId().toString());
                            PlayerData targetplayerData = playerManager.getPlayerData(targetplayer.getUniqueId());
                            targetplayerData.setVariable("service", null);
                        } else {
                            player.sendMessage(Prefix.ERROR + "Der Service wird nicht von dir bearbeitet.");
                        }
                    } else {
                        player.sendMessage(Prefix.ERROR + targetplayer.getName() + " hat keinen Service offen.");
                    }
                } else {
                    player.sendMessage(Prefix.ERROR + args[0] + " ist nicht online.");
                }
            } else {
                player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /closeservice [Spieler]");
            }
        } else {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
        }
        return false;
    }
}
