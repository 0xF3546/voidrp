package de.polo.core.admin.services.impl;

import de.polo.api.Utils.ApiUtils;
import de.polo.api.VoidAPI;
import de.polo.api.player.VoidPlayer;
import de.polo.core.admin.services.AdminService;
import de.polo.core.player.entities.PlayerData;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.entity.Player;

import static de.polo.core.Main.adminManager;
import static de.polo.core.Main.playerManager;

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
        for (VoidPlayer player : VoidAPI.getPlayers()) {
            if (player.isAduty() || player.notificationsEnabled()) {
                player.sendMessage("§b§lNotify §8┃ " + ApiUtils.colorToTextColor(color) + msg);
            }
        }
    }

    @Override
    public void sendGuideMessage(String msg, Color color) {
        if (color == null) {
            color = Color.AQUA;
        }
        for (VoidPlayer player : VoidAPI.getPlayers()) {
            if (player.getData().getPermlevel() >= 40) {
                player.sendMessage("§eGuide §8┃ " + color + msg);
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
