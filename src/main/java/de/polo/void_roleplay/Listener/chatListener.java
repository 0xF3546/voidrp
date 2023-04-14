package de.polo.void_roleplay.Listener;

import de.polo.void_roleplay.Main;
import de.polo.void_roleplay.Utils.SupportManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class chatListener implements Listener {
    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = (Player) event.getPlayer();
        event.setCancelled(true);
        System.out.println(SupportManager.isInConnection(player));
        if (SupportManager.isInConnection(player)) {
            for (Player players : Bukkit.getOnlinePlayers()) {
                if (SupportManager.getConnection(player).equalsIgnoreCase(players.getUniqueId().toString())) {
                    players.sendMessage(Main.support_prefix + ChatColor.GOLD + player.getName() + "§8:§7 " + event.getMessage());
                    player.sendMessage(Main.support_prefix + ChatColor.GOLD + player.getName() + "§8:§7 " + event.getMessage());
                }
            }
        } else {
            for (Player players : Bukkit.getOnlinePlayers()) {
                if (player.getLocation().distance(players.getLocation()) <= 3) {
                    players.sendMessage(player.getDisplayName() + "§8:§f " + event.getMessage());
                } else if (player.getLocation().distance(players.getLocation()) <= 6) {
                    players.sendMessage(player.getDisplayName() + "§8:§7 " + event.getMessage());
                } else if (player.getLocation().distance(players.getLocation()) <= 12) {
                    players.sendMessage(player.getDisplayName() + "§8:§8 " + event.getMessage());
                }
            }
        }
    }
}
