package de.polo.voidroleplay.listeners;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.utils.PlayerManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class InventoryCloseListener implements Listener {
    private final PlayerManager playerManager;
    public InventoryCloseListener(PlayerManager playerManager) {
        this.playerManager = playerManager;
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player =  (Player) event.getPlayer();
        if (!player.isOnline()) {
            return;
        }
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (playerData.getVariable("jugendschutz") != null) {
            player.kickPlayer("§cDa du den Jugendschutz nicht aktzeptieren konntest, kannst du auf dem Server §lnicht§c Spielen.\n§cBitte deine Erziehungsberechtigten um Erlabunis oder warte bis du 18 bist.");
        }
    }
}
