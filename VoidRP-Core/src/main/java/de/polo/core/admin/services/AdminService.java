package de.polo.core.admin.services;

import org.bukkit.Color;
import org.bukkit.entity.Player;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public interface AdminService {

    /**
     * Sends a message to all admins
     */
    void send_message(String msg, Color color);

    /**
     * Sends a message to all guides
     */
    void sendGuideMessage(String msg, Color color);

    /**
     * Inserts a note into the database
     */
    void insertNote(String punisher, String target, String note) throws Exception;

    /**
     * Starts the memory usage updater for a player
     */
    void startMemoryUsageUpdater(Player player);
}
