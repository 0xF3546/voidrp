package de.polo.core.news.commands;

import de.polo.api.Utils.enums.Prefix;
import de.polo.api.VoidAPI;
import de.polo.api.news.Advertisement;
import de.polo.api.player.VoidPlayer;
import de.polo.core.admin.services.AdminService;
import de.polo.core.handler.CommandBase;
import de.polo.core.news.services.NewsService;
import de.polo.core.player.entities.PlayerData;
import org.bukkit.Color;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
@CommandBase.CommandMeta(
        name = "acceptadvertisement",
        permissionLevel = 40,
        usage = "/acceptadvertisement [Id]"
)
public class AcceptAdvertisementCommand extends CommandBase {
    public AcceptAdvertisementCommand(@NotNull CommandMeta meta) {
        super(meta);
    }

    @Override
    public void execute(@NotNull VoidPlayer player, @NotNull PlayerData playerData, @NotNull String[] args) throws Exception {
        if (args.length < 1) {
            showSyntax(player);
            return;
        }
        UUID uuid;
        try {
            uuid = UUID.fromString(args[0]);
        } catch (IllegalArgumentException e) {
            player.sendMessage("§cInvalid advertisement ID.");
            return;
        }
        NewsService newsService = VoidAPI.getService(NewsService.class);
        Advertisement advertisement = newsService.getAdvertisement(uuid);
        if (advertisement == null) {
            player.sendMessage("Werbung nicht gefunden.", Prefix.ERROR);
            return;
        }
        newsService.acceptAdvertisement(advertisement);
        player.sendMessage("Werbung akzeptiert.", Prefix.ADMIN);
        AdminService adminService = VoidAPI.getService(AdminService.class);
        adminService.sendGuideMessage(player.getName() + " hat die Werbung von " + advertisement.getPublisher().getName() + " angenommen.", Color.GREEN);
    }
}
