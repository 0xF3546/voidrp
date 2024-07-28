package de.polo.voidroleplay.game.faction.gangwar;

import de.polo.api.faction.gangwar.IGangzone;
import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.PlayerData;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

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


    public void start() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (getMinutes() <= 0 && getSeconds() <= 1) {
                    Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
                        Main.getInstance().utils.gangwarUtils.endGangwar(gangZone.getName());
                    });
                    cancel();
                }
                captured.clear();
                for (int i = 1; i <= 3; i++) {
                    Location location = Main.getInstance().locationManager.getLocation("gangwar_capture_" + gangZone.getName().toLowerCase().replace(" ", "") + "-" + i);
                    if (location != null) {
                        Main.getInstance().utils.summonCircle(location, 2, Particle.REDSTONE);

                        boolean locationCaptured = false;

                        for (PlayerData playerData : Main.getInstance().playerManager.getPlayers()) {
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
        }.runTaskTimerAsynchronously(Main.plugin, 0, 20);
    }
}
