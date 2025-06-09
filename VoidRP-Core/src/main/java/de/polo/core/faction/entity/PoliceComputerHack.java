package de.polo.core.faction.entity;

import de.polo.api.Utils.ApiUtils;
import de.polo.api.Utils.enums.Prefix;
import de.polo.api.VoidAPI;
import de.polo.api.player.VoidPlayer;
import de.polo.core.faction.service.FactionService;
import de.polo.core.location.services.LocationService;
import de.polo.core.utils.Utils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class PoliceComputerHack {
    public static final int MAX_DISTANCE = 20;
    public static final int HACKING_DURATION_MINUTES = 10;
    public static final int COOLDOWN_HOURS = 6;
    public static final List<VoidPlayer> targets = new ObjectArrayList<>();
    public static LocalDateTime lastHackAttempt = ApiUtils.getTime().minusHours(COOLDOWN_HOURS);
    public static boolean isActive = false;
    private static int dots = 0;

    public static void doTick() {
        LocationService locationService = VoidAPI.getService(LocationService.class);
        Location policeComputerLocation = locationService.getLocation("policecomputer");
        if (dots >= 3) dots = 0;
        else dots++;
        for (VoidPlayer target : targets) {
            Utils.sendActionBar(target.getPlayer(), "§9Hacke Polizeicomputer" + "§7.".repeat(dots) + " (§7" + (HACKING_DURATION_MINUTES - ApiUtils.getTime().until(lastHackAttempt.plusMinutes(HACKING_DURATION_MINUTES), java.time.temporal.ChronoUnit.MINUTES)) + " Minuten§9)");
            if (target.getPlayer().getLocation().distance(policeComputerLocation) > MAX_DISTANCE) {
                targets.remove(target);
                target.sendMessage("§cDu bist zu weit entfernt vom Polizeicomputer. Deine Akte wird nun nicht mehr gelöscht.", Prefix.POLICE_COMPUTER);
                continue;
            }
            if (ApiUtils.getTime().isAfter(lastHackAttempt.plusMinutes(HACKING_DURATION_MINUTES))) {
                target.getData().clearWanted();
                target.sendMessage("§aDeine Akte wurde erfolgreich gelöscht.", Prefix.POLICE_COMPUTER);
                targets.remove(target);
                isActive = false;
            }
        }
        if (ApiUtils.getTime().isAfter(lastHackAttempt.plusMinutes(HACKING_DURATION_MINUTES))) {
            FactionService factionService = VoidAPI.getService(FactionService.class);
            factionService.sendCustomMessageToFactions(Prefix.POLICE_COMPUTER.getPrefix() + "§cDer Polizeicomputer wurde erfolgreich gehackt.", "Polizei", "FBI");
        }
    }
}
