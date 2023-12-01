package de.polo.metropiacity.listeners;

import de.polo.metropiacity.dataStorage.PlayerData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.utils.PlayerManager;
import de.polo.metropiacity.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.ArrayList;
import java.util.List;

public class CommandListener implements Listener {
    private final PlayerManager playerManager;
    private final Utils utils;
    public CommandListener(PlayerManager playerManager, Utils utils) {
        this.playerManager = playerManager;
        this.utils = utils;
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }
    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String msg = event.getMessage();
        String[] args = msg.split(" ");
        Player player = event.getPlayer();
        String[] nonBlockedCommands = {"support", "report", "help", "vote", "jailtime", "ad", "aduty"};
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        playerData.setIntVariable("afk", 0);
        if (playerData.isAFK()) {
            utils.setAFK(player, false);
        }
        if (Bukkit.getServer().getHelpMap().getHelpTopic(args[0]) == null) {
            event.setCancelled(true);
            player.sendMessage(Main.error + "Der Befehl §c" + msg + "§7 wurde nicht gefunden.");
            return;
        }
        if (playerData.isDead() && !playerData.isAduty()) {
            boolean performCommand = false;
            for (int i = 0; i < nonBlockedCommands.length; i++) {
                if (!Bukkit.getServer().getHelpMap().getHelpTopic(args[0]).toString().equalsIgnoreCase(nonBlockedCommands[i])) {
                    performCommand = true;
                }
            }
            if (!performCommand) {
                player.sendMessage("§7Du kannst diesen  Befehl aktuell nicht nutzen.");
                event.setCancelled(true);
            }
        }
        for (PlayerData playerData2 : playerManager.getPlayers()) {
            if (playerData2.getVariable("isSpec") != null) {
                if (playerData.getVariable("isSpec").equals(player.getUniqueId().toString())) {
                    Player targetplayer = Bukkit.getPlayer(playerData2.getUuid());
                    targetplayer.sendMessage("§8[§cSpec§8]§6 " + player.getName() + "§7 hat den Befehl \"§6" + msg + "§7\" ausgeführt.");
                }
            }
        }
    }
}
