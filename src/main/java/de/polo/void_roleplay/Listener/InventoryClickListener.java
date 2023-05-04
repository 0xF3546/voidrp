package de.polo.void_roleplay.Listener;

import com.sun.tools.javac.file.Locations;
import de.polo.void_roleplay.DataStorage.*;
import de.polo.void_roleplay.Main;
import de.polo.void_roleplay.MySQl.MySQL;
import de.polo.void_roleplay.PlayerUtils.Shop;
import de.polo.void_roleplay.PlayerUtils.rubbellose;
import de.polo.void_roleplay.Utils.*;
import de.polo.void_roleplay.commands.adminmenuCommand;
import de.polo.void_roleplay.commands.openBossMenuCommand;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;

public class InventoryClickListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent event) throws SQLException {
        if (event.getCurrentItem() == null) return;
        Player player = (Player) event.getWhoClicked();
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        FactionData factionData = FactionManager.factionDataMap.get(playerData.getFaction());
        if (event.getView().getTitle().equalsIgnoreCase("§6§lRubbellos")) {
            event.setCancelled(true);
            if (event.getCurrentItem().getType() == Material.GRAY_DYE) {
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
                for (Object[] row : Shop.shop_items) {
                    int shop = LocationManager.isNearShop(player);
                    if ((int) row[1] == shop) {
                        if (event.getCurrentItem().getType() == Material.valueOf((String) row[2]) && event.getCurrentItem().getType() != null) {
                            if (PlayerManager.money(player) >= (int) row[4]) {
                                try {
                                    if (Objects.equals(row[5].toString(), "weapon")) {
                                        String weapon = row[3].toString().replace("&", "").replace("6", "");
                                        Weapons.giveWeaponToPlayer(player, event.getCurrentItem().getType());
                                        player.sendMessage("§6" + LocationManager.getShopNameById(f) + "§8 » §7" + "Danke für deinen Einkauf in höhe von §a" + (int) row[4] + "$.");
                                        PlayerManager.removeMoney(player, (int) row[4], "Kauf der Waffe: " + weapon);
                                    } else if (Objects.equals(row[5].toString(), "ammo")) {
                                        String ammo = row[3].toString().replace("&", "").replace("6", "");
                                        Weapons.giveWeaponAmmoToPlayer(player, ammo, 1);
                                        player.sendMessage("§6" + LocationManager.getShopNameById(f) + "§8 » §7" + "Danke für deinen Einkauf in höhe von §a" + (int) row[4] + "$.");
                                        PlayerManager.removeMoney(player, (int) row[4], "Kauf von Munition: " + ammo);
                                    } else if (Objects.equals(row[5].toString(), "car")) {
                                        Vehicles.giveVehicle(player, row[6].toString());
                                    } else {
                                        PlayerManager.removeMoney(player, (int) row[4], "Kauf von: " + event.getCurrentItem().getType());
                                        player.getInventory().addItem(ItemManager.createItem(Material.valueOf((String) row[2]), 1, 0, event.getCurrentItem().getItemMeta().getDisplayName(), null));
                                        player.sendMessage("§6" + LocationManager.getShopNameById(f) + "§8 » §7" + "Danke für deinen Einkauf in höhe von §a" + (int) row[4] + "$.");
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
                    openBossMenuCommand.openBossMenu(player, playerData.getIntVariable("current_page") - 1);
                    break;
                case GOLD_NUGGET:
                    openBossMenuCommand.openBossMenu(player, playerData.getIntVariable("current_page") + 1);
                    break;
                case PLAYER_HEAD:
                    openBossMenuCommand.editPlayerViaBoss(player, event.getCurrentItem());
                    break;
            }
        }
        if (playerData.getVariable("current_inventory").contains("edit_factionplayer_")) {
            event.setCancelled(true);
            switch (Objects.requireNonNull(event.getCurrentItem()).getType()) {
                case NETHER_WART:
                    openBossMenuCommand.openBossMenu(player, 1);
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
                        adminmenuCommand.openAdminMenu(player, playerData.getIntVariable("current_page") - 1, false);
                    else
                        adminmenuCommand.openAdminMenu(player, playerData.getIntVariable("current_page") - 1, true);
                    break;
                case GOLD_NUGGET:
                    if (playerData.getVariable("offlinePLayers") == "nein")
                        adminmenuCommand.openAdminMenu(player, playerData.getIntVariable("current_page") + 1, false);
                    else
                        adminmenuCommand.openAdminMenu(player, playerData.getIntVariable("current_page") + 1, true);
                    break;
                case PLAYER_HEAD:
                    adminmenuCommand.editPlayerViaAdmin(player, event.getCurrentItem());
                    break;
                case DIAMOND:
                    if (playerData.getVariable("offlinePLayers") == "nein") {
                        adminmenuCommand.openAdminMenu(player, 1, true);
                    } else {
                        adminmenuCommand.openAdminMenu(player, 1, false);
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
                        adminmenuCommand.openAdminMenu(player, 1, false);
                    else adminmenuCommand.openAdminMenu(player, 1, true);
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
                }
            } else if (Objects.equals(playerData.getVariable("current_app"), "aktenapp")) {
                switch (Objects.requireNonNull(event.getCurrentItem()).getType()) {
                    case DIAMOND:
                        TabletUtils.openPlayerAktenList(player, 1);
                        break;
                    case PAPER:
                        TabletUtils.openAktenList(player, 1);
                        playerData.setVariable("current_akte", null);
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
                        TabletUtils.openAktenList(player, 1);
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
                        TabletUtils.openAktenList(player, playerData.getIntVariable("current_page") - 1);
                        break;
                    case GOLD_NUGGET:
                        TabletUtils.openAktenList(player, playerData.getIntVariable("current_page") + 1);
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
                        TabletUtils.openAktenList(player, 1);
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
            }
        }
        if (Objects.equals(playerData.getVariable("current_inventory"), "handy")) {
            event.setCancelled(true);
            if (playerData.getVariable("current_app") == null) {
                switch (Objects.requireNonNull(event.getCurrentItem()).getType()) {
                    case RED_STAINED_GLASS_PANE:
                        event.getCurrentItem().setType(Material.GREEN_STAINED_GLASS_PANE);
                        ItemMeta meta = event.getCurrentItem().getItemMeta();
                        meta.setDisplayName("§a§lGeändert!");
                        event.getCurrentItem().setItemMeta(meta);
                        playerData.setFlightmode(true);
                        break;
                    case GREEN_STAINED_GLASS_PANE:
                        event.getCurrentItem().setType(Material.RED_STAINED_GLASS_PANE);
                        ItemMeta itemMeta = event.getCurrentItem().getItemMeta();
                        itemMeta.setDisplayName("§a§lGeändert!");
                        event.getCurrentItem().setItemMeta(itemMeta);
                        playerData.setFlightmode(false);
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
                        if (vehicleData.getMaxFuel() >= playerData.getIntVariable("plusfuel") + 1) {
                            playerData.setIntVariable("current_fuel", playerData.getIntVariable("current_fuel") + 1);
                            playerData.setIntVariable("plusfuel", playerData.getIntVariable("plusfuel") + 1);
                        }
                        break;
                    case GREEN_DYE:
                        if (vehicleData.getMaxFuel() >= playerData.getIntVariable("plusfuel") + 10) {
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
                        playerData.setVariable("einreise_gender", "Maennlich");
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
            int id = event.getCurrentItem().getItemMeta().getPersistentDataContainer().get(new NamespacedKey(Main.plugin, "id"), PersistentDataType.INTEGER);
            for (NaviData naviData : LocationManager.naviDataMap.values()) {
                if (naviData.getId() == id) {
                    if (naviData.isGroup()) {
                        Inventory inv = Bukkit.createInventory(player, 27, "§8 » " + naviData.getName().replace("&", "§"));
                        int i = 0;
                        for (NaviData newNavi : LocationManager.naviDataMap.values()) {
                            if (newNavi.getGroup().equalsIgnoreCase(naviData.getGroup()) && !newNavi.isGroup()) {
                                inv.setItem(i, ItemManager.createItem(newNavi.getItem(), 1, 0, newNavi.getName().replace("&", "§"), null));
                                ItemMeta meta = inv.getItem(i).getItemMeta();
                                meta.getPersistentDataContainer().set(new NamespacedKey(Main.plugin, "id"), PersistentDataType.INTEGER, newNavi.getId());
                                inv.getItem(i).setItemMeta(meta);
                                i++;
                            }
                        }
                        player.openInventory(inv);
                    } else {
                        player.sendMessage("§8[§6Navi§8]§7 Du hast eine Route zu " + naviData.getName().replace("&", "§") + "§7 gesetzt.");
                        LocationData locationData = LocationManager.locationDataMap.get(" " + naviData.getLocation());
                        Navigation.createNaviByCord(player, locationData.getX(), locationData.getY(), locationData.getZ());
                        player.closeInventory();
                    }
                }
            }
        }
    }
}
