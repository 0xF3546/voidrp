package de.polo.core.game.base;

import de.polo.core.game.base.housing.House;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class CookTimer {
    @Getter
    private final Player player;
    @Getter
    private final House house;
    @Getter
    private final Location location;
    @Getter
    @Setter
    private int minutes;

    public CookTimer(Player player, int minutes, House house, Location location) {
        this.player = player;
        this.minutes = minutes;
        this.house = house;
        this.location = location;
    }
}
