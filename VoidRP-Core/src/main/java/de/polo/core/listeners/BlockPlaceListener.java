package de.polo.core.listeners;

import de.polo.core.Main;
import lombok.SneakyThrows;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class BlockPlaceListener implements Listener {
    public BlockPlaceListener() {
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }

    @EventHandler
    public void onBlockBreak(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (!player.getGameMode().equals(GameMode.CREATIVE)) {
            event.setCancelled(true);
        } else if (event.getBlock().getType().name().contains("SHULKER")) {
            logBlock(player, event.getBlock());
            // event.setCancelled(true);
        }
    }

    @SneakyThrows
    public void logBlock(Player player, Block block) {
        Connection connection = Main.getInstance().coreDatabase.getConnection();
        PreparedStatement statement = connection.prepareStatement("INSERT INTO block_logs (uuid, block, x, y, z, type) VALUES (?, ?, ?, ?, ?, ?)");
        statement.setString(1, player.getUniqueId().toString());
        statement.setString(2, block.getType().name());
        statement.setInt(3, block.getX());
        statement.setInt(4, block.getY());
        statement.setInt(5, block.getZ());
        statement.setString(6, "placed");
        statement.execute();
        statement.close();
        connection.close();
    }
}
