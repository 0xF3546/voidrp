package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.handler.CommandBase;
import de.polo.voidroleplay.manager.ItemManager;
import de.polo.voidroleplay.manager.inventory.CustomItem;
import de.polo.voidroleplay.manager.inventory.InventoryManager;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.storage.ShopBook;
import de.polo.voidroleplay.utils.Prefix;
import dev.vansen.singleline.SingleLine;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;

import static de.polo.voidroleplay.Main.newsManager;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */

@CommandBase.CommandMeta(name = "bibliothek")
public class BibliothekCommand extends CommandBase {
    public BibliothekCommand(@NotNull CommandMeta meta) {
        super(meta);
    }

    @Override
    public void execute(@NotNull Player player, @NotNull PlayerData playerData, @NotNull String[] args) throws Exception {
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§7Bibliothek");
        int i = 0;
        for (ShopBook book : newsManager.getBooks()) {
            inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(Material.WRITTEN_BOOK, 1, 0, book.getTitle(), Arrays.asList("§8 ➥ §e" + book.getType(), "§8 ➥ §a" + book.getPrice() + "$"))) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    if (playerData.getBargeld() < book.getPrice()) {
                        player.sendMessage(Component.text(Prefix.ERROR + "Du hast nicht genug Geld dabei."));
                        return;
                    }
                    playerData.removeMoney(book.getPrice(), "Buchkauf");
                    newsManager.giveBookToPlayer(player, book);
                }
            });
            i++;
        }
    }
}
