package de.polo.core.shop.services.impl;

import de.polo.api.crew.Crew;
import de.polo.core.game.base.shops.ShopData;
import de.polo.core.shop.repository.ShopRepository;
import de.polo.core.shop.services.ShopService;
import de.polo.core.storage.Company;
import de.polo.core.utils.Service;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.List;
import java.util.UUID;

import static de.polo.core.Main.database;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
@Service
public class CoreShopService implements ShopService {
    private final List<ShopData> shops;
    private final ShopRepository shopRepository;
    public CoreShopService() {
        this.shopRepository = new ShopRepository();
        this.shops = shopRepository.loadShops();
    }
    @Override
    public List<ShopData> getShops() {
        return shops;
    }

    @Override
    public ShopData getShop(int id) {
        return shops.stream()
                .filter(shop -> shop.getId() == id)
                .findFirst()
                .orElse(null);
    }

    @Override
    public void setHolder(ShopData shop, Crew holder) {
        if (holder != null) {
            shop.setCrewHolder(holder.getId());
        } else {
            shop.setCrewHolder(0);
        }
        shopRepository.saveShop(shop);
    }

    @Override
    public void setOwner(ShopData shop, Company owner) {
        shop.setCompany(owner.getId());
        shopRepository.saveShop(shop);
    }
}
