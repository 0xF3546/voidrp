package de.polo.void_roleplay.commands;

import de.polo.void_roleplay.DataStorage.PlayerData;
import de.polo.void_roleplay.DataStorage.ServiceData;
import de.polo.void_roleplay.Main;
import de.polo.void_roleplay.Utils.Navigation;
import de.polo.void_roleplay.Utils.PlayerManager;
import de.polo.void_roleplay.Utils.StaatUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class closeserviceCommand implements CommandExecutor {
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
                        if (serviceData.getAcceptedByUuid().equals(player.getUniqueId().toString())) {
                            for (Player p : Bukkit.getOnlinePlayers()) {
                                if (PlayerManager.playerDataMap.get(p.getUniqueId().toString()).getFaction().equals(playerData.getFaction())) {
                                    p.sendMessage("§8[§9Zentrale§8]§3 " + player.getName() + " hat den Service von " + targetplayer.getName() + " geschlossen.");
                                }
                            }
                            targetplayer.sendMessage("§8[§6Notruf§8]§c Dein Notruf wurde von " + player.getName() + " geschlossen.");
                            StaatUtil.serviceDataMap.remove(targetplayer.getUniqueId().toString());
                            PlayerData targetplayerData = PlayerManager.playerDataMap.get(targetplayer.getUniqueId().toString());
                            targetplayerData.setVariable("service", null);
                        } else {
                            player.sendMessage(Main.error + "Der Service wird nicht von dir bearbeitet.");
                        }
                    } else {
                        player.sendMessage(Main.error + targetplayer.getName() + " hat keinen Service offen.");
                    }
                } else {
                    player.sendMessage(Main.error + args[0] + " ist nicht online.");
                }
            } else {
                player.sendMessage(Main.error + "Syntax-Fehler: /closeservice [Spieler]");
            }
        } else {
            player.sendMessage(Main.error_nopermission);
        }
        return false;
    }
}
