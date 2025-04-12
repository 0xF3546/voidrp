package de.polo.core.news.services.impl;

import de.polo.api.VoidAPI;
import de.polo.api.news.Advertisement;
import de.polo.core.admin.services.AdminService;
import de.polo.core.news.services.NewsService;
import de.polo.core.player.services.PlayerService;
import de.polo.core.storage.ShopBook;
import de.polo.core.utils.Service;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

import static de.polo.core.Main.newsManager;
import static net.kyori.adventure.text.Component.text;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
@Service
public class CoreNewsService implements NewsService {
    private final List<Advertisement> advertisements = new ObjectArrayList<>();
    private final List<Advertisement> advertisementQueue = new ObjectArrayList<>();

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

    @Override
    public void QueueAdvertisement(Advertisement advertisement) {
        AdminService adminService = VoidAPI.getService(AdminService.class);
        int adminCount = adminService.getActiveAdmins().size() + adminService.getActiveGuides().size();
        if (adminCount == 0) {
            addAdvertisementQueue(advertisement);
            DisplayAdvertisement(advertisement);
            return;
        }
        advertisementQueue.add(advertisement);
        adminService.sendGuideMessage(advertisement.getPublisher().getName() + " möchte folgende Werbung schalten:" , Color.ORANGE);
        adminService.sendGuideMessage(advertisement.getContent(), Color.ORANGE);
        Component acceptButton = text("[Annehmen]")
                .color(NamedTextColor.GREEN)
                .decorate(TextDecoration.BOLD)
                .clickEvent(ClickEvent.runCommand("/acceptadvertisement " + advertisement.getUuid()))
                .hoverEvent(HoverEvent.showText(text("Klicke hier, um die Werbung anzunehmen.").color(NamedTextColor.GREEN)));

        Component denyButton = text("[Ablehnen]")
                .color(NamedTextColor.RED)
                .decorate(TextDecoration.BOLD)
                .clickEvent(ClickEvent.runCommand("/denyadvertisement " + advertisement.getUuid()))
                .hoverEvent(HoverEvent.showText(text("Klicke hier, um die Werbung abzulehnen.").color(NamedTextColor.RED)));

        Component combined = Component.empty()
                .append(acceptButton)
                .append(text(" "))
                .append(denyButton);

        adminService.sendGuideMessage(combined, Color.ORANGE);

    }

    @Override
    public void addAdvertisementQueue(Advertisement advertisement) {
        advertisements.add(advertisement);
    }

    @Override
    public void acceptAdvertisement(Advertisement advertisement) {
        if (advertisementQueue.contains(advertisement)) {
            advertisementQueue.remove(advertisement);
            advertisements.add(advertisement);
        }
    }

    @Override
    public void denyAdvertisement(Advertisement advertisement) {
        advertisementQueue.remove(advertisement);
    }

    @Override
    public Advertisement getAdvertisement(UUID uuid) {
        Advertisement advertisement = null;
        for (Advertisement ad : advertisements) {
            if (ad.getUuid().equals(uuid)) {
                advertisement = ad;
                break;
            }
        }
        if (advertisement == null) {
            for (Advertisement ad : advertisementQueue) {
                if (ad.getUuid().equals(uuid)) {
                    advertisement = ad;
                    break;
                }
            }
        }
        return advertisement;
    }

    private void DisplayAdvertisement(Advertisement advertisement) {
        Bukkit.broadcast(Component.text("§8[§6News§8] §7" + advertisement.getPublisher().getName() + "§8:§f " + advertisement.getContent()));
    }
}
