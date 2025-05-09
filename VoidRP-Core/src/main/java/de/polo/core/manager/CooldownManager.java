package de.polo.core.manager;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class CooldownManager {
    private final Map<String, Long> cooldowns = new HashMap<>();
    private final Map<String, Long> stringCooldowns = new HashMap<>();

    public boolean isOnCooldown(Player player, String key) {
        if (cooldowns.containsKey(getCooldownKey(player, key))) {
            long cooldownTime = cooldowns.get(getCooldownKey(player, key));
            return cooldownTime - System.currentTimeMillis() > 0;
        }
        return false;
    }

    public void setCooldown(Player player, String key, int seconds) {
        cooldowns.put(getCooldownKey(player, key), System.currentTimeMillis() + (seconds * 1000L));
    }

    public int getRemainingTime(Player player, String key) {
        if (isOnCooldown(player, key)) {
            long cooldownTime = cooldowns.get(getCooldownKey(player, key));
            int remainingTime = (int) Math.ceil((cooldownTime - System.currentTimeMillis()) / 1000.0);
            return remainingTime > 0 ? remainingTime : 0;
        }
        return 0;
    }

    private String getCooldownKey(Player player, String key) {
        return player.getName() + ":" + key;
    }

    public boolean isOnStringCooldown(String string, Object key) {
        if (stringCooldowns.containsKey(getStringCooldownKey(string, key))) {
            long cooldownTime = stringCooldowns.get(getStringCooldownKey(string, key));
            return cooldownTime - System.currentTimeMillis() > 0;
        }
        return false;
    }

    public void setGlobalCooldown(String player, Object key, int seconds) {
        stringCooldowns.put(getStringCooldownKey(player, key), System.currentTimeMillis() + (seconds * 1000L));
    }

    public int getRemainingStringTime(String player, Object key) {
        if (isOnStringCooldown(player, key)) {
            long cooldownTime = stringCooldowns.get(getStringCooldownKey(player, key));
            int remainingTime = (int) Math.ceil((cooldownTime - System.currentTimeMillis()) / 1000.0);
            return remainingTime > 0 ? remainingTime : 0;
        }
        return 0;
    }

    private String getStringCooldownKey(String player, Object key) {
        return player + ":" + key;
    }

    public void setJobCooldown(Player player, String job, int cooldown) {
        setCooldown(player, "job_" + job, cooldown);
    }

    public int getJobCooldownTime(Player player, String job) {
        return getRemainingTime(player, "job_" + job);
    }
}
