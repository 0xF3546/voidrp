package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.manager.PlayerManager;
import de.polo.voidroleplay.utils.Utils;
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
        if (!playerData.getFaction().equals("FBI") && !playerData.getFaction().equalsIgnoreCase("ICA")) {
            player.sendMessage(Main.error_nopermission);
            return false;
        }
        if (playerData.getVariable("ortet") != null) {
            playerData.setVariable("ortet", null);
            playerData.setIntVariable("ortet", null);
            player.sendMessage("§8[§9Orten§8]§3 Du hast das Orten beendet.");
            return false;
        }
        if (args.length < 1) {
            player.sendMessage(Main.error + "Syntax-Fehler: /orten [Spieler]");
            return false;
        }
        if (player.getName().equalsIgnoreCase(args[0])) {
            player.sendMessage(Main.error + "Du kannst dich selbst nicht Orten.");
            return false;
        }
        Player targetplayer = Bukkit.getPlayer(args[0]);
        if (targetplayer == null) {
            player.sendMessage(Main.error + "Der Spieler wurde nicht gefunden.");
            return false;
        }
        playerData.setVariable("ortet", targetplayer);
        utils.sendActionBar(player, "§9Orte Handy...");
        new BukkitRunnable() {
            @Override
            public void run() {
                PlayerData playerData1 = playerManager.getPlayerData(player.getUniqueId());
                if (playerData1.getVariable("ortet") == null) {
                    cancel();
                    return;
                }
                if (!targetplayer.isOnline()) {
                    player.sendMessage(Main.error + "Es konnte keine Verbindung zum Handy hergestellt werden.");
                    return;
                }
                if (playerManager.getPlayerData(targetplayer.getUniqueId()).isFlightmode()) {
                    player.sendMessage("§8[§9Orten§8]§c Das Handy ist nicht mehr erreichbar.");
                    playerData.setVariable("ortet", null);
                    playerData.setIntVariable("ortet", null);
                    cancel();
                    return;
                }
                utils.navigationManager.createNaviByCord(player, (int) targetplayer.getLocation().getX(), (int) targetplayer.getLocation().getY(), (int) targetplayer.getLocation().getZ());
            }
        }.runTaskTimer(Main.getInstance(), 20 * 2, 20 * 60);
        return false;
    }
}
