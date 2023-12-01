package de.polo.metropiacity.utils;

import de.polo.metropiacity.Main;
import de.polo.metropiacity.dataStorage.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;

import java.util.Objects;

public class ComputerUtils implements Listener {
    private final PlayerManager playerManager;
    private final FactionManager factionManager;
    public ComputerUtils(PlayerManager playerManager, FactionManager factionManager) {
        this.playerManager = playerManager;
        this.factionManager = factionManager;
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }
    @EventHandler
    public void onComputerUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && Objects.requireNonNull(event.getClickedBlock()).getType() == Material.END_STONE_BRICK_STAIRS) {
            if (Objects.equals(playerData.getFaction(), "FBI") || Objects.equals(playerData.getFaction(), "Polizei") || Objects.equals(playerData.getFaction(), "Medic") || Objects.equals(playerData.getFaction(), "News")) {
                openComputer(player);
            }
        }
    }

    public void openComputer(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        Inventory inv = Bukkit.createInventory(player, 27, "§8» §eComputer");
        if (playerData.isDuty()) {
            inv.setItem(10, ItemManager.createItem(Material.RED_DYE, 1, 0, "§cAus Dienst gehen", null));
        } else {
            inv.setItem(10, ItemManager.createItem(Material.GREEN_DYE, 1, 0, "§aIn Dienst gehen", null));
        }
        for (int i = 0; i < 27; i++) {
            if (inv.getItem(i) == null) {
                inv .setItem(i, ItemManager.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, 0, "§8", null));
            }
        }
        playerData.setVariable("current_inventory", "computer");
        playerData.setVariable("current_app", null);
        player.openInventory(inv);
    }
}
