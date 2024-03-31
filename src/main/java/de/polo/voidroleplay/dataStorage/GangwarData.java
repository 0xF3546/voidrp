package de.polo.voidroleplay.dataStorage;

import de.polo.voidroleplay.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Timestamp;
import java.util.HashMap;

public class GangwarData {
    private int id;
    private String zone;
    private String owner;
    private boolean canAttack;
    private Timestamp lastAttack;
    private String attacker;
    private int attackerPoints;
    private int defenderPoints;
    private int minutes;
    private int seconds;
    private final HashMap<Integer, String> captured = new HashMap<>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public boolean isCanAttack() {
        return canAttack;
    }

    public void setCanAttack(boolean canAttack) {
        this.canAttack = canAttack;
    }


    public String getAttacker() {
        return attacker;
    }

    public void setAttacker(String attacker) {
        this.attacker = attacker;
    }

    public int getAttackerPoints() {
        return attackerPoints;
    }

    public void setAttackerPoints(int attackerPoints) {
        this.attackerPoints = attackerPoints;
    }


    public Timestamp getLastAttack() {
        return lastAttack;
    }

    public void setLastAttack(Timestamp lastAttack) {
        this.lastAttack = lastAttack;
    }

    public int getDefenderPoints() {
        return defenderPoints;
    }

    public void setDefenderPoints(int defenderPoints) {
        this.defenderPoints = defenderPoints;
    }

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    public int getSeconds() {
        return seconds;
    }

    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }

    public void startGangwar() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (getMinutes() <= 0 && getSeconds() <= 1) {
                    Main.getInstance().utils.gangwarUtils.endGangwar(getZone());
                    cancel();
                }
                captured.clear();
                for (int i = 1; i <= 3; i++) {
                    Location location = Main.getInstance().locationManager.getLocation("gangwar_capture_" + getZone().toLowerCase().replace(" ", "") + "-" + i);
                    if (location != null) {
                        Main.getInstance().utils.summonCircle(location, 2, Particle.REDSTONE);

                        boolean locationCaptured = false;

                        for (PlayerData playerData : Main.getInstance().playerManager.getPlayers()) {
                            if (playerData.getVariable("gangwar") == null) continue;
                            Player player = Bukkit.getPlayer(playerData.getUuid());

                            if (player != null) {
                                double distance = player.getLocation().distance(location);

                                if (playerData.getFaction().equalsIgnoreCase(getAttacker()) && distance < 5) {
                                    captured.computeIfAbsent(i, k -> getAttacker());
                                    locationCaptured = true;
                                } else if (playerData.getFaction().equalsIgnoreCase(getOwner()) && distance < 5) {
                                    captured.computeIfAbsent(i, k -> getOwner());
                                    locationCaptured = true;
                                }
                            }
                        }

                        if (locationCaptured && getSeconds() % 15 == 0) {
                            for (String faction : captured.values()) {
                                if (faction.equalsIgnoreCase(getAttacker())) {
                                    setAttackerPoints(getAttackerPoints() + 3);
                                } else if (faction.equalsIgnoreCase(getOwner())) {
                                    setDefenderPoints(getDefenderPoints() + 3);
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
