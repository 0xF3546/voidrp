package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.handler.CommandBase;
import de.polo.voidroleplay.manager.ServerManager;
import de.polo.voidroleplay.storage.ATM;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.utils.Utils;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static de.polo.voidroleplay.Main.*;

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

    @Override
    public void execute(@NotNull Player player, @NotNull PlayerData playerData, @NotNull String[] args) throws Exception {
        if (locationManager.getDistanceBetweenCoords(player, "geldlieferant") > 5) {
            player.sendMessage(Prefix.ERROR + "Du bist nicht in der nähe des Geldlieferants.");
            return;
        }
        if (Main.getInstance().getCooldownManager().isOnCooldown(player, "job_geldlieferant")) {
            player.sendMessage(Component.text(PREFIX + "Warte noch " + Utils.getTime(Main.getInstance().getCooldownManager().getRemainingTime(player, "job_geldlieferant"))));
            return;
        }
        if (playerData.getVariable("job") != null) {
            player.sendMessage(Component.text(Prefix.ERROR + "Du hast bereits einen Job angenommen."));
            return;
        }
        startJob(player, playerData);
    }

    private void startJob(Player player, PlayerData playerData) {
        playerData.setVariable("job", "geldlieferant");
        int amount = Utils.random(6000, 8000);
        player.sendMessage(Component.text(PREFIX + "Du hast " + amount + "$ erhalten, fülle damit Geldautomaten auf."));
        playerData.setVariable("job::geldlieferant::amount", amount);
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
}
