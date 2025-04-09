package de.polo.core.news.services.impl;

import de.polo.core.news.services.NewsService;
import de.polo.core.storage.ShopBook;
import de.polo.core.utils.Service;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

import static de.polo.core.Main.newsManager;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
@Service
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
