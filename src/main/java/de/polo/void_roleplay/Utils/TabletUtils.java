package de.polo.void_roleplay.Utils;

import de.polo.void_roleplay.DataStorage.PlayerData;
import de.polo.void_roleplay.commands.openBossMenuCommand;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;
import java.util.Objects;

public class TabletUtils implements Listener {

    @EventHandler
    public void onTabletUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && item.getType() == Material.IRON_INGOT) {
            Inventory inv = Bukkit.createInventory(player, 27, "§8» §eTablet");
            PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
            inv.setItem(0, ItemManager.createItem(Material.PLAYER_HEAD, 1, 0, "§cFraktionsapp", null));
            inv.setItem(9, ItemManager.createItem(Material.BLUE_DYE, 1, 0, "§1Aktenapp", null));
            for (int i = 0; i < 27; i++) {
                if (inv.getItem(i) == null) {
                    inv .setItem(i, ItemManager.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, 0, "§8", null));
                }
            }
            playerData.setVariable("current_inventory", "tablet");
            player.openInventory(inv);
        }
    }

    public static void openApp(Player player, String app) throws SQLException {
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        switch (app) {
            case "fraktionsapp":
                openBossMenuCommand.openBossMenu(player, 1);
                playerData.setVariable("current_app", "fraktionsapp");
            case "aktenapp":
                openAktenApp(player);
                playerData.setVariable("current_app", "aktenapp");
            default:
                playerData.setVariable("current_app", null);
        }
    }

    public static void openAktenApp(Player player) {
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        if (Objects.equals(playerData.getFaction(), "FBI") || Objects.equals(playerData.getFaction(), "Polizei")) {
            Inventory inv = Bukkit.createInventory(player, 27, "§8» §1Aktenapp");
            for (int i = 0; i < 27; i++) {
                if (inv.getItem(i) == null) {
                    inv .setItem(i, ItemManager.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, 0, "§8", null));
                }
            }
            player.openInventory(inv);
        }
    }
}
