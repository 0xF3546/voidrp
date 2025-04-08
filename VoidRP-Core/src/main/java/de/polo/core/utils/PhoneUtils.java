package de.polo.core.utils;

import de.polo.core.Main;
import de.polo.core.agreement.services.VertragUtil;
import de.polo.core.storage.PhoneCall;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.game.base.crypto.Miner;
import de.polo.core.game.base.housing.House;
import de.polo.core.game.events.SubmitChatEvent;
import de.polo.core.utils.inventory.CustomItem;
import de.polo.core.utils.inventory.InventoryManager;
import de.polo.core.manager.ItemManager;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.api.player.enums.Gender;
import de.polo.core.utils.enums.RoleplayItem;
import de.polo.core.utils.player.ChatUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.sql.*;
import java.text.DecimalFormat;
import java.util.*;

public class PhoneUtils implements Listener {
    public static final String ERROR_NO_PHONE = "§8[§6Handy§8] §cDu hast kein Handy dabei.";
    public static final String ERROR_FLIGHTMODE = "§8[§6Handy§8] §cDu bist im Flugmodus.";
    private static final List<PhoneCall> phoneCalls = new ObjectArrayList<>();
    private final PlayerManager playerManager;
    private final Utils utils;

    public PhoneUtils(PlayerManager playerManager, Utils utils) {
        this.playerManager = playerManager;
        this.utils = utils;
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
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
            player.sendMessage(PhoneUtils.ERROR_NO_PHONE);
        }
    }

    public static void denyCall(Player player, String targetuuid) {
        player.stopSound(Sound.MUSIC_CREATIVE);
        if (!Main.getInstance().playerManager.getPlayerData(player).isDead()) {
            Player targetplayer = Bukkit.getPlayer(UUID.fromString(targetuuid));
            assert targetplayer != null;
            targetplayer.sendMessage("§8[§6Handy§8] §7" + player.getName() + " hat dein Anruf abgelehnt");
            player.sendMessage("§8[§6Handy§8] §7Du hast den Anruf von " + targetplayer.getName() + " abgelehnt");
            player.playSound(player.getLocation(), Sound.BLOCK_IRON_DOOR_CLOSE, 1, 0);
            targetplayer.playSound(targetplayer.getLocation(), Sound.BLOCK_IRON_DOOR_CLOSE, 1, 0);
            PlayerData playerData = Main.getInstance().playerManager.getPlayerData(targetplayer);
            playerData.setVariable("calling", null);
        } else {
            player.sendMessage(PhoneUtils.ERROR_NO_PHONE);
        }
    }

    public static boolean hasPhone(Player player) {
        return ItemManager.getCustomItemCount(player, RoleplayItem.SMARTPHONE) >= 1;
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
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8» §eHandy", true, true);
        int unreadMessages = 0;
        try {
            Statement statement = Main.getInstance().coreDatabase.getStatement();
            ResultSet result = statement.executeQuery("SELECT COUNT(*) AS unreadCount FROM phone_messages WHERE isRead = false AND uuid = '" + player.getUniqueId() + "'");
            if (result.next()) unreadMessages = result.getInt("unreadCount");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        inventoryManager.setItem(new CustomItem(10, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmViYmJkYmEzNzI5NjNjOWQ2ZDMzMjhjMjliZjEyM2FlMDlkMzBjZTdiYTNhMDU3Y2VkNjA2YzFjODAyOGI3YiJ9fX0=", 1, 0, "§6Kontakte")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                try {
                    openContacts(player, 1, null);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        inventoryManager.setItem(new CustomItem(11, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGFlN2JmNDUyMmIwM2RmY2M4NjY1MTMzNjNlYWE5MDQ2ZmRkZmQ0YWE2ZjFmMDg4OWYwM2MxZTYyMTZlMGVhMCJ9fX0=", 1, 0, "§eNachrichten", Collections.singletonList("§8 ➥ §7Du hast §a" + unreadMessages + "§7 ungelesene Nachrichten."))) {
            @Override
            public void onClick(InventoryClickEvent event) {
                try {
                    openMessages(player, 1, null);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        inventoryManager.setItem(new CustomItem(12, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODI0NDJiYmY3MTcxYjVjYWZjYTIxN2M5YmE0NGNlMjc2NDcyMjVkZjc2Y2RhOTY4OWQ2MWE5ZjFjMGE1ZjE3NiJ9fX0=", 1, 0, "§aAnrufen")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openCallApp(player, true);
            }
        });
        inventoryManager.setItem(new CustomItem(14, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjg4OWNmY2JhY2JlNTk4ZThhMWNkODYxMGI0OWZjYjYyNjQ0ZThjYmE5ZDQ5MTFkMTIxMTM0NTA2ZDhlYTFiNyJ9fX0=", 1, 0, "§3Banking")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openBanking(player);
            }
        });
        inventoryManager.setItem(new CustomItem(15, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTRkNDliYWU5NWM3OTBjM2IxZmY1YjJmMDEwNTJhNzE0ZDYxODU0ODFkNWIxYzg1OTMwYjNmOTlkMjMyMTY3NCJ9fX0=", 1, 0, "§7Einstellungen")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openSettings(player);
            }
        });
        inventoryManager.setItem(new CustomItem(16, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzZmOGEyMTlmMDgwMzk0MGYxZDI3MzQ5ZmIwNTBjMzJkYzdjMDUwZGIzM2NhMWUwYjM2YzIyZjIxYjA3YmU4NiJ9fX0=", 1, 0, "§bInternet")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openInternet(player);
            }
        });
    }

    public void openSettings(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8» §7Einstellungen", true, true);
        if (playerData.isFlightmode()) {
            inventoryManager.setItem(new CustomItem(10, ItemManager.createItem(Material.GREEN_STAINED_GLASS_PANE, 1, 0, "§aFlugmodus abschalten")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    event.getCurrentItem().setType(Material.RED_STAINED_GLASS_PANE);
                    ItemMeta itemMeta = event.getCurrentItem().getItemMeta();
                    itemMeta.setDisplayName("§cFlugmodus einschalten");
                    event.getCurrentItem().setItemMeta(itemMeta);
                    playerData.setFlightmode(false);
                }
            });
        } else {
            inventoryManager.setItem(new CustomItem(10, ItemManager.createItem(Material.RED_STAINED_GLASS_PANE, 1, 0, "§cFlugmodus einschalten")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    event.getCurrentItem().setType(Material.GREEN_STAINED_GLASS_PANE);
                    ItemMeta meta = event.getCurrentItem().getItemMeta();
                    meta.setDisplayName("§aFlugmodus abschalten");
                    event.getCurrentItem().setItemMeta(meta);
                    playerData.setFlightmode(true);
                }
            });
        }
        inventoryManager.setItem(new CustomItem(22, ItemManager.createItem(Material.REDSTONE, 1, 0, "§cZurück")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openPhone(player);
            }
        });
    }

    public void openBanking(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8» §3Banking", true, true);
        inventoryManager.setItem(new CustomItem(4, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTY0MzlkMmUzMDZiMjI1NTE2YWE5YTZkMDA3YTdlNzVlZGQyZDUwMTVkMTEzYjQyZjQ0YmU2MmE1MTdlNTc0ZiJ9fX0=", 1, 0, "§bKontostand", Collections.singletonList("§8 ➥ §7" + new DecimalFormat("#,###").format(playerData.getBank()) + "$"))) {
            @Override
            public void onClick(InventoryClickEvent event) {

            }
        });
        inventoryManager.setItem(new CustomItem(11, ItemManager.createItem(Material.DIAMOND, 1, 0, "§bTransaktionen", "§8 ➥ §7Alle Transaktionen der Letzten 7 Tage")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                try {
                    openTransactions(player, 1, null);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        inventoryManager.setItem(new CustomItem(22, ItemManager.createItem(Material.REDSTONE, 1, 0, "§cZurück")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openPhone(player);
            }
        });
    }

    public void openTransactions(Player player, int page, String search) throws SQLException {
        if (page <= 0) return;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        Statement statement = Main.getInstance().coreDatabase.getStatement();
        ResultSet result = null;
        if (search == null) {
            result = statement.executeQuery("SELECT *, DATE_FORMAT(datum, '%d.%m.%Y | %H:%i:%s') AS formatted_timestamp FROM `bank_logs` WHERE `uuid` = '" + player.getUniqueId() + "' ORDER BY datum DESC");
        } else {
            result = statement.executeQuery("SELECT *, DATE_FORMAT(datum, '%d.%m.%Y | %H:%i:%s') AS formatted_timestamp FROM `bank_logs` WHERE LOWER(`reason`) LIKE LOWER('%" + search + "%') AND `uuid` = '" + player.getUniqueId() + "' ORDER BY datum DESC");
        }
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8» §bTransaktionen §8- §bSeite§8:§7 " + page, true, true);
        int i = 0;
        int rows = 0;
        while (result.next()) {
            rows++;
            if (result.getRow() >= (18 * (page - 1)) && result.getRow() <= (18 * page)) {
                if (result.getBoolean(2) && result.getInt(4) >= 0) {
                    inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(Material.PAPER, 1, 0, "§8» §aEinzahlung", Arrays.asList("§8 ➥ §7Höhe§8:§a +" + result.getInt(4) + "$", "§8 ➥ §7Grund§8:§6 " + result.getString(5), "§8 ➥ §7Datum§8:§6 " + result.getString("formatted_timestamp")))) {
                        @Override
                        public void onClick(InventoryClickEvent event) {

                        }
                    });
                } else {
                    inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(Material.PAPER, 1, 0, "§8» §cAuszahlung", Arrays.asList("§8 ➥ §7Höhe§8:§c -" + result.getInt(4) + "$", "§8 ➥ §7Grund§8:§6 " + result.getString(5), "§8 ➥ §7Datum§8:§6 " + result.getString("formatted_timestamp")))) {
                        @Override
                        public void onClick(InventoryClickEvent event) {

                        }
                    });
                }
                i++;
            }
        }
        inventoryManager.setItem(new CustomItem(26, ItemManager.createItem(Material.GOLD_NUGGET, 1, 0, "§cNächste Seite")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                try {
                    openTransactions(player, page + 1, search);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        inventoryManager.setItem(new CustomItem(18, ItemManager.createItem(Material.NETHER_WART, 1, 0, "§cVorherige Seite")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                try {
                    openTransactions(player, page - 1, null);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        inventoryManager.setItem(new CustomItem(21, ItemManager.createItem(Material.REDSTONE, 1, 0, "§cZurück")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openBanking(player);
            }
        });
        if (search == null) {
            inventoryManager.setItem(new CustomItem(23, ItemManager.createItem(Material.CLOCK, 1, 0, "§7Transaktion suchen...")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    player.closeInventory();
                    playerData.setVariable("chatblock", "checktransactions");
                    player.sendMessage("§8[§3Banking§8]§7 Gib nun den Transaktionsgrund an.");
                }

                @Override
                @SneakyThrows
                public void onChatSubmit(SubmitChatEvent event) {
                    if (!event.getSubmitTo().equalsIgnoreCase("checktransactions")) {
                        return;
                    }
                    if (event.isCancel()) {
                        event.end();
                        event.sendCancelMessage();
                        return;
                    }
                    openTransactions(event.getPlayer(), 1, event.getMessage());
                    event.end();
                }
            });
        } else {
            inventoryManager.setItem(new CustomItem(23, ItemManager.createItem(Material.CLOCK, 1, 0, "§7Suche löschen")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    try {
                        openTransactions(player, page, null);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
        result.close();
    }

    public void openMessages(Player player, int page, String search) throws SQLException {
        if (page <= 0) return;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        playerData.setVariable("current_app", "messages");
        playerData.setIntVariable("current_page", page);
        Statement statement = Main.getInstance().coreDatabase.getStatement();
        ResultSet result = null;
        if (search == null) {
            result = statement.executeQuery("SELECT *, DATE_FORMAT(datum, '%d.%m.%Y | %H:%i:%s') AS formatted_timestamp FROM `phone_messages` WHERE `uuid` = '" + player.getUniqueId() + "' ORDER BY datum DESC");
        } else {
            result = statement.executeQuery("SELECT *, DATE_FORMAT(datum, '%d.%m.%Y | %H:%i:%s') AS formatted_timestamp FROM `phone_messages` WHERE LOWER(`message`) LIKE LOWER('%" + search + "%') AND `uuid` = '" + player.getUniqueId() + "' ORDER BY datum DESC");
        }
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8» §eNachrichten §8- §eSeite§8:§7 " + page, true, true);
        int i = 0;
        int rows = 0;
        while (result.next()) {
            rows++;
            if (result.getRow() >= (18 * (page - 1)) && result.getRow() <= (18 * page)) {
                int id = result.getInt("id");
                if (result.getBoolean(7)) {
                    inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(Material.PAPER, 1, 0, "§8» §e" + result.getInt(6), Arrays.asList("§8 ➥ §7Nachricht§8:§6 " + result.getString(4), "§8 ➥ §7Datum§8:§6 " + result.getString("formatted_timestamp")))) {
                        @Override
                        public void onClick(InventoryClickEvent event) {

                        }
                    });
                } else {
                    inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(Material.PAPER, 1, 0, "§8» §e" + result.getInt(6), Arrays.asList("§8 ➥ §7Nachricht§8:§6 " + result.getString(4), "§8 ➥ §7Datum§8:§6 " + result.getString("formatted_timestamp"), "", "§8» §aAls gelesen markieren"))) {
                        @Override
                        public void onClick(InventoryClickEvent event) {
                            try {
                                Connection connection = Main.getInstance().coreDatabase.getConnection();
                                PreparedStatement preparedStatement = connection.prepareStatement("UPDATE phone_messages SET isRead = true WHERE id = ?");
                                preparedStatement.setInt(1, id);
                                preparedStatement.executeUpdate();
                                openMessages(player, page, search);
                                preparedStatement.close();
                                connection.close();
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
                }
                i++;
            }
        }
        inventoryManager.setItem(new CustomItem(26, ItemManager.createItem(Material.GOLD_NUGGET, 1, 0, "§cNächste Seite")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                try {
                    openTransactions(player, page + 1, search);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        inventoryManager.setItem(new CustomItem(18, ItemManager.createItem(Material.NETHER_WART, 1, 0, "§cVorherige Seite")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                try {
                    openTransactions(player, page - 1, search);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        if (search == null) {
            inventoryManager.setItem(new CustomItem(23, ItemManager.createItem(Material.CLOCK, 1, 0, "§7Nachricht suchen...")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    player.closeInventory();
                    playerData.setVariable("chatblock", "checkmessages");
                    player.sendMessage("§8[§6SMS§8]§7 Gib nun die Nachricht an.");
                }

                @Override
                public void onChatSubmit(SubmitChatEvent event) {
                    if (!event.getSubmitTo().equalsIgnoreCase("checkmessages")) {
                        return;
                    }
                    if (event.isCancel()) {
                        event.end();
                        event.sendCancelMessage();
                        return;
                    }
                    try {
                        openMessages(event.getPlayer(), 1, event.getMessage());
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    event.end();
                }
            });
        } else {
            inventoryManager.setItem(new CustomItem(23, ItemManager.createItem(Material.CLOCK, 1, 0, "§7Suche leeren")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    try {
                        openTransactions(player, page, null);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
        result.close();
    }

    public void openContacts(Player player, int page, String search) throws SQLException {
        if (page <= 0) return;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        playerData.setVariable("current_app", "contacts");
        playerData.setIntVariable("current_page", page);
        Statement statement = Main.getInstance().coreDatabase.getStatement();
        ResultSet result = null;
        if (search == null) {
            result = statement.executeQuery("SELECT * FROM `phone_contacts`");
        } else {
            result = statement.executeQuery("SELECT * FROM `phone_contacts` WHERE LOWER(`contact_name`) LIKE LOWER('%" + search + "%') ");
        }
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8» §6Kontakte §8- §6Seite§8:§7 " + page, true, true);
        int i = 0;
        int rows = 0;
        while (result.next()) {
            rows++;
            if (result.getRow() >= (18 * (page - 1)) && result.getRow() <= (18 * page)) {
                inventoryManager.setItem(new CustomItem(i, ItemManager.createItemHead(result.getString(5), 1, 0, "§8» §7" + result.getString(3).replace("&", "§"), Arrays.asList("§8 ➥ §6Nummer§8:§7 " + result.getInt(4), "§8 ➥ §aNeue SMS§8:§7 §cBald verfügbar!"))) {
                    @Override
                    public void onClick(InventoryClickEvent event) {

                    }
                });
                i++;
            }
        }
        inventoryManager.setItem(new CustomItem(26, ItemManager.createItem(Material.GOLD_NUGGET, 1, 0, "§cNächste Seite")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                try {
                    openContacts(player, page + 1, search);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        inventoryManager.setItem(new CustomItem(18, ItemManager.createItem(Material.NETHER_WART, 1, 0, "§cVorherige Seite")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                try {
                    openContacts(player, page - 1, search);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        inventoryManager.setItem(new CustomItem(22, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjA1NmJjMTI0NGZjZmY5OTM0NGYxMmFiYTQyYWMyM2ZlZTZlZjZlMzM1MWQyN2QyNzNjMTU3MjUzMWYifX19", 1, 0, "§aKontakt hinzufügen")) {
            @Override
            public void onClick(InventoryClickEvent event) {

            }
        });
        if (search == null) {
            inventoryManager.setItem(new CustomItem(23, ItemManager.createItem(Material.CLOCK, 1, 0, "§7Kontakt suchen...")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    playerData.setVariable("chatblock", "contactsearch");
                    player.closeInventory();
                    player.sendMessage("§8[§6Kontakte§8]§7 Gib nun den Namen des Kontaktes ein.");
                }

                @Override
                public void onChatSubmit(SubmitChatEvent event) {
                    if (!event.getSubmitTo().equals("contactsearch")) {
                        return;
                    }
                    if (event.isCancel()) {
                        event.end();
                        event.sendCancelMessage();
                        return;
                    }
                    try {
                        openContacts(event.getPlayer(), 1, event.getMessage());
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    event.end();
                }
            });
        } else {
            inventoryManager.setItem(new CustomItem(23, ItemManager.createItem(Material.CLOCK, 1, 0, "§7Suche leeren")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    try {
                        openContacts(player, page, null);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
        result.close();
        if (search != null) player.sendMessage("§8[§6Kontakte§8]§7 Es gab §a" + rows + " Ergebnisse§7.");
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
                    Statement statement = Main.getInstance().coreDatabase.getStatement();
                    ResultSet result = statement.executeQuery("SELECT * FROM `phone_contacts` WHERE `contact_uuid` = '" + uuid + "' AND `uuid` = '" + player.getUniqueId() + "'");
                    if (result.next()) {
                        Inventory inv = Bukkit.createInventory(player, 27, "§8» §6Kontakt§8:§e " + result.getString(3).replace("&", "§"));
                        inv.setItem(4, ItemManager.createItemHead(targetplayer.getUniqueId().toString(), 1, 0, "§8"));
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
                player.sendMessage(Prefix.ERROR + "Spieler konnte nicht geladen werden.");
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
        InventoryManager inventoryManager = new InventoryManager(player, 9, "Lade...", true, true);
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (isNew) {
            inventoryManager = new InventoryManager(player, 54, "§8 » §aAnrufen§8:§2 ", true, true);
            playerData.setVariable("current_phone_callnumber", "");
        } else {
            inventoryManager = new InventoryManager(player, 54, "§8 » §aAnrufen§8:§2 " + playerData.getVariable("current_phone_callnumber"), true, true);
        }
        inventoryManager.setItem(new CustomItem(12, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmNmYWZmYThjNmM3ZjYyNjIxNjgyZmU1NjcxMWRjM2I4OTQ0NjVmZGY3YTYyZjQzYjMxYTBkMzQwM2YzNGU3In19fQ==", 1, 0, "§6§l1")) {
            @Override
            public void onClick(InventoryClickEvent event) {

            }
        });
        inventoryManager.setItem(new CustomItem(13, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzQxMGRiNjIzNzM1YzE0NmM3YzQ4N2UzNjkyZDFjNWI1ZTIzYmY2OTFlZjA2NjVjMmU5NTQ5NDc5ZDgyOGYifX19", 1, 0, "§6§l2")) {
            @Override
            public void onClick(InventoryClickEvent event) {

            }
        });
        inventoryManager.setItem(new CustomItem(14, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWY4ZTdhY2Y3MjQyYWZhNTg2YzNkMDk1Zjg3ZmU5ZGU3YjdjYTI0YzRhMjhhNTYwNDAzNDdjNjU3OTYwZTM2In19fQ==", 1, 0, "§6§l3")) {
            @Override
            public void onClick(InventoryClickEvent event) {

            }
        });
        inventoryManager.setItem(new CustomItem(21, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTQ3YzczZDdkMmVmMTQ3MTJhZTU5YzU3ZDY0ZTUxMmE3ZjljNTI3NzQ2YjhiYzQyNDI3MGY5ZTM3YzE4MWM3OCJ9fX0=", 1, 0, "§6§l4")) {
            @Override
            public void onClick(InventoryClickEvent event) {

            }
        });
        inventoryManager.setItem(new CustomItem(22, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzVhZmY5MmRlODkyZDU5MjJhZDc1M2RhZDVhZTM0NzlhYjE1MGFmNGVkMjg0YWJmYTc1Y2E3YTk5NWMxODkzIn19fQ==", 1, 0, "§6§l5")) {
            @Override
            public void onClick(InventoryClickEvent event) {

            }
        });
        inventoryManager.setItem(new CustomItem(23, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjE2ZDQyM2M3ZmQ2NGM1YzdmNDA4NTQ2ZGE0Yjc4N2U5M2RiZWFjMGU3N2IxOWM0OWI5YWQ0ZmY0MThmMmQxIn19fQ==", 1, 0, "§6§l6")) {
            @Override
            public void onClick(InventoryClickEvent event) {

            }
        });
        inventoryManager.setItem(new CustomItem(30, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzQ4N2U5MTJhOGU5YmZjNDkxMjQzNmFmNTZjNDZjMmU2YTE1YTFkMjJkMWFkMThkNDZhMjI5ZDc2NDhlIn19fQ==", 1, 0, "§6§l7")) {
            @Override
            public void onClick(InventoryClickEvent event) {

            }
        });
        inventoryManager.setItem(new CustomItem(31, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTEwMWQ0YjQ3ZGNjYjc2MTJhYzVlZmRlNWFlMjQ0MWM4MmMzZjBhNjg0MDQxYWVkMzgyNzZkYmRmOTQifX19", 1, 0, "§6§l8")) {
            @Override
            public void onClick(InventoryClickEvent event) {

            }
        });
        inventoryManager.setItem(new CustomItem(32, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmE0Njg1NzQ0MWNhYWU2ZTE2YzkyOTZmYjU3MTQ4MmFhNTEzNjI2OGQzOWUzNWI3YWNmYmY1MTM5YTM3ZTAzZCJ9fX0=", 1, 0, "§6§l9")) {
            @Override
            public void onClick(InventoryClickEvent event) {

            }
        });
        inventoryManager.setItem(new CustomItem(40, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWJmYTYzYTBhNTQyOGIyNzM0NTNmZmU3ODRkM2U0ODljYmNmNmQxMmI3ODQ1MGEzNTE1NzE2Y2U3MjRmNCJ9fX0=", 1, 0, "§6§l0")) {
            @Override
            public void onClick(InventoryClickEvent event) {

            }
        });
        inventoryManager.setItem(new CustomItem(45, ItemManager.createItem(Material.REDSTONE, 1, 0, "§cZurück")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openPhone(player);
            }
        });
        inventoryManager.setItem(new CustomItem(53, ItemManager.createItem(Material.EMERALD, 1, 0, "§aAnrufen")) {
            @Override
            public void onClick(InventoryClickEvent event) {

            }
        });
    }

    public Collection<PhoneCall> getCalls() {
        return phoneCalls;
    }

    public boolean isInCall(Player player) {
        return getCall(player) != null;
    }

    public List<Player> getPlayersInCall(PhoneCall call) {
        List<Player> players = new ObjectArrayList<>();

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
        PlayerData playerData = playerManager.getPlayerData(targetplayer.getUniqueId());
        Main.getInstance().getCoreDatabase().insertAsync("INSERT INTO phone_contacts (uuid, contact_name, contact_number, contact_uuid) VALUES (?, ?, ?, ?)",
                player.getUniqueId().toString(),
                targetplayer.getName(),
                playerData.getNumber(),
                targetplayer.getUniqueId().toString());
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
                player.sendMessage(Prefix.ERROR + "Die gewünschte Rufnummer ist zurzeit nicht erreichbar.");
            }
        } else {
            player.sendMessage(Prefix.ERROR + "Dein Handy konnte keine Verbindung aufbauen. §o(Systemfehler)");
        }
    }

    public void sendSMS(Player player, Player players, StringBuilder message) {
        if (!playerManager.getPlayerData(players).isDead()) {
            players.sendMessage("§8[§6SMS§8] §e" + player.getName() + "§8: §7" + message);
            player.sendMessage("§8[§6SMS§8] §e" + player.getName() + "§8: §7" + message);
            player.playSound(player.getLocation(), Sound.BLOCK_WEEPING_VINES_STEP, 1, 0);
            players.playSound(players.getLocation(), Sound.BLOCK_WEEPING_VINES_STEP, 1, 0);
        } else {
            player.sendMessage(Prefix.ERROR + "§8[§6Handy§8] §cAuto-Response§8:§7 Die SMS konnte zugestellt werden, jedoch nicht gelesen werden.");
        }
        PlayerData targetplayerData = playerManager.getPlayerData(players.getUniqueId());
        Main.getInstance().getCoreDatabase().insertAsync("INSERT INTO phone_messages (uuid, contact_uuid, message, number) VALUES (?, ?, ?, ?)",
                players.getUniqueId().toString(),
                player.getUniqueId().toString(),
                message,
                targetplayerData.getId());
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
                    player.stopSound(Sound.MUSIC_CREATIVE);
                }
            }
        } else {
            player.sendMessage("§8[§6Handy§8]§7 Du bist in keinem Anruf.");
        }
    }

    @EventHandler
    public void onChatSubmit(SubmitChatEvent event) throws SQLException {
        if (event.getSubmitTo().equalsIgnoreCase("wallet::transaction::player")) {
            if (event.isCancel()) {
                event.end();
                event.sendCancelMessage();
                return;
            }
            Player target = Bukkit.getPlayer(event.getMessage());
            if (target == null) {
                event.getPlayer().sendMessage(Prefix.ERROR + "Spieler nicht gefunden.");
                openTransaction(event.getPlayer());
                event.end();
                return;
            }
            event.getPlayerData().setVariable("wallet::transaction::player", target);
            openTransaction(event.getPlayer());
        }
        if (event.getSubmitTo().equalsIgnoreCase("wallet::transaction::coins")) {
            if (event.isCancel()) {
                event.end();
                event.sendCancelMessage();
                return;
            }
            try {
                event.getPlayerData().setVariable("wallet::transaction::coins", Float.parseFloat(event.getMessage()));
            } catch (Exception e) {
                event.getPlayer().sendMessage(Prefix.ERROR + "Die Zahl muss numerisch sein.");
            }
            openTransaction(event.getPlayer());
        }

        if (event.getSubmitTo().equalsIgnoreCase("dating::description")) {
            if (event.isCancel()) {
                event.end();
                event.sendCancelMessage();
                return;
            }
            Connection connection = Main.getInstance().coreDatabase.getConnection();
            PreparedStatement statement = connection.prepareStatement("UPDATE app_dating_profiles SET description = ? WHERE uuid = ?");
            statement.setString(1, event.getMessage());
            statement.setString(2, event.getPlayer().getUniqueId().toString());
            statement.executeUpdate();
            statement.close();
            connection.close();
            openDatingApp(event.getPlayer());
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
                editContact(event.getPlayer(), ItemManager.createItemHead(event.getPlayerData().getVariable("current_contact_uuid"), 1, 0, "§8"), false, true);
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
                editContact(event.getPlayer(), ItemManager.createItemHead(event.getPlayerData().getVariable("current_contact_uuid"), 1, 0, "§8"), false, true);
            }
            event.end();
        }
    }

    public void openInternet(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8» §bInternet", true, true);
        inventoryManager.setItem(new CustomItem(11, ItemManager.createItem(Material.DIAMOND, 1, 0, "§bAnwalt", "§8 ➥ §7Anwalt anheuern (§c15-55$/PayDay§7)")) {
            @SneakyThrows
            @Override
            public void onClick(InventoryClickEvent event) {
                Statement statement = Main.getInstance().coreDatabase.getStatement();
                if (playerData.hasAnwalt()) {
                    playerData.setHasAnwalt(false);
                    player.closeInventory();
                    player.sendMessage("§8[§6Anwalt§8]§7 Du hast deinen Anwalt §cabbestellt§7.");
                    statement.execute("UPDATE players SET hasAnwalt = " + playerData.hasAnwalt() + " WHERE uuid = '" + player.getUniqueId() + "'");
                } else {
                    playerData.setHasAnwalt(true);
                    player.closeInventory();
                    player.sendMessage("§8[§6Anwalt§8]§7 Du hast deinen Anwalt §aeingestellt§7.");
                    statement.execute("UPDATE players SET hasAnwalt = " + playerData.hasAnwalt() + " WHERE uuid = '" + player.getUniqueId() + "'");
                }
            }
        });
        inventoryManager.setItem(new CustomItem(12, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjRlYWY4OTQyOGEzNjQ5YzI2ZWRjMWY3MWNjMTlmMjYzZTlmNGViMzFlZDE4Yzk3Njg2YWFjODJmNzY0MjQyIn19fQ==", 1, 0, "§5Swiper", Collections.singletonList("§8 ➥ §7Finde einen Beziehungs-Partner"))) {
            @SneakyThrows
            @Override
            public void onClick(InventoryClickEvent event) {
                openDatingApp(player);
            }
        });
        inventoryManager.setItem(new CustomItem(13, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWNkNzBjZTQ4MTg1ODFjYTQ3YWRmNmI4MTY3OWZkMTY0NmZkNjg3YzcxMjdmZGFhZTk0ZmVkNjQwMTU1ZSJ9fX0=", 1, 0, "§eCrypto Wallet", Collections.singletonList("§8 ➥ §a" + playerData.getCrypto() + " Coins"))) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openCryptoWallet(player);
            }
        });
        inventoryManager.setItem(new CustomItem(22, ItemManager.createItem(Material.REDSTONE, 1, 0, "§cZurück")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openPhone(player);
            }
        });
    }

    private void openCryptoWallet(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player);
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8» §eCrypto Wallet");
        inventoryManager.setItem(new CustomItem(4, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWNkNzBjZTQ4MTg1ODFjYTQ3YWRmNmI4MTY3OWZkMTY0NmZkNjg3YzcxMjdmZGFhZTk0ZmVkNjQwMTU1ZSJ9fX0=", 1, 0, "§eCrypto Wallet", Arrays.asList("§8 ➥ §a" + playerData.getCrypto() + " Coins", "§8 ➥ §ePreis§8:§7 " + Main.getInstance().gamePlay.getCrypto().getPrice() + "$"))) {
            @Override
            public void onClick(InventoryClickEvent event) {

            }
        });

        inventoryManager.setItem(new CustomItem(11, ItemManager.createItem(Material.PAPER, 1, 0, "§7Coins senden")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openTransaction(player);
            }
        });

        inventoryManager.setItem(new CustomItem(12, ItemManager.createItem(Material.PAPER, 1, 0, "§7Transaktionen")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openCryptoTransactions(player, 1, null);
            }
        });
        inventoryManager.setItem(new CustomItem(13, ItemManager.createItem(Material.PAPER, 1, 0, "§7Cryptofarmen verwalten")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openFarmManager(player);
            }
        });
        inventoryManager.setItem(new CustomItem(14, ItemManager.createItem(Material.PAPER, 1, 0, "§cVerkaufen")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openSellApp(player, 1);
            }
        });
        inventoryManager.setItem(new CustomItem(15, ItemManager.createItem(Material.PAPER, 1, 0, "§aKaufen")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openBuyApp(player, 1);
            }
        });

        inventoryManager.setItem(new CustomItem(22, ItemManager.createItem(Material.REDSTONE, 1, 0, "§cZurück")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openInternet(player);
            }
        });
    }

    private void openSellApp(Player player, int page) {
        PlayerData playerData = playerManager.getPlayerData(player);
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8 » §cVerkauf §8-§c Seite§8: §7" + page);
        inventoryManager.setItem(new CustomItem(26, ItemManager.createItem(Material.GOLD_NUGGET, 1, 0, "§cNächste Seite")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openSellApp(player, page + 1);
            }
        });
        inventoryManager.setItem(new CustomItem(18, ItemManager.createItem(Material.NETHER_WART, 1, 0, "§cVorherige Seite")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openSellApp(player, page - 1);
            }
        });
        inventoryManager.setItem(new CustomItem(22, ItemManager.createItem(Material.REDSTONE, 1, 0, "§cZurück")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openCryptoWallet(player);
            }
        });
    }

    private void openBuyApp(Player player, int page) {
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8 » §aAnkauf §8-§a Seite§8: §7" + page);
    }

    private void openFarmManager(Player player) {
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8 » §eFarmen");
        int i = 0;
        for (House house : Main.getInstance().houseManager.getHouses(player)) {
            if (!house.isServerRoom()) return;
            for (Miner miner : house.getActiveMiner()) {
                inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(Material.GOLD_INGOT, 1, 0, "§6Miner #" + miner.getId(), "§8 ➥ §7Haus " + house.getNumber())) {
                    @Override
                    public void onClick(InventoryClickEvent event) {

                    }
                });
                i++;
            }
        }
    }

    private void openTransaction(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player);
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8 » §eWallet-Überweisung");
        Player target = playerData.getVariable("wallet::transaction::toPlayer");
        inventoryManager.setItem(new CustomItem(12, target == null ? ItemManager.createItem(Material.PLAYER_HEAD, 1, 0, "§7Spieler angeben") : ItemManager.createItemHead(target.getUniqueId().toString(), 1, 0, "§7" + target.getName())) {
            @Override
            public void onClick(InventoryClickEvent event) {
                player.sendMessage("§8[§eWallet§8]§7 Gib nun den Spieler an, an welchen die Coins gehen.");
                playerData.setVariable("chatblock", "wallet::transaction::player");
                player.closeInventory();
            }
        });
        float amount = playerData.getVariable("wallet::transaction::coins") != null ? playerData.getVariable("wallet::transaction::coins") : 0;
        inventoryManager.setItem(new CustomItem(14, ItemManager.createItem(Material.PAPER, 1, 0, "§7Anzahl angeben", "§8 ➥ §a" + amount + "$")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                player.sendMessage("§8[§eWallet§8]§7 Gib nun den Coins der Spieler erhalten soll.");
                playerData.setVariable("chatblock", "wallet::transaction::coins");
                player.closeInventory();
            }
        });

        if (target != null && amount >= 1) {
            inventoryManager.setItem(new CustomItem(27, ItemManager.createItem(Material.EMERALD, 1, 0, "§aTransaktion ausführen", Arrays.asList("§8 ➥ §e" + target.getName(), "§8 ➥ §a" + amount + "$"))) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    PlayerData targetData = playerManager.getPlayerData(target);
                    if (playerData.getCrypto() < amount) {
                        return;
                    }
                    targetData.addCrypto(amount, "Überweisung von " + player.getName(), false);
                    playerData.removeCrypto(amount, "Überweisung an " + target.getName(), false);
                    player.sendMessage("§8[§eWallet§8]§a Du hast " + target.getName() + " " + amount + " Coins überwiesen.");
                    target.sendMessage("§8[§eWallet§8]§a " + player.getName() + " hat dir " + amount + " Coins überwiesen.");
                }
            });
        }
    }

    @SneakyThrows
    public void openCryptoTransactions(Player player, int page, String search) {
        if (page <= 0) return;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        Statement statement = Main.getInstance().coreDatabase.getStatement();
        ResultSet result = null;
        if (search == null) {
            result = statement.executeQuery("SELECT *, DATE_FORMAT(created, '%d.%m.%Y | %H:%i:%s') AS formatted_timestamp FROM `crypto_transactions` WHERE `uuid` = '" + player.getUniqueId() + "' ORDER BY created DESC");
        } else {
            result = statement.executeQuery("SELECT *, DATE_FORMAT(created, '%d.%m.%Y | %H:%i:%s') AS formatted_timestamp FROM `crypto_transactions` WHERE LOWER(`reason`) LIKE LOWER('%" + search + "%') AND `uuid` = '" + player.getUniqueId() + "' ORDER BY created DESC");
        }
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8» §bTransaktionen §8- §bSeite§8:§7 " + page, true, true);
        int i = 0;
        int rows = 0;
        while (result.next()) {
            rows++;
            if (result.getRow() >= (18 * (page - 1)) && result.getRow() <= (18 * page)) {
                if (result.getFloat("amount") >= 0) {
                    inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(Material.PAPER, 1, 0, "§8» §aEinzahlung", Arrays.asList("§8 ➥ §7Höhe§8:§a +" + result.getFloat("amount") + "$", "§8 ➥ §7Grund§8:§6 " + result.getString("reason"), "§8 ➥ §7Datum§8:§6 " + result.getString("formatted_timestamp")))) {
                        @Override
                        public void onClick(InventoryClickEvent event) {

                        }
                    });
                } else {
                    inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(Material.PAPER, 1, 0, "§8» §cAuszahlung", Arrays.asList("§8 ➥ §7Höhe§8:§c -" + result.getFloat("amount") + "$", "§8 ➥ §7Grund§8:§6 " + result.getString("reason"), "§8 ➥ §7Datum§8:§6 " + result.getString("formatted_timestamp")))) {
                        @Override
                        public void onClick(InventoryClickEvent event) {

                        }
                    });
                }
                i++;
            }
        }
        inventoryManager.setItem(new CustomItem(26, ItemManager.createItem(Material.GOLD_NUGGET, 1, 0, "§cNächste Seite")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openCryptoTransactions(player, page + 1, search);
            }
        });
        inventoryManager.setItem(new CustomItem(18, ItemManager.createItem(Material.NETHER_WART, 1, 0, "§cVorherige Seite")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openCryptoTransactions(player, page - 1, null);
            }
        });
        inventoryManager.setItem(new CustomItem(21, ItemManager.createItem(Material.REDSTONE, 1, 0, "§cZurück")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openCryptoWallet(player);
            }
        });
        if (search == null) {
            inventoryManager.setItem(new CustomItem(23, ItemManager.createItem(Material.CLOCK, 1, 0, "§7Transaktion suchen...")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    player.closeInventory();
                    playerData.setVariable("chatblock", "checktransactions");
                    player.sendMessage("§8[§3Banking§8]§7 Gib nun den Transaktionsgrund an.");
                }

                @Override
                @SneakyThrows
                public void onChatSubmit(SubmitChatEvent event) {
                    if (!event.getSubmitTo().equalsIgnoreCase("checktransactions")) {
                        return;
                    }
                    if (event.isCancel()) {
                        event.end();
                        event.sendCancelMessage();
                        return;
                    }
                    openTransactions(event.getPlayer(), 1, event.getMessage());
                    event.end();
                }
            });
        } else {
            inventoryManager.setItem(new CustomItem(23, ItemManager.createItem(Material.CLOCK, 1, 0, "§7Suche löschen")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    openCryptoTransactions(player, page, null);
                }
            });
        }
        result.close();
    }

    @SneakyThrows
    private void openDatingApp(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8» §5Swiper", true, true);
        Connection connection = Main.getInstance().coreDatabase.getConnection();
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM app_dating_profiles WHERE uuid = ?");
        statement.setString(1, player.getUniqueId().toString());
        ResultSet result = statement.executeQuery();
        boolean hasProfile = result.next();
        System.out.println("RESULT: " + hasProfile);

        if (hasProfile) {
            inventoryManager.setItem(new CustomItem(11, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmJiOThhMjE5YmE0YWY1MTMyMWE4NWRiZjVmZjgzN2M1NjdkODBmMTA2NWE4ZGIxYTJjZWNiMTI1ZTYyMzAyNyJ9fX0=", 1, 0, "§5Beschreibung ändern", Collections.singletonList("§8 ➥ §e" + result.getString("description")))) {
                @SneakyThrows
                @Override
                public void onClick(InventoryClickEvent event) {
                    playerData.setVariable("chatblock", "dating::description");
                    player.sendMessage("§8[§6Handy§8]§7 Gib nun deine neue Beschreibung ein.");
                    player.closeInventory();
                }
            });
            inventoryManager.setItem(new CustomItem(13, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjRlYWY4OTQyOGEzNjQ5YzI2ZWRjMWY3MWNjMTlmMjYzZTlmNGViMzFlZDE4Yzk3Njg2YWFjODJmNzY0MjQyIn19fQ==", 1, 0, "§5Swipen starten")) {
                @SneakyThrows
                @Override
                public void onClick(InventoryClickEvent event) {
                    openSwiping(player);
                }
            });

            Gender gender = Gender.valueOf(result.getString("preferences"));
            inventoryManager.setItem(new CustomItem(15, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmJiOThhMjE5YmE0YWY1MTMyMWE4NWRiZjVmZjgzN2M1NjdkODBmMTA2NWE4ZGIxYTJjZWNiMTI1ZTYyMzAyNyJ9fX0=", 1, 0, "§5Präferenzen", Collections.singletonList("§8 ➥ §e" + gender.getTranslation()))) {
                @SneakyThrows
                @Override
                public void onClick(InventoryClickEvent event) {
                    Gender gender = Gender.valueOf(result.getString("preferences"));
                    if (gender == Gender.MALE) {
                        gender = Gender.FEMALE;
                    } else {
                        gender = Gender.MALE;
                    }
                    PreparedStatement updateStatement = connection.prepareStatement("UPDATE app_dating_profiles SET preferences = ? WHERE uuid = ?");
                    updateStatement.setString(1, gender.name());
                    updateStatement.setString(2, player.getUniqueId().toString());
                    updateStatement.executeUpdate();
                    updateStatement.close();
                    openDatingApp(player);
                }
            });

            // Add item to show matches
            PreparedStatement matchStatement = connection.prepareStatement("SELECT * FROM app_dating_matches WHERE uuid = ? OR target = ?");
            matchStatement.setString(1, player.getUniqueId().toString());
            matchStatement.setString(2, player.getUniqueId().toString());
            ResultSet matchResult = matchStatement.executeQuery();
            List<String> matches = new ObjectArrayList<>();
            while (matchResult.next()) {
                String matchedUUID = matchResult.getString("uuid").equals(player.getUniqueId().toString()) ? matchResult.getString("target") : matchResult.getString("uuid");
                matches.add("§8 ➥ §e" + Bukkit.getOfflinePlayer(UUID.fromString(matchedUUID)).getName() + " §8- §7" + Utils.localDateTimeToReadableString(Utils.toLocalDateTime(matchResult.getDate("matched"))));
            }

            inventoryManager.setItem(new CustomItem(22, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjFlNTVkZjRjY2FkZDU5ZTM0MTU4NWI3MWM2ZDZlZjMxYmE3NTQzYmRjNmY4MjQ0OTU2N2ZmNTRjMzZiZjg3OSJ9fQ==", 1, 0, "§5Matches", matches)) {
                @SneakyThrows
                @Override
                public void onClick(InventoryClickEvent event) {
                    player.sendMessage("§8[§6Handy§8]§7 Deine Matches:");
                    for (String match : matches) {
                        player.sendMessage(match);
                    }
                    player.closeInventory();
                }
            });

        } else {
            inventoryManager.setItem(new CustomItem(13, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWZmMzE0MzFkNjQ1ODdmZjZlZjk4YzA2NzU4MTA2ODFmOGMxM2JmOTZmNTFkOWNiMDdlZDc4NTJiMmZmZDEifX19", 1, 0, "§aAccount erstellen")) {
                @SneakyThrows
                @Override
                public void onClick(InventoryClickEvent event) {
                    player.closeInventory();
                    PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO app_dating_profiles (uuid) VALUES (?)");
                    insertStatement.setString(1, player.getUniqueId().toString());
                    insertStatement.execute();
                    insertStatement.close();
                    openDatingApp(player);
                }
            });
        }
    }

    private void openSwiping(Player player) {
        try {
            PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
            InventoryManager inventoryManager = new InventoryManager(player, 27, "§8» §5Swiper", true, true);
            Connection connection = Main.getInstance().coreDatabase.getConnection();

            PreparedStatement statement = connection.prepareStatement(
                    "SELECT adp.uuid, adp.description, p.firstname, p.lastname " +
                            "FROM app_dating_profiles adp " +
                            "JOIN players p ON adp.uuid = p.uuid " +
                            "WHERE adp.uuid != ? AND adp.uuid NOT IN (SELECT target_uuid FROM app_dating_swipes WHERE swiper_uuid = ?)"
            );
            statement.setString(1, player.getUniqueId().toString());
            statement.setString(2, player.getUniqueId().toString());
            ResultSet result = statement.executeQuery();

            if (result.next()) {
                String targetUuid = result.getString("uuid");
                String description = result.getString("description");
                String firstname = result.getString("firstname");
                String lastname = result.getString("lastname");

                inventoryManager.setItem(new CustomItem(13, ItemManager.createItemHead(targetUuid, 1, 0, "§5" + firstname + " " + lastname, "§8 ➥ §d" + description
                )) {
                    @SneakyThrows
                    @Override
                    public void onClick(InventoryClickEvent event) {
                        recordSwipe(player.getUniqueId().toString(), targetUuid, true);
                        checkForMatch(player.getUniqueId().toString(), targetUuid);
                        openSwiping(player); // Show the next profile
                    }
                });

                inventoryManager.setItem(new CustomItem(15, ItemManager.createCustomHead(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWRjZGE2ZTNjNmRjYTdlOWI4YjZiYTNmZWJmNWNkMDkxN2Y5OTdiNjRiMmFlZjE4YzNmNzczNzY1ZTNhNTc5In19fQ==", 1, 0, "§aSwipe Rechts"
                )) {
                    @SneakyThrows
                    @Override
                    public void onClick(InventoryClickEvent event) {
                        recordSwipe(player.getUniqueId().toString(), targetUuid, true);
                        checkForMatch(player.getUniqueId().toString(), targetUuid);
                        openSwiping(player); // Show the next profile
                    }
                });

                inventoryManager.setItem(new CustomItem(11, ItemManager.createCustomHead(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTI4YjhjZjQwNWVhZjYwNmEwMjEwZjAzMDNiMDEzMTc5ZjhmMTJlYWE5NTgyNDEyOWViZWVmOWU0NGI2ODIzMCJ9fX0=", 1, 0, "§cSwipe Links"
                )) {
                    @SneakyThrows
                    @Override
                    public void onClick(InventoryClickEvent event) {
                        recordSwipe(player.getUniqueId().toString(), targetUuid, false);
                        openSwiping(player); // Show the next profile
                    }
                });

            } else {
                Bukkit.getScheduler().runTask(Main.getInstance(), () -> player.sendMessage("§8[§6Handy§8]§7 Keine weiteren Profile verfügbar."));
            }
            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }


    @SneakyThrows
    private void recordSwipe(String swiperUuid, String targetUuid, boolean swipeRight) {
        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {
            Main.getInstance().getCoreDatabase().insertAsync("INSERT INTO app_dating_swipes (swiper_uuid, target_uuid, swipe_right) VALUES (?, ?, ?)",
                    swiperUuid,
                    targetUuid,
                    swipeRight);
        });
    }


    @SneakyThrows
    private void checkForMatch(String swiperUuid, String targetUuid) {
        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {
            try {
                Connection connection = Main.getInstance().coreDatabase.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "SELECT * FROM app_dating_swipes WHERE swiper_uuid = ? AND target_uuid = ? AND swipe_right = ?"
                );
                statement.setString(1, targetUuid);
                statement.setString(2, swiperUuid);
                statement.setBoolean(3, true);
                ResultSet result = statement.executeQuery();

                if (result.next()) {
                    PreparedStatement matchStatement = connection.prepareStatement(
                            "INSERT INTO app_dating_matches (uuid, target, matched) VALUES (?, ?, ?)"
                    );
                    matchStatement.setString(1, swiperUuid);
                    matchStatement.setString(2, targetUuid);
                    matchStatement.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
                    matchStatement.execute();
                    matchStatement.close();

                    OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(swiperUuid));
                    OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(UUID.fromString(targetUuid));

                    Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
                        if (player.isOnline()) {
                            Player onPlayer = Bukkit.getPlayer(player.getUniqueId());
                            if (onPlayer != null)
                                onPlayer.sendMessage("§8[§6Handy§8]§7 Du hast ein Match mit " + targetPlayer.getName() + "!");
                        }

                        if (targetPlayer.isOnline()) {
                            Player onPlayer = Bukkit.getPlayer(targetPlayer.getUniqueId());
                            if (onPlayer != null)
                                onPlayer.sendMessage("§8[§6Handy§8]§7 Du hast ein Match mit " + player.getName() + "!");
                        }
                    });
                }
                statement.close();
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }
}
