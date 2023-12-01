package de.polo.metropiacity.utils;

import de.polo.metropiacity.dataStorage.*;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.database.MySQL;
import de.polo.metropiacity.utils.events.SubmitChatEvent;
import de.polo.metropiacity.commands.OpenBossMenuCommand;
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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

public class TabletUtils implements Listener {
    private final PlayerManager playerManager;
    private final FactionManager factionManager;
    public TabletUtils(PlayerManager playerManager, FactionManager factionManager) {
        this.playerManager = playerManager;
        this.factionManager = factionManager;
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }

    @EventHandler
    public void onTabletUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() == Material.IRON_INGOT && (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR)) {
            openTablet(player);
        }
    }

    public void openTablet(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        Inventory inv = Bukkit.createInventory(player, 27, "§8» §eTablet");
        inv.setItem(0, ItemManager.createItem(Material.PLAYER_HEAD, 1, 0, "§cFraktionsapp", null));
        inv.setItem(1, ItemManager.createItem(Material.MINECART, 1, 0, "§6Fahrzeugübersicht", null));
        inv.setItem(9, ItemManager.createItem(Material.BLUE_DYE, 1, 0, "§1Aktenapp", null));
        inv.setItem(10, ItemManager.createItem(Material.ORANGE_DYE, 1, 0, "§6Gefängnisapp", null));
        for (int i = 0; i < 27; i++) {
            if (inv.getItem(i) == null) {
                inv.setItem(i, ItemManager.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, 0, "§8", null));
            }
        }
        playerData.setVariable("current_inventory", "tablet");
        playerData.setVariable("current_app", null);
        player.openInventory(inv);
    }

    public void openApp(Player player, String app) throws SQLException {
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        playerData.setVariable("current_app", null);
        switch (app) {
            case "fraktionsapp":
                Main.getInstance().commands.openBossMenuCommand.openBossMenu(player, 1);
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
            case "vehiclesapp":
                openVehiclesApp(player, 1);
                playerData.setVariable("current_app", "vehiclesapp");
                break;
        }
    }

    public void openAktenApp(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
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

    public void openPlayerAktenList(Player player, int page) {
        if (page <= 0) return;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        FactionData factionData = factionManager.getFactionData(playerData.getFaction());
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

    public void editPlayerAkte(Player player, ItemStack stack) {
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        FactionData factionData = factionManager.getFactionData(playerData.getFaction());
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
            PlayerData targetplayerData = playerManager.getPlayerData(targetplayer.getUniqueId());
            if (targetplayerData.isJailed()) {
                inv.setItem(16, ItemManager.createItem(Material.BARRIER, 1, 0, "§cAus Gefängnis entlassen", null));
            }
            inv.setItem(22, ItemManager.createItem(Material.REDSTONE, 1, 0, "§cZurück", null));
            for (int i = 0; i < 27; i++) {
                if (inv.getItem(i) == null)
                    inv.setItem(i, ItemManager.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, 0, "§8", null));
            }
            player.openInventory(inv);
        } else {
            player.closeInventory();
            player.sendMessage(Main.error + "Spieler konnte nicht geladen werden.");
        }
    }

    public void openAktenList(Player player, int page, String search) throws SQLException {
        if (page <= 0) return;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        playerData.setVariable("current_app", "aktenlist");
        playerData.setIntVariable("current_page", page);
        Statement statement = Main.getInstance().mySQL.getStatement();
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
        if (playerData.getFactionGrade() >= 7)
            inv.setItem(22, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWZmMzE0MzFkNjQ1ODdmZjZlZjk4YzA2NzU4MTA2ODFmOGMxM2JmOTZmNTFkOWNiMDdlZDc4NTJiMmZmZDEifX19", 1, 0, "§aAkte einfügen", null));
        inv.setItem(23, ItemManager.createItem(Material.CLOCK, 1, 0, "§7Akte suchen...", null));
        result.close();
        player.openInventory(inv);
    }

    public void openPlayerAkte(Player player, int page) throws SQLException {
        if (page <= 0) return;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        playerData.setVariable("current_app", "player_aktenlist");
        playerData.setIntVariable("current_page", page);
        Statement statement = Main.getInstance().mySQL.getStatement();
        ResultSet result = statement.executeQuery("SELECT `id`, `akte`, `hafteinheiten`, `geldstrafe`, `vergebendurch`, DATE_FORMAT(datum, '%d.%m.%Y | %H:%i:%s') AS formatted_timestamp FROM `player_akten` WHERE `uuid` = '" + playerData.getVariable("current_akte") + "'");
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
                meta.setLore(Arrays.asList("§8 ➥ §bHaftineinheiten§8:§7 " + result.getInt(3), "§8 ➥ §bGeldstrafe§8:§7 " + result.getInt(4) + "$", "§8 ➥ §bDurch§8:§7 " + result.getString(5), "§8 ➥ §bDatum§8:§7 " + result.getString("formatted_timestamp")));
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

    public void openJailApp(Player player, int page) {
        if (page <= 0) return;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (!factionManager.faction(player).equals("FBI") || !factionManager.faction(player).equals("Polizei")) return;
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

    public void openVehiclesApp(Player player, int page) {
        if (page <= 0) return;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        playerData.setVariable("current_inventory", "tablet");
        playerData.setVariable("current_app", "vehiclesapp");
        playerData.setIntVariable("current_page", page);
        Inventory inv = Bukkit.createInventory(player, 27, "§8» §6Fahrzeuge §8- §6Seite§8:§7 " + page);
        int i = 0;
        int j = 0;
        for (PlayerVehicleData playerVehicleData : Vehicles.playerVehicleDataMap.values()) {
            if (playerVehicleData.getUuid().equals(player.getUniqueId().toString())) {
                if (i >= (18 * (page - 1)) && i < (18 * page)) {
                    VehicleData vehicleData = Vehicles.vehicleDataMap.get(playerVehicleData.getType());
                    int slotIndex = i % 9;
                    j++;
                    if (j > 9) {
                        slotIndex += 9;
                    }

                    if (playerVehicleData.isParked()) {
                        inv.setItem(i, ItemManager.createItem(Material.MINECART, 1, 0, "§6" + vehicleData.getName(), null));
                        ItemMeta meta = inv.getItem(i).getItemMeta();
                        meta.setLore(Arrays.asList("§8 ➥ §eID§8:§7 " + playerVehicleData.getId(), "§8 ➥ §eGarage§8:§7 " + LocationManager.garageDataMap.get(playerVehicleData.getGarage()).getName(), "§8 ➥ §aEingeparkt"));
                        inv.getItem(i).setItemMeta(meta);
                    } else {
                        inv.setItem(i, ItemManager.createItem(Material.MINECART, 1, 0, "§6" + vehicleData.getName(), null));
                        ItemMeta meta = inv.getItem(i).getItemMeta();
                        meta.setLore(Arrays.asList("§8 ➥ §eID§8:§7 " + playerVehicleData.getId(), "§8 ➥ §eGarage§8:§7 " + LocationManager.garageDataMap.get(playerVehicleData.getGarage()).getName(), "§8 ➥ §cAusgeparkt", "", "§8 » §aOrten"));
                        inv.getItem(i).setItemMeta(meta);
                    }
                }
                i++;
            }
        }
        inv.setItem(26, ItemManager.createItem(Material.GOLD_NUGGET, 1, 0, "§cNächste Seite", null));
        inv.setItem(18, ItemManager.createItem(Material.NETHER_WART, 1, 0, "§cVorherige Seite", null));
        inv.setItem(22, ItemManager.createItem(Material.REDSTONE, 1, 0, "§cZurück", null));
        player.openInventory(inv);

    }

    public void createAkte(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        Inventory inv = Bukkit.createInventory(player, 27, "§8 » §aAkte einfügen");
        playerData.setVariable("current_inventory", "tablet");
        playerData.setVariable("current_app", "createakte");
        if (playerData.getVariable("input_akte") == null) {
            inv.setItem(11, ItemManager.createItem(Material.CHEST, 1, 0, "§aAkte", "§8 ➥ §cNicht angegeben"));
        } else {
            inv.setItem(11, ItemManager.createItem(Material.CHEST, 1, 0, "§aAkte", "§8 ➥ §e" + playerData.getVariable("input_akte")));
        }
        inv.setItem(13, ItemManager.createItem(Material.CHEST, 1, 0, "§aHafteinheiten", "§8 ➥ §e" + playerData.getIntVariable("input_hafteinheiten")));
        inv.setItem(15, ItemManager.createItem(Material.CHEST, 1, 0, "§aGeldstrafe", "§8 ➥ §e" + playerData.getIntVariable("input_geldstrafe")));
        inv.setItem(26, ItemManager.createItem(Material.EMERALD, 1, 0, "§aBestätigen", null));
        for (int i = 0; i < 27; i++) {
            if (inv.getItem(i) == null) inv.setItem(i, ItemManager.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, 0, "§c", null));
        }
        player.openInventory(inv);
    }
    public void createNewAkte(Player player) throws SQLException {
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        Statement statement = Main.getInstance().mySQL.getStatement();
        statement.execute("INSERT INTO akten (akte, hafteinheiten, geldstrafe) VALUES ('" + playerData.getVariable("input_akte") + "', " + playerData.getIntVariable("input_hafteinheiten") + ", " + playerData.getIntVariable("input_geldstrafe") + ")");
        player.sendMessage("§8[§aAkte§8]§7 Akte wurde hinzugefügt.");
        player.closeInventory();
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
        if (event.getSubmitTo().equals("createakte_akte")) {
            if (event.isCancel()) {
                event.sendCancelMessage();
                event.end();
                return;
            }
            event.getPlayerData().setVariable("input_akte", event.getMessage());
            createAkte(event.getPlayer());
            event.end();
        }
        if (event.getSubmitTo().equals("createakte_hafteinheiten")) {
            if (event.isCancel()) {
                event.sendCancelMessage();
                event.end();
                return;
            }
            int input = 0;
            try {
                input = Integer.parseInt(event.getMessage());
            } catch (IllegalArgumentException e) {
                event.getPlayer().sendMessage(Main.error + "Du hast keine gültige Zahl angegeben");
                event.end();
                return;
            }
            event.getPlayerData().setIntVariable("input_hafteinheiten", input);
            createAkte(event.getPlayer());
            event.end();
        }
        if (event.getSubmitTo().equals("createakte_geldstrafe")) {
            if (event.isCancel()) {
                event.sendCancelMessage();
                event.end();
                return;
            }
            int input = 0;
            try {
                input = Integer.parseInt(event.getMessage());
            } catch (IllegalArgumentException e) {
                event.getPlayer().sendMessage(Main.error + "Du hast keine gültige Zahl angegeben");
                event.end();
                return;
            }
            event.getPlayerData().setIntVariable("input_geldstrafe", input);
            createAkte(event.getPlayer());
            event.end();
        }
    }
}
