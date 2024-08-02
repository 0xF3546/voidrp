package de.polo.voidroleplay.listeners;

import de.polo.voidroleplay.dataStorage.PhoneCall;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.Ticket;
import de.polo.voidroleplay.utils.PlayerManager;
import de.polo.voidroleplay.utils.Utils;
import de.polo.voidroleplay.game.events.SubmitChatEvent;
import de.polo.voidroleplay.utils.SupportManager;
import de.polo.voidroleplay.utils.playerUtils.ChatUtils;
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
                    Ticket ticket = supportManager.getTicket(player);
                    for (Player p : supportManager.getPlayersInTicket(ticket)) {
                        p.sendMessage(Main.support_prefix + ChatColor.GOLD + player.getName() + "§8:§7 " + event.getMessage());
                    }

            } else {
                if (!playerData.isDead()) {
                    if (utils.phoneUtils.isInCall(player)) {
                        PhoneCall call = utils.phoneUtils.getCall(player);
                        for (Player p : utils.phoneUtils.getPlayersInCall(call)) {
                            if (p != player) p.sendMessage("§8[§6Handy§8] " + ChatColor.GOLD + player.getName() + "§8:§7 " + event.getMessage());
                        }
                    }
                    String msg = event.getMessage();
                    String type = "sagt";

                    if (checkUppercasePercentage(msg)) {
                        msg = msg.toLowerCase();
                    }

                    if (msg.charAt(msg.length() - 1) == '?') {
                        type = "fragt";
                    }
                    /*if (msg.length() >= 4) {
                        String firstChar = String.valueOf(msg.charAt(0)).toUpperCase();
                        String restOfString = msg.substring(1).toLowerCase();
                        msg = firstChar + restOfString;
                    }*/
                    for (Player players : Bukkit.getOnlinePlayers()) {
                        if (player.getLocation().distance(players.getLocation()) <= 8) {
                            players.sendMessage("§8[§c" + playerData.getLevel() + "§8] §f" + player.getName() + " " + type + ":§f " + msg);
                        } else if (player.getLocation().distance(players.getLocation()) <= 15) {
                            players.sendMessage("§8[§c" + playerData.getLevel() + "§8] §7" + player.getName() + " " + type + "§7:§7 " + msg);
                        } else if (player.getLocation().distance(players.getLocation()) <= 28) {
                            players.sendMessage("§8[§c" + playerData.getLevel() + "§8] §8" + player.getName() + " " + type + "§8:§8 " + msg);
                        }
                    }
                    ChatUtils.LogMessage(msg, player.getUniqueId());
                } else {
                    player.sendMessage("§7Du bist bewusstlos.");
                }
            }
        } else {
            Bukkit.getScheduler().runTask(Main.plugin, () -> {
                Bukkit.getPluginManager().callEvent(new SubmitChatEvent(player, event.getMessage()));
                if (playerData.getVariable("chatblock") == null) return;
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

    private boolean checkUppercasePercentage(String msg) {
        int uppercaseCount = 0;
        for (char c : msg.toCharArray()) {
            if (Character.isUpperCase(c)) {
                uppercaseCount++;
            }
        }
        return ((double) uppercaseCount / msg.length()) >= 0.75;
    }
}
