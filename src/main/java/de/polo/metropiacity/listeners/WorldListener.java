package de.polo.metropiacity.listeners;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class WorldListener implements Listener {
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getClickedBlock().getType() == Material.CHEST) {
            if (!player.getGameMode().equals(GameMode.CREATIVE)) {
                event.setCancelled(true);
            }
        }
        if (event.getClickedBlock().getType() == Material.TRAPPED_CHEST) {
            if (!player.getGameMode().equals(GameMode.CREATIVE)) {
                event.setCancelled(true);
            }
        }
        if (event.getClickedBlock().getType() == Material.ENDER_CHEST) {
            if (!player.getGameMode().equals(GameMode.CREATIVE)) {
                event.setCancelled(true);
            }
        }
        if (event.getClickedBlock().getType().name().equals("Sign")) {
            if (!player.getGameMode().equals(GameMode.CREATIVE)) {
                event.setCancelled(true);
            }
        }
        if (event.getClickedBlock().getType() == Material.ANVIL) {
            if (!player.getGameMode().equals(GameMode.CREATIVE)) {
                event.setCancelled(true);
            }
        }
        if (event.getClickedBlock().getType() == Material.DAYLIGHT_DETECTOR) {
            if (!player.getGameMode().equals(GameMode.CREATIVE)) {
                event.setCancelled(true);
            }
        }
        if (event.getClickedBlock().getType() == Material.CRAFTING_TABLE) {
            if (!player.getGameMode().equals(GameMode.CREATIVE)) {
                event.setCancelled(true);
            }
        }
        if (event.getClickedBlock().getType() == Material.SHULKER_BOX) {
            if (!player.getGameMode().equals(GameMode.CREATIVE)) {
                event.setCancelled(true);
            }
        }
        if (event.getClickedBlock().getType() == Material.BARREL) {
            if (!player.getGameMode().equals(GameMode.CREATIVE)) {
                event.setCancelled(true);
            }
        }
        if (event.getAction() == Action.PHYSICAL && event.getClickedBlock() != null) {
            if (event.getClickedBlock().getType() == Material.FARMLAND) {
                event.setCancelled(true);
            }
        }
    }
    @EventHandler
    public void onVehicleDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            event.setCancelled(true);
            return;
        }
        Player player = (Player) event.getEntity();
        if (event.getEntity().getType() == EntityType.PAINTING) {
            if (!player.getGameMode().equals(GameMode.CREATIVE)) {
                event.setCancelled(true);
            }
        }
        if (event.getEntity().getType() == EntityType.ITEM_FRAME) {
            if (!player.getGameMode().equals(GameMode.CREATIVE)) {
                event.setCancelled(true);
            }
        }
    }
}
