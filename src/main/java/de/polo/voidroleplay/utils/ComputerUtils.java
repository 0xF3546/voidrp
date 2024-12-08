package de.polo.voidroleplay.utils;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.manager.FactionManager;
import de.polo.voidroleplay.manager.inventory.CustomItem;
import de.polo.voidroleplay.manager.inventory.InventoryManager;
import de.polo.voidroleplay.manager.ItemManager;
import de.polo.voidroleplay.manager.PlayerManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.meta.ItemMeta;

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
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8» §eComputer", true, true);
        if (playerData.isDuty()) {
            inventoryManager.setItem(new CustomItem(10, ItemManager.createItem(Material.RED_DYE, 1, 0, "§cAus Dienst gehen")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    event.getCurrentItem().setType(Material.GREEN_DYE);
                    ItemMeta meta = event.getCurrentItem().getItemMeta();
                    meta.setDisplayName("§c§lDienst verlassen!");
                    event.getCurrentItem().setItemMeta(meta);
                    factionManager.setDuty(player, false);
                    factionManager.sendMessageToFaction(playerData.getFaction(), player.getName() + " hat den Dienst verlassen.");
                }
            });
        } else {
            inventoryManager.setItem(new CustomItem(10, ItemManager.createItem(Material.GREEN_DYE, 1, 0, "§aIn Dienst gehen")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    event.getCurrentItem().setType(Material.RED_DYE);
                    ItemMeta itemMeta = event.getCurrentItem().getItemMeta();
                    itemMeta.setDisplayName("§a§lDienst betreten!");
                    event.getCurrentItem().setItemMeta(itemMeta);
                    factionManager.setDuty(player, true);
                    factionManager.sendMessageToFaction(playerData.getFaction(), player.getName() + " hat den Dienst betreten.");

                }
            });
        }
    }
}
