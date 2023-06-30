package de.polo.metropiacity.Listener;

import de.polo.metropiacity.DataStorage.PlayerData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.Utils.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.UUID;

public class commandListener implements Listener {
    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String msg = event.getMessage();
        String[] args = msg.split(" ");
        Player player = event.getPlayer();
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        if (playerData.isDead() && playerData.getPermlevel() <= 40) {
            System.out.println(args[0]);
            player.sendMessage("§7Du kannst diesen Befehl aktuell nicht nutzen.");
            event.setCancelled(true);
        } else {
            if (Bukkit.getServer().getHelpMap().getHelpTopic(args[0]) == null) {
                event.setCancelled(true);
                player.sendMessage(Main.error + "Der Befehl §c" + msg + "§7 wurde nicht gefunden.");
            }
        }
        for (PlayerData playerData2 : PlayerManager.playerDataMap.values()) {
            if (playerData2.getVariable("isSpec") != null) {
                Player targetplayer = Bukkit.getPlayer(playerData2.getUuid());
                targetplayer.sendMessage("§8[§cSpec§8]§6 " + player.getName() + "§7 hat den Befehl \"§6" + msg + "§7\" ausgeführt.");
            }
        }
    }
}
