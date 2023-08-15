package de.polo.metropiacity.listeners;

import de.polo.metropiacity.Main;
import de.polo.metropiacity.dataStorage.PlayerData;
import de.polo.metropiacity.utils.ItemManager;
import de.polo.metropiacity.utils.PlayerManager;
import de.polo.metropiacity.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;
import java.util.Collection;

public class PlayerSwapHandItemsListener implements Listener {
    @EventHandler
    public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        event.setCancelled(true);
        if (!player.isSneaking()) {
            return;
        }
        Collection<Entity> entities = player.getWorld().getNearbyEntities(player.getLocation(), 3, 3, 3);
        Item nearestSkull = null;
        double nearestDistance = Double.MAX_VALUE;
        for (Entity entity : entities) {
            if (entity instanceof Item && entity.getType() == EntityType.DROPPED_ITEM) {
                Item item = (Item) entity;
                if (item.getItemStack().getType() == Material.PLAYER_HEAD) {
                    double distance = item.getLocation().distance(player.getLocation());
                    if (distance < nearestDistance) {
                        if (!item.getCustomName().contains("§8")) {
                            nearestSkull = item;
                            nearestDistance = distance;
                        }
                    }
                }
            }
        }
        PlayerData playerData = PlayerManager.getPlayerData(player);
        playerData.setVariable("current_inventory", "tasche");
        if (nearestSkull == null) {
            Inventory inv = Bukkit.createInventory(player, 27, "§8 » §6Deine Tasche");
            inv.setItem(11, ItemManager.createItem(Material.BOOK, 1, 0, "§ePortmonee", "§8 ➥ §7" + playerData.getBargeld() + "$"));
            for (int i = 0; i < 27; i++) {
                if (inv.getItem(i) == null) {
                    inv.setItem(i, ItemManager.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, 0, "", ""));
                }
            }
            player.openInventory(inv);
            return;
        }
        SkullMeta skullMeta = (SkullMeta) nearestSkull.getItemStack().getItemMeta();
        final Item skull = nearestSkull;
        Player targetplayer = Bukkit.getPlayer(skullMeta.getOwningPlayer().getUniqueId());
        System.out.println(targetplayer.getName());
        PlayerData targetplayerData = PlayerManager.getPlayerData(targetplayer);
        Inventory inv = Bukkit.createInventory(player, 27, "§8 » §7Bewusstlose Person (" + nearestSkull.getName() + ")");
        inv.setItem(11, ItemManager.createItem(Material.BOOK, 1, 0, "§ePortmonee", "§8 ➥ §7" + Utils.toDecimalFormat(targetplayerData.getBargeld()) + "$"));
        ItemMeta meta = inv.getItem(11).getItemMeta();
        meta.setLore(Arrays.asList("§8 ➥ §7" + targetplayerData.getBargeld() + "$", "", "§8[§6Linksklick§8]§7 Geld rauben"));
        inv.getItem(11).setItemMeta(meta);
        inv.setItem(12, ItemManager.createItem(Material.RED_DYE, 1, 0, "§cStabilisieren", null));
        for (int i = 0; i < 27; i++) {
            if (inv.getItem(i) == null) {
                inv.setItem(i, ItemManager.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, 0, "", ""));
            }
        }
        playerData.setVariable("current_inventory", "tasche_" + targetplayer.getUniqueId().toString());
        player.openInventory(inv);
    }
}
