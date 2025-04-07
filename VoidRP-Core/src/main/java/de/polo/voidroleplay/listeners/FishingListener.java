package de.polo.voidroleplay.listeners;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.player.services.impl.PlayerManager;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;

public class FishingListener implements Listener {
    private final PlayerManager playerManager;

    public FishingListener(PlayerManager playerManager) {
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
        this.playerManager = playerManager;
    }

    @EventHandler
    public void onFish(PlayerFishEvent event) {
        Player player = event.getPlayer();
        event.setExpToDrop(0);
        if (event.getCaught() != null && event.getCaught() instanceof Item) {
            if (((Item) event.getCaught()).getItemStack().getType().equals(Material.PLAYER_HEAD)) {
                event.setCancelled(true);
            }
        }

        /*if (event.getCaught() != null && event.getCaught() instanceof Item) {
            event.setCancelled(true);
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
                caughtItem.setItemStack(ItemManager.createItem(Material.CHEST, 1, 0, CaseType.BASIC.getDisplayName(), "§8 ➥ §8[§6Rechtsklick§8]§7 Öffnen"));
                player.sendMessage("§8 » §7Du hast eine §6§lCase§7 geangelt!");
            }
            player.getInventory().addItem(caughtItem.getItemStack());

            Main.getInstance().seasonpass.didQuest(player, 20);
            playerManager.addExp(player, EXPType.SKILL_FISHING, Main.random(3, 8));
        }*/

    }

}
