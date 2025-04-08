package de.polo.core.faction.commands;

import de.polo.core.Main;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.storage.ServiceData;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.utils.Prefix;
import de.polo.core.utils.StaatUtil;
import de.polo.core.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static de.polo.core.Main.navigationService;

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
                            navigationService.createNaviByCord(player, (int) serviceData.getLocation().getX(), (int) serviceData.getLocation().getY(), (int) serviceData.getLocation().getZ());
                            for (Player p : Bukkit.getOnlinePlayers()) {
                                PlayerData playersData = playerManager.getPlayerData(p);
                                if (playersData.getFaction() == null) continue;
                                if (playersData.getFaction().equals(playerData.getFaction())) {
                                    p.sendMessage("§8[§9Zentrale§8]§3 " + player.getName() + " hat den Service von " + targetplayer.getName() + " angenommen [" + (int) player.getLocation().distance(targetplayer.getLocation()) + "m].");
                                }
                            }
                            targetplayer.sendMessage("§8[§6Notruf§8]§a Dein Notruf wird von " + player.getName() + " bearbeitet.");
                        } else {
                            player.sendMessage(Prefix.ERROR + "Der Service wird bereits bearbeitet.");
                        }
                    } else {
                        player.sendMessage(Prefix.ERROR + targetplayer.getName() + " hat keinen Service offen.");
                    }
                } else {
                    player.sendMessage(Prefix.ERROR + args[0] + " ist nicht online.");
                }
            } else {
                player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /acceptservice [Spieler]");
            }
        } else {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
        }
        return false;
    }
}
