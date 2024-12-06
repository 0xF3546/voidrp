package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.dataStorage.ServiceData;
import de.polo.voidroleplay.manager.PlayerManager;
import de.polo.voidroleplay.utils.StaatUtil;
import de.polo.voidroleplay.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AcceptServiceCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final Utils utils;

    public AcceptServiceCommand(PlayerManager playerManager, Utils utils) {
        this.playerManager = playerManager;
        this.utils = utils;
        Main.registerCommand("acceptservice", this);
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
                        if (serviceData.getAcceptedByUuid() == null) {
                            serviceData.setAcceptedByUuid(player.getUniqueId().toString());
                            utils.navigationManager.createNaviByCord(player, (int) serviceData.getLocation().getX(), (int) serviceData.getLocation().getY(), (int) serviceData.getLocation().getZ());
                            for (Player p : Bukkit.getOnlinePlayers()) {
                                PlayerData playersData = playerManager.getPlayerData(p);
                                if (playersData.getFaction() == null) continue;
                                if (playersData.getFaction().equals(playerData.getFaction())) {
                                    p.sendMessage("§8[§9Zentrale§8]§3 " + player.getName() + " hat den Service von " + targetplayer.getName() + " angenommen [" + (int) player.getLocation().distance(targetplayer.getLocation()) + "m].");
                                }
                            }
                            targetplayer.sendMessage("§8[§6Notruf§8]§a Dein Notruf wird von " + player.getName() + " bearbeitet.");
                        } else {
                            player.sendMessage(Main.error + "Der Service wird bereits bearbeitet.");
                        }
                    } else {
                        player.sendMessage(Main.error + targetplayer.getName() + " hat keinen Service offen.");
                    }
                } else {
                    player.sendMessage(Main.error + args[0] + " ist nicht online.");
                }
            } else {
                player.sendMessage(Main.error + "Syntax-Fehler: /acceptservice [Spieler]");
            }
        } else {
            player.sendMessage(Main.error_nopermission);
        }
        return false;
    }
}
