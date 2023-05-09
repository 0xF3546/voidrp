package de.polo.void_roleplay.Listener;

import de.polo.void_roleplay.DataStorage.PlayerData;
import de.polo.void_roleplay.Main;
import de.polo.void_roleplay.PlayerUtils.DeathUtil;
import de.polo.void_roleplay.PlayerUtils.FFA;
import de.polo.void_roleplay.Utils.PlayerManager;
import de.polo.void_roleplay.commands.aduty;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class deathListener implements Listener {
    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = ((Player) event.getEntity()).getPlayer();
            assert player != null;
            PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
            event.setKeepInventory(true);
            if (playerData.getVariable("current_lobby") != null) {
                FFA.useSpawn(player, playerData.getIntVariable("current_lobby"));
            } else {
                playerData.setVariable("death_loc", String.valueOf(player.getLocation()));
                DeathUtil.startDeathTimer(player);
                player.sendMessage(Main.debug_prefix + " Du bist gestorben.");
                aduty.send_message("§c" + player.getName() + "§7 starb.");
                playerData.setDead(true);
            }
        }
    }
}
