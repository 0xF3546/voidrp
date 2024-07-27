package de.polo.voidroleplay.listeners;

import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.utils.PlayerManager;
import de.polo.voidroleplay.utils.Utils;
import de.polo.voidroleplay.utils.playerUtils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Arrays;
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
        List<String> nonBlockedCommands = Arrays.asList("support", "report", "help", "vote", "jailtime", "ad", "aduty");
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        playerData.setIntVariable("afk", 0);
        if (playerData.isAFK()) {
            utils.setAFK(player, false);
        }

        for (PlayerData playerData2 : playerManager.getPlayers()) {
            if (playerData2.getVariable("isSpec") != null) {
                if (playerData.getVariable("isSpec").equals(player.getUniqueId().toString())) {
                    Player targetplayer = Bukkit.getPlayer(playerData2.getUuid());
                    if (targetplayer == null) {
                        return;
                    }
                    targetplayer.sendMessage("§8[§cSpec§8]§6 " + player.getName() + "§7 hat den Befehl \"§6" + msg + "§7\" ausgeführt.");
                }
            }
        }

        if (Bukkit.getServer().getHelpMap().getHelpTopic(args[0]) == null) {
            event.setCancelled(true);
            player.sendMessage(Main.error + "Der Befehl §c" + msg + "§7 wurde nicht gefunden.");
            return;
        }
        if (playerData.isDead() && !playerData.isAduty()) {
            String command = args[0].substring(1); // Entferne das führende '/'
            if (!nonBlockedCommands.contains(command)) {
                player.sendMessage("§7Du kannst diesen Befehl aktuell nicht nutzen.");
                event.setCancelled(true);
                return;
            }
        }
        ChatUtils.LogCommand(msg, player.getUniqueId());
    }
}
