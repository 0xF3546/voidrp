package de.polo.void_roleplay.Listener;

import de.polo.void_roleplay.PlayerUtils.rubbellose;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class playerInteractListener implements Listener {
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (event.getItem().getItemMeta().getDisplayName().contains("Rubbellos")) {
                rubbellose.startGame(player);
                ItemStack itemStack = event.getItem().clone();
                itemStack.setAmount(1);
                player.getInventory().removeItem(itemStack);

            }
        }
    }
}
