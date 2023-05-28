package de.polo.metropiacity.Listener;

import de.polo.metropiacity.PlayerUtils.DeathUtil;
import de.polo.metropiacity.DataStorage.PlayerData;
import de.polo.metropiacity.Utils.*;
import de.polo.metropiacity.commands.aduty;
import org.bukkit.Effect;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.sql.SQLException;

public class  JoinEvent implements Listener {
    @EventHandler
    public void onJoin(PlayerJoinEvent event) throws SQLException {
        Player player = event.getPlayer();
        String uuid = player.getUniqueId().toString();
        PlayerData playerData = PlayerManager.playerDataMap.get(uuid);
        event.setJoinMessage("");
        if (PlayerManager.isCreated(String.valueOf(player.getUniqueId()))) {
            PlayerManager.loadPlayer(player);
            if (DeathUtil.isDead(player)) {
                DeathUtil.killPlayer(player);
            }
            aduty.send_message("§c" + player.getName() + "§7 hat den Server betreten.");
            player.sendMessage("§6Willkommen zurück, " + player.getName() + "!");
            Vehicles.spawnPlayerVehicles(player);
            ServerManager.updateTablist(null);
        } else {
            player.sendMessage(" ");
            player.sendMessage("§6MetropiaCity §8»§7 Herzlich Wilkommen in Metropia - der Stadt mit Zukunft, " + player.getName() + ".");
            player.sendMessage(" ");
            LocationManager.useLocation(player, "Spawn");
            player.getWorld().playEffect(player.getLocation().add(0.0D, 0.0D, 0.0D), Effect.ENDER_SIGNAL, 1);
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1,2);
            aduty.send_message("§c" + player.getName() + "§7 hat sich gerade registriert.");
        }
    }
}
