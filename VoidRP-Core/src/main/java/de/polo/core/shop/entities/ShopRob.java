package de.polo.core.shop.entities;

import de.polo.api.Utils.enums.Prefix;
import de.polo.api.player.VoidPlayer;
import de.polo.core.game.base.shops.ShopData;
import de.polo.core.utils.Utils;
import lombok.Getter;

import java.time.LocalDateTime;

public class ShopRob {
    @Getter
    private final ShopData shop;

    @Getter
    private final VoidPlayer robber;

    @Getter
    private final LocalDateTime startTime;

    public ShopRob(ShopData shop, VoidPlayer robber) {
        this.shop = shop;
        this.robber = robber;
        this.startTime = Utils.getTime();
    }
}
