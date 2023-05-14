package de.polo.void_roleplay.Listener;

import de.polo.void_roleplay.DataStorage.PlayerData;
import de.polo.void_roleplay.Main;
import de.polo.void_roleplay.PlayerUtils.DeathUtil;
import de.polo.void_roleplay.PlayerUtils.FFA;
import de.polo.void_roleplay.Utils.ItemManager;
import de.polo.void_roleplay.Utils.PlayerManager;
import de.polo.void_roleplay.commands.aduty;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class deathListener implements Listener {
    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
            Player player = event.getEntity().getPlayer();
            assert player != null;
            PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
            event.setKeepInventory(true);
            if (playerData.getVariable("current_lobby") != null) {
                FFA.useSpawn(player, playerData.getIntVariable("current_lobby"));
            } else {
                playerData.setDeathLocation(player.getLocation());
                DeathUtil.startDeathTimer(player);
                aduty.send_message("§c" + player.getName() + "§7 starb.");
                playerData.setDead(true);
                ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta meta = (SkullMeta) skull.getItemMeta();
                assert meta != null;
                meta.setOwningPlayer(Bukkit.getOfflinePlayer(player.getUniqueId()));
                meta.setDisplayName("§7" + player.getName());
                skull.setItemMeta(meta);

                Item item = player.getLocation().getWorld().dropItemNaturally(player.getLocation(), skull);
                DeathUtil.deathSkulls.put(player.getUniqueId().toString(), item);
                Entity entity = item;
                entity.setCustomName("§7" + player.getName());
                entity.setCustomNameVisible(true);

            }
        }
}
