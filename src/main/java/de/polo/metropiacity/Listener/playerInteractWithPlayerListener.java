package de.polo.metropiacity.Listener;

import de.polo.metropiacity.Main;
import de.polo.metropiacity.PlayerUtils.ChatUtils;
import de.polo.metropiacity.Utils.PlayerManager;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class playerInteractWithPlayerListener implements Listener {
    @EventHandler
    public void onPlayerInteractWithPlayer(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof Player) {
            Player player = event.getPlayer();
            Player targetplayer = (Player) event.getRightClicked();
            System.out.println(player.getName());
            System.out.println(targetplayer.getName());
            ItemStack item = player.getInventory().getItemInMainHand();
            if (item.getType() == Material.LEAD) {
                if (!Main.cooldownManager.isOnCooldown(player, "handschellen")) {
                    if (!PlayerManager.canPlayerMove(targetplayer)) {
                        ChatUtils.sendGrayMessageAtPlayer(player, player.getName() + " hat " + targetplayer.getName() + " Handschellen angelegt.");
                        PlayerManager.setPlayerMove(targetplayer, false);
                    } else {
                        ChatUtils.sendGrayMessageAtPlayer(player, player.getName() + " hat " + targetplayer.getName() + " Handschellen abgenommen.");
                        PlayerManager.setPlayerMove(targetplayer, true);
                    }
                    Main.cooldownManager.setCooldown(player, "handschellen", 1);
                }
            }
        }
        if ((event.getRightClicked().getType() == EntityType.ARMOR_STAND || event.getRightClicked().getType() == EntityType.ITEM_FRAME || event.getRightClicked().getType() == EntityType.PAINTING) && !PlayerManager.playerDataMap.get(event.getPlayer().getUniqueId().toString()).isAduty()) {
            event.setCancelled(true);
        }
        if (event.getRightClicked() instanceof Villager) {
            Villager villager = (Villager) event.getRightClicked();
            String command = villager.getPersistentDataContainer().get(new NamespacedKey(Main.plugin, "command"), PersistentDataType.STRING);
            assert command != null;
            event.getPlayer().performCommand(command);
        }
    }
}
