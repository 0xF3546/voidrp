package de.polo.core.storage;

import de.polo.api.gangwar.IGangzone;
import de.polo.core.Main;
import de.polo.core.utils.Utils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;

import java.time.LocalDateTime;

public class Dealer {
    @Getter
    private final int price;
    @Getter
    @Setter
    private int id;
    @Getter
    @Setter
    private Location location;
    @Getter
    @Setter
    private int sold = 0;

    @Getter
    @Setter
    private String gangzone;

    @Setter
    private String owner;

    @Getter
    @Setter
    private String attacker;

    @Getter
    @Setter
    private LocalDateTime lastAttack = Utils.getTime();

    public Dealer() {
        this.price = Utils.random(1300, 1600);
    }

    public String getOwner() {
        if (owner == null) {
            IGangzone gz = Main.utils.gangwarUtils.getGangzoneByName(gangzone);
            owner = gz.getOwner();
        }

        return owner;
    }

    public boolean canSell() {
        return sold < 8;
    }

}
