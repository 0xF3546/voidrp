package de.polo.voidroleplay.utils;

import de.polo.voidroleplay.dataStorage.PhoneCall;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.utils.playerUtils.ChatUtils;
import de.polo.voidroleplay.utils.events.SubmitChatEvent;
import org.bukkit.*;
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
import java.text.DecimalFormat;
import java.util.*;

public class PhoneUtils implements Listener {
    private static final List<PhoneCall> phoneCalls = new ArrayList<>();
    public static final String error_nophone = "§8[§6Handy§8] §cDas kannst du aktuell nicht machen.";
    public static final String error_flightmode = "§8[§6Handy§8] §cDu bist im Flugmodus.";

    private final PlayerManager playerManager;
    private final Utils utils;

    public PhoneUtils(PlayerManager playerManager, Utils utils) {
        this.playerManager = playerManager;
        this.utils = utils;
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }

    @EventHandler
    public void onPhoneUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() == Material.IRON_NUGGET && (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR)) {
            openPhone(player);
        }
    }

    public void openPhone(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        Inventory inv = Bukkit.createInventory(player, 27, "§8» §eHandy");
        int unreadMessages = 0;
        try {
            Statement statement = Main.getInstance().mySQL.getStatement();
            ResultSet result = statement.executeQuery("SELECT COUNT(*) AS unreadCount FROM phone_messages WHERE isRead = false AND uuid = '" + player.getUniqueId() + "'");
            if (result.next()) unreadMessages = result.getInt("unreadCount");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        inv.setItem(10, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmViYmJkYmEzNzI5NjNjOWQ2ZDMzMjhjMjliZjEyM2FlMDlkMzBjZTdiYTNhMDU3Y2VkNjA2YzFjODAyOGI3YiJ9fX0=", 1, 0, "§6Kontakte", null));
        inv.setItem(11, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGFlN2JmNDUyMmIwM2RmY2M4NjY1MTMzNjNlYWE5MDQ2ZmRkZmQ0YWE2ZjFmMDg4OWYwM2MxZTYyMTZlMGVhMCJ9fX0=", 1, 0, "§eNachrichten", Arrays.asList("§8 ➥ §7Du hast §a" + unreadMessages + "§7 ungelesene Nachrichten.")));
        inv.setItem(12, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODI0NDJiYmY3MTcxYjVjYWZjYTIxN2M5YmE0NGNlMjc2NDcyMjVkZjc2Y2RhOTY4OWQ2MWE5ZjFjMGE1ZjE3NiJ9fX0=", 1, 0, "§aAnrufen", null));
        inv.setItem(14, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjg4OWNmY2JhY2JlNTk4ZThhMWNkODYxMGI0OWZjYjYyNjQ0ZThjYmE5ZDQ5MTFkMTIxMTM0NTA2ZDhlYTFiNyJ9fX0=", 1, 0, "§3Banking", null));
        inv.setItem(15, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTRkNDliYWU5NWM3OTBjM2IxZmY1YjJmMDEwNTJhNzE0ZDYxODU0ODFkNWIxYzg1OTMwYjNmOTlkMjMyMTY3NCJ9fX0=", 1, 0, "§7Einstellungen", null));
        inv.setItem(16, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzZmOGEyMTlmMDgwMzk0MGYxZDI3MzQ5ZmIwNTBjMzJkYzdjMDUwZGIzM2NhMWUwYjM2YzIyZjIxYjA3YmU4NiJ9fX0=", 1, 0, "§bInternet", null));
        for (int i = 0; i < 27; i++) {
            if (inv.getItem(i) == null) {
                inv.setItem(i, ItemManager.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, 0, "§8"));
            }
        }
        playerData.setVariable("current_inventory", "handy");
        playerData.setVariable("current_app", null);
        player.openInventory(inv);
    }

    public void openSettings(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        Inventory inv = Bukkit.createInventory(player, 27, "§8» §7Einstellungen");
        if (playerData.isFlightmode()) {
            inv.setItem(10, ItemManager.createItem(Material.GREEN_STAINED_GLASS_PANE, 1, 0, "§aFlugmodus abschalten"));
        } else {
            inv.setItem(10, ItemManager.createItem(Material.RED_STAINED_GLASS_PANE, 1, 0, "§cFlugmodus einschalten"));
        }
        inv.setItem(22, ItemManager.createItem(Material.REDSTONE, 1, 0, "§cZurück"));
        for (int i = 0; i < 27; i++) {
            if (inv.getItem(i) == null) {
                inv.setItem(i, ItemManager.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, 0, "§8"));
            }
        }
        playerData.setVariable("current_app", "settings");
        player.openInventory(inv);
    }

    public void openBanking(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        Inventory inv = Bukkit.createInventory(player, 27, "§8» §3Banking");
        inv.setItem(4, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTY0MzlkMmUzMDZiMjI1NTE2YWE5YTZkMDA3YTdlNzVlZGQyZDUwMTVkMTEzYjQyZjQ0YmU2MmE1MTdlNTc0ZiJ9fX0=", 1, 0, "§bKontostand", Arrays.asList("§8 ➥ §7" + new DecimalFormat("#,###").format(playerData.getBank()) + "$")));
        inv.setItem(11, ItemManager.createItem(Material.DIAMOND, 1, 0, "§bTransaktionen", "§8 ➥ §7Alle Transaktionen der Letzten 7 Tage"));
        inv.setItem(22, ItemManager.createItem(Material.REDSTONE, 1, 0, "§cZurück"));
        for (int i = 0; i < 27; i++) {
            if (inv.getItem(i) == null) {
                inv.setItem(i, ItemManager.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, 0, "§8"));
            }
        }
        playerData.setVariable("current_app", "banking");
        player.openInventory(inv);
    }

    public void openTransactions(Player player, int page, String search) throws SQLException {
        if (page <= 0) return;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        playerData.setVariable("current_app", "transactions");
        playerData.setIntVariable("current_page", page);
        Statement statement = Main.getInstance().mySQL.getStatement();
        ResultSet result = null;
        if (search == null) {
            result = statement.executeQuery("SELECT *, DATE_FORMAT(datum, '%d.%m.%Y | %H:%i:%s') AS formatted_timestamp FROM `bank_logs` WHERE `uuid` = '" + player.getUniqueId() + "' ORDER BY datum DESC");
        } else {
            result = statement.executeQuery("SELECT *, DATE_FORMAT(datum, '%d.%m.%Y | %H:%i:%s') AS formatted_timestamp FROM `bank_logs` WHERE LOWER(`reason`) LIKE LOWER('%" + search + "%') AND `uuid` = '" + player.getUniqueId() + "' ORDER BY datum DESC");
        }
        Inventory inv = Bukkit.createInventory(player, 27, "§8» §bTransaktionen §8- §bSeite§8:§7 " + page);
        int i = 0;
        int rows = 0;
        while (result.next()) {
            rows++;
            if (result.getRow() >= (18 * (page - 1)) && result.getRow() <= (18 * page)) {
                if (result.getBoolean(2) && result.getInt(4) >= 0) {
                    inv.setItem(i, ItemManager.createItem(Material.PAPER, 1, 0, "§8» §aEinzahlung", "Lädt..."));
                    ItemMeta meta = Objects.requireNonNull(inv.getItem(i)).getItemMeta();
                    assert meta != null;
                    meta.setLore(Arrays.asList("§8 ➥ §7Höhe§8:§a +" + result.getInt(4) + "$", "§8 ➥ §7Grund§8:§6 " + result.getString(5), "§8 ➥ §7Datum§8:§6 " + result.getString("formatted_timestamp")));
                    Objects.requireNonNull(inv.getItem(i)).setItemMeta(meta);
                } else {
                    inv.setItem(i, ItemManager.createItem(Material.PAPER, 1, 0, "§8» §cAuszahlung", "Lädt..."));
                    ItemMeta meta = Objects.requireNonNull(inv.getItem(i)).getItemMeta();
                    assert meta != null;
                    String höhe = String.valueOf(result.getInt(4)).replace("-", "");
                    meta.setLore(Arrays.asList("§8 ➥ §7Höhe§8:§c -" + höhe + "$", "§8 ➥ §7Grund§8:§6 " + result.getString(5), "§8 ➥ §7Datum§8:§6 " + result.getString("formatted_timestamp")));
                    Objects.requireNonNull(inv.getItem(i)).setItemMeta(meta);
                }
                i++;
            }
        }
        inv.setItem(26, ItemManager.createItem(Material.GOLD_NUGGET, 1, 0, "§cNächste Seite"));
        inv.setItem(18, ItemManager.createItem(Material.NETHER_WART, 1, 0, "§cVorherige Seite"));
        inv.setItem(21, ItemManager.createItem(Material.REDSTONE, 1, 0, "§cZurück"));
        inv.setItem(23, ItemManager.createItem(Material.CLOCK, 1, 0, "§7Transaktion suchen..."));
        for (int j = 0; j < 27; j++) {
            if (inv.getItem(j) == null) {
                inv.setItem(j, ItemManager.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, 0, "§8"));
            }
        }
        result.close();
        player.openInventory(inv);
    }

    public void openMessages(Player player, int page, String search) throws SQLException {
        if (page <= 0) return;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        playerData.setVariable("current_app", "messages");
        playerData.setIntVariable("current_page", page);
        Statement statement = Main.getInstance().mySQL.getStatement();
        ResultSet result = null;
        if (search == null) {
            result = statement.executeQuery("SELECT *, DATE_FORMAT(datum, '%d.%m.%Y | %H:%i:%s') AS formatted_timestamp FROM `phone_messages` WHERE `uuid` = '" + player.getUniqueId() + "' ORDER BY datum DESC");
        } else {
            result = statement.executeQuery("SELECT *, DATE_FORMAT(datum, '%d.%m.%Y | %H:%i:%s') AS formatted_timestamp FROM `phone_messages` WHERE LOWER(`message`) LIKE LOWER('%" + search + "%') AND `uuid` = '" + player.getUniqueId() + "' ORDER BY datum DESC");
        }
        Inventory inv = Bukkit.createInventory(player, 27, "§8» §eNachrichten §8- §eSeite§8:§7 " + page);
        int i = 0;
        int rows = 0;
        while (result.next()) {
            rows++;
            if (result.getRow() >= (18 * (page - 1)) && result.getRow() <= (18 * page)) {
                inv.setItem(i, ItemManager.createItem(Material.PAPER, 1, 0, "§8» §e" + result.getInt(6), "Lädt..."));
                ItemMeta meta = Objects.requireNonNull(inv.getItem(i)).getItemMeta();
                assert meta != null;
                NamespacedKey read = new NamespacedKey(Main.plugin, "isRead");
                NamespacedKey message = new NamespacedKey(Main.plugin, "message");
                NamespacedKey date = new NamespacedKey(Main.plugin, "date");
                NamespacedKey id = new NamespacedKey(Main.plugin, "message_id");
                if (result.getBoolean(7)) {
                    meta.setLore(Arrays.asList("§8 ➥ §7Nachricht§8:§6 " + result.getString(4), "§8 ➥ §7Datum§8:§6 " + result.getString("formatted_timestamp")));
                    meta.getPersistentDataContainer().set(read, PersistentDataType.INTEGER, 1);
                } else {
                    meta.setLore(Arrays.asList("§8 ➥ §7Nachricht§8:§6 " + result.getString(4), "§8 ➥ §7Datum§8:§6 " + result.getString("formatted_timestamp"), "", "§8» §aAls gelesen markieren"));
                    meta.getPersistentDataContainer().set(read, PersistentDataType.INTEGER, 0);
                    meta.getPersistentDataContainer().set(message, PersistentDataType.STRING, result.getString(4));
                    meta.getPersistentDataContainer().set(date, PersistentDataType.STRING, result.getString("formatted_timestamp"));
                    meta.getPersistentDataContainer().set(id, PersistentDataType.INTEGER, result.getInt(1));
                }
                Objects.requireNonNull(inv.getItem(i)).setItemMeta(meta);
                i++;
            }
        }
        inv.setItem(26, ItemManager.createItem(Material.GOLD_NUGGET, 1, 0, "§cNächste Seite"));
        inv.setItem(18, ItemManager.createItem(Material.NETHER_WART, 1, 0, "§cVorherige Seite"));
        inv.setItem(21, ItemManager.createItem(Material.REDSTONE, 1, 0, "§cZurück"));
        inv.setItem(23, ItemManager.createItem(Material.CLOCK, 1, 0, "§7Nachricht suchen..."));
        for (int j = 0; j < 27; j++) {
            if (inv.getItem(j) == null) {
                inv.setItem(j, ItemManager.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, 0, "§8"));
            }
        }
        result.close();
        player.openInventory(inv);
    }

    public void openContacts(Player player, int page, String search) throws SQLException {
        if (page <= 0) return;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        playerData.setVariable("current_app", "contacts");
        playerData.setIntVariable("current_page", page);
        Statement statement = Main.getInstance().mySQL.getStatement();
        ResultSet result = null;
        if (search == null) {
            result = statement.executeQuery("SELECT * FROM `phone_contacts`");
        } else {
            result = statement.executeQuery("SELECT * FROM `phone_contacts` WHERE LOWER(`contact_name`) LIKE LOWER('%" + search + "%') ");
        }
        Inventory inv = Bukkit.createInventory(player, 27, "§8» §6Kontakte §8- §6Seite§8:§7 " + page);
        int i = 0;
        int rows = 0;
        while (result.next()) {
            rows++;
            if (result.getRow() >= (18 * (page - 1)) && result.getRow() <= (18 * page)) {
                inv.setItem(i, ItemManager.createItemHead(result.getString(5), 1, 0, "§8» §7" + result.getString(3).replace("&", "§"), "Lädt..."));
                ItemMeta meta = Objects.requireNonNull(inv.getItem(i)).getItemMeta();
                NamespacedKey id = new NamespacedKey(Main.plugin, "id");
                NamespacedKey number = new NamespacedKey(Main.plugin, "number");
                assert meta != null;
                meta.getPersistentDataContainer().set(id, PersistentDataType.INTEGER, result.getInt(1));
                meta.getPersistentDataContainer().set(number, PersistentDataType.INTEGER, result.getInt(4));
                meta.setLore(Arrays.asList("§8 ➥ §6Nummer§8:§7 " + result.getInt(4), "§8 ➥ §aNeue SMS§8:§7 §cBald verfügbar!"));
                Objects.requireNonNull(inv.getItem(i)).setItemMeta(meta);
                i++;
            }
        }
        inv.setItem(26, ItemManager.createItem(Material.GOLD_NUGGET, 1, 0, "§cNächste Seite"));
        inv.setItem(18, ItemManager.createItem(Material.NETHER_WART, 1, 0, "§cVorherige Seite"));
        inv.setItem(21, ItemManager.createItem(Material.REDSTONE, 1, 0, "§cZurück"));
        inv.setItem(22, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjA1NmJjMTI0NGZjZmY5OTM0NGYxMmFiYTQyYWMyM2ZlZTZlZjZlMzM1MWQyN2QyNzNjMTU3MjUzMWYifX19", 1, 0, "§aKontakt hinzufügen", null));
        inv.setItem(23, ItemManager.createItem(Material.CLOCK, 1, 0, "§7Kontakt suchen..."));
        for (int j = 0; j < 27; j++) {
            if (inv.getItem(j) == null) {
                inv.setItem(j, ItemManager.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, 0, "§8"));
            }
        }
        result.close();
        if (search != null) player.sendMessage("§8[§6Kontakte§8]§7 Es gab §a" + rows + " Ergebnisse§7.");
        player.openInventory(inv);
    }

    public void editContact(Player player, ItemStack stack, boolean newContact, boolean canSave) {
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        System.out.println(newContact);
        playerData.setVariable("current_app", "edit_contact");
        if (!newContact) {
            ItemStack tempItemStack = new ItemStack(stack.getType());
            tempItemStack.setItemMeta(stack.getItemMeta());
            if (tempItemStack.getItemMeta() instanceof SkullMeta) {
                SkullMeta skullMeta = (SkullMeta) tempItemStack.getItemMeta();
                UUID uuid = Objects.requireNonNull(skullMeta.getOwningPlayer()).getUniqueId();
                OfflinePlayer targetplayer = Bukkit.getOfflinePlayer(uuid);
                playerData.setVariable("current_contact", targetplayer.getUniqueId().toString());
                try {
                    Statement statement = Main.getInstance().mySQL.getStatement();
                    ResultSet result = statement.executeQuery("SELECT * FROM `phone_contacts` WHERE `contact_uuid` = '" + uuid + "' AND `uuid` = '" + player.getUniqueId() + "'");
                    if (result.next()) {
                        Inventory inv = Bukkit.createInventory(player, 27, "§8» §6Kontakt§8:§e " + result.getString(3).replace("&", "§"));
                        inv.setItem(4, ItemManager.createItemHead(targetplayer.getUniqueId().toString(), 1, 0, "§8", null));
                        inv.setItem(15, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODI0NDJiYmY3MTcxYjVjYWZjYTIxN2M5YmE0NGNlMjc2NDcyMjVkZjc2Y2RhOTY4OWQ2MWE5ZjFjMGE1ZjE3NiJ9fX0=", 1, 0, "§aAnrufen", null));
                        inv.setItem(16, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGFlN2JmNDUyMmIwM2RmY2M4NjY1MTMzNjNlYWE5MDQ2ZmRkZmQ0YWE2ZjFmMDg4OWYwM2MxZTYyMTZlMGVhMCJ9fX0=", 1, 0, "§eNachricht schreiben", null));
                        inv.setItem(22, ItemManager.createItem(Material.REDSTONE, 1, 0, "§cZurück"));
                        if (!canSave) playerData.setIntVariable("current_contact_number", result.getInt(4));
                        if (!canSave) playerData.setVariable("current_contact_name", result.getString(3));
                        inv.setItem(10, ItemManager.createItem(Material.BOOK, 1, 0, "§eNummer", "§8 ➥ §7" + playerData.getIntVariable("current_contact_number")));
                        inv.setItem(11, ItemManager.createItem(Material.PAPER, 1, 0, "§eName", "§8 ➥ §7" + playerData.getVariable("current_contact_name").toString().replace("&", "§")));
                        inv.setItem(12, ItemManager.createItem(Material.RED_DYE, 1, 0, "§c§lKontakt löschen"));
                        if (canSave) inv.setItem(26, ItemManager.createItem(Material.EMERALD, 1, 0, "§aBestätigen"));
                        playerData.setIntVariable("current_contact_id", result.getInt(1));
                        playerData.setVariable("current_contact_uuid", targetplayer.getUniqueId().toString());
                        for (int i = 0; i < 27; i++) {
                            if (inv.getItem(i) == null)
                                inv.setItem(i, ItemManager.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, 0, "§8"));
                        }
                        player.openInventory(inv);
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            } else {
                player.closeInventory();
                player.sendMessage(Main.error + "Spieler konnte nicht geladen werden.");
            }
        } else {
            Inventory inv = Bukkit.createInventory(player, 27, "§8» §6Kontakt erstellen");
            playerData.setIntVariable("current_contact_id", 0);
            inv.setItem(4, ItemManager.createItem(Material.SKELETON_SKULL, 1, 0, "§8"));
            inv.setItem(10, ItemManager.createItem(Material.BOOK, 1, 0, "§eNummer", "§8 ➥ §7" + playerData.getIntVariable("current_contact_number")));
            inv.setItem(11, ItemManager.createItem(Material.PAPER, 1, 0, "§eName", "§8 ➥ §7" + playerData.getVariable("current_contact_name").toString().replace("&", "§")));
            inv.setItem(22, ItemManager.createItem(Material.REDSTONE, 1, 0, "§cZurück"));
            inv.setItem(26, ItemManager.createItem(Material.EMERALD, 1, 0, "§aBestätigen"));
            for (int i = 0; i < 27; i++) {
                if (inv.getItem(i) == null)
                    inv.setItem(i, ItemManager.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, 0, "§8"));
            }
            player.openInventory(inv);
        }
    }

    public void openCallApp(Player player, boolean isNew) {
        Inventory inv;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (isNew) {
            inv = Bukkit.createInventory(player, 54, "§8 » §aAnrufen§8:§2 ");
            playerData.setVariable("current_phone_callnumber", "");
        } else {
            inv = Bukkit.createInventory(player, 54, "§8 » §aAnrufen§8:§2 " + playerData.getVariable("current_phone_callnumber"));
        }
        playerData.setVariable("current_app", "phonecall");
        inv.setItem(12, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmNmYWZmYThjNmM3ZjYyNjIxNjgyZmU1NjcxMWRjM2I4OTQ0NjVmZGY3YTYyZjQzYjMxYTBkMzQwM2YzNGU3In19fQ==", 1, 0, "§6§l1", null));
        inv.setItem(13, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzQxMGRiNjIzNzM1YzE0NmM3YzQ4N2UzNjkyZDFjNWI1ZTIzYmY2OTFlZjA2NjVjMmU5NTQ5NDc5ZDgyOGYifX19", 1, 0, "§6§l2", null));
        inv.setItem(14, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWY4ZTdhY2Y3MjQyYWZhNTg2YzNkMDk1Zjg3ZmU5ZGU3YjdjYTI0YzRhMjhhNTYwNDAzNDdjNjU3OTYwZTM2In19fQ==", 1, 0, "§6§l3", null));
        inv.setItem(21, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTQ3YzczZDdkMmVmMTQ3MTJhZTU5YzU3ZDY0ZTUxMmE3ZjljNTI3NzQ2YjhiYzQyNDI3MGY5ZTM3YzE4MWM3OCJ9fX0=", 1, 0, "§6§l4", null));
        inv.setItem(22, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzVhZmY5MmRlODkyZDU5MjJhZDc1M2RhZDVhZTM0NzlhYjE1MGFmNGVkMjg0YWJmYTc1Y2E3YTk5NWMxODkzIn19fQ==", 1, 0, "§6§l5", null));
        inv.setItem(23, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjE2ZDQyM2M3ZmQ2NGM1YzdmNDA4NTQ2ZGE0Yjc4N2U5M2RiZWFjMGU3N2IxOWM0OWI5YWQ0ZmY0MThmMmQxIn19fQ==", 1, 0, "§6§l6", null));
        inv.setItem(30, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzQ4N2U5MTJhOGU5YmZjNDkxMjQzNmFmNTZjNDZjMmU2YTE1YTFkMjJkMWFkMThkNDZhMjI5ZDc2NDhlIn19fQ==", 1, 0, "§6§l7", null));
        inv.setItem(31, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTEwMWQ0YjQ3ZGNjYjc2MTJhYzVlZmRlNWFlMjQ0MWM4MmMzZjBhNjg0MDQxYWVkMzgyNzZkYmRmOTQifX19", 1, 0, "§6§l8", null));
        inv.setItem(32, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmE0Njg1NzQ0MWNhYWU2ZTE2YzkyOTZmYjU3MTQ4MmFhNTEzNjI2OGQzOWUzNWI3YWNmYmY1MTM5YTM3ZTAzZCJ9fX0=", 1, 0, "§6§l9", null));
        inv.setItem(40, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWJmYTYzYTBhNTQyOGIyNzM0NTNmZmU3ODRkM2U0ODljYmNmNmQxMmI3ODQ1MGEzNTE1NzE2Y2U3MjRmNCJ9fX0=", 1, 0, "§6§l0", null));
        inv.setItem(45, ItemManager.createItem(Material.REDSTONE, 1, 0, "§cZurück"));
        inv.setItem(53, ItemManager.createItem(Material.EMERALD, 1, 0, "§aAnrufen"));
        for (int i = 0; i < 54; i++) {
            if (inv.getItem(i) == null)
                inv.setItem(i, ItemManager.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, 0, "§8"));
        }
        player.openInventory(inv);
    }

    public Collection<PhoneCall> getCalls() {
        return phoneCalls;
    }

    public boolean isInCall(Player player) {
        return getCall(player) != null;
    }

    public List<Player> getPlayersInCall(PhoneCall call) {
        List<Player> players = new ArrayList<>();

        players.add(Bukkit.getPlayer(call.getCaller()));

        for (UUID uuid : call.getParticipants()) {
            players.add(Bukkit.getPlayer(uuid));
        }

        return players;
    }

    public PhoneCall getCall(Player player) {
        for (PhoneCall call : getCalls()) {
            if (call.getCaller() == player.getUniqueId()) {
                return call;
            }
            for (UUID uuid : call.getParticipants()) {
                if (uuid == player.getUniqueId()) {
                    return call;
                }
            }
        }
        return null;
    }

    public void addNumberToContacts(Player player, Player targetplayer) throws SQLException {
        String uuid = player.getUniqueId().toString();
        Statement statement = Main.getInstance().mySQL.getStatement();
        PlayerData playerData = playerManager.getPlayerData(targetplayer.getUniqueId());
        statement.executeQuery("INSERT INTO `phone_contacts` (`uuid`, `contact_name`, `contact_number`, `contact_uuid`) VALUES ('" + player.getUniqueId() + "', '" + targetplayer.getName() + "', " + playerData.getNumber() + ", '" + targetplayer.getUniqueId() + "')");
    }

    public void callNumber(Player player, Player players) throws SQLException {
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (VertragUtil.setVertrag(player, players, "phonecall", player.getUniqueId().toString())) {
            if (!playerManager.getPlayerData(players).isDead()) {
                ChatUtils.sendGrayMessageAtPlayer(players, players.getName() + "'s Handy klingelt...");
                ChatUtils.sendGrayMessageAtPlayer(player, player.getName() + " wählt eine Nummer auf dem Handy.");
                player.sendMessage("§8[§6Handy§8] §eDu rufst §l" + players.getName() + "§e an.");
                players.sendMessage("§8[§6Handy§8] §eDu wirst von §l" + player.getName() + "§e angerufen.");
                playerData.setVariable("calling", players.getUniqueId().toString());
                utils.vertragUtil.sendInfoMessage(players);
                players.playSound(players.getLocation(), Sound.MUSIC_CREATIVE, 1, 0);
            } else {
                player.sendMessage(Main.error + "Die gewünschte Rufnummer ist zurzeit nicht erreichbar.");
            }
        } else {
            player.sendMessage(Main.error + "Dein Handy konnte keine Verbindung aufbauen. §o(Systemfehler)");
        }
    }

    public void sendSMS(Player player, Player players, StringBuilder message) {
        if (!playerManager.getPlayerData(players).isDead()) {
            players.sendMessage("§8[§6SMS§8] §e" + player.getName() + "§8: §7" + message);
            player.sendMessage("§8[§6SMS§8] §e" + player.getName() + "§8: §7" + message);
            player.playSound(player.getLocation(), Sound.BLOCK_WEEPING_VINES_STEP, 1, 0);
            players.playSound(players.getLocation(), Sound.BLOCK_WEEPING_VINES_STEP, 1, 0);
        } else {
            player.sendMessage(Main.error + "§8[§6Handy§8] §cAuto-Response§8:§7 Die SMS konnte zugestellt werden, jedoch nicht gelesen werden.");
        }
        PlayerData targetplayerData = playerManager.getPlayerData(players.getUniqueId());
        try {
            Statement statement = Main.getInstance().mySQL.getStatement();
            statement.execute("INSERT INTO `phone_messages` (`uuid`, `contact_uuid`, `message`, `number`) VALUES ('" + players.getUniqueId() + "', '" + player.getUniqueId() + "', '" + message + "', " + targetplayerData.getId() + ");");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void acceptCall(Player player, String targetuuid) {
        player.stopSound(Sound.MUSIC_CREATIVE);
        if (!Main.getInstance().playerManager.getPlayerData(player).isDead()) {
            PhoneCall phoneCall = new PhoneCall();
            Player targetplayer = Bukkit.getPlayer(UUID.fromString(targetuuid));
            assert targetplayer != null;
            phoneCall.setCaller(targetplayer.getUniqueId());
            phoneCall.addParticipant(player.getUniqueId());
            phoneCalls.add(phoneCall);
            targetplayer.sendMessage("§8[§6Handy§8] §7" + player.getName() + " hat dein Anruf angenommen");
            player.sendMessage("§8[§6Handy§8] §7Du hast den Anruf von " + targetplayer.getName() + " angenommen");
            player.playSound(player.getLocation(), Sound.BLOCK_IRON_DOOR_OPEN, 1, 0);
            targetplayer.playSound(targetplayer.getLocation(), Sound.BLOCK_IRON_DOOR_OPEN, 1, 0);
            PlayerData playerData = Main.getInstance().playerManager.getPlayerData(targetplayer);
            playerData.setVariable("calling", null);
        } else {
            player.sendMessage(PhoneUtils.error_nophone);
        }
    }

    public static void denyCall(Player player, String targetuuid) {
        player.stopSound(Sound.MUSIC_CREATIVE);
        if (!Main.getInstance().playerManager.getPlayerData(player).isDead()) {
            Player targetplayer = Bukkit.getPlayer(UUID.fromString(targetuuid));
            assert targetplayer != null;
            targetplayer.sendMessage("§8[§6Handy§8] §7" + player.getName() + " hat dein Anruf abgelehnt");
            player.sendMessage("§8[§6Handy§8] §7Du hast den Anruf von " + player.getName() + " abgelehnt");
            player.playSound(player.getLocation(), Sound.BLOCK_IRON_DOOR_CLOSE, 1, 0);
            targetplayer.playSound(targetplayer.getLocation(), Sound.BLOCK_IRON_DOOR_CLOSE, 1, 0);
            PlayerData playerData = Main.getInstance().playerManager.getPlayerData(targetplayer);
            playerData.setVariable("calling", null);
        } else {
            player.sendMessage(PhoneUtils.error_nophone);
        }
    }

    public void closeCall(Player player) {
        player.stopSound(Sound.MUSIC_CREATIVE);
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        PhoneCall call = getCall(player);
        if (getCall(player) != null) {
            for (Player targetPlayers : getPlayersInCall(call)) {
                if (targetPlayers != player) {
                    targetPlayers.playSound(targetPlayers.getLocation(), Sound.BLOCK_IRON_DOOR_CLOSE, 1, 0);
                    targetPlayers.sendMessage("§8[§6Handy§8] §7" + player.getName() + " hat aufgelegt.");
                }
            }
            player.sendMessage("§8[§6Handy§8]§7 Du hast aufgelegt.");
            player.playSound(player.getLocation(), Sound.BLOCK_IRON_DOOR_CLOSE, 1, 0);
            call.setCaller(null);
            call.setParticipants(null);
            phoneCalls.remove(call);
        } else if (playerData.getVariable("calling") != null) {
            for (Player players : Bukkit.getOnlinePlayers()) {
                if (playerData.getVariable("calling").equals(players.getUniqueId().toString())) {
                    VertragUtil.deleteVertrag(player);
                    VertragUtil.deleteVertrag(players);
                    player.sendMessage("§8[§6Handy§8]§7 Du hast aufgelegt.");
                    players.sendMessage("§8[§6Handy§8]§7 " + player.getName() + "§7 hat aufgelegt.");
                    playerData.setVariable("calling", null);
                }
            }
        } else {
            player.sendMessage("§8[§6Handy§8]§7 Du bist in keinem Anruf.");
        }
    }

    public static boolean hasPhone(Player player) {
        Inventory inv = player.getInventory();
        Material phone = Material.IRON_NUGGET;
        boolean returnval = false;
        for (ItemStack item : inv.getContents()) {
            if (item != null && item.getType() == phone) {
                returnval = true;
            }
        }
        return returnval;
    }

    @EventHandler
    public void onChatSubmit(SubmitChatEvent event) throws SQLException {
        if (event.getSubmitTo().equals("contactsearch")) {
            if (event.isCancel()) {
                event.end();
                event.sendCancelMessage();
                return;
            }
            openContacts(event.getPlayer(), 1, event.getMessage());
            event.end();
        }
        if (event.getSubmitTo().equals("sendsms")) {
            if (event.isCancel()) {
                event.end();
                event.sendCancelMessage();
                return;
            }
            event.getPlayer().performCommand("sms " + event.getPlayerData().getIntVariable("current_contact_number") + " " + event.getMessage());
            event.end();
        }
        if (event.getSubmitTo().equals("changename")) {
            if (event.isCancel()) {
                event.end();
                event.sendCancelMessage();
                return;
            }
            event.getPlayerData().setVariable("current_contact_name", event.getMessage());
            if (event.getPlayerData().getIntVariable("current_contact_id") == 0) {
                editContact(event.getPlayer(), null, true, true);
            } else {
                editContact(event.getPlayer(), ItemManager.createItemHead(event.getPlayerData().getVariable("current_contact_uuid"), 1, 0, "§8", null), false, true);
            }
            event.end();
        }
        if (event.getSubmitTo().equals("changenumber")) {
            if (event.isCancel()) {
                event.end();
                event.sendCancelMessage();
                return;
            }
            event.getPlayerData().setIntVariable("current_contact_number", Integer.parseInt(event.getMessage()));
            if (event.getPlayerData().getIntVariable("current_contact_id") == 0) {
                editContact(event.getPlayer(), null, true, true);
            } else {
                editContact(event.getPlayer(), ItemManager.createItemHead(event.getPlayerData().getVariable("current_contact_uuid"), 1, 0, "§8", null), false, true);
            }
            event.end();
        }
        if (event.getSubmitTo().equals("checktransactions")) {
            if (event.isCancel()) {
                event.end();
                event.sendCancelMessage();
                return;
            }
            openTransactions(event.getPlayer(), 1, event.getMessage());
            event.end();
        }
        if (event.getSubmitTo().equals("checkmessages")) {
            if (event.isCancel()) {
                event.end();
                event.sendCancelMessage();
                return;
            }
            openMessages(event.getPlayer(), 1, event.getMessage());
            event.end();
        }
    }

    public void openInternet(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        Inventory inv = Bukkit.createInventory(player, 27, "§8» §bInternet");
        inv.setItem(4, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTY0MzlkMmUzMDZiMjI1NTE2YWE5YTZkMDA3YTdlNzVlZGQyZDUwMTVkMTEzYjQyZjQ0YmU2MmE1MTdlNTc0ZiJ9fX0=", 1, 0, "§bKontostand", Arrays.asList("§8 ➥ §7" + new DecimalFormat("#,###").format(playerData.getBank()) + "$")));
        inv.setItem(11, ItemManager.createItem(Material.DIAMOND, 1, 0, "§6Anwalt", "§8 ➥ §7Anwalt anheuern (§c15-55$/PayDay§7)"));
        inv.setItem(22, ItemManager.createItem(Material.REDSTONE, 1, 0, "§cZurück"));
        for (int i = 0; i < 27; i++) {
            if (inv.getItem(i) == null) {
                inv.setItem(i, ItemManager.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, 0, "§8"));
            }
        }
        playerData.setVariable("current_app", "internet");
        player.openInventory(inv);
    }
}
