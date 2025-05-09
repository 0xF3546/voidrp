package de.polo.core.shop.services;

import de.polo.api.crew.Crew;
import de.polo.core.game.base.shops.ShopData;
import de.polo.core.shop.entities.CrewTakeShop;
import de.polo.core.shop.entities.ShopRob;
import de.polo.core.storage.Company;

import java.util.List;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public interface ShopService {
    List<ShopData> getShops();

    ShopData getShop(int id);

    void setHolder(ShopData shop, Crew holder);

    void setOwner(ShopData shop, Company owner);

    List<ShopRob> getActiveRobberies();

    void addRobbery(ShopRob shopRob);

    void removeRobbery(ShopRob shopRob);

    List<CrewTakeShop> getActiveCrewTakes();

    void addCrewTake(CrewTakeShop crewTake);

    void removeCrewTake(CrewTakeShop crewTake);
}
