package de.polo.voidroleplay.utils;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.storage.ShopBook;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.json.JSONArray;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Mayson1337
 * @version 1.1.0
 * @since 1.0.0
 */
public class NewsManager {

    private static final Logger LOGGER = Logger.getLogger(NewsManager.class.getName());
    private final List<ShopBook> books = new ObjectArrayList<>();

    public NewsManager() {
        loadBooksFromDatabase();
    }

    public void addBookToStore(ItemStack book, String type, int price) {
        if (book == null || !(book.getItemMeta() instanceof BookMeta meta)) {
            LOGGER.log(Level.SEVERE, "Invalid book item passed.");
            return;
        }

        Component titleComponent = meta.displayName();
        String title = LegacyComponentSerializer.legacyAmpersand().serialize(titleComponent);
        String author = meta.getAuthor();
        List<Component> pages = meta.pages();

        if (isNullOrBlank(title)) {
            LOGGER.log(Level.SEVERE, "Book title is missing.");
            return;
        }
        if (isNullOrBlank(author)) {
            LOGGER.log(Level.SEVERE, "Book author is missing.");
            return;
        }
        if (pages.isEmpty()) {
            LOGGER.log(Level.SEVERE, "Book pages are empty.");
            return;
        }

        BookSerializer bookSerializer = new BookSerializer(meta);
        String jsonContent = bookSerializer.serializeAll();

        Main.getInstance().getMySQL().insertAndGetKeyAsync(
                        "INSERT INTO news_store (price, title, type, author, content) VALUES (?, ?, ?, ?, ?)",
                        price,
                        title,
                        type,
                        author,
                jsonContent)
                .thenApply(key -> {
                    key.ifPresentOrElse(k -> {
                        ShopBook shopBook = new ShopBook(k, book.displayName(), author, type, pages, jsonContent);
                        shopBook.setPrice(price);
                        synchronized (books) {
                            books.add(shopBook);
                        }
                    }, () -> LOGGER.log(Level.WARNING, "Failed to retrieve key for the inserted book."));
                    return null;
                });
    }

    private void loadBooksFromDatabase() {
        books.clear();
        Main.getInstance().getMySQL().queryThreaded("SELECT * FROM news_store")
                .thenAcceptAsync(result -> {
                    try {
                        while (result.next()) {
                            int id = result.resultSet().getInt("id");
                            String author = result.resultSet().getString("author");
                            String type = result.resultSet().getString("type");
                            String content = result.resultSet().getString("content");
                            int price = result.resultSet().getInt("price");
                            LOGGER.log(Level.INFO, "The serialized content is: {0}", content);
                            Book book = BookSerializer.deserializeAll(content);

                            ShopBook shopBook = new ShopBook(id, book.title(), author, type, book.pages(), content);
                            shopBook.setPrice(price);

                            synchronized (books) {
                                books.add(shopBook);
                            }
                        }
                        LOGGER.log(Level.INFO, "Loaded {0} books from the database.", books.size());
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "Error while loading books from the database.", e);
                    } finally {
                        result.close();
                    }
                })
                .exceptionally(e -> {
                    LOGGER.log(Level.SEVERE, "Failed to execute query for loading books.", e);
                    return null;
                });
    }

    private boolean isNullOrBlank(String str) {
        return str == null || str.isBlank();
    }

    public void giveBookToPlayer(Player player, ShopBook book) {
        if (book.getJsonContent() == null) {
            ItemStack stack = new ItemStack(Material.WRITTEN_BOOK, 1);
            ItemMeta meta = stack.getItemMeta();
            meta.displayName(book.getTitle());
            stack.setItemMeta(meta);
            BookMeta bookMeta = (BookMeta) meta;
            bookMeta.setAuthor(bookMeta.getAuthor());
            bookMeta.pages(book.getContent());
            bookMeta.setTitle(book.getTitle().toString());
            stack.setItemMeta(bookMeta);
            player.getInventory().addItem(stack);
        } else {
            ItemStack bookItemStack = BookSerializer.deserializeItemStack(book.getJsonContent());
            player.getInventory().addItem(bookItemStack);
        }
    }

    public List<ShopBook> getBooks() {
        return books;
    }

    public void removeBook(ShopBook shopBook) {
        books.remove(shopBook);
        Main.getInstance().getMySQL().deleteAsync("DELETE FROM news_store WHERE id = ?", shopBook.getId());
    }
}
