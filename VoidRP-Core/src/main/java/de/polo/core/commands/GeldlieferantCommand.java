package de.polo.core.commands;

import de.polo.api.VoidAPI;
import de.polo.api.player.VoidPlayer;
import de.polo.core.Main;
import de.polo.core.handler.CommandBase;
import de.polo.core.location.services.LocationService;
import de.polo.core.manager.ServerManager;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.storage.ATM;
import de.polo.core.utils.Prefix;
import de.polo.core.utils.Utils;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static de.polo.core.Main.playerManager;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
@CommandBase.CommandMeta(name = "moneytransport")
public class GeldlieferantCommand extends CommandBase {
    private static final String PREFIX = "§8[§6Geldtransport§8]§7 ";

    public GeldlieferantCommand(@NotNull CommandMeta meta) {
        super(meta);
    }

    public static void drop(Player player, ATM atm) {
        PlayerData playerData = playerManager.getPlayerData(player);
        int amount = playerData.getVariable("job::geldlieferant::amount");

        if (amount < 1000) {
            player.sendMessage(Component.text(PREFIX + "Du hast nicht genug Geld übrig, um einen ATM aufzufüllen. Job beendet."));
            finishJob(player);
            return;
        }

        int diff = 100000 - atm.getMoneyAmount();

        if (diff >= 1000) {
            int depositAmount = Math.min(amount, diff);

            int payoutMultiplier = depositAmount / 1000;
            int payout = Utils.random(ServerManager.getPayout("geldlieferant_1000_from"), ServerManager.getPayout("geldlieferant_1000_to"));
            payout = payout * payoutMultiplier;

            playerData.addMoney(payout, "Geldlieferant");
            playerData.setVariable("job::geldlieferant::amount", amount - depositAmount);
            player.sendMessage(Component.text(PREFIX + "Du hast " + depositAmount + "$ abgegeben ("
                    + Utils.toDecimalFormat(amount - depositAmount) + "$ verbleibend)."));

            atm.setMoneyAmount(atm.getMoneyAmount() + depositAmount);

            if (amount <= depositAmount) {
                player.sendMessage(Component.text(PREFIX + "Du hast alles abgegeben. Job abgeschlossen."));
                finishJob(player);
            }
        } else {
            player.sendMessage(Component.text(Prefix.ERROR + "Der ATM ist zu voll, um mehr Geld aufzunehmen."));
        }
    }

    private static void finishJob(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player);
        playerData.setVariable("job", null);
        playerManager.addExp(player, Utils.random(12, 24));
        Main.getInstance().getCooldownManager().setJobCooldown(player, "geldlieferant", 360);
    }

    @Override
    public void execute(@NotNull VoidPlayer player, @NotNull PlayerData playerData, @NotNull String[] args) throws Exception {
        LocationService locationService = VoidAPI.getService(LocationService.class);
        if (locationService.getDistanceBetweenCoords(player, "geldlieferant") > 5) {
            player.sendMessage(Prefix.ERROR + "Du bist nicht in der nähe des Geldlieferants.");
            return;
        }
        if (Main.getInstance().getCooldownManager().isOnCooldown(player.getPlayer(), "job_geldlieferant")) {
            player.sendMessage(Component.text(PREFIX + "Warte noch " + Utils.getTime(Main.getInstance().getCooldownManager().getRemainingTime(player.getPlayer(), "job_geldlieferant"))));
            return;
        }
        if (playerData.getVariable("job") != null) {
            player.sendMessage(Component.text(Prefix.ERROR + "Du hast bereits einen Job angenommen."));
            return;
        }
        startJob(player);
    }

    private void startJob(VoidPlayer player) {
        player.getData().setVariable("job", "geldlieferant");
        int amount = Utils.random(6000, 8000);
        player.sendMessage(Component.text(PREFIX + "Du hast " + amount + "$ erhalten, fülle damit Geldautomaten auf."));
        player.getData().setVariable("job::geldlieferant::amount", amount);
    }
}
