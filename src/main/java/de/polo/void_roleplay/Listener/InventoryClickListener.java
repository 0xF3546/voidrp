package de.polo.void_roleplay.Listener;

import de.polo.void_roleplay.Main;
import de.polo.void_roleplay.DataStorage.FactionData;
import de.polo.void_roleplay.DataStorage.PlayerData;
import de.polo.void_roleplay.PlayerUtils.Shop;
import de.polo.void_roleplay.Utils.*;
import de.polo.void_roleplay.commands.adminmenuCommand;
import de.polo.void_roleplay.commands.openBossMenuCommand;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.sql.SQLException;
import java.util.Objects;
import java.util.UUID;

public class InventoryClickListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent event) throws SQLException {
        if (event.getCurrentItem() == null) return;
        Player player = (Player) event.getWhoClicked();
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        FactionData factionData = FactionManager.factionDataMap.get(playerData.getFaction());
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
                            if (playerData.getVariable("offlinePLayers") == "nein") adminmenuCommand.openAdminMenu(player, 1, false);
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
                        case REDSTONE:
                            player.performCommand("ablehnen");
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
                                            players.sendMessage("§cGefängnis §8» §6" + FactionManager.getTitle(player) + " " + player.getName() + "§7 hat §6" + targetlpayer.getName() + "§7 entlassen.");
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
                                    player.sendMessage("§9Zentrale §8» §7Akte wurde für " + targetplayer.getName() + " hinzugefügt.");
                                    player.sendMessage("§9Zentrale §8» §7Akte: " + newAkte + " §8-§7 Hafteinheiten: " + newHafteinheiten + "§8 - §7Geldstrafe: " + newGeldstrafe + "$.");
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
                                    player.sendMessage("§9Zentrale §8» §7Akte von " + targetplayer.getName() + " entfernt.");
                                }
                                break;
                            case REDSTONE:
                                TabletUtils.openAktenList(player, 1);
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
                        break;
                    case GREEN_DYE:
                        event.getCurrentItem().setType(Material.RED_DYE);
                        ItemMeta itemMeta = event.getCurrentItem().getItemMeta();
                        itemMeta.setDisplayName("§a§lDienst betreten!");
                        event.getCurrentItem().setItemMeta(itemMeta);
                        FactionManager.setDuty(player, true);
                        break;
                }
            }
        }
                }
}
