package de.polo.metropiacity.utils.Game;

import de.polo.metropiacity.dataStorage.*;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.database.MySQL;
import de.polo.metropiacity.utils.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class GangwarUtils implements CommandExecutor, TabCompleter {
    public static final HashMap<String, GangwarData> gangwarDataMap = new HashMap<>();

    private final PlayerManager playerManager;
    private final FactionManager factionManager;
    private final LocationManager locationManager;
    public GangwarUtils(PlayerManager playerManager, FactionManager factionManager, LocationManager locationManager) {
        this.playerManager = playerManager;
        this.factionManager = factionManager;
        this.locationManager = locationManager;
        try {
            loadGangwar();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadGangwar() throws SQLException {
        Statement statement = Main.getInstance().mySQL.getStatement();
        ResultSet result = statement.executeQuery("SELECT * FROM `gangwar`");
        while (result.next()) {
            GangwarData gangwarData = new GangwarData();
            gangwarData.setId(result.getInt(1));
            gangwarData.setZone(result.getString(2));
            gangwarData.setOwner(result.getString(3));
            gangwarData.setLastAttack(result.getTimestamp(4));
            gangwarDataMap.put(gangwarData.getZone(), gangwarData);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("info")) {
                player.sendMessage("§7   ===§8[§cGangwar§8]§7===");
                for (GangwarData gangwarData : gangwarDataMap.values()) {
                    FactionData factionData = factionManager.getFactionData(gangwarData.getOwner());
                    player.sendMessage("§8 ➥ §e" + gangwarData.getZone() + " §8| §" + factionData.getPrimaryColor() + factionData.getFullname());
                }
            }
            PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
            if (args[0].equalsIgnoreCase("leave")) {
                if (playerData.getFaction() != null && !Objects.equals(playerData.getFaction(), "Zivilist")) {
                    if (playerData.getVariable("gangwar") != null) {
                        FactionData factionData = factionManager.getFactionData(playerData.getFaction());
                        if (factionData.getCurrent_gangwar() != null) {
                            leaveGangwar(player);
                            player.sendMessage("§8[§cGangwar§8]§7 Du hast den Gangwar verlassen.");
                            factionManager.sendMessageToFaction(playerData.getFaction(), player.getName() + " hat den Gangwar verlassen.");
                        } else {
                            player.sendMessage("§8[§cGangwar§8]§c Deine Fraktion befindet sich aktuell in keinem Gangwar.");
                        }
                    } else {
                    player.sendMessage("§8[§cGangwar§8]§c Du bist in keinem Gangwar.");
                }
                } else {
                    player.sendMessage(Main.error + "Du bist in keienr Fraktion.");
                }
            }
            if (args[0].equalsIgnoreCase("join")) {
                if (playerData.getFaction() != null && !Objects.equals(playerData.getFaction(), "Zivilist")) {
                    if (playerData.getVariable("gangwar") == null) {
                        FactionData factionData = factionManager.getFactionData(playerData.getFaction());
                        if (factionData.getCurrent_gangwar() != null) {
                            joinGangwar(player, factionData.getCurrent_gangwar());
                            GangwarData gangwarData = gangwarDataMap.get(factionData.getCurrent_gangwar());
                            player.sendMessage("§8[§cGangwar§8]§7 Du hast den Gangwar §c" + gangwarData.getZone() + "§7 betreten.");
                            factionManager.sendMessageToFaction(playerData.getFaction(), player.getName() + " ist dem Gangwar beigetreten.");
                        } else {
                            player.sendMessage("§8[§cGangwar§8]§c Deine Fraktion befindet sich aktuell in keinem Gangwar.");
                        }
                    } else {
                        player.sendMessage("§8[§cGangwar§8]§c Du bist bereits im Gangwar.");
                    }
                } else {
                    player.sendMessage(Main.error + "Du bist in keiner Fraktion.");
                }
            }
            if (args[0].equalsIgnoreCase("attack")) {
                if (args.length >= 2) {
                    if (playerData.getFaction() != null && !Objects.equals(playerData.getFaction(), "Zivilist")) {
                        StringBuilder msg = new StringBuilder(args[1]);
                        for (int i = 2; i < args.length; i++) {
                            msg.append(" ").append(args[i]);
                        }
                        if (locationManager.getDistanceBetweenCoords(player, "gangwar_attack_" + msg.toString().replace(" ", "")) < 5) {
                            GangwarData gangwarData = gangwarDataMap.get(msg.toString());
                            Inventory inv = Bukkit.createInventory(player, 27, "§8 » §c" + gangwarData.getZone());
                            inv.setItem(13, ItemManager.createItem(Material.PAPER, 1, 0, "§bInformationen", "Lädt..."));
                            ItemMeta meta = inv.getItem(13).getItemMeta();

                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM | HH:mm");
                            String date = gangwarData.getLastAttack().toLocalDateTime().format(formatter);

                            FactionData ownerData = factionManager.getFactionData(gangwarData.getOwner());
                            meta.setLore(Arrays.asList("§8 ➥ §6Besitzer§8:§e " + ownerData.getFullname(), "§8 ➥ §6Letzter Angriff§8:§e " + date));
                            inv.getItem(13).setItemMeta(meta);

                            inv.setItem(15, ItemManager.createItem(Material.LIME_DYE, 1, 0, "§aAttackieren", null));
                            ItemMeta meta1 = inv.getItem(15).getItemMeta();
                            meta1.getPersistentDataContainer().set(new NamespacedKey(Main.plugin, "zone"), PersistentDataType.STRING, gangwarData.getZone());
                            inv.getItem(15).setItemMeta(meta1);
                            for (int i = 0; i < 27; i++) {
                                if (inv.getItem(i) == null) {
                                    inv.setItem(i, ItemManager.createItem(Material.BLACK_STAINED_GLASS_PANE, 1,0, "§8", null));
                                }
                            }
                            player.openInventory(inv);
                            playerData.setVariable("current_inventory", "gangwar");
                        }
                    } else {
                        player.sendMessage(Main.error + "Du bist in keienr Fraktion.");
                    }
                } else {
                    player.sendMessage(Main.error + "Syntax-Fehler: /gangwar attack [Zone]");
                }
            }
        } else {
            player.sendMessage(Main.error + "Syntax-Fehler: /gangwar [info/leave/join/attack] [(Zone)]");
        }
        return false;
    }

    public void joinGangwar(Player player, String zone) {
        GangwarData gangwarData = gangwarDataMap.get(zone);
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        playerData.setVariable("gangwar", zone);
        if (gangwarData.getAttacker().equals(playerData.getFaction())) {
            locationManager.useLocation(player, "attacker_spawn_" + zone.replace(" ", ""));
        } else {
            locationManager.useLocation(player, "defender_spawn_" + zone.replace(" ", ""));
        }
        equipPlayer(player);
    }

    public static void equipPlayer(Player player) {
        Main.getInstance().weapons.giveWeaponToPlayer(player, Material.DIAMOND_HORSE_ARMOR, "Gangwar");
    }

    public void leaveGangwar(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (playerData.getVariable("gangwar") != null) {
            locationManager.useLocation(player, playerData.getFaction());
            playerData.setVariable("gangwar", null);
            for (ItemStack item : player.getInventory().getContents()) {
                for (WeaponData weaponData : Weapons.weaponDataMap.values()) {
                    if (weaponData.getMaterial() != null && item != null) {
                        if (item.getType() == weaponData.getMaterial()) {
                            ItemMeta meta = item.getItemMeta();
                            if (meta.getPersistentDataContainer().get(new NamespacedKey(Main.plugin, "type"), PersistentDataType.STRING) != null) {
                                if (meta.getPersistentDataContainer().get(new NamespacedKey(Main.plugin, "type"), PersistentDataType.STRING) == "Gangwar") {
                                    player.getInventory().remove(item);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void respawnPlayer(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        GangwarData gangwarData = GangwarUtils.gangwarDataMap.get(playerData.getVariable("gangwar"));
        if (gangwarData.getAttacker().equals(playerData.getFaction())) {
            locationManager.useLocation(player, "attacker_spawn_" + playerData.getVariable("gangwar").toString().replace(" ", ""));
        } else {
            locationManager.useLocation(player, "defender_spawn_" + playerData.getVariable("gangwar").toString().replace(" ", ""));
        }
        for (ItemStack item : player.getInventory().getContents()) {
            for (WeaponData weaponData : Weapons.weaponDataMap.values()) {
                if (weaponData.getMaterial() != null && item != null) {
                    if (item.getType() == weaponData.getMaterial()) {
                        ItemMeta meta = item.getItemMeta();
                        if (meta.getPersistentDataContainer().get(new NamespacedKey(Main.plugin, "type"), PersistentDataType.STRING) != null) {
                            if (meta.getPersistentDataContainer().get(new NamespacedKey(Main.plugin, "type"), PersistentDataType.STRING) == "Gangwar") {
                                player.getInventory().remove(item);
                            }
                        }
                    }
                }
            }
        }
        player.sendMessage("§8[§cGangwar§8]§a Du bist wieder am Leben.");
        equipPlayer(player);
    }

    public void startGangwar(Player player, String zone) {
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if ((LocalDateTime.now().getHour() >= 18 && LocalDateTime.now().getHour() <= 22) || (playerData.isAduty() && playerData.getPermlevel() >= 80)) {
            GangwarData gangwarData = gangwarDataMap.get(zone);
            FactionData factionData = factionManager.getFactionData(playerData.getFaction());
            if (factionData.canDoGangwar()) {
                Timestamp timestamp = Timestamp.valueOf(gangwarData.getLastAttack().toLocalDateTime());
                LocalDateTime currentDateTime = LocalDateTime.now();
                LocalDateTime twoDaysAfterTimestamp = timestamp.toLocalDateTime().plusDays(2);
                boolean isTwoDaysAfter = currentDateTime.isAfter(twoDaysAfterTimestamp);
                if (isTwoDaysAfter || (playerData.isAduty() && playerData.getPermlevel() >= 80)) {
                    if (factionData.getCurrent_gangwar() == null) {
                        if (!factionData.getName().equals(gangwarData.getOwner())) {
                            FactionData defenderData = factionManager.getFactionData(gangwarData.getOwner());
                            if (defenderData.getCurrent_gangwar() == null) {
                                for (Player players : Bukkit.getOnlinePlayers()) {
                                    String playersFaction = playerManager.getPlayerData(players.getUniqueId()).getFaction();
                                    if (playersFaction.equals(gangwarData.getOwner())) {
                                        players.sendMessage("§8[§cGangwar§8]§c Euer Gebiet §l§n" + gangwarData.getZone() + "§c wird von §l" + factionData.getName() + "§c angegriffen!");
                                    }
                                    if (playersFaction.equals(factionData.getName())) {
                                        players.sendMessage("§8[§cGangwar§8]§c Ihr greift das Gebiet §l§n" + gangwarData.getZone() + "§c von §l" + defenderData.getName() + "§c an!");
                                    }
                                }
                                defenderData.setCurrent_gangwar(gangwarData.getZone());
                                factionData.setCurrent_gangwar(gangwarData.getZone());
                                gangwarData.setAttacker(factionData.getName());
                                gangwarData.setAttackerPoints(0);
                                gangwarData.setDefenderPoints(0);
                                gangwarData.setMinutes(25);
                                gangwarData.setSeconds(0);
                                gangwarData.startGangwar();
                                joinGangwar(player, gangwarData.getZone());
                            } else {
                                player.sendMessage(Main.error + "Diese Fraktion ist bereits im Gangwar.");
                            }
                        } else {
                            player.sendMessage(Main.error + "Du kannst dein eigenes Gebiet nicht angreifen.");
                        }
                    } else {
                        player.sendMessage(Main.error + "Deine Fraktion ist bereits im Gangwar.");
                    }
                } else {
                    player.sendMessage(Main.error + "Dieses Gebiet kann noch nicht angegriffen werden.");
                }
            } else {
                player.sendMessage(Main.error + "Deine Fraktion kann kein Gangwar-Gebiet angreifen.");
            }
        } else {
            player.sendMessage(Main.error + "Gangwar ist nur von 18-22 Uhr verfügbar.");
        }
    }

    public void endGangwar(String zone) {
        GangwarData gangwarData = gangwarDataMap.get(zone);
        FactionData attackerData = factionManager.getFactionData(gangwarData.getAttacker());
        FactionData defenderData = factionManager.getFactionData(gangwarData.getOwner());
        gangwarData.setAttacker(null);
        attackerData.setCurrent_gangwar(null);
        defenderData.setCurrent_gangwar(null);
        if (gangwarData.getDefenderPoints() >= gangwarData.getAttackerPoints()) {
            for (Player players : Bukkit.getOnlinePlayers()) {
                String playersFaction = playerManager.getPlayerData(players.getUniqueId()).getFaction();
                if (playersFaction.equals(defenderData.getName())) {
                    players.sendMessage("§8[§cGangwar§8]§a Ihr habt das Gebiet §l§n" + gangwarData.getZone() + "§a gegen §l" + attackerData.getName() + "§a verteitigt!");
                    leaveGangwar(players);
                }
                if (playersFaction.equals(attackerData.getName())) {
                    players.sendMessage("§8[§cGangwar§8]§c Ihr habt den Angriff des Gebietes §l§n" + gangwarData.getZone() + "§c von §l" + defenderData.getName() + "§c verloren!");
                    leaveGangwar(players);
                }
            }
            try {
                Statement statement = Main.getInstance().mySQL.getStatement();
                statement.executeUpdate("UPDATE `gangwar` SET `lastAttack` = NOW() WHERE `zone` = '" + gangwarData.getZone() + "'");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else {
            for (Player players : Bukkit.getOnlinePlayers()) {
                String playersFaction = playerManager.getPlayerData(players.getUniqueId()).getFaction();
                if (playersFaction.equals(defenderData.getName())) {
                    players.sendMessage("§8[§cGangwar§8]§c Ihr habt die Vertetigung des Gebietes §l§n" + gangwarData.getZone() + "§c gegen §l" + attackerData.getName() + "§c verloren!");
                    leaveGangwar(players);
                }
                if (playersFaction.equals(attackerData.getName())) {
                    players.sendMessage("§8[§cGangwar§8]§a Ihr habt den Angriff des Gebietes §l§n" + gangwarData.getZone() + "§a gegen §l" + defenderData.getName() + "§a gewonnen!");
                    leaveGangwar(players);
                }
            }
            gangwarData.setOwner(attackerData.getName());
            try {
                Statement statement = Main.getInstance().mySQL.getStatement();
                statement.executeUpdate("UPDATE `gangwar` SET `lastAttack` = NOW(), `owner` = '" + attackerData.getName() + "' WHERE `zone` = '" + gangwarData.getZone() + "'");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        gangwarData.setLastAttack(new Timestamp(System.currentTimeMillis()));
    }

    @Nullable
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>();
            suggestions.add("info");
            suggestions.add("attack");
            suggestions.add("join");
            suggestions.add("leave");

            return suggestions;
        }
        if (args.length == 2) {
            List<String> suggestions = new ArrayList<>();
            for (GangwarData gangwarData : gangwarDataMap.values()) {
                suggestions.add(gangwarData.getZone());
            }

            return suggestions;
        }
        return null;
    }
}
