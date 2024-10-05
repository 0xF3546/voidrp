package de.polo.voidroleplay.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.UUID;

public class AdrenalinListener  implements Listener {

    private final HashMap<UUID, Long> playerKillTimestamps = new HashMap<>();
    private final HashMap<UUID, UUID> lastKilledPlayer = new HashMap<>();

    @EventHandler
    public void onPlayerKill(PlayerDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null || killer.getGameMode() != GameMode.SURVIVAL) return;

        Player killed = event.getEntity();
        UUID killerId = killer.getUniqueId();
        UUID killedId = killed.getUniqueId();


        if (playerKillTimestamps.containsKey(killerId) && System.currentTimeMillis() - playerKillTimestamps.get(killerId) <= 120_000
                && !lastKilledPlayer.get(killerId).equals(killedId)) {

            triggerAdrenalineRush(killer);
        }


        playerKillTimestamps.put(killerId, System.currentTimeMillis());
        lastKilledPlayer.put(killerId, killedId);
    }

    private void triggerAdrenalineRush(Player player) {

        player.sendTitle("§x§0§0§F§F§E§0§l§oA§x§1§1§E§D§E§2§l§od§x§2§2§D§B§E§4§l§or§x§3§2§C§8§E§7§l§oe§x§4§3§B§6§E§9§l§on§x§5§4§A§4§E§B§l§oa§x§6§5§9§2§E§D§l§ol§x§7§6§8§0§F§0§l§oi§x§8§6§6§D§F§2§l§on§x§9§7§5§B§F§4§l§or§x§A§8§4§9§F§6§l§oa§x§B§9§3§7§F§8§l§ou§x§C§9§2§4§F§B§l§os§x§D§A§1§2§F§D§l§oc§x§E§B§0§0§F§F§l§oh", "", 10, 20, 10);


        AttributeInstance healthAttribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (healthAttribute != null) {
            healthAttribute.setBaseValue(healthAttribute.getBaseValue() + 10);
        }


        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 5 * 20, 2));


        new BukkitRunnable() {
            @Override
            public void run() {
                if (healthAttribute != null) {
                    healthAttribute.setBaseValue(healthAttribute.getBaseValue() - 10); // Entfernen der 5 Herzen
                }
            }
        }.runTaskLater(Bukkit.getPluginManager().getPlugin("VoidRoleplay"), 5 * 20L);
    }
}
