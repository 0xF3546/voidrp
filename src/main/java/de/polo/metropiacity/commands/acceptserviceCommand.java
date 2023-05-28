package de.polo.metropiacity.commands;

import de.polo.metropiacity.DataStorage.PlayerData;
import de.polo.metropiacity.DataStorage.ServiceData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.Utils.Navigation;
import de.polo.metropiacity.Utils.PlayerManager;
import de.polo.metropiacity.Utils.StaatUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class acceptserviceCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        if (playerData.getFaction().equals("Medic") || playerData.getFaction().equals("Polizei")) {
            if (args.length >= 1) {
                Player targetplayer = Bukkit.getPlayer(args[0]);
                if (targetplayer.isOnline()) {
                    ServiceData serviceData = StaatUtil.serviceDataMap.get(targetplayer.getUniqueId().toString());
                    if (serviceData != null) {
                        if (serviceData.getAcceptedByUuid() == null) {
                            serviceData.setAcceptedByUuid(player.getUniqueId().toString());
                            Navigation.createNaviByCord(player, (int) serviceData.getLocation().getX(), (int) serviceData.getLocation().getY(), (int) serviceData.getLocation().getZ());
                            for (Player p : Bukkit.getOnlinePlayers()) {
                                if (PlayerManager.playerDataMap.get(p.getUniqueId().toString()).getFaction().equals(playerData.getFaction())) {
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
