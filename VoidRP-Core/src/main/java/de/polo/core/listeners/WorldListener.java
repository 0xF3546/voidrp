package de.polo.core.listeners;

import de.polo.core.Main;
import lombok.SneakyThrows;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.EnumSet;

public class WorldListener implements Listener {
    private static final EnumSet<Material> BLOCK_INTERACT_RESTRICTED = EnumSet.of(
            Material.CHEST, Material.TRAPPED_CHEST, Material.ENDER_CHEST, Material.ANVIL,
            Material.DAYLIGHT_DETECTOR, Material.CRAFTING_TABLE, Material.SHULKER_BOX,
            Material.BARREL, Material.BREWING_STAND, Material.BEACON, Material.HOPPER,
            Material.FURNACE, Material.TURTLE_EGG, Material.CANDLE, Material.FLOWER_POT
    );

    public WorldListener() {
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }

    @SneakyThrows
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        if (block == null) return;

        if (block.getType().name().contains("SHULKER")) {
            try (Connection connection = Main.getInstance().coreDatabase.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "INSERT INTO block_logs (uuid, block, x, y, z, type) VALUES (?, ?, ?, ?, ?, ?)")) {

                statement.setString(1, player.getUniqueId().toString());
                statement.setString(2, block.getType().name());
                statement.setInt(3, block.getX());
                statement.setInt(4, block.getY());
                statement.setInt(5, block.getZ());
                statement.setString(6, "interacted");
                statement.execute();
            }
        }

        if (BLOCK_INTERACT_RESTRICTED.contains(block.getType())) {
            if (player.getGameMode() != GameMode.CREATIVE) {
                event.setCancelled(true);
            }
        }

        if (block.getType() == Material.FARMLAND && event.getAction() == Action.PHYSICAL) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            event.setCancelled(true);
            return;
        }

        Player player = (Player) event.getEntity();

        if (event.getEntityType() == EntityType.PAINTING || event.getEntityType() == EntityType.ITEM_FRAME) {
            if (player.getGameMode() != GameMode.CREATIVE) {
                event.setCancelled(true);
            }
        }
    }
}
