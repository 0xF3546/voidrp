package de.polo.voidroleplay.admin.services.impl;

import de.polo.voidroleplay.admin.services.AdminService;
import de.polo.voidroleplay.storage.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.entity.Player;

import static de.polo.voidroleplay.Main.adminManager;
import static de.polo.voidroleplay.Main.playerManager;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class CoreAdminService implements AdminService {
    @Override
    public void send_message(String msg, Color color) {
        if (color == null) {
            color = Color.AQUA;
        }
        for (Player player1 : Bukkit.getOnlinePlayers()) {
            PlayerData playerData = playerManager.getPlayerData(player1.getUniqueId());
            if (playerData.isAduty() || playerData.isSendAdminMessages()) {
                player1.sendMessage("§8[§c§l!§8] " + color + msg);
            }
        }
    }

    @Override
    public void sendGuideMessage(String msg, Color color) {
        if (color == null) {
            color = Color.AQUA;
        }
        for (Player player1 : Bukkit.getOnlinePlayers()) {
            PlayerData playerData = playerManager.getPlayerData(player1.getUniqueId());
            if (playerData.getPermlevel() >= 40) {
                player1.sendMessage("§8[§eGuide§8] " + color + msg);
            }
        }
    }

    @Override
    public void insertNote(String punisher, String target, String note) throws Exception {
        adminManager.insertNote(punisher, target, note);
    }

    @Override
    public void startMemoryUsageUpdater(Player player) {
        adminManager.startMemoryUsageUpdater(player);
    }
}
