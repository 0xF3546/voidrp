package de.polo.core.jobs.commands;

import de.polo.api.jobs.TransportJob;
import de.polo.api.player.VoidPlayer;
import de.polo.core.Main;
import de.polo.api.VoidAPI;
import de.polo.api.jobs.enums.MiniJob;
import de.polo.core.handler.CommandBase;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.location.services.impl.LocationManager;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.utils.Prefix;
import de.polo.core.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

import static de.polo.core.Main.locationManager;
import static de.polo.core.Main.playerService;

@CommandBase.CommandMeta(
        name = "lebensmittellieferant",
        usage = "/lebensmittellieferant"
)
public class LebensmittelLieferantCommand extends CommandBase implements TransportJob {
    String prefix = "§aLieferant §8┃ ➜ §7";

    public LebensmittelLieferantCommand(@NotNull CommandMeta meta) {
        super(meta);
    }

    @Override
    public void execute(@NotNull VoidPlayer player, @NotNull PlayerData playerData, @NotNull String[] args) throws Exception {
        if (player.getActiveJob() == null) {
            if (locationManager.getDistanceBetweenCoords(player, "lieferant") <= 5) {
                startJob(player);
            } else {
                player.sendMessage(Prefix.ERROR + "Du bist §cnicht§7 in der nähe des §aLebensmittel-Lieferanten§7 Jobs!");
            }
        } else {
            if (playerData.getVariable("job").equals("lieferant")) {
                if (locationManager.getDistanceBetweenCoords(player, "lieferant") <= 5) {
                    player.sendMessage(prefix + "Du hast den Job Lebensmittel-Lieferant beendet.");
                    playerData.setVariable("job", null);
                    endJob(player);
                }
            } else {
                player.sendMessage(Prefix.ERROR + "Du übst bereits den Job " + playerData.getVariable("job") + " aus.");
            }
        }
    }

    @Override
    public void handleDrop(VoidPlayer player) {
        int shop = locationManager.isNearShop(player.getPlayer());
        if (shop > 0) {
            int drinks = (int) player.getVariable("drinks");
            int snacks = (int) player.getVariable("snacks");
            int payout = (snacks * 4) + (drinks * 3);
            int exp = (snacks * 2) + (drinks * 3);
            playerService.addExp(player.getPlayer(), exp);
            this.endJob(player);
            player.getData().addMoney(payout, "Lieferant");
            player.sendMessage("§aLieferant §8» §7Danke für die Lieferung! §a+" + payout + "$");
        } else {
            player.sendMessage(Prefix.ERROR + "Du bist bei keinem Shop.");
        }
    }

    @Override
    public void startJob(VoidPlayer player) {
        player.setMiniJob(MiniJob.FOOD_SUPPLIER);
        player.setActiveJob(this);
        player.sendMessage(prefix + "Du bist nun §aLebensmittel-Lieferant§7.");
        player.sendMessage(prefix + "Bringe die Lebensmittel zu einem Shop deiner Wahl!");
        player.setVariable("snacks", Utils.random(3, 7));
        player.setVariable("drinks", Utils.random(3, 7));
    }

    @Override
    public void endJob(VoidPlayer player) {
        Main.getInstance().beginnerpass.didQuest(player.getPlayer(), 5);
        playerService.handleJobFinish(player, MiniJob.FOOD_SUPPLIER, 3600, 10);
        //playerData.getScoreboard("lebensmittellieferant").killScoreboard();
    }
}
