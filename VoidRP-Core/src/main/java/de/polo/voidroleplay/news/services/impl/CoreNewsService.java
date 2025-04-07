package de.polo.voidroleplay.news.services.impl;

import de.polo.voidroleplay.news.services.NewsService;
import de.polo.voidroleplay.storage.ShopBook;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

import static de.polo.voidroleplay.Main.newsManager;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class CoreNewsService implements NewsService {

    @Override
    public void addBookToStore(ItemStack book, String type, int price) {
        newsManager.addBookToStore(book, type, price);
    }

    @Override
    public void giveBookToPlayer(Player player, ShopBook book) {
        newsManager.giveBookToPlayer(player, book);
    }

    @Override
    public List<ShopBook> getBooks() {
        return newsManager.getBooks();
    }

    @Override
    public void removeBook(ShopBook shopBook) {
        newsManager.removeBook(shopBook);
    }
}
