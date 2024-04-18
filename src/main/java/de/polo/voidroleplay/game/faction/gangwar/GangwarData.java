package de.polo.voidroleplay.game.faction.gangwar;

import java.sql.Timestamp;
import java.util.HashMap;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class GangwarData  {
    private boolean canAttack;
    private int attackerPoints;
    private int defenderPoints;
    private int minutes;
    private int seconds;
    public final HashMap<Integer, String> captured = new HashMap<>();


    public boolean isCanAttack() {
        return canAttack;
    }

    public void setCanAttack(boolean canAttack) {
        this.canAttack = canAttack;
    }

    public int getAttackerPoints() {
        return attackerPoints;
    }

    public void setAttackerPoints(int attackerPoints) {
        this.attackerPoints = attackerPoints;
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
}
