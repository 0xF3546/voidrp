package de.polo.void_roleplay.Listener;

import de.polo.void_roleplay.PlayerUtils.DeathUtil;
import de.polo.void_roleplay.DataStorage.PlayerData;
import de.polo.void_roleplay.Utils.*;
import de.polo.void_roleplay.commands.aduty;
import org.bukkit.Bukkit;
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
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.setPlayerListHeader("§6§lVoid Roleplay §8- §cV1.0");
            p.setPlayerListFooter("§8» §e" + Bukkit.getOnlinePlayers().size() + "§8/§6" + Bukkit.getMaxPlayers() + "§8«");
        }
        if (PlayerManager.isCreated(String.valueOf(player.getUniqueId()))) {
            PlayerManager.loadPlayer(player);
            if (DeathUtil.isDead(player)) {
                DeathUtil.killPlayer(player);
            }
            Vehicles.spawnPlayerVehicles(player);
            aduty.send_message("§c" + player.getName() + "§7 hat den Server betreten.");
            player.sendMessage("§6Wilkommen zurück, " + player.getName() + "!");
        } else {
            player.sendMessage(" ");
            player.sendMessage("§6Void Roleplay §8»§7 Herzlich Wilkommen auf Void Roleplay - Roleplay mit Stil, " + player.getName() + ".");
            player.sendMessage(" ");
            LocationManager.useLocation(player, "Spawn");
            player.getWorld().playEffect(player.getLocation().add(0.0D, 0.0D, 0.0D), Effect.ENDER_SIGNAL, 1);
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1,2);
            aduty.send_message("§c" + player.getName() + "§7 hat sich gerade registriert.");
        }
    }
}
