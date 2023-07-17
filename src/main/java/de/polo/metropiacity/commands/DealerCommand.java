package de.polo.metropiacity.commands;

import de.polo.metropiacity.DataStorage.DealerData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.Utils.ItemManager;
import de.polo.metropiacity.Utils.LocationManager;
import de.polo.metropiacity.Utils.ServerManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class DealerCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        int dealer = LocationManager.isNearDealer(player);
        if (dealer != 0) {
            DealerData data = ServerManager.dealerDataMap.get(dealer);
            Inventory inv = Bukkit.createInventory(player, 54, "§8 » §e" + data.getType());
            for (int i = 0; i < 54; i++) {
                inv.setItem(i, ItemManager.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, 0, "§8", null));
            }
            player.openInventory(inv);
        } else {
            player.sendMessage(Main.error + "Du bist nicht in der nähe eines Dealers.");
        }
        return false;
    }
}
