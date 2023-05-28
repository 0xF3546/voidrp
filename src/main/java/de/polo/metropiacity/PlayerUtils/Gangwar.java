package de.polo.metropiacity.PlayerUtils;

import de.polo.metropiacity.DataStorage.FactionData;
import de.polo.metropiacity.DataStorage.GangwarData;
import de.polo.metropiacity.DataStorage.PlayerData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.MySQl.MySQL;
import de.polo.metropiacity.Utils.FactionManager;
import de.polo.metropiacity.Utils.ItemManager;
import de.polo.metropiacity.Utils.LocationManager;
import de.polo.metropiacity.Utils.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
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

public class Gangwar implements CommandExecutor, TabCompleter {
    public static HashMap<String, GangwarData> gangwarDataMap = new HashMap<>();

    public static void loadGangwar() throws SQLException {
        Statement statement = MySQL.getStatement();
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
                    FactionData factionData = FactionManager.factionDataMap.get(gangwarData.getOwner());
                    player.sendMessage("§8 ➥ §e" + gangwarData.getZone() + " §8| §" + factionData.getPrimaryColor() + factionData.getName());
                }
            }
            PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
            if (args[0].equalsIgnoreCase("leave")) {
                if (playerData.getFaction() != null && !Objects.equals(playerData.getFaction(), "Zivilist")) {
                    FactionData factionData = FactionManager.factionDataMap.get(playerData.getFaction());
                    if (factionData.getCurrent_gangwar() != null) {
                        leaveGangwar(player);
                        player.sendMessage("§8[§cGangwar§8]§7 Du hast den Gangwar verlassen.");
                    }
                } else {
                    player.sendMessage(Main.error + "Du bist in keienr Fraktion.");
                }
            }
            if (args[0].equalsIgnoreCase("join")) {
                if (playerData.getFaction() != null && !Objects.equals(playerData.getFaction(), "Zivilist")) {
                    FactionData factionData = FactionManager.factionDataMap.get(playerData.getFaction());
                    if (factionData.getCurrent_gangwar() != null) {
                        joinGangwar(player, factionData.getCurrent_gangwar());
                        GangwarData gangwarData = gangwarDataMap.get(factionData.getCurrent_gangwar());
                        player.sendMessage("§8[§cGangwar§8]§7 Du hast den Gangwar §c" + gangwarData.getZone() + "§7 betreten.");
                    }
                } else {
                    player.sendMessage(Main.error + "Du bist in keienr Fraktion.");
                }
            }
            if (args[0].equalsIgnoreCase("attack")) {
                if (args.length >= 2) {
                    if (playerData.getFaction() != null && !Objects.equals(playerData.getFaction(), "Zivilist")) {
                        if (LocationManager.getDistanceBetweenCoords(player, "gangwar_attack_" + args[1].replace(" ", "")) < 5) {
                            GangwarData gangwarData = gangwarDataMap.get(args[1].replace(" ", ""));
                            Inventory inv = Bukkit.createInventory(player, 27, "§8 » §c" + gangwarData.getZone());
                            inv.setItem(13, ItemManager.createItem(Material.PAPER, 1, 0, "§bInformationen", "Lädt..."));
                            ItemMeta meta = inv.getItem(13).getItemMeta();

                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM | HH:mm");
                            String date = gangwarData.getLastAttack().toLocalDateTime().format(formatter);

                            meta.setLore(Arrays.asList("§8 ➥ §6Besitzer§8:§e " + gangwarData.getOwner(), "§8 ➥ §6Letzter Angriff§8:§e " + date));
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

    public static void joinGangwar(Player player, String zone) {
        GangwarData gangwarData = gangwarDataMap.get(zone);
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        playerData.setVariable("gangwar", zone);
        if (gangwarData.getAttacker().equals(playerData.getFaction())) {
            LocationManager.useLocation(player, "attacker_spawn_" + zone);
        } else {
            LocationManager.useLocation(player, "defender_spawn_" + zone);
        }
    }

    public static void leaveGangwar(Player player) {
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        if (playerData.getVariable("gangwar") != null) {
            LocationManager.useLocation(player, "gangwar_out_" + playerData.getFaction());
        }
    }

    public static void startGangwar(Player player, String zone) {
        if (LocalDateTime.now().getHour() >= 18 && LocalDateTime.now().getHour() <= 22) {
            GangwarData gangwarData = gangwarDataMap.get(zone);
            FactionData factionData = FactionManager.factionDataMap.get(player.getUniqueId().toString());
            if (factionData.canDoGangwar()) {
                Timestamp timestamp = Timestamp.valueOf(gangwarData.getLastAttack().toLocalDateTime());
                LocalDateTime currentDateTime = LocalDateTime.now();
                LocalDateTime twoDaysAfterTimestamp = timestamp.toLocalDateTime().plusDays(2);
                boolean isTwoDaysAfter = currentDateTime.isAfter(twoDaysAfterTimestamp);
                if (isTwoDaysAfter) {
                    if (factionData.getCurrent_gangwar() == null) {
                        if (!factionData.getName().equals(gangwarData.getOwner())) {
                            FactionData defenderData = FactionManager.factionDataMap.get(gangwarData.getZone());
                            if (defenderData.getCurrent_gangwar() == null) {
                                for (Player players : Bukkit.getOnlinePlayers()) {
                                    String playersFaction = PlayerManager.playerDataMap.get(players.getUniqueId().toString()).getFaction();
                                    if (playersFaction.equals(gangwarData.getOwner())) {
                                        players.sendMessage("§8[§cGangwar§8]§c Euer Gebiet §l§n" + gangwarData.getZone() + "§c wird von §l" + factionData.getName() + "§c angegriffen!");
                                    }
                                    if (playersFaction.equals(factionData.getName())) {
                                        players.sendMessage("§8[§cGangwar§8]§c Ihr greift das Gebiet Gebiet §l§n" + gangwarData.getZone() + "§c von §l" + defenderData.getName() + "§c an!");
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

    public static void endGangwar(String zone) {
        GangwarData gangwarData = gangwarDataMap.get(zone);
        FactionData attackerData = FactionManager.factionDataMap.get(gangwarData.getAttacker());
        FactionData defenderData = FactionManager.factionDataMap.get(gangwarData.getOwner());
        if (gangwarData.getDefenderPoints() >= gangwarData.getAttackerPoints()) {
            for (Player players : Bukkit.getOnlinePlayers()) {
                String playersFaction = PlayerManager.playerDataMap.get(players.getUniqueId().toString()).getFaction();
                if (playersFaction.equals(defenderData.getName())) {
                    players.sendMessage("§8[§cGangwar§8]§a Ihr habt das Gebiet §l§n" + gangwarData.getZone() + "§a gegen §l" + attackerData.getName() + "§a verteitigt!");
                }
                if (playersFaction.equals(attackerData.getName())) {
                    players.sendMessage("§8[§cGangwar§8]§c Ihr habt den Angriff des Gebietes §l§n" + gangwarData.getZone() + "§c von §l" + defenderData.getName() + "§c verloren!");
                }
            }
            try {
                Statement statement = MySQL.getStatement();
                statement.executeUpdate("UPDATE `gangwar` SET `lastAttack` = NOW() WHERE `zone` = '" + gangwarData.getZone() + "'");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else {
            for (Player players : Bukkit.getOnlinePlayers()) {
                String playersFaction = PlayerManager.playerDataMap.get(players.getUniqueId().toString()).getFaction();
                if (playersFaction.equals(defenderData.getName())) {
                    players.sendMessage("§8[§cGangwar§8]§c Ihr habt die vertetigung des Gebietes §l§n" + gangwarData.getZone() + "§c gegen §l" + attackerData.getName() + "§c verloren!");
                }
                if (playersFaction.equals(attackerData.getName())) {
                    players.sendMessage("§8[§cGangwar§8]§a Ihr habt den Angriff des Gebietes §l§n" + gangwarData.getZone() + "§a von §l" + defenderData.getName() + "§a gewonnen!");
                }
            }
            gangwarData.setOwner(attackerData.getName());
            try {
                Statement statement = MySQL.getStatement();
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
