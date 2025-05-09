package de.polo.voidroleplay.utils.player;

import de.polo.voidroleplay.utils.enums.RoleplayItem;
import lombok.Getter;
import lombok.Setter;

public class PlayerInventoryItem {

    @Getter
    private final RoleplayItem item;
    @Getter
    @Setter
    private int id;
    @Getter
    @Setter
    private int amount;

    public PlayerInventoryItem(RoleplayItem item, int amount) {
        this.item = item;
        this.amount = amount;
    }

    public PlayerInventoryItem(int id, RoleplayItem item, int amount) {
        this.id = id;
        this.item = item;
        this.amount = amount;
    }
}
