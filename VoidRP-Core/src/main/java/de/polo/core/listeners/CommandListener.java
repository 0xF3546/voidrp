package de.polo.core.listeners;

import de.polo.api.VoidAPI;
import de.polo.api.player.VoidPlayer;
import de.polo.core.Main;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.utils.Prefix;
import de.polo.core.utils.player.ChatUtils;
import de.polo.core.utils.Event;
import de.polo.core.utils.player.PlayerPacket;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Arrays;
import java.util.List;

import static de.polo.core.Main.playerManager;
import static de.polo.core.Main.utils;

@Event
public class CommandListener implements Listener {
    private final List<String> nonBlockedCommands;
    private final List<String> blockedStarts;
    private final List<String> blockedContains;

    public CommandListener() {
        this.nonBlockedCommands = Arrays.asList("support", "report", "help", "vote", "jailtime", "ad", "aduty");
        this.blockedStarts = Arrays.asList("/to", "//to", "minecraft:msg", "minecraft:advancement", "minecraft:attribute",
                "minecraft:ban", "minecraft:ban-ip", "minecraft:banlist", "minecraft:bossbar",
                "minecraft:clear", "minecraft:clone", "minecraft:data", "minecraft:datapack",
                "minecraft:debug", "minecraft:defaultgamemode", "minecraft:deop", "minecraft:difficulty",
                "minecraft:effect", "minecraft:enchant", "minecraft:execute", "minecraft:experience",
                "minecraft:fill", "minecraft:forceload", "minecraft:function", "minecraft:gamemode",
                "minecraft:give", "minecraft:help", "minecraft:kick", "minecraft:kill", "minecraft:list",
                "minecraft:loot", "minecraft:locate", "minecraft:locatebiome", "minecraft:message",
                "minecraft:me", "minecraft:op", "minecraft:pardon", "minecraft:pardon-ip",
                "minecraft:particle", "minecraft:playsound", "minecraft:recipe", "minecraft:reload",
                "minecraft:replaceitem", "minecraft:say", "minecraft:schedule", "minecraft:scoreboard",
                "minecraft:seed", "minecraft:setblock", "minecraft:setidletimeout", "minecraft:setworldspawn",
                "minecraft:spawnpoint", "minecraft:spectate", "minecraft:spreadplayers", "minecraft:stop",
                "minecraft:stopsound", "minecraft:summon", "minecraft:tag", "minecraft:team",
                "minecraft:teammsg", "minecraft:tell", "minecraft:tellraw", "minecraft:teleport",
                "minecraft:time", "minecraft:tp", "minecraft:trigger", "minecraft:weather",
                "minecraft:whitelist", "minecraft:worldborder", "minecraft:w", "minecraft:xp", "minecraft:title",
                "minecraft:tm");
        this.blockedContains = Arrays.asList("while", "targetoffset", "for(", "^(.", "*.", "@a", "@e", "@p", "@s", "@r");
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String msg = event.getMessage();
        String[] args = msg.split(" ");
        Player player = event.getPlayer();
        String command = args[0].substring(1);
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (playerData == null) {
            player.sendMessage("§cEs gab Probleme beim laden deines Spielstandes.");
            event.setCancelled(true);
            return;
        }
        VoidPlayer voidPlayer = VoidAPI.getPlayer(player);

        // Check if the command starts with any blocked start
        for (String blockedStart : blockedStarts) {
            if (command.startsWith(blockedStart) && !voidPlayer.isAduty()) {
                event.setCancelled(true);
                player.sendMessage(Prefix.ERROR + "Der Befehl \"" + msg + "\" ist nicht erlaubt.");
                return;
            }
        }

        // Check if the command contains any blocked substring
        for (String blockedContain : blockedContains) {
            if (msg.contains(blockedContain) && !voidPlayer.isAduty()) {
                event.setCancelled(true);
                player.sendMessage(Prefix.ERROR + "Der Befehl \"" + msg + "\" ist nicht erlaubt.");
                return;
            }
        }

        playerData.setIntVariable("afk", 0);
        if (playerData.isAFK()) {
            utils.setAFK(player, false);
            PlayerPacket packet = new PlayerPacket(player);
            packet.renewPacket();
        }

        for (VoidPlayer voidPlayers : VoidAPI.getPlayers()) {
            if (voidPlayers.getVariable("isSpec") == null) continue;
            if (!voidPlayers.getVariable("isSpec").equals(player.getUniqueId().toString())) continue;
            voidPlayers.sendMessage("§8[§cSpec§8]§6 " + player.getName() + "§7 hat den Befehl \"§6" + msg + "§7\" ausgeführt.");
        }

        if (Bukkit.getServer().getHelpMap().getHelpTopic(args[0]) == null) {
            event.setCancelled(true);
            player.sendMessage(Prefix.ERROR + "Der Befehl " + msg + " wurde nicht gefunden.");
            return;
        }
        if (playerData.isDead() && !voidPlayer.isAduty()) {
            if (!nonBlockedCommands.contains(command)) {
                player.sendMessage("§7Du kannst diesen Befehl aktuell nicht nutzen.");
                event.setCancelled(true);
                return;
            }
        }
        ChatUtils.logCommand(msg, player.getUniqueId());
    }
}
