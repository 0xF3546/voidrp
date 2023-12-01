package de.polo.metropiacity.dataStorage;

import de.polo.metropiacity.Main;
import de.polo.metropiacity.utils.Game.GangwarUtils;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Timestamp;

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
                    Main.getInstance().gangwarUtils.endGangwar(getZone());
                    cancel();
                }
                if (getSeconds() <= 0) {
                    setSeconds(60);
                    setMinutes(getMinutes() - 1);
                } else {
                    setSeconds(getSeconds() - 1);
                }
            }
        }.runTaskTimer(Main.plugin, 0, 20);
    }
}
