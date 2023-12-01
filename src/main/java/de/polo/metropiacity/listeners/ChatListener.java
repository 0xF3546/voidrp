package de.polo.metropiacity.listeners;

import de.polo.metropiacity.dataStorage.PlayerData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.utils.PhoneUtils;
import de.polo.metropiacity.utils.PlayerManager;
import de.polo.metropiacity.utils.Utils;
import de.polo.metropiacity.utils.events.SubmitChatEvent;
import de.polo.metropiacity.utils.SupportManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {
    private final PlayerManager playerManager;
    private final SupportManager supportManager;
    private final Utils utils;
    public ChatListener(PlayerManager playerManager, SupportManager supportManager, Utils utils) {
        this.playerManager = playerManager;
        this.supportManager = supportManager;
        this.utils = utils;
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }
    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        playerData.setIntVariable("afk", 0);
        if (playerData.isAFK()) {
            utils.setAFK(player, false);
        }
        event.setCancelled(true);
        if (playerData.getVariable("chatblock") == null) {
            if (supportManager.isInConnection(player)) {
                for (Player players : Bukkit.getOnlinePlayers()) {
                    if (supportManager.getConnection(player).equalsIgnoreCase(players.getUniqueId().toString())) {
                        players.sendMessage(Main.support_prefix + ChatColor.GOLD + player.getName() + "§8:§7 " + event.getMessage());
                        player.sendMessage(Main.support_prefix + ChatColor.GOLD + player.getName() + "§8:§7 " + event.getMessage());
                    }
                }
            } else {
                if (!playerData.isDead()) {
                    if (PhoneUtils.isInConnection(player)) {
                        for (Player players : Bukkit.getOnlinePlayers()) {
                            if (PhoneUtils.getConnection(player).equalsIgnoreCase(players.getUniqueId().toString())) {
                                players.sendMessage("§8[§6Handy§8]§e " + player.getName() + "§8:§7 " + event.getMessage());
                            }
                        }
                    }
                    String msg = event.getMessage();
                    String type = "sagt";
                    if (msg.charAt(msg.length() - 1) == '?') {
                        type = "fragt";
                    }
                    if (msg.length() >= 4) {
                        String firstChar = String.valueOf(msg.charAt(0)).toUpperCase();
                        String restOfString = msg.substring(1).toLowerCase();
                        msg = firstChar + restOfString;
                    }
                    for (Player players : Bukkit.getOnlinePlayers()) {
                        int distance = (int) player.getLocation().distance(players.getLocation());
                        if (player.getLocation().distance(players.getLocation()) <= 8) {
                            players.sendMessage("§8[§2" + distance + "§8] §f" + player.getName() + " " + type + ":§f " + msg);
                        } else if (player.getLocation().distance(players.getLocation()) <= 15) {
                            players.sendMessage("§8[§2" + distance + "§8] §7" + player.getName() + " " + type + "§8:§7 " + msg);
                        } else if (player.getLocation().distance(players.getLocation()) <= 28) {
                            players.sendMessage("§8[§2" + distance + "§8] §8" + player.getName() + " " + type + "§8:§8 " + msg);
                        }
                    }
                } else {
                    player.sendMessage("§7Du bist bewusstlos.");
                }
            }
        } else {
            Bukkit.getScheduler().runTask(Main.plugin, () -> {
                Bukkit.getPluginManager().callEvent(new SubmitChatEvent(player, event.getMessage()));
                switch (playerData.getVariable("chatblock").toString()) {
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
