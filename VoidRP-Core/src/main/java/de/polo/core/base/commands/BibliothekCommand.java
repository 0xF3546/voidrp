package de.polo.core.base.commands;

import de.polo.api.Utils.inventorymanager.CustomItem;
import de.polo.api.Utils.inventorymanager.InventoryManager;
import de.polo.api.player.VoidPlayer;
import de.polo.core.handler.CommandBase;
import de.polo.core.manager.ItemManager;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.storage.ShopBook;
import de.polo.core.utils.Prefix;
import lombok.SneakyThrows;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import static de.polo.core.Main.factionManager;
import static de.polo.core.Main.newsManager;

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
    public void execute(@NotNull VoidPlayer player, @NotNull PlayerData playerData, @NotNull String[] args) throws Exception {
        InventoryManager inventoryManager = new InventoryManager(player.getPlayer(), 27, Component.text("§7Bibliothek"));
        int i = 0;
        for (ShopBook book : newsManager.getBooks()) {
            Component titleComponent = book.getTitle();
            String title = LegacyComponentSerializer.legacyAmpersand().serialize(titleComponent);
            inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(Material.WRITTEN_BOOK, 1, 0, title.replace("&", "§"), Arrays.asList("§8 ➥ §e" + book.getType(), "§8 ➥ §a" + book.getPrice() + "$"))) {
                @SneakyThrows
                @Override
                public void onClick(InventoryClickEvent event) {
                    if (event.isLeftClick() && playerData.getFaction().equalsIgnoreCase("News") && playerData.getFactionGrade() >= 4) {
                        openDeleteInv(player, book);
                        return;
                    }
                    if (playerData.getBargeld() < book.getPrice()) {
                        player.sendMessage(Component.text(Prefix.ERROR + "Du hast nicht genug Geld dabei."));
                        return;
                    }
                    playerData.removeMoney(book.getPrice(), "Buchkauf");
                    factionManager.addFactionMoney("News", book.getPrice(), "Buchkauf");
                    newsManager.giveBookToPlayer(player.getPlayer(), book);
                }
            });
            i++;
        }
    }

    private void openDeleteInv(VoidPlayer player, ShopBook shopBook) {
        InventoryManager sellInventory = new InventoryManager(player.getPlayer(), 27, Component.text("§8 » §e" + shopBook.getTitle() + " löschen"));
        sellInventory.setItem(new CustomItem(12, ItemManager.createItem(Material.RED_WOOL, 1, 0, "§cLöschen")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                player.getPlayer().closeInventory();
                player.sendMessage(Component.text(Prefix.MAIN + "Du hast das Buch " + shopBook.getTitle() + " gelöscht."));
                newsManager.removeBook(shopBook);
            }
        });
        sellInventory.setItem(new CustomItem(14, ItemManager.createItem(Material.GREEN_WOOL, 1, 0, "§aAbbrechen")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                player.getPlayer().closeInventory();
            }
        });
    }
}
