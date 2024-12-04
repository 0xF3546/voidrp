package de.polo.api.nametags;

import org.bukkit.entity.Player;

public interface INameTagProvider {
    boolean setNametag(Player player, String name, String prefix, String suffix);

    boolean clearNametag(Player player);

    boolean clearAll();
}
