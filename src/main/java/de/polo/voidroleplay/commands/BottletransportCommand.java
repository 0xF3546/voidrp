package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.handler.CommandBase;
import de.polo.voidroleplay.manager.ServerManager;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.utils.Prefix;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static de.polo.voidroleplay.Main.*;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
@CommandBase.CommandMeta(name = "bottletransport")
public class BottletransportCommand extends CommandBase {
    private final String PREFIX = "§8[§2Flaschenlieferant§8]§7 ";
    public BottletransportCommand(@NotNull CommandMeta meta) {
        super(meta);
    }

    @Override
    public void execute(@NotNull Player player, @NotNull PlayerData playerData, @NotNull String[] args) throws Exception {
        if (args.length > 0) {
            if (locationManager.getDistanceBetweenCoords(player, "bar_storage") < 5
                    && playerData.getVariable("job") != null
                    && playerData.getVariable("job") == "flaschentransport"
                    && args[0].equalsIgnoreCase("drop")) {
                drop(player, playerData);
                return;
            }
        }
        if (locationManager.getDistanceBetweenCoords(player, "flaschenlieferant") > 5) {
            player.sendMessage(Prefix.ERROR + "Du bist nicht in der nähe des Flaschenlieferants.");
            return;
        }
        if (Main.getInstance().getCooldownManager().isOnCooldown(player, "job_flaschentransport")) {
            player.sendMessage(Component.text(PREFIX + "Warte noch " + Main.getTime(Main.getInstance().getCooldownManager().getRemainingTime(player, "job_flaschentransport"))));
            return;
        }
        if (playerData.getVariable("job") != null) {
            player.sendMessage(Component.text(Prefix.ERROR + "Du hast bereits einen Job angenommen."));
            return;
        }
        startJob(player, playerData);
    }

    private void startJob(Player player, PlayerData playerData) {
        playerData.setVariable("job", "flaschentransport");
        int amount = Main.random(2, 4);
        player.sendMessage(Component.text(PREFIX + "Du hast " + amount + " erhalten, bringe diese in das Lager der Bar."));
        utils.navigationManager.createNavi(player, "bar_storage", true);
        playerData.setVariable("job::flaschentransport::amount", amount);
    }

    private void drop(Player player, PlayerData playerData) {
        int boxPrice = Main.random(ServerManager.getPayout("flaschenlieferant_kiste_from"), ServerManager.getPayout("flaschenlieferant_kiste_to"));
        int amount = playerData.getVariable("job::flaschentransport::amount");
        if (amount <= 0) return;
        player.sendMessage(Component.text(PREFIX + "§aDu hast eine Kiste abgegeben."));
        playerData.setVariable("job::flaschentransport::amount", amount - 1);
        playerData.addMoney(boxPrice, "Kiste Flaschenlieferant");
        playerManager.addExp(player, Main.random(6, 10));
        if (amount == 1) {
            playerData.setVariable("job", null);
            playerData.setVariable("job::flaschentransport::amount", 0);
            Main.getInstance().getCooldownManager().setJobCooldown(player, "flaschentransport", 360);
            player.sendMessage(PREFIX + "Du hast den Job beendet.");
        }
    }
}
