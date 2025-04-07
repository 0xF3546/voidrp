package de.polo.voidroleplay.listeners;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.faction.entity.Faction;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.game.base.vehicle.Vehicles;
import de.polo.voidroleplay.faction.service.impl.FactionManager;
import de.polo.voidroleplay.location.services.impl.LocationManager;
import de.polo.voidroleplay.player.services.impl.PlayerManager;
import de.polo.voidroleplay.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
        Faction factionData = null;
        if (playerData.getVariable("current_inventory") == null) return;
        if (playerData.getFaction() != null) factionData = factionManager.getFactionData(playerData.getFaction());
        if (playerData.getVariable("current_inventory").toString().contains("edit_factionplayer_")) {
            event.setCancelled(true);
            switch (Objects.requireNonNull(event.getCurrentItem()).getType()) {
                case REDSTONE:
                    UUID uuid = UUID.fromString(playerData.getVariable("current_inventory").toString().replace("edit_factionplayer_", ""));
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
                    player.performCommand("uninvite " + offlinePlayer.getName());
                    break;
                case DIAMOND:
                    UUID uuid1 = UUID.fromString(playerData.getVariable("current_inventory").toString().replace("edit_factionplayer_", ""));
                    OfflinePlayer offlinePlayer1 = Bukkit.getOfflinePlayer(uuid1);
                    factionManager.sendMessageToFaction(playerData.getFaction(), "§c" + offlinePlayer1.getName() + "§7 wurde von §c" + player.getName() + " befördert.");
                    Statement statement = Main.getInstance().coreDatabase.getStatement();
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
                    Statement statement1 = Main.getInstance().coreDatabase.getStatement();
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
        if (playerData.getVariable("current_inventory").toString().contains("edit_player_")) {
            event.setCancelled(true);
            UUID uuid = UUID.fromString(playerData.getVariable("current_inventory").toString().replace("edit_player_", ""));
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            switch (Objects.requireNonNull(event.getCurrentItem()).getType()) {
                case NETHER_WART:
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
                            NamespacedKey akte = new NamespacedKey(Main.getInstance(), "akte");
                            NamespacedKey hafteinheiten = new NamespacedKey(Main.getInstance(), "hafteinheiten");
                            NamespacedKey geldstrafe = new NamespacedKey(Main.getInstance(), "geldstrafe");
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
                                Statement statement = Main.getInstance().coreDatabase.getStatement();
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
                                Statement statement = Main.getInstance().coreDatabase.getStatement();
                                statement.executeUpdate("UPDATE `phone_contacts` SET `contact_name` = '" + playerData.getVariable("current_contact_name") + "', `contact_number` = " + playerData.getIntVariable("current_contact_number") + " WHERE `id` = " + playerData.getIntVariable("current_contact_id"));
                                player.sendMessage("§8[§6Kontakte§8]§7 Kontakt " + playerData.getVariable("current_contact_name").toString().replace("&", "§") + "§7 angepasst.");
                                utils.phoneUtils.openContacts(player, 1, null);
                            }
                        } else {
                            player.sendMessage("§8[§6Kontakte§8]§7 Gib bitte Namen & Nummer an.");
                        }
                        break;
                    case RED_DYE:
                        Statement statement = Main.getInstance().coreDatabase.getStatement();
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
                Statement statement = Main.getInstance().coreDatabase.getStatement();
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
                int id = event.getCurrentItem().getItemMeta().getPersistentDataContainer().get(new NamespacedKey(Main.getInstance(), "id"), PersistentDataType.INTEGER);
                Vehicles.toggleVehicleState(id, player);
                player.closeInventory();
                playerData.setVariable("current_inventory", null);
            }
        }
    }
}
