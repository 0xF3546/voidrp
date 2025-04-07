package de.polo.voidroleplay.listeners;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.game.events.SubmitChatEvent;
import de.polo.voidroleplay.manager.PlayerManager;
import de.polo.voidroleplay.manager.SupportManager;
import de.polo.voidroleplay.storage.PhoneCall;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.storage.Ticket;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.utils.Utils;
import de.polo.voidroleplay.utils.player.ChatUtils;
import de.polo.voidroleplay.utils.player.PlayerPacket;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.List;

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

        resetAFKStatus(player, playerData);
        event.setCancelled(true);

        if (playerData.getVariable("chatblock") == null) {
            handleRegularChat(event, player, playerData);
        } else {
            handleChatBlock(event, player, playerData);
        }
    }

    private void resetAFKStatus(Player player, PlayerData playerData) {
        playerData.setIntVariable("afk", 0);
        if (playerData.isAFK()) {
            utils.setAFK(player, false);
            PlayerPacket packet = new PlayerPacket(player);
            packet.renewPacket();
        }
    }

    private void handleRegularChat(AsyncPlayerChatEvent event, Player player, PlayerData playerData) {
        if (supportManager.isInAcceptedTicket(player)) {
            handleSupportChat(event, player);
        } else if (!playerData.isDead()) {
            handleGameplayChat(event, player, playerData);
        } else {
            player.sendMessage("§7Du bist bewusstlos.");
        }
    }

    private void handleSupportChat(AsyncPlayerChatEvent event, Player player) {
        Ticket ticket = supportManager.getTicket(player);
        List<Player> participants = supportManager.getPlayersInTicket(ticket);

        participants.forEach(p -> {
            if (p != null) {
                p.sendMessage(Prefix.SUPPORT + ChatColor.GOLD + player.getName() + "§8:§7 " + event.getMessage());
            }
        });
    }

    private void handleGameplayChat(AsyncPlayerChatEvent event, Player player, PlayerData playerData) {
        handleLocalChat(event, player, playerData);
        if (utils.phoneUtils.isInCall(player)) {
            handlePhoneChat(event, player);
        }
    }

    private void handlePhoneChat(AsyncPlayerChatEvent event, Player player) {
        PhoneCall call = utils.phoneUtils.getCall(player);
        List<Player> participants = utils.phoneUtils.getPlayersInCall(call);

        participants.forEach(p -> {
            if (p != null && p != player) {
                p.sendMessage("§8[§6Handy§8] " + ChatColor.GOLD + player.getName() + "§8:§7 " + event.getMessage());
            }
        });
    }

    private void handleLocalChat(AsyncPlayerChatEvent event, Player player, PlayerData playerData) {
        String message = formatMessage(event.getMessage());
        String type = determineMessageType(message);
        String playerName = getPlayerName(player);

        Bukkit.getOnlinePlayers().forEach(onlinePlayer -> {
            if (isNearby(player, onlinePlayer)) {
                sendLocalizedMessage(player, playerData, onlinePlayer, playerName, type, message);
            }
        });

        ChatUtils.logMessage(message, player.getUniqueId());
    }

    private String formatMessage(String message) {
        if (checkUppercasePercentage(message)) {
            message = message.toLowerCase();
        }
        return message;
    }

    private String determineMessageType(String message) {
        return message.endsWith("?") ? "fragt" : "sagt";
    }

    private String getPlayerName(Player player) {
        return Main.getInstance().gamePlay.getMaskState(player) != null ? "Maskierter" : player.getName();
    }

    private boolean isNearby(Player source, Player target) {
        return target.getWorld().equals(source.getWorld()) && source.getLocation().distance(target.getLocation()) <= 28;
    }

    private void sendLocalizedMessage(Player source, PlayerData playerData, Player target, String name, String type, String message) {
        double distance = source.getLocation().distance(target.getLocation());
        ChatColor color = distance <= 8 ? ChatColor.WHITE : distance <= 15 ? ChatColor.GRAY : ChatColor.DARK_GRAY;
        target.sendMessage("§8[§c" + playerData.getLevel() + "§8] " + color + name + " " + type + ": " + color + message);
    }

    private void handleChatBlock(AsyncPlayerChatEvent event, Player player, PlayerData playerData) {
        Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
            Bukkit.getPluginManager().callEvent(new SubmitChatEvent(player, event.getMessage()));
            String chatBlock = (String) playerData.getVariable("chatblock");
            if (chatBlock != null) {
                handleChatBlockInput(player, playerData, chatBlock, event.getMessage());
            }
        });
    }

    private void handleChatBlockInput(Player player, PlayerData playerData, String chatBlock, String message) {
        String field;
        switch (chatBlock) {
            case "firstname":
                field = "einreise_firstname";
                break;
            case "lastname":
                field = "einreise_lastname";
                break;
            case "dob":
                field = "einreise_dob";
                break;
            default:
                return;
        }
        playerData.setVariable(field, message);
        player.sendMessage(Prefix.MAIN + "Dein " + field.split("_")[1] + " lautet nun: " + message + " §8-§7 Klicke den NPC wieder an!");
        playerData.setVariable("chatblock", null);
    }

    private boolean checkUppercasePercentage(String msg) {
        long uppercaseCount = msg.chars().filter(Character::isUpperCase).count();
        return ((double) uppercaseCount / msg.length()) >= 0.75;
    }
}
