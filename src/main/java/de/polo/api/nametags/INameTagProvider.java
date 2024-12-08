package de.polo.api.nametags;

import org.bukkit.entity.Player;

public interface INameTagProvider {
    boolean setNametag(Player player, String name, String prefix, String suffix);

    boolean clearNametag(Player player);

    boolean clearAll();

    boolean setNametagForGroup(Player player, Iterable<Player> viewers, String name, String prefix, String suffix);

    boolean clearNametagForGroup(Player player, Iterable<Player> viewers);
}
