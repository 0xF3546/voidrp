package de.polo.core.game.faction.gangwar;

import de.polo.api.VoidAPI;
import de.polo.api.gangwar.IGangzone;
import de.polo.core.Main;
import de.polo.core.location.services.LocationService;
import de.polo.core.player.entities.PlayerData;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import static de.polo.core.Main.*;

public class Gangwar extends GangwarData {

    @Getter
    @Setter
    private String attacker;

    @Getter
    @Setter
    private String defender;

    @Getter
    @Setter
    private IGangzone gangZone;

    @Getter
    private final int maxMember;

    public Gangwar(int maxMember) {
        this.maxMember = maxMember;
    }

    private int getDiff() {
        return getAttackerPoints() - getDefenderPoints();
    }


    public void start() {
        LocationService locationService = VoidAPI.getService(LocationService.class);
        new BukkitRunnable() {
            @Override
            public void run() {
                if ((getMinutes() <= 0 && getSeconds() <= 1 ) || Math.abs(getDiff()) >= 200) {
                    Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
                        utils.gangwarUtils.endGangwar(gangZone.getName());
                        cancel();
                    });
                    return;
                }
                captured.clear();
                for (int i = 1; i <= 3; i++) {
                    Location location = locationService.getLocation("gangwar_capture_" + gangZone.getName().toLowerCase().replace(" ", "") + "-" + i);
                    if (location != null) {
                        utils.summonCircle(location, 2, Particle.REDSTONE);

                        boolean locationCaptured = false;

                        for (PlayerData playerData : playerManager.getPlayers()) {
                            if (playerData.getVariable("gangwar") == null) continue;
                            if (playerData.getFaction() == null) continue;
                            Player player = Bukkit.getPlayer(playerData.getUuid());

                            if (player != null && !playerData.isDead()) {
                                double distance = player.getLocation().distance(location);

                                if (playerData.getFaction().equalsIgnoreCase(getAttacker()) && distance < 5) {
                                    captured.computeIfAbsent(i, k -> getAttacker());
                                    locationCaptured = true;
                                } else if (playerData.getFaction().equalsIgnoreCase(gangZone.getOwner()) && distance < 5) {
                                    captured.computeIfAbsent(i, k -> gangZone.getOwner());
                                    locationCaptured = true;
                                }
                            }
                        }

                        if (locationCaptured && getSeconds() % 15 == 0) {
                            for (String faction : captured.values()) {
                                if (faction.equalsIgnoreCase(getAttacker())) {
                                    setAttackerPoints(getAttackerPoints() + 1);
                                } else if (faction.equalsIgnoreCase(gangZone.getOwner())) {
                                    setDefenderPoints(getDefenderPoints() + 1);
                                }
                            }
                        }
                    }
                }
                if (getSeconds() <= 0) {
                    setSeconds(60);
                    setMinutes(getMinutes() - 1);
                } else {
                    setSeconds(getSeconds() - 1);
                }
            }
        }.runTaskTimerAsynchronously(Main.getInstance(), 0, 20);
    }
}
