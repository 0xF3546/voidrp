package de.polo.core.admin.services.impl;

import de.polo.api.utils.ApiUtils;
import de.polo.api.VoidAPI;
import de.polo.api.player.VoidPlayer;
import de.polo.api.player.enums.Setting;
import de.polo.core.Main;
import de.polo.core.admin.services.AdminService;
import de.polo.core.utils.Service;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.SneakyThrows;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Color;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
@Service
public class CoreAdminService implements AdminService {
    @Getter
    private final List<VoidPlayer> vanishedPlayers = new ObjectArrayList<>();
    @Override
    public void sendMessage(String msg, Color color) {
        if (color == null) {
            color = Color.AQUA;
        }
        for (VoidPlayer player : VoidAPI.getPlayers()) {
            if (player.getData().getPermlevel() >= 60 && (player.isAduty()) || player.hasSetting(Setting.TOGGLE_ADMIN_MESSAGES)) {
                player.sendMessage("§b§lNotify §8┃ " + ApiUtils.colorToLegacyCode(color) + "➜ " + msg);
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
                player.sendMessage("§eGuide §8┃ " + ApiUtils.colorToLegacyCode(color) + "➜ " + msg);
            }
        }
    }

    @Override
    public void sendGuideMessage(Component msg, Color color) {
        if (color == null) {
            color = Color.AQUA;
        }
        msg = msg.color(TextColor.color(color.asRGB()));
        for (VoidPlayer player : VoidAPI.getPlayers()) {
            if (player.getData().getPermlevel() >= 40) {
                player.sendMessage(Component.text("§eGuide §8┃ " + ApiUtils.colorToLegacyCode(color) + "➜ ").append(msg));
            }
        }
    }

    @Override
    public void sendMessage(Component msg, Color color) {
        if (color == null) {
            color = Color.AQUA;
        }
        for (VoidPlayer player : VoidAPI.getPlayers()) {
            if (player.getData().getPermlevel() >= 60 && (player.isAduty()) || player.hasSetting(Setting.TOGGLE_ADMIN_MESSAGES)) {
                player.sendMessage("§b§lNotify §8┃ " + ApiUtils.colorToLegacyCode(color) + "➜ " + msg);
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

    @Override
    public List<VoidPlayer> getActiveGuides() {
        return VoidAPI.getPlayers()
                .stream()
                .filter(player -> player.getData().getPermlevel() >= 50)
                .toList();
    }

    @Override
    public List<VoidPlayer> getActiveAdmins() {
        return VoidAPI.getPlayers()
                .stream()
                .filter(player -> player.getData().getPermlevel() >= 40 && player.getData().getPermlevel() < 50)
                .toList();
    }

    @Override
    public void setPlayerVanish(VoidPlayer player, boolean vanish) {
        if (vanish) vanishedPlayers.add(player);
        else vanishedPlayers.remove(player);
    }
}
