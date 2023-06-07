package de.polo.metropiacity.Listener;

import de.polo.metropiacity.Main;
import de.polo.metropiacity.Utils.ItemManager;
import de.polo.metropiacity.Utils.PlayerManager;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

public class fishingListener implements Listener {

        @EventHandler
        public void onFish(PlayerFishEvent event) {
            Player player = event.getPlayer();
            event.setExpToDrop(0);

            if (event.getCaught() != null && event.getCaught() instanceof Item) {
                Item caughtItem = (Item) event.getCaught();

                double randomNumber = Math.random() * 100;

                if (randomNumber < 70) {
                    caughtItem.setItemStack(new ItemStack(Material.COD));
                    player.sendMessage("§8 » §7Du hast einen §9Kabeljau§7 geangelt!");
                } else if (randomNumber < 80) {
                    caughtItem.setItemStack(new ItemStack(Material.TROPICAL_FISH));
                    player.sendMessage("§8 » §7Du hast einen §6Tropenfisch§7 geangelt!");
                } else if (randomNumber < 86) {
                    caughtItem.setItemStack(new ItemStack(Material.SALMON));
                    player.sendMessage("§8 » §7Du hast einen §6Lachs§7 geangelt!");
                } else if (randomNumber < 90) {
                    caughtItem.setItemStack(new ItemStack(Material.PUFFERFISH));
                    player.sendMessage("§8 » §7Du hast einen §eKugelfisch§7 geangelt!");
                } else if (randomNumber < 96) {
                    caughtItem.setItemStack(ItemManager.createItem(Material.CHEST, 1, 0, "§b§lXP-Case", "§8 ➥ §8[§6Rechtsklick§8]§7 Öffnen"));
                    player.sendMessage("§8 » §7Du hast eine §b§lXP-Case§7 geangelt!");
                } else {
                    caughtItem.setItemStack(ItemManager.createItem(Material.CHEST, 1, 0, "§6§lCase", "§8 ➥ §8[§6Rechtsklick§8]§7 Öffnen"));
                    player.sendMessage("§8 » §7Du hast eine §6§lCase§7 geangelt!");
                }
            }
        }

}
