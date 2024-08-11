package de.polo.voidroleplay.listeners;

import de.polo.voidroleplay.Main;
import lombok.SneakyThrows;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class WorldListener implements Listener {
    public WorldListener() {
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }
    @SneakyThrows
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getClickedBlock() == null) {
            return;
        }
        if (event.getClickedBlock().getType().name().contains("SHULKER")) {
            Block block = event.getClickedBlock();
            Connection connection = Main.getInstance().mySQL.getConnection();
            PreparedStatement statement = connection.prepareStatement("INSERT INTO block_logs (uuid, block, x, y, z, type) VALUES (?, ?, ?, ?, ?, ?)");
            statement.setString(1, player.getUniqueId().toString());
            statement.setString(2, block.getType().name());
            statement.setInt(3, block.getX());
            statement.setInt(4, block.getY());
            statement.setInt(5, block.getZ());
            statement.setString(6, "interacted");
            statement.execute();
            statement.close();
            connection.close();
        }
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
        if (event.getClickedBlock() instanceof Sign) {
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
        if (event.getClickedBlock().getType() == Material.BEACON) {
            if (!player.getGameMode().equals(GameMode.CREATIVE)) {
                event.setCancelled(true);
            }
        }
        if (event.getClickedBlock().getType() == Material.HOPPER) {
            if (!player.getGameMode().equals(GameMode.CREATIVE)) {
                event.setCancelled(true);
            }
        }
        if (event.getClickedBlock().getType() == Material.SHULKER_SHELL) {
            if (!player.getGameMode().equals(GameMode.CREATIVE)) {
                event.setCancelled(true);
            }
        }
        if (event.getClickedBlock().getType() == Material.FURNACE) {
            if (!player.getGameMode().equals(GameMode.CREATIVE)) {
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
