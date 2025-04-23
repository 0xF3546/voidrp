package de.polo.core.admin.services;

import de.polo.api.player.VoidPlayer;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;

import java.util.List;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public interface AdminService {

    /**
     * Sends a message to all admins
     */
    void sendMessage(String msg, Color color);

    /**
     * Sends a message to all guides
     */
    void sendAdminMessage(String msg, Color color);

    void sendAdminMessage(Component msg, Color color);

    /**
     * Inserts a note into the database
     */
    void insertNote(String punisher, String target, String note);

    List<VoidPlayer> getActiveGuides();
    List<VoidPlayer> getActiveAdmins();
}
