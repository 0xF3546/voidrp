package de.polo.void_roleplay.Listener;

import de.polo.void_roleplay.Main;
import de.polo.void_roleplay.PlayerUtils.ChatUtils;
import de.polo.void_roleplay.Utils.PlayerManager;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;

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
    }
}
