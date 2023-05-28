package de.polo.metropiacity.Listener;

import com.jeff_media.customblockdata.CustomBlockData;
import de.polo.metropiacity.DataStorage.PlayerData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.Utils.Housing;
import de.polo.metropiacity.Utils.PlayerManager;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
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
                    PersistentDataType<?, ?> type = CustomBlockData.getDataType(container, value);
                    if (type == PersistentDataType.INTEGER) {
                        int number = Objects.requireNonNull(container.get(value, PersistentDataType.INTEGER));
                        if (!Housing.canPlayerInteract(player, number)) {
                            event.setCancelled(true);
                        }
                    }
                    if (type == PersistentDataType.STRING) {
                        String containerString = container.get(value, PersistentDataType.STRING);
                        if (!containerString.contains(playerData.getFaction())) {
                            event.setCancelled(true);
                        }
                    }
                }
            }
        }
    }
}
