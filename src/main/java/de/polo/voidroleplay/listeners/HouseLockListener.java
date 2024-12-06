package de.polo.voidroleplay.listeners;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.manager.PlayerManager;
import de.polo.voidroleplay.utils.Utils;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class HouseLockListener implements Listener {
    private final PlayerManager playerManager;
    private final Utils utils;
    public HouseLockListener(PlayerManager playerManager, Utils utils) {
        this.playerManager = playerManager;
        this.utils = utils;
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }
    @EventHandler
    public void onHouseOpen(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        Player player = event.getPlayer();
        /*if (block != null) {
            if (block.getType().toString().contains("DOOR") && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                NamespacedKey value = new NamespacedKey(Main.plugin, "value");
                PersistentDataContainer container = new CustomBlockData(block, Main.plugin);
                PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
                if (!playerData.isAduty()) {
                    if (type == PersistentDataType.INTEGER) {
                        int number = Objects.requireNonNull(container.get(value, PersistentDataType.INTEGER));
                        if (!utils.housing.canPlayerInteract(player, number)) {
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
        }*/
    }
}
