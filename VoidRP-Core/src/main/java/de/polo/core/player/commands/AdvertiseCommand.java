package de.polo.core.player.commands;

import de.polo.api.VoidAPI;
import de.polo.api.player.VoidPlayer;
import de.polo.core.handler.CommandBase;
import de.polo.core.manager.ServerManager;
import de.polo.core.news.entities.CoreAdvertisement;
import de.polo.core.news.services.NewsService;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.player.services.PlayerService;
import de.polo.core.utils.Prefix;
import org.jetbrains.annotations.NotNull;

import static de.polo.core.Main.factionManager;

@CommandBase.CommandMeta(
        name = "advertise",
        usage = "/advertise [Nachricht]",
        permissionLevel = 20
)
public class AdvertiseCommand extends CommandBase {
    public AdvertiseCommand(@NotNull CommandMeta meta) {
        super(meta);
    }

    @Override
    public void execute(@NotNull VoidPlayer player, @NotNull PlayerData playerData, @NotNull String[] args) throws Exception {
        if (args.length < 1) {
            showSyntax(player);
            return;
        }
        String message = String.join(" ", args);
        int price = message.length() * ServerManager.getPayout("werbung");
        if (player.getData().getBargeld() >= price) {
            NewsService newsService = VoidAPI.getService(NewsService.class);
            newsService.queueAdvertisement(new CoreAdvertisement(player, message));
            player.sendMessage("§8[§2Werbung§8]§a Werbung wird nun geprüft. §c-" + price + "$");
            PlayerService playerService = VoidAPI.getService(PlayerService.class);
            playerService.removeMoney(player.getPlayer(), price, "Werbung");
            factionManager.addFactionMoney("News", price, "Werbung - " + player.getName());
        } else {
            player.sendMessage(Prefix.ERROR + "Du benötigst " + price + "$.");
        }
    }
}
