package de.polo.void_roleplay.Listener;

import de.polo.void_roleplay.Main;
import de.polo.void_roleplay.Utils.PlayerManager;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class houseLockListener implements Listener {
    @EventHandler
    public void onHouseOpen(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        Player player = event.getPlayer();
        if (block != null) {
            System.out.println(block.getType().toString());
            if (block.getType().toString().contains("DOOR") && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                int centerX = block.getLocation().getBlockX();
                int centerY = block.getLocation().getBlockY();
                int centerZ = block.getLocation().getBlockZ();
                World world = block.getWorld();
                for (int x = centerX - 3; x <= centerX + 3; x++) {
                    for (int y = centerY - 3; y <= centerY + 3; y++) {
                        for (int z = centerZ - 3; z <= centerZ + 3; z++) {
                            Location location = new Location(world, x, y, z);
                            Block block2 = location.getBlock();
                            if (block2.getType().toString().contains("SIGN")) {
                                System.out.println("sign found");
                                Sign sign = (Sign) block2.getState();
                                if (!sign.getLine(2).contains(player.getName())) {
                                    player.sendMessage(Main.debug_prefix + " Owner not found.");
                                    event.setCancelled(true);
                                } else {
                                    player.sendMessage(Main.debug_prefix + " Owner found.");
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
