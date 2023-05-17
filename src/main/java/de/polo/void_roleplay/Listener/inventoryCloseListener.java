package de.polo.void_roleplay.Listener;

import de.polo.void_roleplay.DataStorage.PlayerData;
import de.polo.void_roleplay.Utils.ItemManager;
import de.polo.void_roleplay.Utils.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class inventoryCloseListener implements Listener {
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player =  (Player) event.getPlayer();
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        if (playerData.getVariable("jugendschutz") != null) {
            player.kickPlayer("§cDa du den Jugendschutz nicht aktzeptieren konntest, kannst du auf dem Server §lnicht§c Spielen.\n§cBitte deine Erziehungsberechtigten um Erlabunis oder warte bis du 18 bist.");
        }
    }
}
