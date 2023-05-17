package de.polo.void_roleplay.Listener;

import de.polo.void_roleplay.DataStorage.PlayerData;
import de.polo.void_roleplay.Main;
import de.polo.void_roleplay.Utils.PhoneUtils;
import de.polo.void_roleplay.Utils.PlayerManager;
import de.polo.void_roleplay.Utils.SubmitChatEvent;
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
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        event.setCancelled(true);
        if (playerData.getVariable("chatblock") == null) {
            if (SupportManager.isInConnection(player)) {
                for (Player players : Bukkit.getOnlinePlayers()) {
                    if (SupportManager.getConnection(player).equalsIgnoreCase(players.getUniqueId().toString())) {
                        players.sendMessage(Main.support_prefix + ChatColor.GOLD + player.getName() + "§8:§7 " + event.getMessage());
                        player.sendMessage(Main.support_prefix + ChatColor.GOLD + player.getName() + "§8:§7 " + event.getMessage());
                    }
                }
            } else {
                if (PhoneUtils.isInConnection(player)) {
                    for (Player players : Bukkit.getOnlinePlayers()) {
                        if (PhoneUtils.getConnection(player).equalsIgnoreCase(players.getUniqueId().toString())) {
                            players.sendMessage("§6Handy§8 »§e " + player.getName() + "§8:§7 " + event.getMessage());
                        }
                    }
                }
                for (Player players : Bukkit.getOnlinePlayers()) {
                    if (player.getLocation().distance(players.getLocation()) <= 5) {
                        players.sendMessage("§f" + player.getName() + " sagt§8:§f " + event.getMessage());
                    } else if (player.getLocation().distance(players.getLocation()) <= 9) {
                        players.sendMessage("§7" + player.getName() + " sagt§8:§7 " + event.getMessage());
                    } else if (player.getLocation().distance(players.getLocation()) <= 16) {
                        players.sendMessage("§8" + player.getName() + " sagt§8:§8 " + event.getMessage());
                    }
                }
            }
        } else {
            Bukkit.getScheduler().runTask(Main.plugin, () -> {
                Bukkit.getPluginManager().callEvent(new SubmitChatEvent(player, event.getMessage()));
                switch (playerData.getVariable("chatblock")) {
                    case "firstname":
                        playerData.setVariable("einreise_firstname", event.getMessage());
                        player.sendMessage(Main.prefix + "Dein Vorname lautet nun: " + event.getMessage() + " §8-§7 Klicke den NPC wieder an!");
                        playerData.setVariable("chatblock", null);
                        break;
                    case "lastname":
                        playerData.setVariable("einreise_lastname", event.getMessage());
                        player.sendMessage(Main.prefix + "Dein Nachname lautet nun: " + event.getMessage() + " §8-§7 Klicke den NPC wieder an!");
                        playerData.setVariable("chatblock", null);
                        break;
                    case "dob":
                        playerData.setVariable("einreise_dob", event.getMessage());
                        player.sendMessage(Main.prefix + "Dein Geburtstag lautet nun: " + event.getMessage() + " §8-§7 Klicke den NPC wieder an!");
                        playerData.setVariable("chatblock", null);
                        break;
                    default:
                        playerData.setVariable("chatblock", null);
                        break;
                }
            });
        }
    }
}
