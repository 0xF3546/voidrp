package de.polo.core.shop.entities;

import de.polo.api.crew.Crew;
import de.polo.core.game.base.shops.ShopData;
import de.polo.core.utils.Utils;

import java.time.LocalDateTime;

public record CrewTakeShop(Crew crew, ShopData shop, LocalDateTime startTime) {
    public CrewTakeShop(Crew crew, ShopData shop) {
        this(crew, shop, Utils.getTime());
    }
}
