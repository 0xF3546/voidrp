package de.polo.core.admin.services.impl;

import de.polo.api.Utils.ApiUtils;
import de.polo.api.VoidAPI;
import de.polo.api.player.VoidPlayer;
import de.polo.core.Main;
import de.polo.core.admin.services.AdminService;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.utils.Service;
import lombok.SneakyThrows;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;

import static de.polo.core.Main.playerManager;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
@Service
public class CoreAdminService implements AdminService {
    @Override
    public void send_message(String msg, Color color) {
        if (color == null) {
            color = Color.AQUA;
        }
        for (VoidPlayer player : VoidAPI.getPlayers()) {
            if (player.isAduty() || player.notificationsEnabled()) {
                player.sendMessage("§b§lNotify §8┃ " + ApiUtils.colorToLegacyCode(color) + msg);
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

    @SneakyThrows
    @Override
    public void insertNote(String punisher, String target, String note) {
        Connection connection = Main.getInstance().coreDatabase.getConnection();
        PreparedStatement statement = connection.prepareStatement("INSERT INTO notes (uuid, target, note) VALUES (?, ?, ?)");
        statement.setString(1, punisher);
        statement.setString(2, target);
        statement.setString(3, note);
        statement.execute();
        statement.close();
        connection.close();
    }


}
