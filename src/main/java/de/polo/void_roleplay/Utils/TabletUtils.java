package de.polo.void_roleplay.Utils;

import de.polo.void_roleplay.DataStorage.FactionData;
import de.polo.void_roleplay.DataStorage.JailData;
import de.polo.void_roleplay.DataStorage.PlayerData;
import de.polo.void_roleplay.Main;
import de.polo.void_roleplay.MySQl.MySQL;
import de.polo.void_roleplay.commands.openBossMenuCommand;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import javax.naming.Name;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

public class TabletUtils implements Listener {

    @EventHandler
    public void onTabletUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() == Material.IRON_INGOT && (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR)) {
            openTablet(player);
        }
    }

    public static void openTablet(Player player) {
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        Inventory inv = Bukkit.createInventory(player, 27, "§8» §eTablet");
        inv.setItem(0, ItemManager.createItem(Material.PLAYER_HEAD, 1, 0, "§cFraktionsapp", null));
        inv.setItem(9, ItemManager.createItem(Material.BLUE_DYE, 1, 0, "§1Aktenapp", null));
        inv.setItem(10, ItemManager.createItem(Material.ORANGE_DYE, 1, 0, "§6Gefängnisapp", null));
        for (int i = 0; i < 27; i++) {
            if (inv.getItem(i) == null) {
                inv .setItem(i, ItemManager.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, 0, "§8", null));
            }
        }
        playerData.setVariable("current_inventory", "tablet");
        playerData.setVariable("current_app", null);
        player.openInventory(inv);
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
            case "gefängnisapp":
                openJailApp(player, 1);
                playerData.setVariable("current_app", "gefängnisapp");
                break;
        }
    }

    public static void openAktenApp(Player player) {
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        if (Objects.equals(playerData.getFaction(), "FBI") || Objects.equals(playerData.getFaction(), "Polizei")) {
            Inventory inv = Bukkit.createInventory(player, 27, "§8» §1Aktenapp");
            inv.setItem(0, ItemManager.createItem(Material.DIAMOND, 1, 0, "§9Akten bearbeiten", "§bBearbeite Akten von Spielern"));
            inv.setItem(1, ItemManager.createItem(Material.PAPER, 1, 0, "§9Aktenübersicht", null));
            inv.setItem(22, ItemManager.createItem(Material.REDSTONE, 1, 0, "§cZurück", null));
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
            if (i == 26 && i == 18 && i == 22) {
                i++;
            } else if (j >= (25 * (page - 1)) && j <= (25 * page)) {
                inv.setItem(i, ItemManager.createItemHead(players.getUniqueId().toString(), 1, 0, "§8» §6" + players.getName(), null));
                i++;
            }
            inv.setItem(26, ItemManager.createItem(Material.GOLD_NUGGET, 1, 0, "§cNächste Seite", null));
            inv.setItem(18, ItemManager.createItem(Material.NETHER_WART, 1, 0, "§cVorherige Seite", null));
            inv.setItem(22, ItemManager.createItem(Material.REDSTONE, 1, 0, "§cZurück", null));
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
                inv.setItem(10, ItemManager.createItem(Material.BOOK, 1, 0, "§9Offene Akten", null));
                inv.setItem(11, ItemManager.createItem(Material.GREEN_DYE, 1, 0, "§9Akte hinzufügen", null));
                PlayerData targetplayerData = PlayerManager.playerDataMap.get(targetplayer.getUniqueId().toString());
                if (targetplayerData.isJailed()) {
                    inv.setItem(16, ItemManager.createItem(Material.BARRIER, 1, 0, "§cAus Gefängnis entlassen", null));
                }
                inv.setItem(22, ItemManager.createItem(Material.REDSTONE, 1, 0, "§cZurück", null));
                for (int i = 0; i < 27; i++) {
                    if (inv.getItem(i) == null) inv.setItem(i, ItemManager.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, 0, "§8", null));
                }
            player.openInventory(inv);
        } else {
            player.closeInventory();
            player.sendMessage(Main.error + "Spieler konnte nicht geladen werden.");
        }
    }

    public static void openAktenList(Player player, int page, String search) throws SQLException {
        if (page <= 0) return;
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        playerData.setVariable("current_app", "aktenlist");
        playerData.setIntVariable("current_page", page);
        Statement statement = MySQL.getStatement();
        ResultSet result = null;
        if (search == null) {
            result = statement.executeQuery("SELECT `id`, `akte`, `hafteinheiten`, `geldstrafe` FROM `akten`");
        } else {
            result = statement.executeQuery("SELECT `id`, `akte`, `hafteinheiten`, `geldstrafe` FROM `akten` WHERE LOWER(`akte`) LIKE LOWER('%" + search + "%') ");
        }
        Inventory inv = Bukkit.createInventory(player, 27, "§8» §9Aktenübersicht §8- §9Seite§8:§7 " + page);
        int i = 0;
        while (result.next()) {
            if (result.getRow() >= (18 * (page - 1)) && result.getRow() <= (18 * page)) {
                inv.setItem(i, ItemManager.createItem(Material.PAPER, 1, 0, "§8» §3" + result.getString(2), "Lädt..."));
                ItemMeta meta = Objects.requireNonNull(inv.getItem(i)).getItemMeta();
                NamespacedKey akte = new NamespacedKey(Main.plugin, "akte");
                NamespacedKey hafteinheiten = new NamespacedKey(Main.plugin, "hafteinheiten");
                NamespacedKey geldstrafe = new NamespacedKey(Main.plugin, "geldstrafe");
                assert meta != null;
                meta.getPersistentDataContainer().set(akte, PersistentDataType.STRING, result.getString(2));
                meta.getPersistentDataContainer().set(hafteinheiten, PersistentDataType.INTEGER, result.getInt(3));
                meta.getPersistentDataContainer().set(geldstrafe, PersistentDataType.INTEGER, result.getInt(4));
                meta.setLore(Arrays.asList("§8 ➥ §bHaftineinheiten§8:§7 " + result.getInt(3), "§8 ➥ §bGeldstrafe§8:§7 " + result.getInt(4) + "$"));
                Objects.requireNonNull(inv.getItem(i)).setItemMeta(meta);
                i++;
            }
        }
        inv.setItem(26, ItemManager.createItem(Material.GOLD_NUGGET, 1, 0, "§cNächste Seite", null));
        inv.setItem(18, ItemManager.createItem(Material.NETHER_WART, 1, 0, "§cVorherige Seite", null));
        inv.setItem(21, ItemManager.createItem(Material.REDSTONE, 1, 0, "§cZurück", null));
        inv.setItem(23, ItemManager.createItem(Material.CLOCK, 1, 0, "§7Akte suchen...", null));
        result.close();
        player.openInventory(inv);
    }

    public static void openPlayerAkte(Player player, int page) throws SQLException {
        if (page <= 0) return;
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        playerData.setVariable("current_app", "player_aktenlist");
        playerData.setIntVariable("current_page", page);
        Statement statement = MySQL.getStatement();
        ResultSet result = statement.executeQuery("SELECT `id`, `akte`, `hafteinheiten`, `geldstrafe`, `vergebendurch` FROM `player_akten` WHERE `uuid` = '" + playerData.getVariable("current_akte") + "'");
        Inventory inv = Bukkit.createInventory(player, 27, "§8» §9Aktenübersicht §8- §9Seite§8:§7 " + page);
        int i = 0;
        while (result.next()) {
            if (i == 26 && i == 18 && i == 22) {
                i++;
            } else if (result.getRow() >= (25 * (page - 1)) && result.getRow() <= (25 * page)) {
                inv.setItem(i, ItemManager.createItem(Material.WRITTEN_BOOK, 1, 0, "§8» §3" + result.getString(2), "Lädt..."));
                ItemMeta meta = Objects.requireNonNull(inv.getItem(i)).getItemMeta();
                NamespacedKey id = new NamespacedKey(Main.plugin, "id");
                assert meta != null;
                meta.setLore(Arrays.asList("§8 ➥ §bHaftineinheiten§8:§7 " + result.getInt(3), "§8 ➥ §bGeldstrafe§8:§7 " + result.getInt(4) + "$"));
                meta.getPersistentDataContainer().set(id, PersistentDataType.INTEGER, result.getInt(1));
                Objects.requireNonNull(inv.getItem(i)).setItemMeta(meta);
                i++;
            }
        }
        inv.setItem(26, ItemManager.createItem(Material.GOLD_NUGGET, 1, 0, "§cNächste Seite", null));
        inv.setItem(18, ItemManager.createItem(Material.NETHER_WART, 1, 0, "§cVorherige Seite", null));
        inv.setItem(22, ItemManager.createItem(Material.REDSTONE, 1, 0, "§cZurück", null));
        result.close();
        player.openInventory(inv);
    }

    public static void openJailApp(Player player, int page) {
        if (page <= 0) return;
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        FactionData factionData = FactionManager.factionDataMap.get(playerData.getFaction());
        playerData.setVariable("current_inventory", "tablet");
        playerData.setVariable("current_app", "gefängnisapp");
        playerData.setIntVariable("current_page", page);
        Inventory inv = Bukkit.createInventory(player, 27, "§8» §6Gefängnis §8- §6Seite§8:§7 " + page);
        int i = 0;
        int j = 0;
        for (JailData jailData : StaatUtil.jailDataMap.values()) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(jailData.getUuid()));
            if (offlinePlayer.isOnline()) {
                if (i == 26 && i == 18 && i == 22) {
                    i++;
                } else if (j >= (25 * (page - 1)) && j <= (25 * page)) {
                    inv.setItem(i, ItemManager.createItemHead(jailData.getUuid(), 1, 0, "§8» §6" + offlinePlayer.getName(), null));
                    i++;
                }
                j++;
            }
        }
        inv.setItem(26, ItemManager.createItem(Material.GOLD_NUGGET, 1, 0, "§cNächste Seite", null));
        inv.setItem(18, ItemManager.createItem(Material.NETHER_WART, 1, 0, "§cVorherige Seite", null));
        inv.setItem(22, ItemManager.createItem(Material.REDSTONE, 1, 0, "§cZurück", null));
        player.openInventory(inv);

    }

    @EventHandler
    public void onChatSubmit(SubmitChatEvent event) throws SQLException {
        if (event.getSubmitTo().equals("aktensearch")) {
            if (event.isCancel()) {
                event.sendCancelMessage();
                event.end();
                return;
            }
            openAktenList(event.getPlayer(), 1, event.getMessage());
            event.end();
        }
    }
}
