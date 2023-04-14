package de.polo.void_roleplay.Listener;

import de.polo.void_roleplay.Main;
import de.polo.void_roleplay.PlayerUtils.DeathUtil;
import de.polo.void_roleplay.Utils.PlayerManager;
import de.polo.void_roleplay.commands.aduty;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class deathListener implements Listener {
    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = ((Player) event.getEntity()).getPlayer();
            event.setKeepInventory(true);
            assert player != null;
            Location loc = player.getLocation();
            DeathUtil.startDeathTimer(player);
            player.sendMessage(Main.debug_prefix + " Du bist gestorben.");
            player.spigot().respawn();
            player.teleport(loc);
            PlayerManager.setPlayerMove(player, false);
            aduty.send_message("§c" + player.getName() + "§7 starb.");
        }
    }
}
