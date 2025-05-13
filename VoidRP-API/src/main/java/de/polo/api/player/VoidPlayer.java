package de.polo.api.player;

import de.polo.api.Utils.GUI;
import de.polo.api.Utils.enums.Prefix;
import de.polo.api.company.Company;
import de.polo.api.company.CompanyRole;
import de.polo.api.jobs.Job;
import de.polo.api.jobs.enums.MiniJob;
import de.polo.api.player.enums.Setting;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public interface VoidPlayer {
    Player getPlayer();

    PlayerCharacter getData();


    List<PlayerSetting> getSettings();

    PlayerRuntimeStatistic getRuntimeStatistic();

    default void addSetting(Setting setting) {
        this.addSetting(setting, "");
    }

    void addSetting(Setting setting, String value);

    void removeSetting(Setting setting);

    default boolean hasSetting(Setting setting) {
        return this.getSetting(setting) != null;
    }

    PlayerSetting getSetting(Setting setting);

    MiniJob getMiniJob();

    void setMiniJob(MiniJob miniJob);

    Job getActiveJob();

    void setActiveJob(Job job);

    UUID getUuid();

    default String getName() {
        return getPlayer().getName();
    }

    default Location getLocation() {
        return getPlayer().getLocation();
    }

    default void sendMessage(String message) {
        this.sendMessage(Component.text(message));
    }

    default void sendMessage(String message, Prefix prefix) {
        this.sendMessage(Component.text(prefix.getPrefix() + message));
    }

    default void sendMessage(final Component component, final Prefix prefix) {
        this.sendMessage(Component.text(prefix.getPrefix() + component));
    }

    void sendMessage(Component component);

    void setVariable(String key, Object value);

    Object getVariable(String key);

    boolean isAduty();

    void setAduty(boolean aduty);
    boolean isAFK();
    void setAFK(boolean afk);

    boolean notificationsEnabled();

    void setNotificationsEnabled(boolean enabled);

    GUI getLastGUI();

    void setLastGUI(GUI gui);
}
