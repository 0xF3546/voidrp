package de.polo.void_roleplay.Listener;

import de.polo.void_roleplay.PlayerUtils.ChatUtils;
import de.polo.void_roleplay.Utils.PlayerManager;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
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
                ChatUtils.sendGrayMessageAtPlayer(player, player.getName() + " hat " + targetplayer.getName() + " handschellen angelegt.");
            }
        }
        if (event.getRightClicked() instanceof ArmorStand) {
            event.setCancelled(true);
        }
    }
}
