package de.polo.metropiacity.commands;

import de.polo.metropiacity.dataStorage.PlayerData;
import de.polo.metropiacity.dataStorage.ShopData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.playerUtils.Shop;
import de.polo.metropiacity.utils.ItemManager;
import de.polo.metropiacity.utils.LocationManager;
import de.polo.metropiacity.utils.PlayerManager;
import de.polo.metropiacity.utils.ServerManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class ShopCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        int shop = LocationManager.isNearShop(player);
        if (shop > 0) {
            ShopData shopData = ServerManager.shopDataMap.get(shop);
            boolean canAccess = false;
            if (shopData.getFaction() == null) {
                canAccess = true;
            } else {
                PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
                if (playerData.getFaction().equalsIgnoreCase(shopData.getFaction())) {
                    canAccess = true;
                }
            }
            if (canAccess) {
                Inventory inv = Bukkit.createInventory(player, 54, "§8» §c" + LocationManager.getShopNameById(shop));
                for (int i = 0; i < 54; i++) {
                    if (i % 9 == 0 || i % 9 == 8 || i < 9 || i > 44) {
                        inv.setItem(i, ItemManager.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, 0, "§8", null));
                    }
                }
                int j = 10;
                for (Object[] row : Shop.shop_items) {
                    if ((int) row[1] == shop) {
                        if (inv.getItem(j) == null) {
                            inv.setItem(j, ItemManager.createItem(Material.valueOf((String) row[2]), 1, 0, row[3].toString().replace("&", "§"), "§8 ➥ §a" + row[4] + "$"));
                            j++;
                        } else {
                            j++;
                            inv.setItem(j, ItemManager.createItem(Material.valueOf((String) row[2]), 1, 0, row[3].toString().replace("&", "§"), "§8 ➥ §a" + row[4] + "$"));
                            j++;
                        }
                    }
                }
                player.openInventory(inv);
            } else {
                player.sendMessage(Main.error + "Du kannst auf diesen Shop nicht zugreifen.");
            }
        }
        return false;
    }
}
