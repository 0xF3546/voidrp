package de.polo.metropiacity.listeners;

import de.polo.metropiacity.dataStorage.PlayerData;
import de.polo.metropiacity.utils.PlayerManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class InventoryCloseListener implements Listener {
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player =  (Player) event.getPlayer();
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        if (playerData.getVariable("jugendschutz") != null) {
            player.kickPlayer("§cDa du den Jugendschutz nicht aktzeptieren konntest, kannst du auf dem Server §lnicht§c Spielen.\n§cBitte deine Erziehungsberechtigten um Erlabunis oder warte bis du 18 bist.");
        }
    }
}
