package de.polo.void_roleplay.Listener;

import com.jeff_media.customblockdata.CustomBlockData;
import de.polo.void_roleplay.DataStorage.PlayerData;
import de.polo.void_roleplay.Main;
import de.polo.void_roleplay.Utils.PlayerManager;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Objects;

public class houseLockListener implements Listener {
    @EventHandler
    public void onHouseOpen(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        Player player = event.getPlayer();
        if (block != null) {
            if (block.getType().toString().contains("DOOR") && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                NamespacedKey value = new NamespacedKey(Main.plugin, "value");
                PersistentDataContainer container = new CustomBlockData(block, Main.plugin);
                PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
                if (!playerData.isAduty()) {
                    if (!Objects.requireNonNull(container.get(value, PersistentDataType.STRING)).contains(playerData.getFaction())) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }
}
