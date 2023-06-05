package de.polo.metropiacity.commands;

import de.polo.metropiacity.PlayerUtils.Shop;
import de.polo.metropiacity.Utils.ItemManager;
import de.polo.metropiacity.Utils.LocationManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class shopCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        int shop = LocationManager.isNearShop(player);
        if (shop > 0) {
            Inventory inv = Bukkit.createInventory(player, 54, "§8» §c" + LocationManager.getShopNameById(shop));
            for(int i=0; i<54; i++) {
                if(i%9 == 0 || i%9 == 8 || i<9 || i>44) {
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
            }
        return false;
    }
}
