package de.polo.metropiacity.commands;

import de.polo.metropiacity.DataStorage.PlayerData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.Utils.Navigation;
import de.polo.metropiacity.Utils.PlayerManager;
import de.polo.metropiacity.Utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class OrtenCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        if (playerData.getFaction().equals("FBI")) {
            if (playerData.getVariable("ortet") == null) {
                if (args.length >= 1) {
                    playerData.setIntVariable("ortet", Integer.parseInt(args[0]));
                    playerData.setVariable("ortet", args[0]);
                    Utils.sendActionBar(player, "§9Orte Handy...");
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            PlayerData playerData1 = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
                            if (playerData1.getVariable("ortet") != null) {
                                for (Player players : Bukkit.getOnlinePlayers()) {
                                    if (PlayerManager.playerDataMap.get(players.getUniqueId().toString()).getNumber() == playerData1.getIntVariable("ortet")) {
                                        if (!PlayerManager.playerDataMap.get(players.getUniqueId().toString()).isFlightmode()) {
                                            Navigation.createNaviByCord(player, (int) players.getLocation().getX(), (int) players.getLocation().getY(), (int) players.getLocation().getZ());
                                            player.sendMessage("§8[§9Orten§8]§3 Navi geupdated.");
                                        } else {
                                            player.sendMessage("§8[§9Orten§8]§c Das Handy ist nicht mehr erreichbar.");
                                            playerData.setVariable("ortet", null);
                                            playerData.setIntVariable("ortet", null);
                                            this.cancel();
                                        }
                                    }
                                }
                            } else {
                                cancel();
                            }
                        }
                    }.runTaskTimer(Main.getInstance(), 20*2, 20*60);
                } else {
                    player.sendMessage(Main.error + "Syntax-Fehler: /orten [Nummer]");
                }
            } else {
                playerData.setVariable("ortet", null);
                playerData.setIntVariable("ortet", null);
                player.sendMessage("§8[§9Orten§8]§3 Du hast das Orten beendet.");
            }
        } else {
            player.sendMessage(Main.error_nopermission);
        }
        return false;
    }
}
