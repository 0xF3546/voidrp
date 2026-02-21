package de.polo.core.faction.commands;

import de.polo.api.utils.ApiUtils;
import de.polo.api.utils.enums.Prefix;
import de.polo.api.VoidAPI;
import de.polo.api.player.VoidPlayer;
import de.polo.core.faction.entity.PoliceComputerHack;
import de.polo.core.faction.service.FactionService;
import de.polo.core.handler.CommandBase;
import de.polo.core.location.services.LocationService;
import de.polo.core.player.entities.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
@CommandBase.CommandMeta(
        name = "hackpolicecomputer",
        usage = "/hackpolicecomputer [Spieler...]"
)
public class HackPoliceComputerCommand extends CommandBase {
    public HackPoliceComputerCommand(@NotNull CommandMeta meta) {
        super(meta);
    }

    @Override
    public void execute(@NotNull VoidPlayer player, @NotNull PlayerData playerData, @NotNull String[] args) throws Exception {
        LocationService locationService = VoidAPI.getService(LocationService.class);
        Location policeComputerLocation = locationService.getLocation("policecomputer");
        if (policeComputerLocation.distance(player.getPlayer().getLocation()) > 5) {
            player.sendMessage("Du musst dich in der nähe eines Polizeicomputers befinden!", Prefix.ERROR);
            return;
        }
        if (args.length < 1) {
            showSyntax(player);
            return;
        }
        if (PoliceComputerHack.lastHackAttempt.isAfter(ApiUtils.getTime().minusHours(PoliceComputerHack.COOLDOWN_HOURS))) {
            player.sendMessage("Du kannst erst wieder in " + (PoliceComputerHack.COOLDOWN_HOURS - ApiUtils.getTime().until(PoliceComputerHack.lastHackAttempt, java.time.temporal.ChronoUnit.HOURS)) + " Stunden hacken!", Prefix.ERROR);
            return;
        }
        FactionService factionService = VoidAPI.getService(FactionService.class);
        int executives = factionService.getOnlineMemberCount("FBI") + factionService.getOnlineMemberCount("Polizei");
        if (executives < 3) {
            player.sendMessage("Es müssen mindestens 3 Beamte online sein, um den Polizeicomputer zu hacken!", Prefix.ERROR);
            return;
        }
        for (String targetName : args) {
            Player bukkitTarget = Bukkit.getPlayer(targetName);
            if (bukkitTarget == null) {
                player.sendMessage("Der Spieler " + targetName + " ist nicht online!", Prefix.ERROR);
                continue;
            }
            VoidPlayer target = VoidAPI.getPlayer(bukkitTarget);
            if (target.getPlayer().getLocation().distance(policeComputerLocation) > PoliceComputerHack.MAX_DISTANCE) {
                player.sendMessage("Der Spieler " + targetName + " ist zu weit entfernt!", Prefix.ERROR);
                continue;
            }
            if (target.getData().getWanted() == null) {
                player.sendMessage("Der Spieler " + targetName + " ist nicht gesucht!", Prefix.ERROR);
                continue;
            }
            PoliceComputerHack.lastHackAttempt = ApiUtils.getTime();
            target.sendMessage(player.getName() + " fängt an den Polizeicomputer zu hacken!", Prefix.POLICE_COMPUTER);
            PoliceComputerHack.targets.add(target);
        }
        player.sendMessage("Du fängst an den Polizeicomputer zu hacken!", Prefix.POLICE_COMPUTER);
        PoliceComputerHack.isActive = true;
        factionService.sendCustomMessageToFactions(Prefix.POLICE_COMPUTER.getPrefix() + "Das Alarmsystem des Polizeicomputers ist angeschlagen.", "Polizei", "FBI");

    }
}
