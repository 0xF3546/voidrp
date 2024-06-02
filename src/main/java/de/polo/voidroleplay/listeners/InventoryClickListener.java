package de.polo.voidroleplay.listeners;

import de.polo.voidroleplay.dataStorage.*;
import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.game.base.vehicle.Vehicles;
import de.polo.voidroleplay.utils.*;
import de.polo.voidroleplay.utils.Server;
import de.polo.voidroleplay.utils.enums.Gender;
import de.polo.voidroleplay.utils.playerUtils.*;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.sql.*;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

public class InventoryClickListener implements Listener {
    private final PlayerManager playerManager;
    private final FactionManager factionManager;
    private final Utils utils;
    private final LocationManager locationManager;
    public InventoryClickListener(PlayerManager playerManager, FactionManager factionManager, Utils utils, LocationManager locationManager) {
        this.playerManager = playerManager;
        this.factionManager = factionManager;
        this.utils = utils;
        this.locationManager = locationManager;
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) throws SQLException {
        if (event.getWhoClicked().getOpenInventory().getTopInventory().getType().equals(InventoryType.CHEST) && event.getView().getTitle().equalsIgnoreCase("§7Lager")) {
            // event.setCancelled(true);
        }
        if (event.getCurrentItem() == null || !event.getWhoClicked().getOpenInventory().getTopInventory().getType().equals(InventoryType.CHEST))
            return;
        Player player = (Player) event.getWhoClicked();
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        FactionData factionData = null;
        if (playerData.getVariable("current_inventory") == null) return;
        if (playerData.getFaction() != null) factionData = factionManager.getFactionData(playerData.getFaction());
        if (event.getView().getTitle().equalsIgnoreCase("§6§lRubbellos")) {
            event.setCancelled(true);
            playerData.setVariable("current_inventory", null);
            if (event.getCurrentItem().getType() == Material.GRAY_DYE) {
                playerData.setVariable("current_inventory", "rubbellos");
                int pers = event.getCurrentItem().getItemMeta().getPersistentDataContainer().get(new NamespacedKey(Main.plugin, "isWin"), PersistentDataType.INTEGER);
                ItemMeta meta = event.getCurrentItem().getItemMeta();
                if (pers == 1 && playerData.getIntVariable("rubbellose_wins") <= 3) {
                    meta.setDisplayName("§aGewonnen!");
                    event.getCurrentItem().setItemMeta(meta);
                    event.getCurrentItem().setType(Material.LIME_DYE);
                    playerData.setIntVariable("rubbellose_wins", playerData.getIntVariable("rubbellose_wins") + 1);
                } else {
                    meta.setDisplayName("§cVerloren!");
                    event.getCurrentItem().setItemMeta(meta);
                    event.getCurrentItem().setType(Material.RED_DYE);
                }
                playerData.setIntVariable("rubbellose_gemacht", playerData.getIntVariable("rubbellose_gemacht") + 1);
                if (playerData.getIntVariable("rubbellose_gemacht") >= 5) {
                    new Rubbellose(playerManager).endGame(player);
                    player.closeInventory();
                }
            } else if (event.getCurrentItem().getType() == Material.STRUCTURE_VOID) {
                if (playerData.getIntVariable("rubbellose_gemacht") == 0) {
                    player.closeInventory();
                    player.sendMessage("§8[§6Rubbellos§8]§c Du hast das Spiel abgebrochen!");
                }
            }
        }
        if (Objects.equals(playerData.getVariable("current_inventory"), "bossmenu_" + playerData.getFaction())) {
            event.setCancelled(true);
            switch (Objects.requireNonNull(event.getCurrentItem()).getType()) {
                case NETHER_WART:
                    Main.getInstance().commands.openBossMenuCommand.openBossMenu(player, playerData.getIntVariable("current_page") - 1);
                    break;
                case GOLD_NUGGET:
                    Main.getInstance().commands.openBossMenuCommand.openBossMenu(player, playerData.getIntVariable("current_page") + 1);
                    break;
                case PLAYER_HEAD:
                    Main.getInstance().commands.openBossMenuCommand.editPlayerViaBoss(player, event.getCurrentItem());
                    break;
            }
        }
        if (playerData.getVariable("current_inventory").toString().contains("edit_factionplayer_")) {
            event.setCancelled(true);
            switch (Objects.requireNonNull(event.getCurrentItem()).getType()) {
                case NETHER_WART:
                    Main.getInstance().commands.openBossMenuCommand.openBossMenu(player, 1);
                    break;
                case REDSTONE:
                    UUID uuid = UUID.fromString(playerData.getVariable("current_inventory").toString().replace("edit_factionplayer_", ""));
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
                    player.performCommand("uninvite " + offlinePlayer.getName());
                    break;
                case DIAMOND:
                    UUID uuid1 = UUID.fromString(playerData.getVariable("current_inventory").toString().replace("edit_factionplayer_", ""));
                    OfflinePlayer offlinePlayer1 = Bukkit.getOfflinePlayer(uuid1);
                    factionManager.sendMessageToFaction(playerData.getFaction(), "§c" + offlinePlayer1.getName() + "§7 wurde von §c" + player.getName() + " befördert.");
                    Statement statement = Main.getInstance().mySQL.getStatement();
                    ResultSet res = statement.executeQuery("SELECT `faction_grade` FROM `players` WHERE `uuid` = '" + offlinePlayer1.getUniqueId() + "'");
                    if (res.next()) {
                        if (res.getInt(1) < 8 && res.getInt(1) > 0) {
                            statement.executeUpdate("UPDATE `players` SET `faction_grade` = " + (res.getInt(1) + 1) + " WHERE `uuid` = '" + offlinePlayer1.getUniqueId() + "'");
                        } else {
                            return;
                        }
                    }
                    if (offlinePlayer1.isOnline()) {
                        PlayerData offlinePlayerData = playerManager.getPlayerData(offlinePlayer1.getUniqueId());
                        offlinePlayerData.setFactionGrade(offlinePlayerData.getFactionGrade() + 1);
                    }
                    break;
                case GLOWSTONE_DUST:
                    UUID uuid2 = UUID.fromString(playerData.getVariable("current_inventory").toString().replace("edit_factionplayer_", ""));
                    OfflinePlayer offlinePlayer2 = Bukkit.getOfflinePlayer(uuid2);
                    Statement statement1 = Main.getInstance().mySQL.getStatement();
                    ResultSet res1 = statement1.executeQuery("SELECT `faction_grade` FROM `players` WHERE `uuid` = '" + offlinePlayer2.getUniqueId() + "'");
                    if (res1.next()) {
                        if (res1.getInt(1) < 8 && res1.getInt(1) > 0) {
                            statement1.executeUpdate("UPDATE `players` SET `faction_grade` = " + (res1.getInt(1) - 1) + " WHERE `uuid` = '" + offlinePlayer2.getUniqueId() + "'");
                        } else {
                            return;
                        }
                    }
                    factionManager.sendMessageToFaction(playerData.getFaction(), "§c" + offlinePlayer2.getName() + "§7 wurde von §c" + player.getName() + " degradiert.");
                    if (offlinePlayer2.isOnline()) {
                        PlayerData offlinePlayerData = playerManager.getPlayerData(offlinePlayer2.getUniqueId());
                        offlinePlayerData.setFactionGrade(offlinePlayerData.getFactionGrade() - 1);
                    }
                    break;
            }
        }
        if (Objects.equals(playerData.getVariable("current_inventory"), "adminmenu")) {
            event.setCancelled(true);
            switch (Objects.requireNonNull(event.getCurrentItem()).getType()) {
                case NETHER_WART:
                    Main.getInstance().commands.adminMenuCommand.openAdminMenu(player, playerData.getIntVariable("current_page") - 1, playerData.getVariable("offlinePLayers") != "nein");
                    break;
                case GOLD_NUGGET:
                    Main.getInstance().commands.adminMenuCommand.openAdminMenu(player, playerData.getIntVariable("current_page") + 1, playerData.getVariable("offlinePLayers") != "nein");
                    break;
                case PLAYER_HEAD:
                    Main.getInstance().commands.adminMenuCommand.editPlayerViaAdmin(player, event.getCurrentItem());
                    break;
                case DIAMOND:
                    Main.getInstance().commands.adminMenuCommand.openAdminMenu(player, 1, playerData.getVariable("offlinePLayers") == "nein");
                    break;
            }
        }
        if (playerData.getVariable("current_inventory").toString().contains("edit_player_")) {
            event.setCancelled(true);
            UUID uuid = UUID.fromString(playerData.getVariable("current_inventory").toString().replace("edit_player_", ""));
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            switch (Objects.requireNonNull(event.getCurrentItem()).getType()) {
                case NETHER_WART:
                    Main.getInstance().commands.adminMenuCommand.openAdminMenu(player, 1, playerData.getVariable("offlinePLayers") != "nein");
                    break;
                case REDSTONE:
                    if (!offlinePlayer.isOnline()) return;
                    playerManager.kickPlayer((Player) offlinePlayer, "[System] Kick durch Administrationsoberfläche");
                    break;
                case EMERALD_BLOCK:
                    player.performCommand("tphere " + offlinePlayer.getName());
                    break;
                case DIAMOND_BLOCK:
                    player.performCommand("tp " + offlinePlayer.getName());
                    break;
            }
        }
        if (Objects.equals(playerData.getVariable("current_inventory"), "tablet")) {
            event.setCancelled(true);
            if (Objects.equals(playerData.getVariable("current_app"), "aktenlist")) {
                switch (Objects.requireNonNull(event.getCurrentItem()).getType()) {
                    case PAPER:
                        if (playerData.getVariable("current_akte") != null) {
                            ItemMeta meta = event.getCurrentItem().getItemMeta();
                            NamespacedKey akte = new NamespacedKey(Main.plugin, "akte");
                            NamespacedKey hafteinheiten = new NamespacedKey(Main.plugin, "hafteinheiten");
                            NamespacedKey geldstrafe = new NamespacedKey(Main.plugin, "geldstrafe");
                            Player targetplayer = Bukkit.getPlayer(UUID.fromString(playerData.getVariable("current_akte")));
                            String newAkte = meta.getPersistentDataContainer().get(akte, PersistentDataType.STRING);
                            int newHafteinheiten = meta.getPersistentDataContainer().get(hafteinheiten, PersistentDataType.INTEGER);
                            int newGeldstrafe = meta.getPersistentDataContainer().get(geldstrafe, PersistentDataType.INTEGER);
                            utils.staatUtil.addAkteToPlayer(player, targetplayer, newHafteinheiten, newAkte, newGeldstrafe);
                            player.sendMessage("§8[§9Zentrale§8] §7Akte wurde für " + targetplayer.getName() + " hinzugefügt.");
                            player.sendMessage("§8[§9Zentrale§8] §7Akte: " + newAkte + " §8-§7 Hafteinheiten: " + newHafteinheiten + "§8 - §7Geldstrafe: " + newGeldstrafe + "$.");
                        }
                        break;
                    case REDSTONE:
                        utils.tabletUtils.openApp(player, "aktenapp");
                        break;
                    case CLOCK:
                        playerData.setVariable("chatblock", "aktensearch");
                        player.sendMessage("§8[§9Akte§8]§7 Gib nun die Akte ein.");
                        player.closeInventory();
                        break;
                    case PLAYER_HEAD:
                        playerData.setIntVariable("input_hafteinheiten", 0);
                        playerData.setIntVariable("input_geldstrafe", 0);
                        playerData.setVariable("input_akte", null);
                        utils.tabletUtils.createAkte(player);
                        break;
                }
            } else if (Objects.equals(playerData.getVariable("current_app"), "gefängnisapp")) {
                switch (Objects.requireNonNull(event.getCurrentItem()).getType()) {
                    case REDSTONE:
                        utils.tabletUtils.openTablet(player);
                        break;
                    case NETHER_WART:
                        utils.tabletUtils.openJailApp(player, playerData.getIntVariable("current_page") - 1);
                        break;
                    case GOLD_NUGGET:
                        utils.tabletUtils.openJailApp(player, playerData.getIntVariable("current_page") + 1);
                        break;
                }
            } else if (Objects.equals(playerData.getVariable("current_app"), "vehiclesapp")) {
                switch (Objects.requireNonNull(event.getCurrentItem()).getType()) {
                    case REDSTONE:
                        utils.tabletUtils.openTablet(player);
                        break;
                    case NETHER_WART:
                        utils.tabletUtils.openVehiclesApp(player, playerData.getIntVariable("current_page") - 1);
                        break;
                    case GOLD_NUGGET:
                        utils.tabletUtils.openVehiclesApp(player, playerData.getIntVariable("current_page") + 1);
                        break;
                }
            } else if (Objects.equals(playerData.getVariable("current_app"), "createakte")) {
                switch (event.getSlot()) {
                    case 11:
                        playerData.setVariable("chatblock", "createakte_akte");
                        player.closeInventory();
                        player.sendMessage("§8[§aAkte§8]§7 Gib nun den Namen der Akte an.");
                        break;
                    case 13:
                        playerData.setVariable("chatblock", "createakte_hafteinheiten");
                        player.closeInventory();
                        player.sendMessage("§8[§aAkte§8]§7 Gib nun die Hafteinheiten an.");
                        break;
                    case 15:
                        playerData.setVariable("chatblock", "createakte_geldstrafe");
                        player.closeInventory();
                        player.sendMessage("§8[§aAkte§8]§7 Gib nun die Geldstrafe an an.");
                        break;
                    case 26:
                        utils.tabletUtils.createNewAkte(player);
                        break;
                }
            }
        }
        if (Objects.equals(playerData.getVariable("current_inventory"), "handy")) {
            event.setCancelled(true);
            if (playerData.getVariable("current_app") == null) {
                switch (event.getSlot()) {
                    case 10:
                        utils.phoneUtils.openContacts(player, 1, null);
                        break;
                    case 11:
                        utils.phoneUtils.openMessages(player, 1, null);
                        break;
                    case 12:
                        utils.phoneUtils.openCallApp(player, true);
                        break;
                    case 14:
                        utils.phoneUtils.openBanking(player);
                        break;
                    case 15:
                        utils.phoneUtils.openSettings(player);
                        break;
                    case 16:
                        utils.phoneUtils.openInternet(player);
                        break;
                }
            } else if (playerData.getVariable("current_app").equals("contacts")) {
                switch (event.getCurrentItem().getType()) {
                    case REDSTONE:
                        utils.phoneUtils.openPhone(player);
                        break;
                    case NETHER_WART:
                        utils.phoneUtils.openContacts(player, playerData.getIntVariable("current_page") - 1, null);
                        break;
                    case GOLD_NUGGET:
                        utils.phoneUtils.openContacts(player, playerData.getIntVariable("current_page") + 1, null);
                        break;
                    case PLAYER_HEAD:
                        if (!(event.getSlot() == 22))
                            utils.phoneUtils.editContact(player, event.getCurrentItem(), false, false);
                        break;
                    case CLOCK:
                        playerData.setVariable("chatblock", "contactsearch");
                        player.closeInventory();
                        player.sendMessage("§8[§6Kontakte§8]§7 Gib nun den Namen des Kontaktes ein.");
                        break;
                }
                switch (event.getSlot()) {
                    case 22:
                        playerData.setIntVariable("current_contact_number", 0);
                        playerData.setVariable("current_contact_name", "&6Name");
                        utils.phoneUtils.editContact(player, null, true, true);
                        break;
                }
            } else if (playerData.getVariable("current_app").equals("edit_contact")) {
                switch (event.getCurrentItem().getType()) {
                    case REDSTONE:
                        utils.phoneUtils.openContacts(player, 1, null);
                        break;
                    case BOOK:
                        playerData.setVariable("chatblock", "changenumber");
                        player.sendMessage("§8[§6Kontakte§8]§7 Gib nun die Nummer ein.");
                        player.closeInventory();
                        break;
                    case PAPER:
                        playerData.setVariable("chatblock", "changename");
                        player.sendMessage("§8[§6Kontakte§8]§7 Gib nun den Namen ein ein.");
                        player.sendMessage("§8 ➥ §bInfo§8:§f Nutze \"&\" Zeichen um Farben zu verwenden.");
                        player.closeInventory();
                        break;
                    case EMERALD:
                        if (!playerData.getVariable("current_contact_name").equals("&6Name") && playerData.getIntVariable("current_contact_number") != 0) {
                            if (playerData.getIntVariable("current_contact_id") == 0) {
                                Statement statement = Main.getInstance().mySQL.getStatement();
                                String uuid = null;
                                ResultSet res = statement.executeQuery("SELECT `uuid` FROM `players` WHERE `id` = " + playerData.getIntVariable("current_contact_number"));
                                if (res.next()) {
                                    uuid = res.getString(1);
                                    statement.execute("INSERT INTO `phone_contacts` (`uuid`, `contact_name`, `contact_number`, `contact_uuid`) VALUES ('" + player.getUniqueId() + "', '" + playerData.getVariable("current_contact_name") + "', " + playerData.getIntVariable("current_contact_number") + ", '" + uuid + "')");
                                    player.sendMessage("§8[§6Kontakte§8]§a Nummer " + playerData.getIntVariable("current_contact_number") + "§7 unter " + playerData.getVariable("current_contact_name").toString().replace("&", "§") + "§7 eingespeichert.");
                                    utils.phoneUtils.openContacts(player, 1, null);
                                } else {
                                    player.sendMessage("§8[§6Kontakte§8]§c Nummer konnte nicht gefunden werden.");
                                }
                            } else {
                                Statement statement = Main.getInstance().mySQL.getStatement();
                                statement.executeUpdate("UPDATE `phone_contacts` SET `contact_name` = '" + playerData.getVariable("current_contact_name") + "', `contact_number` = " + playerData.getIntVariable("current_contact_number") + " WHERE `id` = " + playerData.getIntVariable("current_contact_id"));
                                player.sendMessage("§8[§6Kontakte§8]§7 Kontakt " + playerData.getVariable("current_contact_name").toString().replace("&", "§") + "§7 angepasst.");
                                utils.phoneUtils.openContacts(player, 1, null);
                            }
                        } else {
                            player.sendMessage("§8[§6Kontakte§8]§7 Gib bitte Namen & Nummer an.");
                        }
                        break;
                    case RED_DYE:
                        Statement statement = Main.getInstance().mySQL.getStatement();
                        statement.execute("DELETE FROM `phone_contacts` WHERE `id` = " + playerData.getIntVariable("current_contact_id"));
                        player.sendMessage("§8[§6Kontakte§8]§c Kontakt gelöscht.");
                        utils.phoneUtils.openContacts(player, 1, null);
                        break;
                }
                switch (event.getSlot()) {
                    case 15:
                        player.performCommand("call " + playerData.getIntVariable("current_contact_number"));
                        player.closeInventory();
                        break;
                    case 16:
                        player.closeInventory();
                        playerData.setVariable("chatblock", "sendsms");
                        player.sendMessage("§8[§6SMS§8]§7 Gib nun die SMS ein.");
                        break;
                }
            } else if (playerData.getVariable("current_app").equals("phonecall")) {
                switch (event.getSlot()) {
                    case 12:
                        playerData.setVariable("current_phone_callnumber", playerData.getVariable("current_phone_callnumber").toString() + 1);
                        utils.phoneUtils.openCallApp(player, false);
                        break;
                    case 13:
                        playerData.setVariable("current_phone_callnumber", playerData.getVariable("current_phone_callnumber").toString() + 2);
                        utils.phoneUtils.openCallApp(player, false);
                        break;
                    case 14:
                        playerData.setVariable("current_phone_callnumber", playerData.getVariable("current_phone_callnumber").toString() + 3);
                        utils.phoneUtils.openCallApp(player, false);
                        break;
                    case 21:
                        playerData.setVariable("current_phone_callnumber", playerData.getVariable("current_phone_callnumber").toString() + 4);
                        utils.phoneUtils.openCallApp(player, false);
                        break;
                    case 22:
                        playerData.setVariable("current_phone_callnumber", playerData.getVariable("current_phone_callnumber").toString() + 5);
                        utils.phoneUtils.openCallApp(player, false);
                        break;
                    case 23:
                        playerData.setVariable("current_phone_callnumber", playerData.getVariable("current_phone_callnumber").toString() + 6);
                        utils.phoneUtils.openCallApp(player, false);
                        break;
                    case 30:
                        playerData.setVariable("current_phone_callnumber", playerData.getVariable("current_phone_callnumber").toString() + 7);
                        utils.phoneUtils.openCallApp(player, false);
                        break;
                    case 31:
                        playerData.setVariable("current_phone_callnumber", playerData.getVariable("current_phone_callnumber").toString() + 8);
                        utils.phoneUtils.openCallApp(player, false);
                        break;
                    case 32:
                        playerData.setVariable("current_phone_callnumber", playerData.getVariable("current_phone_callnumber").toString() + 9);
                        utils.phoneUtils.openCallApp(player, false);
                        break;
                    case 40:
                        playerData.setVariable("current_phone_callnumber", playerData.getVariable("current_phone_callnumber").toString() + 0);
                        utils.phoneUtils.openCallApp(player, false);
                        break;
                    case 53:
                        player.closeInventory();
                        player.performCommand("call " + Integer.parseInt(playerData.getVariable("current_phone_callnumber")));
                        break;
                    case 45:
                        utils.phoneUtils.openPhone(player);
                        break;
                }
            } else if (playerData.getVariable("current_app").equals("internet")) {
                Statement statement = Main.getInstance().mySQL.getStatement();
                switch (event.getSlot()) {
                    case 11:
                        if (playerData.hasAnwalt()) {
                            playerData.setHasAnwalt(false);
                            statement.execute("UPDATE players SET hasAnwalt = " + playerData.hasAnwalt() + " WHERE uuid = '" + player.getUniqueId() + "'");
                            player.closeInventory();
                            player.sendMessage("§8[§6Anwalt§8]§7 Du hast deinen Anwalt §cabbestellt§7.");
                        } else {
                            playerData.setHasAnwalt(true);
                            statement.execute("UPDATE players SET hasAnwalt = " + playerData.hasAnwalt() + " WHERE uuid = '" + player.getUniqueId() + "'");
                            player.closeInventory();
                            player.sendMessage("§8[§6Anwalt§8]§7 Du hast deinen Anwalt §aeingestellt§7.");
                        }
                        break;
                }
            }
        }
        if (Objects.equals(playerData.getVariable("current_inventory"), "carlock")) {
            event.setCancelled(true);
            if (event.getCurrentItem().getType() == Material.MINECART) {
                int id = event.getCurrentItem().getItemMeta().getPersistentDataContainer().get(new NamespacedKey(Main.plugin, "id"), PersistentDataType.INTEGER);
                Vehicles.toggleVehicleState(id, player);
                player.closeInventory();
                playerData.setVariable("current_inventory", null);
            }
        }
        if (Objects.equals(playerData.getVariable("current_inventory"), "ffa_menu")) {
            event.setCancelled(true);
            switch (Objects.requireNonNull(event.getCurrentItem()).getType()) {
                case NETHER_WART:
                    utils.ffaUtils.openFFAMenu(player, playerData.getIntVariable("current_page") - 1);
                    SoundManager.clickSound(player);
                    break;
                case GOLD_NUGGET:
                    utils.ffaUtils.openFFAMenu(player, playerData.getIntVariable("current_page") + 1);
                    SoundManager.clickSound(player);
                    break;
                case LIME_DYE:
                    ItemMeta meta = event.getCurrentItem().getItemMeta();
                    int id = meta.getPersistentDataContainer().get(new NamespacedKey(Main.plugin, "id"), PersistentDataType.INTEGER);
                    if (FFAUtils.FFAlobbyDataMap.get(id).getPassword() != null && !FFAUtils.FFAlobbyDataMap.get(id).getName().equals(player.getUniqueId().toString())) {
                        playerData.setVariable("chatblock", "ffa_joinpassword");
                        playerData.setIntVariable("ffa_passwordlobby", id);
                        player.sendMessage("§8[§6FFA§8]§e Gib das Passwort für die Lobby ein.");
                    } else {
                        utils.ffaUtils.joinLobby(player, id);
                    }
                    player.closeInventory();
                    break;
                case EMERALD:
                    if (playerData.getPermlevel() >= 20) {
                        System.out.println("lets go");
                        Inventory inv = Bukkit.createInventory(player, 27, "§8 » §aLobby erstellen");
                        inv.setItem(11, ItemManager.createItem(Material.PLAYER_HEAD, 1, 0, "§eMaximale Spieler", "Lädt..."));
                        ItemMeta imeta = inv.getItem(11).getItemMeta();
                        imeta.setLore(Arrays.asList("§8 ➥ §7[§6Linksklick§8]§e +1 Slot", "§8 ➥ §7[§6Rechtsklick§8]§e -1 Slot"));
                        inv.getItem(11).setItemMeta(imeta);
                        inv.setItem(13, ItemManager.createItem(Material.PAPER, 1, 0, "§aLobby", "Lädt..."));
                        ItemMeta itemMeta = inv.getItem(13).getItemMeta();
                        playerData.setIntVariable("ffa_maxplayer", 10);
                        itemMeta.setLore(Arrays.asList("§8 ➥ §eMaximale Spieler§8:§7 " + playerData.getIntVariable("ffa_maxplayer"), "§8 ➥ §ePasswort§8:§c Nicht vorhanden"));
                        inv.getItem(13).setItemMeta(itemMeta);
                        inv.setItem(15, ItemManager.createItem(Material.CHEST, 1, 0, "§ePasswort setzen"));
                        inv.setItem(18, ItemManager.createItem(Material.NETHER_WART, 1, 0, "§cZurück"));
                        inv.setItem(26, ItemManager.createItem(Material.EMERALD, 1, 0, "§aLobby erstellen"));
                        for (int i = 0; i < 27; i++) {
                            if (inv.getItem(i) == null) {
                                inv.setItem(i, ItemManager.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, 0, "§8"));
                            }
                        }
                        player.openInventory(inv);
                        Main.getInstance().getCooldownManager().setCooldown(player, "ffa_creator", 2);
                        playerData.setVariable("current_inventory", "ffa_createlobby");
                    } else {
                        player.sendMessage(Main.error + "Du brauchst mindestens §ePremium§7 um eine Lobby zu erstellen!");
                        player.closeInventory();
                    }
                    break;
            }
        }
        if (Objects.equals(playerData.getVariable("current_inventory"), "ffa_createlobby")) {
            event.setCancelled(true);
            System.out.println("lobby createn");
            switch (Objects.requireNonNull(event.getCurrentItem()).getType()) {
                case PLAYER_HEAD:
                    if (event.isLeftClick()) {
                        playerData.setIntVariable("ffa_maxplayer", playerData.getIntVariable("ffa_maxplayer") + 1);
                    } else {
                        if (playerData.getIntVariable("ffa_maxplayer") >= 3) {
                            playerData.setIntVariable("ffa_maxplayer", playerData.getIntVariable("ffa_maxplayer") - 1);
                        }
                    }
                    ItemMeta itemMeta = event.getInventory().getItem(13).getItemMeta();
                    if (playerData.getVariable("ffa_password") == null) {
                        itemMeta.setLore(Arrays.asList("§8 ➥ §eMaximale Spieler§8:§7 " + playerData.getIntVariable("ffa_maxplayer"), "§8 ➥ §ePasswort§8:§c Nicht vorhanden"));
                    } else {
                        itemMeta.setLore(Arrays.asList("§8 ➥ §eMaximale Spieler§8:§7 " + playerData.getIntVariable("ffa_maxplayer"), "§8 ➥ §ePasswort§8:§a " + playerData.getVariable("ffa_password")));
                    }
                    event.getInventory().getItem(13).setItemMeta(itemMeta);
                    SoundManager.clickSound(player);
                    break;
                case CHEST:
                    playerData.setVariable("chatblock", "ffa");
                    player.closeInventory();
                    player.sendMessage("§8[§6FFA§8]§7 Gib das Passwort bitte in den Chat ein.");
                    SoundManager.clickSound(player);
                    break;
                case EMERALD:
                    if (!Main.getInstance().getCooldownManager().isOnCooldown(player, "ffa_creator"))
                        FFAUtils.createLobby(player, playerData.getIntVariable("ffa_maxplayer"), playerData.getVariable("ffa_password"));
                    break;
                case NETHER_WART:
                    utils.ffaUtils.openFFAMenu(player, 1);
                    SoundManager.clickSound(player);
                    break;
            }
        }
        if (Objects.equals(playerData.getVariable("current_inventory"), "interaktionsmenü")) {
            event.setCancelled(true);
            switch (event.getSlot()) {
                case 20:
                    playerData.setVariable("chatblock", "givemoney");
                    player.sendMessage("§8[§6Interaktion§8]§7 Gib nun einen Wert ein.");
                    player.closeInventory();
                    break;
                case 24:
                    Player targetplayer = Bukkit.getPlayer(UUID.fromString(playerData.getVariable("current_player")));
                    if (targetplayer == null) {
                        return;
                    }
                    player.performCommand("personalausweis show " + targetplayer.getName());
                    if (playerData.getGender().equals(Gender.MALE)) {
                        ChatUtils.sendMeMessageAtPlayer(player, "§o" + player.getName() + " zeigt " + targetplayer.getName() + " seinen Personalausweis.");
                    } else {
                        ChatUtils.sendMeMessageAtPlayer(player, "§o" + player.getName() + " zeigt " + targetplayer.getName() + " ihren Personalausweis.");
                    }
                    player.closeInventory();
                    break;
                case 38:
                    player.closeInventory();
                    Player targetplayer3 = Bukkit.getPlayer(UUID.fromString(playerData.getVariable("current_player")));
                    if (targetplayer3.getLocation().distance(player.getLocation()) < 3) {
                        if (!playerManager.canPlayerMove(targetplayer3)) {
                            ChatUtils.sendGrayMessageAtPlayer(player, player.getName() + " versucht " + targetplayer3.getName() + " zu durchsuchen.");
                            Progress.start(player, 5);
                            Main.waitSeconds(5, () -> {
                                if (targetplayer3.getLocation().distance(player.getLocation()) < 3) {
                                    player.openInventory(targetplayer3.getInventory());
                                    ChatUtils.sendGrayMessageAtPlayer(player, player.getName() + " durchsucht " + targetplayer3.getName());
                                } else {
                                    player.sendMessage("§8[§6Interaktion§8]§7 " + targetplayer3.getName() + " ist nicht in deiner nähe.");
                                }
                            });
                        } else {
                            player.openInventory(targetplayer3.getInventory());
                            ChatUtils.sendGrayMessageAtPlayer(player, player.getName() + " durchsucht " + targetplayer3.getName());
                        }
                    } else {
                        player.sendMessage("§8[§6Interaktion§8]§7 " + targetplayer3.getName() + " ist nicht in deiner nähe.");
                    }
                    break;
                case 40:
                    Player targetplayer2 = Bukkit.getPlayer(UUID.fromString(playerData.getVariable("current_player")));
                    if (targetplayer2 == null) {
                        return;
                    }
                    Server.Utils.kissPlayer(player, targetplayer2);
                    player.closeInventory();
                    break;
                case 53:
                    Main.getInstance().getCooldownManager().setCooldown(player, "interaction_cooldown", 1);
                    playerManager.openFactionInteractionMenu(player, playerData.getFaction());
                    break;
            }
        }
        if (playerData.getVariable("current_inventory").toString().startsWith("interaktionsmenü_")) {
            event.setCancelled(true);
            Player targetplayer = Bukkit.getPlayer(UUID.fromString(playerData.getVariable("current_player")));
            switch (event.getSlot()) {
                case 20:
                    utils.staatUtil.checkBloodGroup(player, targetplayer);
                    player.closeInventory();
                    break;
                case 53:
                    if (!Main.getInstance().getCooldownManager().isOnCooldown(player, "interaction_cooldown"))
                        playerManager.openInterActionMenu(player, targetplayer);
                    break;
            }
        }
    }
}
