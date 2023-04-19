package de.polo.void_roleplay.Utils;

import de.polo.void_roleplay.DataStorage.FactionData;
import de.polo.void_roleplay.DataStorage.PlayerData;
import de.polo.void_roleplay.Main;
import de.polo.void_roleplay.MySQl.MySQL;
import de.polo.void_roleplay.commands.openBossMenuCommand;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import java.util.UUID;

public class TabletUtils implements Listener {

    @EventHandler
    public void onTabletUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        if (item.getType() == Material.IRON_INGOT && (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR)) {
            Inventory inv = Bukkit.createInventory(player, 27, "§8» §eTablet");
            inv.setItem(0, ItemManager.createItem(Material.PLAYER_HEAD, 1, 0, "§cFraktionsapp", null));
            inv.setItem(9, ItemManager.createItem(Material.BLUE_DYE, 1, 0, "§1Aktenapp", null));
            for (int i = 0; i < 27; i++) {
                if (inv.getItem(i) == null) {
                    inv .setItem(i, ItemManager.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, 0, "§8", null));
                }
            }
            playerData.setVariable("current_inventory", "tablet");
            playerData.setVariable("current_app", null);
            player.openInventory(inv);
        }
    }

    public static void openApp(Player player, String app) throws SQLException {
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        playerData.setVariable("current_app", null);
        switch (app) {
            case "fraktionsapp":
                openBossMenuCommand.openBossMenu(player, 1);
                playerData.setVariable("current_app", "fraktionsapp");
                break;
            case "aktenapp":
                openAktenApp(player);
                playerData.setVariable("current_app", "aktenapp");
                break;
        }
    }

    public static void openAktenApp(Player player) {
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        if (Objects.equals(playerData.getFaction(), "FBI") || Objects.equals(playerData.getFaction(), "Polizei")) {
            Inventory inv = Bukkit.createInventory(player, 27, "§8» §1Aktenapp");
            inv.setItem(0, ItemManager.createItem(Material.DIAMOND, 1, 0, "§9Akten bearbeiten", "§bBearbeite Akten von Spielern"));
            inv.setItem(1, ItemManager.createItem(Material.PAPER, 1, 0, "§9Aktenübersicht", null));
            for (int i = 0; i < 27; i++) {
                if (inv.getItem(i) == null) {
                    inv.setItem(i, ItemManager.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, 0, "§8", null));
                }
            }
            player.openInventory(inv);
        }
    }

    public static void openPlayerAktenList(Player player, int page) {
        if (page <= 0) return;
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        FactionData factionData = FactionManager.factionDataMap.get(playerData.getFaction());
        playerData.setVariable("current_inventory", "tablet");
        playerData.setVariable("current_app", "playeraktenlist");
        playerData.setIntVariable("current_page", page);
        Inventory inv = Bukkit.createInventory(player, 27, "§8» §9Akten §8- §9Seite§8:§7 " + page);
        int i = 0;
        int j = 0;
        for (Player players : Bukkit.getOnlinePlayers()) {
            if (i == 26 && i == 18) {
                i++;
            } else if (j >= (25 * (page - 1)) && j <= (25 * page)) {
                inv.setItem(i, ItemManager.createItemHead(players.getUniqueId().toString(), 1, 0, "§8» §6" + players.getName(), null));
                i++;
            }
            inv.setItem(26, ItemManager.createItem(Material.GOLD_NUGGET, 1, 0, "§cNächste Seite", null));
            inv.setItem(18, ItemManager.createItem(Material.NETHER_WART, 1, 0, "§cVorherige Seite", null));
            j++;
        }
            player.openInventory(inv);
    }

    public static void editPlayerAkte(Player player, ItemStack stack) {
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        FactionData factionData = FactionManager.factionDataMap.get(playerData.getFaction());
        ItemStack tempItemStack = new ItemStack(stack.getType());
        tempItemStack.setItemMeta(stack.getItemMeta());
        if (tempItemStack.getItemMeta() instanceof SkullMeta) {
            SkullMeta skullMeta = (SkullMeta) tempItemStack.getItemMeta();
            UUID uuid = Objects.requireNonNull(skullMeta.getOwningPlayer()).getUniqueId();
            OfflinePlayer targetplayer = Bukkit.getOfflinePlayer(uuid);
            playerData.setVariable("current_app", "edit_akte");
            playerData.setVariable("current_akte", targetplayer.getUniqueId().toString());
            Inventory inv = Bukkit.createInventory(player, 27, "§8» §c" + targetplayer.getName());
                inv.setItem(4, ItemManager.createItemHead(targetplayer.getUniqueId().toString(), 1, 0, "§8» §6" + targetplayer.getName(), null));
                inv.setItem(10, ItemManager.createItem(Material.PAPER, 1, 0, "§9Offene Akten", null));
                inv.setItem(11, ItemManager.createItem(Material.DIAMOND, 1, 0, "§9Akte hinzufügen", null));
                for (int i = 0; i < 27; i++) {
                    if (inv.getItem(i) == null) inv.setItem(i, ItemManager.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, 0, "§8", null));
                }
            player.openInventory(inv);
        } else {
            player.closeInventory();
            player.sendMessage(Main.error + "Spieler konnte nicht geladen werden.");
        }
    }
}
