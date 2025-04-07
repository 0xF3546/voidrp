package de.polo.voidroleplay.news.services;

import de.polo.voidroleplay.storage.ShopBook;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

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
}
