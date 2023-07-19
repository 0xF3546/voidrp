package de.polo.metropiacity.Listener;

import de.polo.metropiacity.DataStorage.*;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.MySQl.MySQL;
import de.polo.metropiacity.PlayerUtils.*;
import de.polo.metropiacity.Utils.*;
import de.polo.metropiacity.Utils.Server;
import de.polo.metropiacity.commands.*;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;

public class InventoryClickListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent event) throws SQLException {
        if (event.getCurrentItem() == null || !event.getWhoClicked().getOpenInventory().getTopInventory().getType().equals(InventoryType.CHEST)) return;
        Player player = (Player) event.getWhoClicked();
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        FactionData factionData = FactionManager.factionDataMap.get(playerData.getFaction());
        if (event.getView().getTitle().equalsIgnoreCase("§6§lRubbellos")) {
            event.setCancelled(true);
            playerData.setVariable("current_inventory", null);
            if (event.getCurrentItem().getType() == Material.GRAY_DYE) {
                playerData.setVariable("current_inventory", "rubbellos");
                int pers = event.getCurrentItem().getItemMeta().getPersistentDataContainer().get(new NamespacedKey(Main.plugin, "isWin"), PersistentDataType.INTEGER);
                ItemMeta meta = event.getCurrentItem().getItemMeta();
                System.out.println(playerData.getIntVariable("rubbellose_wins"));
                System.out.println(playerData.getIntVariable("rubbellose_gemacht"));
                if (pers == 1 && playerData.getIntVariable("rubbellose_wins") <= 3) {
                    meta.setDisplayName("§aGewonnen!");
                    event.getCurrentItem().setItemMeta(meta);
                    event.getCurrentItem().setType(Material.LIME_DYE);
                    playerData.setIntVariable("rubbellose_wins", playerData.getIntVariable("rubbellose_wins" ) + 1);
                } else {
                    meta.setDisplayName("§cVerloren!");
                    event.getCurrentItem().setItemMeta(meta);
                    event.getCurrentItem().setType(Material.RED_DYE);
                }
                playerData.setIntVariable("rubbellose_gemacht", playerData.getIntVariable("rubbellose_gemacht") + 1);
                if (playerData.getIntVariable("rubbellose_gemacht") >= 5) {
                    rubbellose.endGame(player);
                    player.closeInventory();
                }
            } else if (event.getCurrentItem().getType() == Material.STRUCTURE_VOID) {
                if (playerData.getIntVariable("rubbellose_gemacht") == 0) {
                    player.closeInventory();
                    player.sendMessage("§8[§6Rubbellos§8]§c Du hast das Spiel abgebrochen!");
                }
            }
        }
        for (int i = 0; i < LocationManager.shops.length; i++) {
            int f = i + 1;
            if (event.getView().getTitle().equalsIgnoreCase("§8» §c" + LocationManager.getShopNameById(f))) {
                event.setCancelled(true);
                playerData.setVariable("current_inventory", null);
                for (Object[] row : Shop.shop_items) {
                    int shop = LocationManager.isNearShop(player);
                    if ((int) row[1] == shop) {
                        if (event.getCurrentItem().getType() == Material.valueOf((String) row[2]) && event.getCurrentItem().getType() != null) {
                            if (PlayerManager.money(player) >= (int) row[4]) {
                                try {
                                    if (Objects.equals(row[5].toString(), "weapon")) {
                                        String weapon = row[3].toString().replace("&", "").replace("6", "");
                                        Weapons.giveWeaponToPlayer(player, event.getCurrentItem().getType(), "default");
                                        player.sendMessage("§8[§6" + LocationManager.getShopNameById(f) + "§8] §7" + "Danke für deinen Einkauf in höhe von §a" + (int) row[4] + "$.");
                                        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1, 0);
                                        PlayerManager.removeMoney(player, (int) row[4], "Kauf der Waffe: " + weapon);
                                    } else if (Objects.equals(row[5].toString(), "ammo")) {
                                        String ammo = row[3].toString().replace("&", "").replace("6", "");
                                        Weapons.giveWeaponAmmoToPlayer(player, ammo, 1);
                                        player.sendMessage("§8[§6" + LocationManager.getShopNameById(f) + "§8] §7" + "Danke für deinen Einkauf in höhe von §a" + (int) row[4] + "$.");
                                        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1, 0);
                                        PlayerManager.removeMoney(player, (int) row[4], "Kauf von Munition: " + ammo);
                                    } else if (Objects.equals(row[5].toString(), "car")) {
                                        Vehicles.giveVehicle(player, row[6].toString());
                                    } else {
                                        PlayerManager.removeMoney(player, (int) row[4], "Kauf von: " + event.getCurrentItem().getType());
                                        player.getInventory().addItem(ItemManager.createItem(Material.valueOf((String) row[2]), 1, 0, event.getCurrentItem().getItemMeta().getDisplayName(), null));
                                        player.sendMessage("§8[§6" + LocationManager.getShopNameById(f) + "§8] §7" + "Danke für deinen Einkauf in höhe von §a" + (int) row[4] + "$.");
                                        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1, 0);
                                    }
                                } catch (SQLException e) {
                                    player.sendMessage(Main.error + "Fehler. Bitte kontaktiere die Entwicklung.");
                                    throw new RuntimeException(e);
                                }
                            } else {
                                player.sendMessage("§6" + LocationManager.getShopNameById(f) + "§8 » §7" + "Du hast leider nicht genug Bargeld.");
                            }
                        }
                    }
                    i++;
                }
            }
        }
        if (Objects.equals(playerData.getVariable("current_inventory"), "bossmenu_" + playerData.getFaction())) {
            event.setCancelled(true);
            switch (Objects.requireNonNull(event.getCurrentItem()).getType()) {
                case NETHER_WART:
                    OpenBossMenuCommand.openBossMenu(player, playerData.getIntVariable("current_page") - 1);
                    break;
                case GOLD_NUGGET:
                    OpenBossMenuCommand.openBossMenu(player, playerData.getIntVariable("current_page") + 1);
                    break;
                case PLAYER_HEAD:
                    OpenBossMenuCommand.editPlayerViaBoss(player, event.getCurrentItem());
                    break;
            }
        }
        if (playerData.getVariable("current_inventory").contains("edit_factionplayer_")) {
            event.setCancelled(true);
            switch (Objects.requireNonNull(event.getCurrentItem()).getType()) {
                case NETHER_WART:
                    OpenBossMenuCommand.openBossMenu(player, 1);
                    break;
                case REDSTONE:
                    UUID uuid = UUID.fromString(playerData.getVariable("current_inventory").replace("edit_factionplayer_", ""));
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
                    player.performCommand("uninvite " + offlinePlayer.getName());
                    break;
                case DIAMOND:
                    UUID uuid1 = UUID.fromString(playerData.getVariable("current_inventory").replace("edit_factionplayer_", ""));
                    OfflinePlayer offlinePlayer1 = Bukkit.getOfflinePlayer(uuid1);
                    FactionManager.sendMessageToFaction(playerData.getFaction(), "§c" + offlinePlayer1.getName() + "§7 wurde von §c" + player.getName() + " befördert.");
                    Statement statement = MySQL.getStatement();
                    ResultSet res = statement.executeQuery("SELECT `faction_grade` FROM `players` WHERE `uuid` = '" + offlinePlayer1.getUniqueId().toString() + "'");
                    if (res.next()) {
                        if (res.getInt(1) < 8 && res.getInt(1) > 0) {
                            statement.executeUpdate("UPDATE `players` SET `faction_grade` = " + (res.getInt(1) + 1) + " WHERE `uuid` = '" + offlinePlayer1.getUniqueId().toString() + "'");
                        } else {
                            return;
                        }
                    }
                    if (offlinePlayer1.isOnline()) {
                        PlayerData offlinePlayerData = PlayerManager.playerDataMap.get(offlinePlayer1.getUniqueId().toString());
                        offlinePlayerData.setFactionGrade(offlinePlayerData.getFactionGrade() + 1);
                    }
                    break;
                case GLOWSTONE_DUST:
                    UUID uuid2 = UUID.fromString(playerData.getVariable("current_inventory").replace("edit_factionplayer_", ""));
                    OfflinePlayer offlinePlayer2 = Bukkit.getOfflinePlayer(uuid2);
                    Statement statement1 = MySQL.getStatement();
                    ResultSet res1 = statement1.executeQuery("SELECT `faction_grade` FROM `players` WHERE `uuid` = '" + offlinePlayer2.getUniqueId().toString() + "'");
                    if (res1.next()) {
                        if (res1.getInt(1) < 8 && res1.getInt(1) > 0) {
                            statement1.executeUpdate("UPDATE `players` SET `faction_grade` = " + (res1.getInt(1) - 1) + " WHERE `uuid` = '" + offlinePlayer2.getUniqueId().toString() + "'");
                        } else {
                            return;
                        }
                    }
                    FactionManager.sendMessageToFaction(playerData.getFaction(), "§c" + offlinePlayer2.getName() + "§7 wurde von §c" + player.getName() + " degradiert.");
                    if (offlinePlayer2.isOnline()) {
                        PlayerData offlinePlayerData = PlayerManager.playerDataMap.get(offlinePlayer2.getUniqueId().toString());
                        offlinePlayerData.setFactionGrade(offlinePlayerData.getFactionGrade() - 1);
                    }
                    break;
            }
        }
        if (Objects.equals(playerData.getVariable("current_inventory"), "adminmenu")) {
            event.setCancelled(true);
            switch (Objects.requireNonNull(event.getCurrentItem()).getType()) {
                case NETHER_WART:
                    if (playerData.getVariable("offlinePLayers") == "nein")
                        AdminmenuCommand.openAdminMenu(player, playerData.getIntVariable("current_page") - 1, false);
                    else
                        AdminmenuCommand.openAdminMenu(player, playerData.getIntVariable("current_page") - 1, true);
                    break;
                case GOLD_NUGGET:
                    if (playerData.getVariable("offlinePLayers") == "nein")
                        AdminmenuCommand.openAdminMenu(player, playerData.getIntVariable("current_page") + 1, false);
                    else
                        AdminmenuCommand.openAdminMenu(player, playerData.getIntVariable("current_page") + 1, true);
                    break;
                case PLAYER_HEAD:
                    AdminmenuCommand.editPlayerViaAdmin(player, event.getCurrentItem());
                    break;
                case DIAMOND:
                    if (playerData.getVariable("offlinePLayers") == "nein") {
                        AdminmenuCommand.openAdminMenu(player, 1, true);
                    } else {
                        AdminmenuCommand.openAdminMenu(player, 1, false);
                    }
                    break;
            }
        }
        if (playerData.getVariable("current_inventory").contains("edit_player_")) {
            event.setCancelled(true);
            UUID uuid = UUID.fromString(playerData.getVariable("current_inventory").replace("edit_player_", ""));
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            switch (Objects.requireNonNull(event.getCurrentItem()).getType()) {
                case NETHER_WART:
                    if (playerData.getVariable("offlinePLayers") == "nein")
                        AdminmenuCommand.openAdminMenu(player, 1, false);
                    else AdminmenuCommand.openAdminMenu(player, 1, true);
                    break;
                case REDSTONE:
                    if (!offlinePlayer.isOnline()) return;
                    PlayerManager.kickPlayer((Player) offlinePlayer, "[System] Kick durch Administrationsoberfläche");
                    break;
                case EMERALD_BLOCK:
                    player.performCommand("tphere " + offlinePlayer.getName());
                    break;
                case DIAMOND_BLOCK:
                    player.performCommand("tp " + offlinePlayer.getName());
                    break;
            }
        }
        if (Objects.equals(playerData.getVariable("current_inventory"), "faction_invite")) {
            event.setCancelled(true);
            switch (Objects.requireNonNull(event.getCurrentItem()).getType()) {
                case EMERALD:
                    player.performCommand("annehmen");
                    player.closeInventory();
                    break;
                case REDSTONE:
                    player.performCommand("ablehnen");
                    player.closeInventory();
                    break;
            }
        }
        if (Objects.equals(playerData.getVariable("current_inventory"), "tablet")) {
            event.setCancelled(true);
            if (playerData.getVariable("current_app") == null) {
                switch (Objects.requireNonNull(event.getCurrentItem()).getType()) {
                    case PLAYER_HEAD:
                        TabletUtils.openApp(player, "fraktionsapp");
                        break;
                    case BLUE_DYE:
                        TabletUtils.openApp(player, "aktenapp");
                        break;
                    case ORANGE_DYE:
                        TabletUtils.openApp(player, "gefängnisapp");
                        break;
                    case MINECART:
                        TabletUtils.openApp(player, "vehiclesapp");
                        break;
                }
            } else if (Objects.equals(playerData.getVariable("current_app"), "aktenapp")) {
                switch (Objects.requireNonNull(event.getCurrentItem()).getType()) {
                    case DIAMOND:
                        TabletUtils.openPlayerAktenList(player, 1);
                        break;
                    case PAPER:
                        playerData.setVariable("current_akte", null);
                        TabletUtils.openAktenList(player, 1, null);
                        break;
                    case REDSTONE:
                        TabletUtils.openTablet(player);
                        break;
                }
            } else if (Objects.equals(playerData.getVariable("current_app"), "playeraktenlist")) {
                switch (Objects.requireNonNull(event.getCurrentItem()).getType()) {
                    case NETHER_WART:
                        TabletUtils.openPlayerAktenList(player, playerData.getIntVariable("current_page") - 1);
                        break;
                    case GOLD_NUGGET:
                        TabletUtils.openPlayerAktenList(player, playerData.getIntVariable("current_page") + 1);
                        break;
                    case PLAYER_HEAD:
                        TabletUtils.editPlayerAkte(player, event.getCurrentItem());
                        break;
                    case REDSTONE:
                        TabletUtils.openApp(player, "aktenapp");
                        break;
                }
            } else if (Objects.equals(playerData.getVariable("current_app"), "edit_akte")) {
                switch (Objects.requireNonNull(event.getCurrentItem()).getType()) {
                    case BOOK:
                        TabletUtils.openPlayerAkte(player, 1);
                        break;
                    case GREEN_DYE:
                        TabletUtils.openAktenList(player, 1, null);
                        break;
                    case REDSTONE:
                        TabletUtils.openPlayerAktenList(player, playerData.getIntVariable("current_page"));
                        break;
                    case BARRIER:
                        if (playerData.getFactionGrade() >= 5) {
                            Player targetlpayer = Bukkit.getPlayer(UUID.fromString(playerData.getVariable("current_akte")));
                            StaatUtil.unarrestPlayer(targetlpayer);
                            for (Player players : Bukkit.getOnlinePlayers()) {
                                PlayerData playerData1 = PlayerManager.playerDataMap.get(players.getUniqueId().toString());
                                if (Objects.equals(playerData1.getFaction(), "FBI") || Objects.equals(playerData1.getFaction(), "Polizei")) {
                                    players.sendMessage("§8[§cGefängnis§8] §6" + FactionManager.getTitle(player) + " " + player.getName() + "§7 hat §6" + targetlpayer.getName() + "§7 entlassen.");
                                }
                            }
                            player.closeInventory();
                        } else {
                            player.sendMessage(Main.error_nopermission);
                        }
                        break;
                }
            } else if (Objects.equals(playerData.getVariable("current_app"), "aktenlist")) {
                switch (Objects.requireNonNull(event.getCurrentItem()).getType()) {
                    case NETHER_WART:
                        TabletUtils.openAktenList(player, playerData.getIntVariable("current_page") - 1, null);
                        break;
                    case GOLD_NUGGET:
                        TabletUtils.openAktenList(player, playerData.getIntVariable("current_page") + 1, null);
                        break;
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
                            StaatUtil.addAkteToPlayer(player, targetplayer, newHafteinheiten, newAkte, newGeldstrafe);
                            player.sendMessage("§8[§9Zentrale§8] §7Akte wurde für " + targetplayer.getName() + " hinzugefügt.");
                            player.sendMessage("§8[§9Zentrale§8] §7Akte: " + newAkte + " §8-§7 Hafteinheiten: " + newHafteinheiten + "§8 - §7Geldstrafe: " + newGeldstrafe + "$.");
                        }
                        break;
                    case REDSTONE:
                        TabletUtils.openApp(player, "aktenapp");
                        break;
                    case CLOCK:
                        playerData.setVariable("chatblock", "aktensearch");
                        player.sendMessage("§8[§9Akte§8]§7 Gib nun die Akte ein.");
                        player.closeInventory();
                        break;
                }
            } else if (Objects.equals(playerData.getVariable("current_app"), "player_aktenlist")) {
                switch (Objects.requireNonNull(event.getCurrentItem()).getType()) {
                    case NETHER_WART:
                        TabletUtils.openPlayerAktenList(player, playerData.getIntVariable("current_page") - 1);
                        break;
                    case GOLD_NUGGET:
                        TabletUtils.openPlayerAktenList(player, playerData.getIntVariable("current_page") + 1);
                        break;
                    case WRITTEN_BOOK:
                        if (playerData.getVariable("current_akte") != null) {
                            ItemMeta meta = event.getCurrentItem().getItemMeta();
                            NamespacedKey id = new NamespacedKey(Main.plugin, "id");
                            Player targetplayer = Bukkit.getPlayer(UUID.fromString(playerData.getVariable("current_akte")));
                            assert meta != null;
                            int newId = meta.getPersistentDataContainer().get(id, PersistentDataType.INTEGER);
                            StaatUtil.removeAkteFromPlayer(player, newId);
                            event.getCurrentItem().setType(Material.BLACK_STAINED_GLASS_PANE);
                            player.sendMessage("§8[§9Zentrale§8] §7Akte von " + targetplayer.getName() + " entfernt.");
                        }
                        break;
                    case REDSTONE:
                        TabletUtils.openAktenList(player, 1, null);
                        break;
                }
            } else if (Objects.equals(playerData.getVariable("current_app"), "gefängnisapp")) {
                switch (Objects.requireNonNull(event.getCurrentItem()).getType()) {
                    case PLAYER_HEAD:
                        TabletUtils.editPlayerAkte(player, event.getCurrentItem());
                        break;
                    case REDSTONE:
                        TabletUtils.openTablet(player);
                        break;
                    case NETHER_WART:
                        TabletUtils.openJailApp(player, playerData.getIntVariable("current_page") - 1);
                        break;
                    case GOLD_NUGGET:
                        TabletUtils.openJailApp(player, playerData.getIntVariable("current_page") + 1);
                        break;
                }
            } else if (Objects.equals(playerData.getVariable("current_app"), "vehiclesapp")) {
            switch (Objects.requireNonNull(event.getCurrentItem()).getType()) {
                case REDSTONE:
                    TabletUtils.openTablet(player);
                    break;
                case NETHER_WART:
                    TabletUtils.openVehiclesApp(player, playerData.getIntVariable("current_page") - 1);
                    break;
                case GOLD_NUGGET:
                    TabletUtils.openVehiclesApp(player, playerData.getIntVariable("current_page") + 1);
                    break;
            }
        }

        }
        if (Objects.equals(playerData.getVariable("current_inventory"), "handy")) {
            event.setCancelled(true);
            if (playerData.getVariable("current_app") == null) {
                switch (event.getSlot()) {
                    case 10:
                        PhoneUtils.openContacts(player, 1, null);
                        break;
                    case 11:
                        PhoneUtils.openMessages(player, 1, null);
                        break;
                    case 12:
                        PhoneUtils.openCallApp(player, true);
                        break;
                    case 14:
                        PhoneUtils.openBanking(player);
                        break;
                    case 15:
                        PhoneUtils.openSettings(player);
                        break;
                }
            } else if (playerData.getVariable("current_app").equals("contacts")) {
                switch (event.getCurrentItem().getType()) {
                    case REDSTONE:
                        PhoneUtils.openPhone(player);
                        break;
                    case NETHER_WART:
                        PhoneUtils.openContacts(player, playerData.getIntVariable("current_page") - 1, null);
                        break;
                    case GOLD_NUGGET:
                        PhoneUtils.openContacts(player, playerData.getIntVariable("current_page") + 1, null);
                        break;
                    case PLAYER_HEAD:
                        if (!(event.getSlot() == 22)) PhoneUtils.editContact(player, event.getCurrentItem(), false, false);
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
                        PhoneUtils.editContact(player, null, true, true);
                        break;
                }
            } else if (playerData.getVariable("current_app").equals("edit_contact")) {
                switch (event.getCurrentItem().getType()) {
                    case REDSTONE:
                        PhoneUtils.openContacts(player, 1, null);
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
                                Statement statement = MySQL.getStatement();
                                String uuid = null;
                                ResultSet res = statement.executeQuery("SELECT `uuid` FROM `players` WHERE `id` = " + playerData.getIntVariable("current_contact_number"));
                                if (res.next()) {
                                    uuid = res.getString(1);
                                    statement.execute("INSERT INTO `phone_contacts` (`uuid`, `contact_name`, `contact_number`, `contact_uuid`) VALUES ('" + player.getUniqueId() + "', '" + playerData.getVariable("current_contact_name") + "', " + playerData.getIntVariable("current_contact_number") + ", '" + uuid + "')");
                                    player.sendMessage("§8[§6Kontakte§8]§a Nummer " + playerData.getIntVariable("current_contact_number") + "§7 unter " + playerData.getVariable("current_contact_name").replace("&", "§") + "§7 eingespeichert.");
                                    PhoneUtils.openContacts(player, 1, null);
                                } else {
                                    player.sendMessage("§8[§6Kontakte§8]§c Nummer konnte nicht gefunden werden.");
                                }
                            } else {
                                Statement statement = MySQL.getStatement();
                                statement.executeUpdate("UPDATE `phone_contacts` SET `contact_name` = '" + playerData.getVariable("current_contact_name") + "', `contact_number` = " + playerData.getIntVariable("current_contact_number") + " WHERE `id` = " + playerData.getIntVariable("current_contact_id"));
                                player.sendMessage("§8[§6Kontakte§8]§7 Kontakt " + playerData.getVariable("current_contact_name").replace("&", "§") + "§7 angepasst.");
                                PhoneUtils.openContacts(player, 1, null);
                            }
                        } else {
                            player.sendMessage("§8[§6Kontakte§8]§7 Gib bitte Namen & Nummer an.");
                        }
                        break;
                    case RED_DYE:
                        Statement statement = MySQL.getStatement();
                        statement.execute("DELETE FROM `phone_contacts` WHERE `id` = " + playerData.getIntVariable("current_contact_id"));
                        player.sendMessage("§8[§6Kontakte§8]§c Kontakt gelöscht.");
                        PhoneUtils.openContacts(player, 1, null);
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
            } else if (playerData.getVariable("current_app").equals("settings")) {
                switch (Objects.requireNonNull(event.getCurrentItem()).getType()) {
                    case RED_STAINED_GLASS_PANE:
                        event.getCurrentItem().setType(Material.GREEN_STAINED_GLASS_PANE);
                        ItemMeta meta = event.getCurrentItem().getItemMeta();
                        meta.setDisplayName("§aFlugmodus abschalten");
                        event.getCurrentItem().setItemMeta(meta);
                        playerData.setFlightmode(true);
                        break;
                    case GREEN_STAINED_GLASS_PANE:
                        event.getCurrentItem().setType(Material.RED_STAINED_GLASS_PANE);
                        ItemMeta itemMeta = event.getCurrentItem().getItemMeta();
                        itemMeta.setDisplayName("§cFlugmodus einschalten");
                        event.getCurrentItem().setItemMeta(itemMeta);
                        playerData.setFlightmode(false);
                        break;
                    case REDSTONE:
                        PhoneUtils.openPhone(player);
                        break;
                }
            } else if (playerData.getVariable("current_app").equals("banking")) {
                switch (Objects.requireNonNull(event.getCurrentItem()).getType()) {
                    case REDSTONE:
                        PhoneUtils.openPhone(player);
                        break;
                    case DIAMOND:
                        PhoneUtils.openTransactions(player, 1, null);
                        break;
                }
            } else if (playerData.getVariable("current_app").equals("transactions")) {
                switch (event.getCurrentItem().getType()) {
                    case REDSTONE:
                        PhoneUtils.openBanking(player);
                        break;
                    case NETHER_WART:
                        PhoneUtils.openTransactions(player, playerData.getIntVariable("current_page") - 1, null);
                        break;
                    case GOLD_NUGGET:
                        PhoneUtils.openTransactions(player, playerData.getIntVariable("current_page") + 1, null);
                        break;
                    case CLOCK:
                        player.closeInventory();
                        playerData.setVariable("chatblock", "checktransactions");
                        player.sendMessage("§8[§3Banking§8]§7 Gib nun den Transaktionsgrund an.");
                        break;
                }
            } else if (playerData.getVariable("current_app").equals("messages")) {
                switch (event.getCurrentItem().getType()) {
                    case REDSTONE:
                        PhoneUtils.openPhone(player);
                        break;
                    case NETHER_WART:
                        PhoneUtils.openMessages(player, playerData.getIntVariable("current_page") - 1, null);
                        break;
                    case GOLD_NUGGET:
                        PhoneUtils.openMessages(player, playerData.getIntVariable("current_page") + 1, null);
                        break;
                    case CLOCK:
                        player.closeInventory();
                        playerData.setVariable("chatblock", "checkmessages");
                        player.sendMessage("§8[§6SMS§8]§7 Gib nun die Nachricht an.");
                        break;
                    case PAPER:
                        ItemMeta meta = event.getCurrentItem().getItemMeta();
                        NamespacedKey read = new NamespacedKey(Main.plugin, "isRead");
                        if (meta.getPersistentDataContainer().get(read, PersistentDataType.INTEGER) == 0) {
                            meta.setLore(Arrays.asList("§8 ➥ §7Nachricht§8:§6 " + meta.getPersistentDataContainer().get(new NamespacedKey(Main.plugin, "message"), PersistentDataType.STRING), "§8 ➥ §7Datum§8:§6 " + meta.getPersistentDataContainer().get(new NamespacedKey(Main.plugin, "date"), PersistentDataType.STRING)));
                            meta.getPersistentDataContainer().set(read, PersistentDataType.INTEGER, 1);
                            event.getCurrentItem().setItemMeta(meta);
                            Statement statement = MySQL.getStatement();
                            statement.executeUpdate("UPDATE `phone_messages` SET `isRead` = true WHERE `id` = " + meta.getPersistentDataContainer().get(new NamespacedKey(Main.plugin, "message_id"), PersistentDataType.INTEGER));
                        }
                        break;
                }
            } else if (playerData.getVariable("current_app").equals("phonecall")) {
                switch (event.getSlot()) {
                    case 12:
                        playerData.setVariable("current_phone_callnumber", playerData.getVariable("current_phone_callnumber") + 1);
                        PhoneUtils.openCallApp(player, false);
                        break;
                    case 13:
                        playerData.setVariable("current_phone_callnumber", playerData.getVariable("current_phone_callnumber") + 2);
                        PhoneUtils.openCallApp(player, false);
                        break;
                    case 14:
                        playerData.setVariable("current_phone_callnumber", playerData.getVariable("current_phone_callnumber") + 3);
                        PhoneUtils.openCallApp(player, false);
                        break;
                    case 21:
                        playerData.setVariable("current_phone_callnumber", playerData.getVariable("current_phone_callnumber") + 4);
                        PhoneUtils.openCallApp(player, false);
                        break;
                    case 22:
                        playerData.setVariable("current_phone_callnumber", playerData.getVariable("current_phone_callnumber") + 5);
                        PhoneUtils.openCallApp(player, false);
                        break;
                    case 23:
                        playerData.setVariable("current_phone_callnumber", playerData.getVariable("current_phone_callnumber") + 6);
                        PhoneUtils.openCallApp(player, false);
                        break;
                    case 30:
                        playerData.setVariable("current_phone_callnumber", playerData.getVariable("current_phone_callnumber") + 7);
                        PhoneUtils.openCallApp(player, false);
                        break;
                    case 31:
                        playerData.setVariable("current_phone_callnumber", playerData.getVariable("current_phone_callnumber") + 8);
                        PhoneUtils.openCallApp(player, false);
                        break;
                    case 32:
                        playerData.setVariable("current_phone_callnumber", playerData.getVariable("current_phone_callnumber") + 9);
                        PhoneUtils.openCallApp(player, false);
                        break;
                    case 40:
                        playerData.setVariable("current_phone_callnumber", playerData.getVariable("current_phone_callnumber") + 0);
                        PhoneUtils.openCallApp(player, false);
                        break;
                    case 53:
                        player.closeInventory();
                        player.performCommand("call " + Integer.parseInt(playerData.getVariable("current_phone_callnumber")));
                        break;
                    case 45:
                        PhoneUtils.openPhone(player);
                        break;
                }
            }
        }
        if (Objects.equals(playerData.getVariable("current_inventory"), "computer")) {
            event.setCancelled(true);
            if (playerData.getVariable("current_app") == null) {
                switch (Objects.requireNonNull(event.getCurrentItem()).getType()) {
                    case RED_DYE:
                        event.getCurrentItem().setType(Material.GREEN_DYE);
                        ItemMeta meta = event.getCurrentItem().getItemMeta();
                        meta.setDisplayName("§c§lDienst verlassen!");
                        event.getCurrentItem().setItemMeta(meta);
                        FactionManager.setDuty(player, false);
                        FactionManager.sendMessageToFaction(playerData.getFaction(), player.getName() + " hat den Dienst verlassen.");
                        break;
                    case GREEN_DYE:
                        event.getCurrentItem().setType(Material.RED_DYE);
                        ItemMeta itemMeta = event.getCurrentItem().getItemMeta();
                        itemMeta.setDisplayName("§a§lDienst betreten!");
                        event.getCurrentItem().setItemMeta(itemMeta);
                        FactionManager.setDuty(player, true);
                        FactionManager.sendMessageToFaction(playerData.getFaction(), player.getName() + " hat den Dienst betreten.");
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
        if (Objects.equals(playerData.getVariable("current_inventory"), "gasstation")) {
            event.setCancelled(true);
            Integer station = LocationManager.isPlayerGasStation(player);
            GasStationData gasStationData = LocationManager.gasStationDataMap.get(station);
            if (playerData.getVariable("current_app") != "fill_options") {
                if (event.getCurrentItem().getType() == Material.MINECART) {
                    String type = event.getCurrentItem().getItemMeta().getPersistentDataContainer().get(new NamespacedKey(Main.plugin, "type"), PersistentDataType.STRING);
                    int id = event.getCurrentItem().getItemMeta().getPersistentDataContainer().get(new NamespacedKey(Main.plugin, "id"), PersistentDataType.INTEGER);
                    VehicleData vehicleData = Vehicles.vehicleDataMap.get(type);
                    Entity vehicle = null;
                    for (Entity entity : Bukkit.getWorld(player.getWorld().getName()).getEntities()) {
                        if (entity.getType() == EntityType.MINECART) {
                            if (entity.getPersistentDataContainer().get(new NamespacedKey(Main.plugin, "id"), PersistentDataType.INTEGER) == id) {
                                vehicle = entity;
                            }
                        }
                    }
                    if (vehicle != null) {
                        if (event.isLeftClick()) {
                            Inventory inv = Bukkit.createInventory(player, 27, "§8 » §6Tankstelle");
                            inv.setItem(10, ItemManager.createItem(Material.PURPLE_DYE, 1, 0, "§5-10 Liter", null));
                            inv.setItem(11, ItemManager.createItem(Material.MAGENTA_DYE, 1, 0, "§d-1 Liter", null));
                            inv.setItem(15, ItemManager.createItem(Material.LIME_DYE, 1, 0, "§a+1 Liter", null));
                            inv.setItem(16, ItemManager.createItem(Material.GREEN_DYE, 1, 0, "§2+10 Liter", null));
                            inv.setItem(13, ItemManager.createItem(Material.MINECART, 1, 0, "§6" + type, "§7 ➥ §e" + Math.floor(vehicle.getPersistentDataContainer().get(new NamespacedKey(Main.plugin, "fuel"), PersistentDataType.FLOAT)) + " Liter"));
                            inv.setItem(26, ItemManager.createItem(Material.EMERALD, 1, 0, "§aBestätigen", "§7 ➥ §7Kosten: 0$ "));
                            playerData.setIntVariable("plusfuel", 0);
                            playerData.setIntVariable("current_fuel", (int) Math.floor(vehicle.getPersistentDataContainer().get(new NamespacedKey(Main.plugin, "fuel"), PersistentDataType.FLOAT)));
                            playerData.setVariable("current_app", "fill_options");
                            playerData.setIntVariable("gas_currrent_vehicle", id);
                            player.openInventory(inv);
                        } else if (event.isRightClick()) {
                            int price = (int) (vehicleData.getMaxFuel() - vehicle.getPersistentDataContainer().get(new NamespacedKey(Main.plugin, "fuel"), PersistentDataType.FLOAT)) * gasStationData.getLiterprice();
                            if (playerData.getBargeld() >= price) {
                                Vehicles.fillVehicle((Vehicle) vehicle, null);
                                PlayerManager.removeMoney(player, price, "Tankrechnung " + type);
                                player.sendMessage(Main.prefix + "Du hast dein §6" + type + "§7 betankt. §c-" + price + "$");
                                player.closeInventory();
                                playerData.setVariable("current_inventory", null);
                            } else {
                                player.sendMessage(Main.error + "Du hast nicht genug Geld dabei (§a" + price + "$§7).");
                            }
                        }
                    }
                }
            } else {
                Entity vehicle = null;
                for (Entity entity : Bukkit.getWorld(player.getWorld().getName()).getEntities()) {
                    if (entity.getType() == EntityType.MINECART) {
                        if (entity.getPersistentDataContainer().get(new NamespacedKey(Main.plugin, "id"), PersistentDataType.INTEGER) == playerData.getIntVariable("gas_currrent_vehicle")) {
                            vehicle = entity;
                        }
                    }
                }
                String type = vehicle.getPersistentDataContainer().get(new NamespacedKey(Main.plugin, "type"), PersistentDataType.STRING);
                VehicleData vehicleData = Vehicles.vehicleDataMap.get(type);
                switch (event.getCurrentItem().getType()) {
                    case PURPLE_DYE:
                        if (0 <= playerData.getIntVariable("plusfuel") - 10) {
                            playerData.setIntVariable("current_fuel", playerData.getIntVariable("current_fuel") - 10);
                            playerData.setIntVariable("plusfuel", playerData.getIntVariable("plusfuel") - 10);
                        }
                        break;
                    case MAGENTA_DYE:
                        if (0 <= playerData.getIntVariable("plusfuel") - 1) {
                            playerData.setIntVariable("current_fuel", playerData.getIntVariable("current_fuel") - 1);
                            playerData.setIntVariable("plusfuel", playerData.getIntVariable("plusfuel") - 1);
                        }
                        break;
                    case LIME_DYE:
                        if (vehicleData.getMaxFuel() <= playerData.getIntVariable("plusfuel") + 1) {
                            playerData.setIntVariable("current_fuel", playerData.getIntVariable("current_fuel") + 1);
                            playerData.setIntVariable("plusfuel", playerData.getIntVariable("plusfuel") + 1);
                        }
                        break;
                    case GREEN_DYE:
                        if (vehicleData.getMaxFuel() <= playerData.getIntVariable("plusfuel") + 10) {
                            playerData.setIntVariable("current_fuel", playerData.getIntVariable("current_fuel") + 10);
                            playerData.setIntVariable("plusfuel", playerData.getIntVariable("plusfuel") + 10);
                        }
                        break;
                    case EMERALD:
                        int price = playerData.getIntVariable("plusfuel") * gasStationData.getLiterprice();
                        if (playerData.getBargeld() >= price) {
                            PlayerManager.removeMoney(player, price, "Tankrechnung " + type);
                            Vehicles.fillVehicle((Vehicle) vehicle, playerData.getIntVariable("plusfuel"));
                            player.sendMessage(Main.prefix + "Du hast dein §6" + type + "§7 betankt. §c-" + price + "$");
                            SoundManager.successSound(player);
                            player.closeInventory();
                        } else {
                            player.sendMessage(Main.error + "Du hast nicht genug Geld dabei (§a" + price + "$§7).");
                        }
                        break;
                }
                ItemMeta meta = event.getInventory().getItem(13).getItemMeta();
                meta.setLore(Collections.singletonList("§7 ➥ §e" + playerData.getIntVariable("current_fuel") + " Liter"));
                event.getInventory().getItem(13).setItemMeta(meta);
                ItemMeta nextMeta = event.getInventory().getItem(26).getItemMeta();
                nextMeta.setLore(Collections.singletonList("§7 ➥ Kosten: " + (playerData.getIntVariable("plusfuel") * gasStationData.getLiterprice()) + "$"));
                event.getInventory().getItem(26).setItemMeta(nextMeta);
            }
        }
        if (Objects.equals(playerData.getVariable("current_inventory"), "einreise")) {
            event.setCancelled(true);
            switch (event.getSlot()) {
                case 1:
                    playerData.setVariable("chatblock", "firstname");
                    player.sendMessage(Main.prefix + "Gib nun deinen §6Vornamen§7 in den §6Chat§7 ein!");
                    player.closeInventory();
                    break;
                case 2:
                    playerData.setVariable("chatblock", "lastname");
                    player.sendMessage(Main.prefix + "Gib nun deinen §6Nachnamen§7 in den §6Chat§7 ein!");
                    player.closeInventory();
                    break;
                case 4:
                    if (event.getClick().isLeftClick()) {
                        playerData.setVariable("einreise_gender", "Männlich");
                        ItemMeta meta = event.getCurrentItem().getItemMeta();
                        meta.setDisplayName("§eMännlich");
                        event.getCurrentItem().setItemMeta(meta);
                    } else if (event.getClick().isRightClick()) {
                        playerData.setVariable("einreise_gender", "Weiblich");
                        ItemMeta meta = event.getCurrentItem().getItemMeta();
                        meta.setDisplayName("§eWeiblich");
                        event.getCurrentItem().setItemMeta(meta);
                    }
                    break;

                case 5:
                    playerData.setVariable("chatblock", "dob");
                    player.sendMessage(Main.prefix + "Gib nun deinen §6Geburtstag§7 in den §6Chat§7 ein!");
                    player.closeInventory();
                    break;
                case 8:
                    if (playerData.getVariable("einreise_firstname") != null) {
                        if (playerData.getVariable("einreise_lastname") != null) {
                            if (playerData.getVariable("einreise_gender") != null) {
                                if (playerData.getVariable("einreise_dob") != null) {
                                    player.closeInventory();
                                    playerData.setFirstname(playerData.getVariable("einreise_firstname"));
                                    playerData.setLastname(playerData.getVariable("einreise_lastname"));
                                    playerData.setBirthday(playerData.getVariable("einreise_dob"));
                                    playerData.setGender(playerData.getVariable("einreise_gender"));
                                    Statement statement = MySQL.getStatement();
                                    statement.executeUpdate("UPDATE `players` SET `firstname` = '" + playerData.getVariable("einreise_firstname") + "', `lastname` = '" + playerData.getVariable("einreise_lastname") + "', `birthday` = '" + playerData.getVariable("einreise_dob") + "', `gender` = '" + playerData.getVariable("einreise_gender") + "' WHERE `uuid` = '" + player.getUniqueId().toString() + "'");
                                    player.sendMessage(Main.prefix + "Du bist nun §6Staatsbürger§7, nutze §l/perso§7 um dir deinen Personalausweis anzuschauen!");
                                    PlayerManager.addExp(player, Main.random(100, 200));
                                    tutorial.createdAusweis(player);
                                    player.playSound(player.getLocation(), Sound.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, 1, 0);
                                } else {
                                    player.sendMessage(Main.error + "Bitte gib deinen Geburtstag noch an!");
                                }
                            } else {
                                player.sendMessage(Main.error + "Bitte gib dein Geschlecht noch an!");
                            }
                        } else {
                            player.sendMessage(Main.error + "Bitte gib deinen Nachnamen noch an!");
                        }
                    } else {
                        player.sendMessage(Main.error + "Bitte gib deinen Vornamen noch an!");
                    }
            }
        }
        if (Objects.equals(playerData.getVariable("current_inventory"), "navi")) {
            event.setCancelled(true);
            if (event.getSlot() == 22) {
                playerData.setVariable("chatblock", "gpssearch");
                player.sendMessage("§8[§eGPS§8]§7 Gib nun den gesuchten GPS Punkt ein.");
                player.closeInventory();
            } else {
                int id = event.getCurrentItem().getItemMeta().getPersistentDataContainer().get(new NamespacedKey(Main.plugin, "id"), PersistentDataType.INTEGER);
                for (NaviData naviData : LocationManager.naviDataMap.values()) {
                    if (naviData.getId() == id) {
                        if (naviData.isGroup()) {
                            SoundManager.clickSound(player);
                            Inventory inv = Bukkit.createInventory(player, 27, "§8 » " + naviData.getName().replace("&", "§"));
                            int i = 0;
                            for (NaviData newNavi : LocationManager.naviDataMap.values()) {
                                if (newNavi.getGroup().equalsIgnoreCase(naviData.getGroup()) && !newNavi.isGroup()) {
                                    inv.setItem(i, ItemManager.createItem(newNavi.getItem(), 1, 0, newNavi.getName().replace("&", "§"), "§7 ➥ §e" + (int) LocationManager.getDistanceBetweenCoords(player, newNavi.getLocation()) + "m"));
                                    ItemMeta meta = inv.getItem(i).getItemMeta();
                                    meta.getPersistentDataContainer().set(new NamespacedKey(Main.plugin, "id"), PersistentDataType.INTEGER, newNavi.getId());
                                    inv.getItem(i).setItemMeta(meta);
                                    i++;
                                }
                            }
                            player.openInventory(inv);
                        } else {
                            player.sendMessage("§8[§6GPS§8]§7 Du hast eine Route zu " + naviData.getName().replace("&", "§") + "§7 gesetzt.");
                            LocationData locationData = LocationManager.locationDataMap.get(naviData.getLocation());
                            Navigation.createNaviByCord(player, locationData.getX(), locationData.getY(), locationData.getZ());
                            player.closeInventory();
                        }
                    }
                }
            }
        }
        if (Objects.equals(playerData.getVariable("current_inventory"), "ffa_menu")) {
            event.setCancelled(true);
            switch (Objects.requireNonNull(event.getCurrentItem()).getType()) {
                case NETHER_WART:
                    FFA.openFFAMenu(player, playerData.getIntVariable("current_page") - 1);
                    SoundManager.clickSound(player);
                    break;
                case GOLD_NUGGET:
                    FFA.openFFAMenu(player, playerData.getIntVariable("current_page") + 1);
                    SoundManager.clickSound(player);
                    break;
                case LIME_DYE:
                    ItemMeta meta = event.getCurrentItem().getItemMeta();
                    int id = meta.getPersistentDataContainer().get(new NamespacedKey(Main.plugin, "id"), PersistentDataType.INTEGER);
                    if (FFA.FFAlobbyDataMap.get(id).getPassword() != null && !FFA.FFAlobbyDataMap.get(id).getName().equals(player.getUniqueId().toString())) {
                        playerData.setVariable("chatblock", "ffa_joinpassword");
                        playerData.setIntVariable("ffa_passwordlobby", id);
                        player.sendMessage("§8[§6FFA§8]§e Gib das Passwort für die Lobby ein.");
                    } else {
                        FFA.joinLobby(player, id);
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
                        inv.setItem(15, ItemManager.createItem(Material.CHEST, 1, 0, "§ePasswort setzen", null));
                        inv.setItem(18, ItemManager.createItem(Material.NETHER_WART, 1, 0, "§cZurück", null));
                        inv.setItem(26, ItemManager.createItem(Material.EMERALD, 1, 0, "§aLobby erstellen", null));
                        for (int i = 0; i < 27; i++) {
                            if (inv.getItem(i) == null) {
                                inv.setItem(i, ItemManager.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, 0, "§8", null));
                            }
                        }
                        player.openInventory(inv);
                        Main.cooldownManager.setCooldown(player, "ffa_creator", 2);
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
                    if (!Main.cooldownManager.isOnCooldown(player, "ffa_creator")) FFA.createLobby(player, playerData.getIntVariable("ffa_maxplayer"), playerData.getVariable("ffa_password"));
                    break;
                case NETHER_WART:
                    FFA.openFFAMenu(player, 1);
                    SoundManager.clickSound(player);
                    break;
            }
        }
        if (Objects.equals(playerData.getVariable("current_inventory"), "gangwar")) {
            event.setCancelled(true);
            switch (Objects.requireNonNull(event.getCurrentItem()).getType()) {
                case LIME_DYE:
                    ItemMeta meta = event.getCurrentItem().getItemMeta();
                    Gangwar.startGangwar(player, meta.getPersistentDataContainer().get(new NamespacedKey(Main.plugin, "zone"), PersistentDataType.STRING));
                    player.closeInventory();
                    break;
            }
        }
        if (Objects.equals(playerData.getVariable("current_inventory"), "atm")) {
            event.setCancelled(true);
            switch (event.getSlot()) {
                case 11:
                    playerData.setVariable("chatblock", "atm_auszahlen");
                    player.sendMessage("§8[§aATM§8]§7 Gib nun einen Wert ein.");
                    player.closeInventory();
                    break;
                case 15:
                    playerData.setVariable("chatblock", "atm_einzahlen");
                    player.sendMessage("§8[§aATM§8]§7 Gib nun einen Wert ein.");
                    player.closeInventory();
                    break;
                case 29:
                    player.sendMessage("§8[§aATM§8]§a Du hast " + playerData.getBank() + "$ ausgezahlt.");
                    PlayerManager.addMoney(player, playerData.getBank());
                    PlayerManager.removeBankMoney(player, playerData.getBank(), "Bankauszahlung");
                    player.closeInventory();
                    break;
                case 33:
                    player.sendMessage("§8[§aATM§8]§a Du hast " + playerData.getBargeld() + "$ eingezahlt.");
                    PlayerManager.addBankMoney(player, playerData.getBargeld(), "Bankeinzahlung");
                    PlayerManager.removeMoney(player, playerData.getBargeld(), "Bankeinzahlung");
                    player.closeInventory();
                    break;
                case 31:
                    playerData.setVariable("chatblock", "atm_transfer_player");
                    player.closeInventory();
                    player.sendMessage("§8[§aATM§8]§7 Gib den Spieler an, an wen das Geld überwiesen werden soll.");
                    break;
                case 44:
                    if (playerData.getFaction() != null && playerData.getFaction() != "Zivilist") {
                        BankingUtils.openFactionBankMenu(player);
                        Main.cooldownManager.setCooldown(player, "atm", 1);
                    }
                    break;
            }
        }

        if (Objects.equals(playerData.getVariable("current_inventory"), "atm_frak")) {
            event.setCancelled(true);
            switch (event.getSlot()) {
                case 15:
                    playerData.setVariable("chatblock", "atm_frak_einzahlen");
                    player.sendMessage("§8[§aATM§8]§7 Gib nun einen Wert ein.");
                    player.closeInventory();
                    break;
                case 11:
                    if (playerData.getFactionGrade() >= 7) {
                        playerData.setVariable("chatblock", "atm_frak_auszahlen");
                        player.sendMessage("§8[§aATM§8]§7 Gib nun einen Wert ein.");
                        player.closeInventory();
                    }
                    break;
                case 29:
                    if (playerData.getFactionGrade() >= 7) {
                        player.sendMessage("§8[§aATM§8]§a Du hast " + factionData.getBank() + "$ ausgezahlt.");
                        FactionManager.sendMessageToFaction(factionData.getName(), player.getName() + " hat " + factionData.getBank() + "$ vom Fraktionskonto ausgezahlt.");
                        PlayerManager.addMoney(player, factionData.getBank());
                        FactionManager.removeFactionMoney(factionData.getName(), factionData.getBank(), "Bankauszahlung " + player.getName());
                        player.closeInventory();
                    }
                    break;
                case 33:
                    player.sendMessage("§8[§aATM§8]§a Du hast " + playerData.getBargeld() + "$ eingezahlt.");
                    FactionManager.sendMessageToFaction(factionData.getName(), player.getName() + " hat " + playerData.getBargeld() + "$ auf das Fraktionskonto eingezahlt.");
                    FactionManager.addFactionMoney(factionData.getName(), playerData.getBargeld(), "Bankeinzahlung " + player.getName());
                    PlayerManager.removeMoney(player, playerData.getBargeld(), "Bankeinzahlung auf " + factionData.getName());
                    player.closeInventory();
                    break;
                case 44:
                    if (!Main.cooldownManager.isOnCooldown(player, "atm")) BankingUtils.openBankMenu(player);
                    break;
            }
        }
        if (Objects.equals(playerData.getVariable("current_inventory"), "jugendschutz")) {
            event.setCancelled(true);
            switch (event.getSlot()) {
                case 11:
                    playerData.setVariable("jugendschutz", null);
                    player.closeInventory();
                    player.sendMessage("§8[§c§lJugendschutz§8]§a Du hast den Jugendschutz aktzeptiert.");
                    Statement statement = MySQL.getStatement();
                    statement.executeUpdate("UPDATE `players` SET `jugendschutz` = true, `jugendschutz_accepted` = NOW() WHERE `uuid` = '" + player.getUniqueId().toString() + "'");
                    player.closeInventory();
                    break;
                case 15:
                    player.kickPlayer("§cDa du den Jugendschutz nicht aktzeptieren konntest, kannst du auf dem Server §lnicht§c Spielen.\n§cBitte deine Erziehungsberechtigten um Erlabunis oder warte bis du 18 bist.");
                    break;
            }
        }
        if (Objects.equals(playerData.getVariable("current_inventory"), "haus")) {
            event.setCancelled(true);
            switch (event.getCurrentItem().getType()) {
                case RED_DYE:
                    if (Housing.resetHouse(player, playerData.getIntVariable("current_house"))) {
                        HouseData houseData = Housing.houseDataMap.get(playerData.getIntVariable("current_house"));
                        PlayerManager.addMoney(player, (int) (houseData.getPrice() * 0.8));
                        player.sendMessage("§8[§6Haus§8]§a Du hast Haus " + houseData.getNumber() + " für " +  (int) (houseData.getPrice() * 0.8) + "$ verkauft.");
                        player.closeInventory();
                    }
                    break;
                case LIME_DYE:
                    player.performCommand("buyhouse " + playerData.getIntVariable("current_house"));
                    player.closeInventory();
                    break;
                case BOOK:
                    PostboteCommand.dropTransport(player, playerData.getIntVariable("current_house"));
                    player.closeInventory();
                    break;
                case CAULDRON:
                    MuellmannCommand.dropTransport(player, playerData.getIntVariable("current_house"));
                    player.closeInventory();
                    break;
            }
        }
        if (Objects.equals(playerData.getVariable("current_inventory"), "farmer")) {
            event.setCancelled(true);
            switch (Objects.requireNonNull(event.getCurrentItem()).getType()) {
                case LIME_DYE:
                    FarmerCommand.startJob(player);
                    player.closeInventory();
                    break;
                case YELLOW_DYE:
                    FarmerCommand.quitJob(player);
                    player.closeInventory();
                    break;
                case WHEAT:
                    FarmerCommand.startTransport(player);
                    player.closeInventory();
                    break;
            }
        }
        if (Objects.equals(playerData.getVariable("current_inventory"), "postbote")) {
            event.setCancelled(true);
            switch (Objects.requireNonNull(event.getCurrentItem()).getType()) {
                case LIME_DYE:
                    PostboteCommand.startTransport(player);
                    player.closeInventory();
                    break;
                case YELLOW_DYE:
                    PostboteCommand.quitJob(player, false);
                    player.closeInventory();
                    break;
            }
        }
        if (Objects.equals(playerData.getVariable("current_inventory"), "müllmann")) {
            event.setCancelled(true);
            switch (Objects.requireNonNull(event.getCurrentItem()).getType()) {
                case LIME_DYE:
                    MuellmannCommand.startTransport(player);
                    player.closeInventory();
                    break;
                case YELLOW_DYE:
                    MuellmannCommand.quitJob(player, false);
                    player.closeInventory();
                    break;
            }
        }
        if (Objects.equals(playerData.getVariable("current_inventory"), "holzfäller")) {
            event.setCancelled(true);
            switch (Objects.requireNonNull(event.getCurrentItem()).getType()) {
                case LIME_DYE:
                    LumberjackCommand.startJob(player);
                    player.closeInventory();
                    break;
                case YELLOW_DYE:
                    LumberjackCommand.quitJob(player, false);
                    player.closeInventory();
                    break;
            }
        }
        if (Objects.equals(playerData.getVariable("current_inventory"), "news")) {
            event.setCancelled(true);
            switch (Objects.requireNonNull(event.getCurrentItem()).getType()) {
                case GREEN_DYE:
                    playerData.setVariable("chatblock", "werbung");
                    player.sendMessage("§8[§2Werbung§8]§7 Gib nun deine Nachricht in den Chat ein.");
                    player.closeInventory();
                    break;
                case YELLOW_DYE:
                    playerData.setVariable("chatblock", "news");
                    player.sendMessage("§8[§6News§8]§7 Gib nun deine Nachricht in den Chat ein.");
                    player.closeInventory();
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
                    if (targetplayer ==  null) {
                        return;
                    }
                    player.performCommand("personalausweis show " + targetplayer.getName());
                    if (playerData.getGender().equals("Männlich")) {
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
                        if (!PlayerManager.canPlayerMove(targetplayer3)) {
                            ChatUtils.sendGrayMessageAtPlayer(player, player.getName() + " versucht " + targetplayer3.getName() + " zu durchsuchen.");
                            progress.start(player, 5);
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
                    if (targetplayer2 ==  null) {
                        return;
                    }
                    Server.Utils.kissPlayer(player, targetplayer2);
                    player.closeInventory();
                    break;
            }
        }
        if (Objects.equals(playerData.getVariable("current_inventory"), "garage")) {
            event.setCancelled(true);
            if (playerData.getVariable("current_app").equalsIgnoreCase("parkin")) {
                switch (event.getCurrentItem().getType()) {
                    case REDSTONE:
                        Vehicles.openGarage(player, playerData.getIntVariable("current_garage"), false);
                        break;
                    case MINECART:
                        ItemMeta meta = event.getCurrentItem().getItemMeta();
                        int id = meta.getPersistentDataContainer().get(new NamespacedKey(Main.plugin, "id"), PersistentDataType.INTEGER);
                        PlayerVehicleData playerVehicleData = Vehicles.playerVehicleDataMap.get(id);
                        VehicleData vehicleData = Vehicles.vehicleDataMap.get(playerVehicleData.getType());
                        playerVehicleData.setParked(true);
                        Vehicles.deleteVehicleById(id);
                        Utils.sendActionBar(player, "§2" + vehicleData.getName() + "§a eingeparkt!");
                        SoundManager.successSound(player);
                        Statement statement = MySQL.getStatement();
                        statement.executeUpdate("UPDATE player_vehicles SET parked = true, garage = " + playerData.getIntVariable("current_garage") + " WHERE id = " + id);
                        playerVehicleData.setGarage(playerData.getIntVariable("current_garage"));
                        player.closeInventory();
                        break;
                }
            } else if (playerData.getVariable("current_app").equalsIgnoreCase("parkout")) {
                switch (event.getCurrentItem().getType()) {
                    case EMERALD:
                        Vehicles.openGarage(player, playerData.getIntVariable("current_garage"), true);
                        break;
                    case MINECART:
                        ItemMeta meta = event.getCurrentItem().getItemMeta();
                        int id = meta.getPersistentDataContainer().get(new NamespacedKey(Main.plugin, "id"), PersistentDataType.INTEGER);
                        PlayerVehicleData playerVehicleData = Vehicles.playerVehicleDataMap.get(id);
                        VehicleData vehicleData = Vehicles.vehicleDataMap.get(playerVehicleData.getType());
                        playerVehicleData.setParked(false);
                        Utils.sendActionBar(player, "§2" + vehicleData.getName() + "§a ausgeparkt!");
                        SoundManager.successSound(player);
                        Statement statement = MySQL.getStatement();
                        statement.executeUpdate("UPDATE player_vehicles SET parked = false WHERE id = " + id);
                        Vehicles.spawnVehicle(player, playerVehicleData);
                        player.closeInventory();
                        break;
                }
            }
        }
        if (playerData.getVariable("current_inventory").contains("verarbeiter")) {
            event.setCancelled(true);
        }
        if (playerData.getVariable("current_inventory").contains("dealer")) {
            event.setCancelled(true);
        }
    }
}
