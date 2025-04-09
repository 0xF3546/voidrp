package de.polo.core.jobs.commands;

import de.polo.api.VoidAPI;
import de.polo.api.jobs.TransportJob;
import de.polo.core.Main;
import de.polo.core.handler.CommandBase;
import de.polo.api.jobs.enums.MiniJob;
import de.polo.core.location.services.NavigationService;
import de.polo.core.manager.ServerManager;
import de.polo.api.player.VoidPlayer;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.utils.Prefix;
import de.polo.core.utils.Utils;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import static de.polo.core.Main.*;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
@CommandBase.CommandMeta(name = "bottletransport")
public class BottletransportCommand extends CommandBase implements TransportJob {
    private final String PREFIX = "§2Flaschenlieferant §8┃ ➜§7 ";
    public BottletransportCommand(@NotNull CommandMeta meta) {
        super(meta);
    }

    @Override
    public void execute(@NotNull VoidPlayer player, @NotNull PlayerData playerData, @NotNull String[] args) throws Exception {
        if (args.length > 0) {
            if (locationManager.getDistanceBetweenCoords(player, "bar_storage") < 5
                    && playerData.getVariable("job") != null
                    && playerData.getVariable("job") == "flaschentransport"
                    && args[0].equalsIgnoreCase("drop")) {
                handleDrop(player);
                return;
            }
        }
        if (locationManager.getDistanceBetweenCoords(player, "flaschenlieferant") > 5) {
            player.sendMessage(Prefix.ERROR + "Du bist nicht in der nähe des Flaschenlieferants.");
            return;
        }
        if (Main.getInstance().getCooldownManager().isOnCooldown(player.getPlayer(), "job_flaschentransport")) {
            player.sendMessage(Component.text(PREFIX + "Warte noch " + Utils.getTime(Main.getInstance().getCooldownManager().getRemainingTime(player.getPlayer(), "job_flaschentransport"))));
            return;
        }
        if (playerData.getVariable("job") != null) {
            player.sendMessage(Component.text(Prefix.ERROR + "Du hast bereits einen Job angenommen."));
            return;
        }
        startJob(player);
    }

    public void startJob(VoidPlayer player) {
        player.setVariable("job", "flaschentransport");
        player.setMiniJob(MiniJob.BOTTLE_TRANSPORT);
        player.setActiveJob(this);
        int amount = Utils.random(2, 4);
        player.sendMessage(Component.text(PREFIX + "Du hast " + amount + " erhalten, bringe diese in das Lager der Bar."));
        NavigationService navigationService = VoidAPI.getService(NavigationService.class);
        navigationService.createNaviByLocation(player.getPlayer(), "bar_storage");
        player.setVariable("job::flaschentransport::amount", amount);
    }

    @Override
    public void endJob(VoidPlayer player) {
        player.setVariable("job", null);
        player.setVariable("job::flaschentransport::amount", 0);
        Main.getInstance().getCooldownManager().setJobCooldown(player.getPlayer(), "flaschentransport", 360);
        player.sendMessage(PREFIX + "Du hast den Job beendet.");
    }

    @Override
    public void handleDrop(VoidPlayer player) {
        player.setMiniJob(null);
        player.setActiveJob(this);
        int boxPrice = Utils.random(ServerManager.getPayout("flaschenlieferant_kiste_from"), ServerManager.getPayout("flaschenlieferant_kiste_to"));
        int amount = (int) player.getVariable("job::flaschentransport::amount");
        if (amount <= 0) return;
        player.sendMessage(Component.text(PREFIX + "§aDu hast eine Kiste abgegeben. §a+" + boxPrice + "$"));
        player.setVariable("job::flaschentransport::amount", amount - 1);
        player.getData().addMoney(boxPrice, "Kiste Flaschenlieferant");
        playerManager.addExp(player.getPlayer(), Utils.random(6, 10));
        if (amount == 1) {
            endJob(player);
        }
    }
}
