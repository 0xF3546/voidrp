package de.polo.api.nametags;

import org.bukkit.entity.Player;

import java.util.Collection;

/**
 * Interface for managing Nametags and Tablist customization.
 */
public interface INameTagProvider {

    /**
     * Sets a player's nametag with a custom prefix and suffix.
     *
     * @param player the player whose nametag is to be set.
     * @param prefix the prefix to display before the player's name.
     * @param suffix the suffix to display after the player's name.
     */
    void setNametag(Player player, String prefix, String suffix);

    /**
     * Sets a player's nametag with a custom prefix and suffix,
     * but makes it visible only to certain players.
     *
     * @param player  the player whose nametag is to be set.
     * @param viewers the players who can see the nametag.
     * @param prefix  the prefix to display before the player's name.
     * @param suffix  the suffix to display after the player's name.
     */
    void setNametagForViewers(Player player, Collection<Player> viewers, String prefix, String suffix);

    /**
     * Clears a player's nametag, removing them from their custom team.
     *
     * @param player the player whose nametag is to be cleared.
     */
    void clearNametag(Player player);

    /**
     * Clears all nametags and resets the scoreboard to its default state.
     */
    void clearAllNametags();

    /**
     * Sets a custom header and footer for a player's Tablist.
     *
     * @param player the player to whom the header and footer should be sent.
     * @param header the text to display at the top of the Tablist.
     * @param footer the text to display at the bottom of the Tablist.
     */
    void setTabHeaderFooter(Player player, String header, String footer);

    /**
     * Updates the Tablist for all online players with a global header and footer.
     *
     * @param header the text to display at the top of the Tablist for all players.
     * @param footer the text to display at the bottom of the Tablist for all players.
     */
    void updateTabForAll(String header, String footer);

    void updateForFaction(String faction);
}
