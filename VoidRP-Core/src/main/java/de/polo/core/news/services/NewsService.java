package de.polo.core.news.services;

import de.polo.api.news.Advertisement;
import de.polo.core.storage.ShopBook;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public interface NewsService {
    /**
     * Adds a book to the news store
     */
    void addBookToStore(ItemStack book, String type, int price);

    /**
     * Gives a specific book to a player
     */
    void giveBookToPlayer(Player player, ShopBook book);

    /**
     * Gets all available books
     */
    List<ShopBook> getBooks();

    /**
     * Removes a book from the store
     */
    void removeBook(ShopBook shopBook);

    void queueAdvertisement(Advertisement advertisement);

    void addAdvertisementQueue(Advertisement advertisement);

    void acceptAdvertisement(Advertisement advertisement);

    void denyAdvertisement(Advertisement advertisement);

    Advertisement getAdvertisement(UUID uuid);
}
