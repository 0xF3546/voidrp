package de.polo.void_roleplay.commands;

import de.polo.void_roleplay.DataStorage.PlayerData;
import de.polo.void_roleplay.Main;
import de.polo.void_roleplay.Utils.Navigation;
import de.polo.void_roleplay.Utils.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class ortenCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        if (playerData.getFaction().equals("FBI")) {
            if (playerData.getVariable("ortet") == null) {
                if (args.length >= 1) {
                    playerData.setIntVariable("ortet", Integer.parseInt(args[0]));
                    playerData.setVariable("ortet", args[0]);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            PlayerData playerData1 = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
                            if (playerData1.getVariable("ortet") != null) {
                                for (Player players : Bukkit.getOnlinePlayers()) {
                                    if (PlayerManager.playerDataMap.get(players.getUniqueId().toString()).getNumber() == playerData1.getIntVariable("ortet")) {
                                        Navigation.createNaviByCord(player, (int) players.getLocation().getX(), (int) players.getLocation().getY(), (int) players.getLocation().getZ());
                                        player.sendMessage("§8[§9Orten§8]§3 Navi geupdated.");
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
