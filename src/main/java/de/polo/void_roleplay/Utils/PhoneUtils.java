package de.polo.void_roleplay.Utils;

import de.polo.void_roleplay.DataStorage.FactionData;
import de.polo.void_roleplay.DataStorage.PlayerData;
import de.polo.void_roleplay.Main;
import de.polo.void_roleplay.MySQl.MySQL;
import de.polo.void_roleplay.PlayerUtils.ChatUtils;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class PhoneUtils implements Listener {
    public static HashMap<String, Boolean> phoneCallIsCreated = new HashMap<>();
    public static HashMap<String, String> phoneCallConnection = new HashMap<>();
    public static HashMap<String, Boolean> isInCallConnection = new HashMap<>();
    public static String error_nophone = "§8[§6Handy§8] §cDu hast kein Handy dabei.";
    public static String error_flightmode = "§8[§6Handy§8] §cDu bist im Flugmodus.";

    @EventHandler
    public void onPhoneUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() == Material.IRON_NUGGET && (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR)) {
            openPhone(player);
        }
    }

    public static void openPhone(Player player) {
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        Inventory inv = Bukkit.createInventory(player, 27, "§8» §eHandy");
        if (playerData.isFlightmode()) {
            inv.setItem(13, ItemManager.createItem(Material.GREEN_STAINED_GLASS_PANE, 1, 0, "§aFlugmodus abschalten", null));
        } else {
            inv.setItem(13, ItemManager.createItem(Material.RED_STAINED_GLASS_PANE, 1, 0, "§cFlugmodus einschalten", null));
        }
        inv.setItem(10, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmViYmJkYmEzNzI5NjNjOWQ2ZDMzMjhjMjliZjEyM2FlMDlkMzBjZTdiYTNhMDU3Y2VkNjA2YzFjODAyOGI3YiJ9fX0=", 1, 0, "§6Kontakte", null));
        inv.setItem(11, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGFlN2JmNDUyMmIwM2RmY2M4NjY1MTMzNjNlYWE5MDQ2ZmRkZmQ0YWE2ZjFmMDg4OWYwM2MxZTYyMTZlMGVhMCJ9fX0=", 1, 0, "§eNachrichten", null));
        inv.setItem(12, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODI0NDJiYmY3MTcxYjVjYWZjYTIxN2M5YmE0NGNlMjc2NDcyMjVkZjc2Y2RhOTY4OWQ2MWE5ZjFjMGE1ZjE3NiJ9fX0=", 1, 0, "§aAnrufen", null));
        for (int i = 0; i < 27; i++) {
            if (inv.getItem(i) == null) {
                inv .setItem(i, ItemManager.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, 0, "§8", null));
            }
        }
        playerData.setVariable("current_inventory", "handy");
        playerData.setVariable("current_app", null);
        player.openInventory(inv);
    }

    public static void openContacts(Player player, int page, String search) throws SQLException {
        if (page <= 0) return;
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        playerData.setVariable("current_app", "contacts");
        playerData.setIntVariable("current_page", page);
        Statement statement = MySQL.getStatement();
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
                inv.setItem(i, ItemManager.createItemHead(result.getString(2), 1, 0, "§8» §7" + result.getString(3).replace("&", "§"), "Lädt..."));
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
        inv.setItem(26, ItemManager.createItem(Material.GOLD_NUGGET, 1, 0, "§cNächste Seite", null));
        inv.setItem(18, ItemManager.createItem(Material.NETHER_WART, 1, 0, "§cVorherige Seite", null));
        inv.setItem(21, ItemManager.createItem(Material.REDSTONE, 1, 0, "§cZurück", null));
        inv.setItem(22, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjA1NmJjMTI0NGZjZmY5OTM0NGYxMmFiYTQyYWMyM2ZlZTZlZjZlMzM1MWQyN2QyNzNjMTU3MjUzMWYifX19", 1, 0, "§aKontakt hinzufügen", null));
        inv.setItem(23, ItemManager.createItem(Material.CLOCK, 1, 0, "§7Kontakt suchen...", null));
        for (int j = 0; j < 27; j++) {
            if (inv.getItem(j) == null) {
                inv.setItem(j, ItemManager.createItem(Material.BLACK_STAINED_GLASS_PANE, 1,0 , "§8", null));
            }
        }
        result.close();
        if (search != null) player.sendMessage("§8[§6Kontakte§8]§7 Es gab §a" + rows + " Ergebnisse§7.");
        player.openInventory(inv);
    }

    public static void editContact(Player player, ItemStack stack, boolean newContact, boolean canSave) {
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
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
                    Statement statement = MySQL.getStatement();
                    ResultSet result = statement.executeQuery("SELECT * FROM `phone_contacts` WHERE `contact_uuid` = '" + uuid + "' AND `uuid` = '" + player.getUniqueId() + "'");
                    if (result.next()) {
                        Inventory inv = Bukkit.createInventory(player, 27, "§8» §6Kontakt§8:§e " + result.getString(3).replace("&", "§"));
                        inv.setItem(4, ItemManager.createItemHead(targetplayer.getUniqueId().toString(), 1, 0, "§8", null));
                        inv.setItem(15, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODI0NDJiYmY3MTcxYjVjYWZjYTIxN2M5YmE0NGNlMjc2NDcyMjVkZjc2Y2RhOTY4OWQ2MWE5ZjFjMGE1ZjE3NiJ9fX0=", 1, 0, "§aAnrufen", null));
                        inv.setItem(16, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGFlN2JmNDUyMmIwM2RmY2M4NjY1MTMzNjNlYWE5MDQ2ZmRkZmQ0YWE2ZjFmMDg4OWYwM2MxZTYyMTZlMGVhMCJ9fX0=", 1, 0, "§eNachricht schreiben", null));
                        inv.setItem(22, ItemManager.createItem(Material.REDSTONE, 1, 0, "§cZurück", null));
                        if (!canSave) playerData.setIntVariable("current_contact_number", result.getInt(4));
                        if (!canSave) playerData.setVariable("current_contact_name", result.getString(3));
                        inv.setItem(10, ItemManager.createItem(Material.BOOK, 1, 0, "§eNummer", "§8 ➥ §7" + playerData.getIntVariable("current_contact_number")));
                        inv.setItem(11, ItemManager.createItem(Material.PAPER, 1, 0, "§eName", "§8 ➥ §7" + playerData.getVariable("current_contact_name").replace("&", "§")));
                        inv.setItem(12, ItemManager.createItem(Material.RED_DYE, 1, 0, "§c§lKontakt löschen", null));
                        if (canSave) inv.setItem(26, ItemManager.createItem(Material.EMERALD, 1, 0, "§aBestätigen", null));
                        playerData.setIntVariable("current_contact_id", result.getInt(1));
                        playerData.setVariable("current_contact_uuid", targetplayer.getUniqueId().toString());
                        for (int i = 0; i < 27; i++) {
                            if (inv.getItem(i) == null)
                                inv.setItem(i, ItemManager.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, 0, "§8", null));
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
            inv.setItem(4, ItemManager.createItem(Material.SKELETON_SKULL, 1, 0, "§8", null));
            inv.setItem(10, ItemManager.createItem(Material.BOOK, 1, 0, "§eNummer", "§8 ➥ §7" + playerData.getIntVariable("current_contact_number")));
            inv.setItem(11, ItemManager.createItem(Material.PAPER, 1, 0, "§eName", "§8 ➥ §7" + playerData.getVariable("current_contact_name").replace("&", "§")));
            inv.setItem(22, ItemManager.createItem(Material.REDSTONE, 1, 0, "§cZurück", null));
            inv.setItem(26, ItemManager.createItem(Material.EMERALD, 1, 0, "§aBestätigen", null));
            for (int i = 0; i < 27; i++) {
                if (inv.getItem(i) == null)
                    inv.setItem(i, ItemManager.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, 0, "§8", null));
            }
            player.openInventory(inv);
        }
    }

    public static boolean inPhoneCall(Player player) {
        return phoneCallIsCreated.get(player.getUniqueId().toString()) != null;
    }
    public static void createPhoneConnection(Player player, String reason) {
        phoneCallIsCreated.put(player.getUniqueId().toString(), true);
    }

    public static void deleteTicket(Player player) {
        if (inPhoneCall(player)) {
            phoneCallIsCreated.remove(player.getUniqueId().toString());
        }
    }

    public static void createTicketConnection(Player player, Player targetplayer) {
        phoneCallConnection.put(player.getUniqueId().toString(), targetplayer.getUniqueId().toString());
        phoneCallConnection.put(targetplayer.getUniqueId().toString(), player.getUniqueId().toString());
        isInCallConnection.put(player.getUniqueId().toString(), true);
        isInCallConnection.put(targetplayer.getUniqueId().toString(), true);
    }
    public static void deleteTicketConnection(Player player, Player targetplayer) {
        phoneCallConnection.remove(player.getUniqueId().toString());
        phoneCallConnection.remove(targetplayer.getUniqueId().toString());
        deleteTicket(targetplayer);
        isInCallConnection.remove(targetplayer.getUniqueId().toString());
        isInCallConnection.remove(player.getUniqueId().toString());
    }
    public static String getConnection(Player player) {
        return phoneCallConnection.get(player.getUniqueId().toString());
    }

    public static boolean isInConnection(Player player) {
        return isInCallConnection.get(player.getUniqueId().toString()) != null;
    }

    public static void addNumberToContacts(Player player, Player targetplayer) throws SQLException {
        String uuid = player.getUniqueId().toString();
        Statement statement = MySQL.getStatement();
        PlayerData playerData = PlayerManager.playerDataMap.get(targetplayer.getUniqueId().toString());
        statement.executeQuery("INSERT INTO `phone_contacts` (`uuid`, `contact_name`, `contact_number`, `contact_uuid`) VALUES ('" + player.getUniqueId().toString() + "', '" + targetplayer.getName() + "', " + playerData.getNumber() + ", '" + targetplayer.getUniqueId().toString() + "')");
    }

    public static void callNumber(Player player, int number) throws SQLException {
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        for (Player players : Bukkit.getOnlinePlayers()) {
            if (PlayerManager.playerDataMap.get(players.getUniqueId().toString()).getNumber() == number) {
                if (VertragUtil.setVertrag(player, players, "phonecall", players.getUniqueId().toString())) {
                    if (PhoneUtils.hasPhone(players)) {
                        ChatUtils.sendGrayMessageAtPlayer(players, players.getName() + "'s Handy klingelt...");
                        ChatUtils.sendGrayMessageAtPlayer(player, players.getName() + " wählt eine Nummer auf dem Handy.");
                        player.sendMessage("§8[§6Handy§8] §eDu rufst §l" + number + "§e an.");
                        players.sendMessage("§8[§6Handy§8] §eDu wirst von §l" + playerData.getNumber() + "§e angerufen.");
                        playerData.setVariable("calling", players.getUniqueId().toString());
                        VertragUtil.sendInfoMessage(players);
                        players.playSound(players.getLocation(), Sound.MUSIC_CREATIVE, 1, 0);
                    } else {
                        player.sendMessage(Main.error + "Die gewünschte Rufnummer ist zurzeit nicht erreichbar.");
                    }
                } else {
                    player.sendMessage(Main.error + "Dein Handy konnte keine Verbindung aufbauen. §o(Systemfehler)");
                }
            }
        }
    }

    public static void sendSMS(Player player, int number, StringBuilder message) {
        for (Player players : Bukkit.getOnlinePlayers()) {
            if (PlayerManager.playerDataMap.get(players.getUniqueId().toString()).getNumber() == number) {
                if (PhoneUtils.hasPhone(players)) {
                    players.sendMessage("§8[§6SMS§8] §e" + player.getName() + "§8: §7" + message);
                    player.sendMessage("§8[§6SMS§8] §e" + player.getName() + "§8: §7" + message);
                    player.playSound(player.getLocation(), Sound.BLOCK_WEEPING_VINES_STEP, 1, 0);
                    players.playSound(players.getLocation(), Sound.BLOCK_WEEPING_VINES_STEP, 1, 0);
                } else {
                    player.sendMessage(Main.error + "§8[§6Handy§8] §cAuto-Response§8:§7 Die SMS konnte zugestellt werden, jedoch nicht gelesen.");
                }
            }
        }
    }

    public static void acceptCall(Player player, String targetuuid) {
        player.stopSound(Sound.MUSIC_CREATIVE);
        if (PhoneUtils.hasPhone(player)) {
            Player targetplayer = Bukkit.getPlayer(UUID.fromString(targetuuid));
            assert targetplayer != null;
            if (PhoneUtils.hasPhone(targetplayer)) {
                targetplayer.sendMessage("§8[§6Handy§8] §7" + player.getName() + " hat dein Anruf angenommen");
                targetplayer.sendMessage("§8[§6Handy§8] §7Du hast den Anruf von " + player.getName() + " angenommen");
                isInCallConnection.put(targetuuid, true);
                isInCallConnection.put(player.getUniqueId().toString(), true);
                phoneCallConnection.put(player.getUniqueId().toString(), targetuuid);
                phoneCallConnection.put(targetuuid, player.getUniqueId().toString());
                player.playSound(player.getLocation(), Sound.BLOCK_IRON_DOOR_OPEN, 1, 0);
                targetplayer.playSound(targetplayer.getLocation(), Sound.BLOCK_IRON_DOOR_OPEN, 1, 0);
            }
        } else {
            player.sendMessage(PhoneUtils.error_nophone);
        }
    }

    public static void denyCall(Player player, String targetuuid) {
        player.stopSound(Sound.MUSIC_CREATIVE);
        if (PhoneUtils.hasPhone(player)) {
            Player targetplayer = Bukkit.getPlayer(UUID.fromString(targetuuid));
            assert targetplayer != null;
            if (PhoneUtils.hasPhone(targetplayer)) {
                targetplayer.sendMessage("§8[§6Handy§8] §7" + player.getName() + " hat dein Anruf abgelehnt");
                player.sendMessage("§8[§6Handy§8] §7Du hast den Anruf von " + player.getName() + " abgelehnt");
                player.playSound(player.getLocation(), Sound.BLOCK_IRON_DOOR_CLOSE, 1, 0);
                targetplayer.playSound(targetplayer.getLocation(), Sound.BLOCK_IRON_DOOR_CLOSE, 1, 0);
            }
        } else {
            player.sendMessage(PhoneUtils.error_nophone);
        }
    }

    public static void closeCall(Player player) {
        player.stopSound(Sound.MUSIC_CREATIVE);
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        if (isInCallConnection.get(player.getUniqueId().toString()) != null) {
            for (Player player1 : Bukkit.getOnlinePlayers()) {
                if (Objects.equals(phoneCallConnection.get(player1.getUniqueId().toString()), player.getUniqueId().toString())) {
                    isInCallConnection.remove(player1.getUniqueId().toString());
                    isInCallConnection.remove(player.getUniqueId().toString());
                    phoneCallConnection.remove(player1.getUniqueId().toString());
                    phoneCallConnection.remove(player.getUniqueId().toString());
                    player.sendMessage("§8[§6Handy§8]§7 Du hast aufgelegt.");
                    player1.sendMessage("§8[§6Handy§8] " + player.getName() + " hat aufgelegt.");
                    player.playSound(player.getLocation(), Sound.BLOCK_IRON_DOOR_CLOSE, 1, 0);
                    player1.playSound(player1.getLocation(), Sound.BLOCK_IRON_DOOR_CLOSE, 1, 0);
                }
            }
        } else if (playerData.getVariable("calling") != null) {
            for (Player players : Bukkit.getOnlinePlayers()) {
                if (playerData.getVariable("calling").equals(players.getUniqueId().toString())) {
                    VertragUtil.deleteVertrag(player);
                    VertragUtil.deleteVertrag(players);
                    player.sendMessage("§8[§6Handy§8]§7 Du hast aufgelegt.");
                    players.sendMessage("§8[§6Handy§8]§7§l " + playerData.getNumber() + "§7 hat aufgelegt.");
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
    }
}
