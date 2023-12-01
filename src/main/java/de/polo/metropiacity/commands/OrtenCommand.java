package de.polo.metropiacity.commands;

import de.polo.metropiacity.dataStorage.PlayerData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.utils.Navigation;
import de.polo.metropiacity.utils.PlayerManager;
import de.polo.metropiacity.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class OrtenCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final Utils utils;
    public OrtenCommand(PlayerManager playerManager, Utils utils) {
        this.playerManager = playerManager;
        this.utils = utils;
        Main.registerCommand("orten", this);
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (playerData.getFaction().equals("FBI")) {
            if (playerData.getVariable("ortet") == null) {
                if (args.length >= 1) {
                    playerData.setIntVariable("ortet", Integer.parseInt(args[0]));
                    playerData.setVariable("ortet", args[0]);
                    utils.sendActionBar(player, "§9Orte Handy...");
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            PlayerData playerData1 = playerManager.getPlayerData(player.getUniqueId());
                            if (playerData1.getVariable("ortet") != null) {
                                for (Player players : Bukkit.getOnlinePlayers()) {
                                    if (playerManager.getPlayerData(players.getUniqueId()).getNumber() == playerData1.getIntVariable("ortet")) {
                                        if (!playerManager.getPlayerData(players.getUniqueId()).isFlightmode()) {
                                            utils.navigation.createNaviByCord(player, (int) players.getLocation().getX(), (int) players.getLocation().getY(), (int) players.getLocation().getZ());
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
