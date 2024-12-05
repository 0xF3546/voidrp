package de.polo.voidroleplay.dataStorage;

import de.polo.api.faction.dealer.IDealer;
import de.polo.api.faction.gangwar.IGangzone;
import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.utils.Utils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;

import java.time.LocalDateTime;

public class Dealer implements IDealer {
    @Getter
    @Setter
    private int id;
    @Getter
    @Setter
    private Location location;

    @Getter
    private final int price;

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

    public String getOwner() {
        if (owner == null) {
            IGangzone gz = Main.getInstance().utils.gangwarUtils.getGangzoneByName(gangzone);
            owner = gz.getOwner();
        }

        return owner;
    }

    public Dealer() {
        this.price = Main.random(1300, 1600);
    }

    public boolean canSell() {
        return sold < 8;
    }
    @Override
    public String getName() {
        return null;
    }

    @Override
    public boolean isFull() {
        return false;
    }

    @Override
    public void setFull(boolean state) {

    }

}
