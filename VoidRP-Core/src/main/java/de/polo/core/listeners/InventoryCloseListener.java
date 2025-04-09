package de.polo.core.listeners;

import de.polo.core.Main;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.utils.Event;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

import static de.polo.core.Main.playerManager;

@Event
public class InventoryCloseListener implements Listener {

    public InventoryCloseListener() {
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        if (!player.isOnline()) {
            return;
        }
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (playerData == null) return;
        if (playerData.getVariable("jugendschutz") == null) return;
        playerManager.kickPlayer(player, "§cDa du den Jugendschutz nicht aktzeptieren konntest, kannst du auf dem Server §lnicht§c Spielen.\n§cBitte deine Erziehungsberechtigten um Erlabunis oder warte bis du 18 bist.");
    }
}
