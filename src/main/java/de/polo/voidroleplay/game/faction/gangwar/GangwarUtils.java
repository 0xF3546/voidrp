package de.polo.voidroleplay.game.faction.gangwar;

import de.polo.api.faction.gangwar.IGangzone;
import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.FactionData;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.dataStorage.WeaponType;
import de.polo.voidroleplay.manager.FactionManager;
import de.polo.voidroleplay.manager.InventoryManager.CustomItem;
import de.polo.voidroleplay.manager.InventoryManager.InventoryManager;
import de.polo.voidroleplay.manager.ItemManager;
import de.polo.voidroleplay.manager.LocationManager;
import de.polo.voidroleplay.manager.PlayerManager;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.utils.Utils;
import de.polo.voidroleplay.utils.enums.RoleplayItem;
import de.polo.voidroleplay.utils.enums.Weapon;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class GangwarUtils implements CommandExecutor, TabCompleter {
    private final List<Gangwar> gangWars = new ObjectArrayList<>();
    private final List<IGangzone> gangZones = new ObjectArrayList<>();

    private final PlayerManager playerManager;
    private final FactionManager factionManager;
    private final LocationManager locationManager;

    public GangwarUtils(PlayerManager playerManager, FactionManager factionManager, LocationManager locationManager) {
        this.playerManager = playerManager;
        this.factionManager = factionManager;
        this.locationManager = locationManager;
        Main.registerCommand("gangwar", this);
        Main.addTabCompeter("gangwar", this);
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
            IGangzone gangZone = new Gangzone();
            gangZone.setId(result.getInt(1));
            gangZone.setName(result.getString(2));
            gangZone.setOwner(result.getString(3));
            gangZone.setLastAttack(result.getTimestamp(4));
            gangZones.add(gangZone);
        }
    }

    public Collection<Gangwar> getGangwars() {
        return gangWars;
    }

    public Collection<IGangzone> getGangzones() {
        return gangZones;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("info")) {
                player.sendMessage("§7   ===§8[§cGangwar§8]§7===");
                for (IGangzone gangZone : gangZones) {
                    FactionData factionData = factionManager.getFactionData(gangZone.getOwner());
                    player.sendMessage("§8 ➥ §e" + gangZone.getName() + " §8| §" + factionData.getPrimaryColor() + factionData.getFullname());
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
                    player.sendMessage(Main.error + "Du bist in keiner Fraktion.");
                }
            }
            /*if (args[0].equalsIgnoreCase("join")) {
                if (playerData.getFaction() != null && !Objects.equals(playerData.getFaction(), "Zivilist")) {
                    if (playerData.getVariable("gangwar") == null) {
                        FactionData factionData = factionManager.getFactionData(playerData.getFaction());
                        if (factionData.getCurrent_gangwar() != null) {
                            joinGangwar(player, factionData.getCurrent_gangwar());
                            Gangwar gangwarData = getGangwarByZone(factionData.getCurrent_gangwar());
                            player.sendMessage("§8[§cGangwar§8]§7 Du hast den Gangwar §c" + gangwarData.getGangZone().getName() + "§7 betreten.");
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
            }*/
            if (args[0].equalsIgnoreCase("attack")) {
                if (args.length >= 2) {
                    if (playerData.getFaction() != null && !Objects.equals(playerData.getFaction(), "Zivilist")) {
                        StringBuilder msg = new StringBuilder(args[1]);
                        for (int i = 2; i < args.length; i++) {
                            msg.append(" ").append(args[i]);
                        }
                        if (locationManager.getDistanceBetweenCoords(player, "gangwar_attack_" + msg.toString().replace(" ", "")) < 5) {
                            IGangzone gangwarData = getGangzoneByName(msg.toString());
                            InventoryManager inventoryManager = new InventoryManager(player, 27, "§8 » §c" + gangwarData.getName(), true, true);

                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM | HH:mm");
                            String date = gangwarData.getLastAttack().toLocalDateTime().format(formatter);

                            FactionData ownerData = factionManager.getFactionData(gangwarData.getOwner());
                            inventoryManager.setItem(new CustomItem(13, ItemManager.createItem(Material.PAPER, 1, 0, "§bInformationen", Arrays.asList("§8 ➥ §6Besitzer§8:§e " + ownerData.getFullname(), "§8 ➥ §6Letzter Angriff§8:§e " + date))) {
                                @Override
                                public void onClick(InventoryClickEvent event) {

                                }
                            });
                            if (playerData.getFaction() == null) {
                                inventoryManager.setItem(new CustomItem(15, ItemManager.createItem(Material.LIME_DYE, 1, 0, "§a§mAttackieren", "§8 ➥ §7Du bist in keiner Fraktion.")) {
                                    @Override
                                    public void onClick(InventoryClickEvent event) {
                                    }
                                });
                                return false;
                            }
                            FactionData factionData = factionManager.getFactionData(playerData.getFaction());
                            if (factionData.canDoGangwar()) {
                                inventoryManager.setItem(new CustomItem(15, ItemManager.createItem(Material.LIME_DYE, 1, 0, "§aAttackieren")) {
                                    @Override
                                    public void onClick(InventoryClickEvent event) {
                                        startGangwar(player, gangwarData.getName());
                                        player.closeInventory();
                                    }
                                });
                            } else {
                                inventoryManager.setItem(new CustomItem(15, ItemManager.createItem(Material.LIME_DYE, 1, 0, "§a§mAttackieren", "§8 ➥ §7Deine Fraktion kann keinen Gangwar starten.")) {
                                    @Override
                                    public void onClick(InventoryClickEvent event) {
                                    }
                                });
                            }
                        }
                    } else {
                        player.sendMessage(Main.error + "Du bist in keiner Fraktion.");
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
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (playerData.isDead()) {
            player.sendMessage(Main.error + "Du kannst aktuell keinem Gangwar beitreten.");
            return;
        }
        Gangwar gangwarData = getGangwarByZone(zone);
        playerData.setVariable("inventory::gangwar", player.getInventory().getContents());
        // playerData.setVariable("inventory::gangwar", InventoryUtils.serializeInventory(player.getInventory()));
        FactionData factionData = factionManager.getFactionData(playerData.getFaction());
        BossBar bossBar = Bukkit.createBossBar("Lade Gangwar...", BarColor.RED, BarStyle.SOLID);
        playerData.setBossBar("gangwar", bossBar);
        player.getInventory().clear();
        playerData.setVariable("gangwar", zone);
        if (gangwarData.getAttacker().equals(playerData.getFaction())) {
            locationManager.useLocation(player, "attacker_spawn_" + zone.replace(" ", ""));
        } else {
            locationManager.useLocation(player, "defender_spawn_" + zone.replace(" ", ""));
        }
        equipPlayer(player);
    }

    public void equipPlayer(Player player) {
        Main.getInstance().weaponManager.giveWeapon(player, Weapon.ASSAULT_RIFLE, WeaponType.GANGWAR, 500);
        player.getInventory().addItem(ItemManager.createItem(RoleplayItem.SNUFF.getMaterial(), 5, 0, RoleplayItem.SNUFF.getDisplayName()));
        player.getInventory().addItem(ItemManager.createItem(RoleplayItem.CIGAR.getMaterial(), 5, 0, RoleplayItem.CIGAR.getDisplayName()));
        player.getInventory().addItem(ItemManager.createItem(Material.COOKED_BEEF, 16, 0, "§6Nahrung"));
        player.getInventory().addItem(ItemManager.createItem(RoleplayItem.SMARTPHONE.getMaterial(), 1, 0, RoleplayItem.SMARTPHONE.getDisplayName()));
    }

    public void leaveGangwar(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (playerData.getFaction() == null) return;
        playerData.removeBossBar("gangwar");
        Main.getInstance().utils.deathUtil.revivePlayer(player, false);
        if (playerData.getVariable("gangwar") != null) {
            locationManager.useLocation(player, playerData.getFaction());
            playerData.setVariable("gangwar", null);
            for (ItemStack item : player.getInventory().getContents()) {
                for (Weapon weaponData : Weapon.values()) {
                    if (weaponData.getMaterial() != null && item != null) {
                        if (item.getType() == weaponData.getMaterial()) {
                            ItemMeta meta = item.getItemMeta();
                            de.polo.voidroleplay.dataStorage.Weapon weapon = Main.getInstance().weaponManager.getWeaponFromItemStack(item);
                            if (weapon.getWeaponType() == WeaponType.GANGWAR) {
                                Main.getInstance().weaponManager.removeWeapon(player, item);
                            }
                        }
                    }
                }
            }
            player.getInventory().clear();
            player.getInventory().setContents(playerData.getVariable("inventory::gangwar"));
            //Inventory inventory = InventoryUtils.deserializeInventory(playerData.getVariable("inventory::gangwar"));
            //if (inventory == null) return;
            //player.getInventory().setContents(inventory.getContents());
        }
    }

    public void respawnPlayer(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        Gangwar gangwarData = getGangwarByZone(playerData.getVariable("gangwar"));
        if (gangwarData.getAttacker().equals(playerData.getFaction())) {
            locationManager.useLocation(player, "attacker_spawn_" + playerData.getVariable("gangwar").toString().replace(" ", ""));
        } else {
            locationManager.useLocation(player, "defender_spawn_" + playerData.getVariable("gangwar").toString().replace(" ", ""));
        }
        for (ItemStack item : player.getInventory().getContents()) {
            for (Weapon weaponData : Weapon.values()) {
                if (weaponData.getMaterial() != null && item != null) {
                    if (item.getType() == weaponData.getMaterial()) {
                        ItemMeta meta = item.getItemMeta();
                        de.polo.voidroleplay.dataStorage.Weapon weapon = Main.getInstance().weaponManager.getWeaponFromItemStack(item);
                        if (weapon.getWeaponType() == WeaponType.GANGWAR) {
                            Main.getInstance().weaponManager.removeWeapon(player, item);
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
        if (playerData.getFaction() == null) {
            player.sendMessage(Main.error_nopermission);
            return;
        }
        int i = 0;
        for (IGangzone gangzone : gangZones) {
            if (gangzone.getOwner().equalsIgnoreCase(playerData.getFaction())) i++;
        }
        if (i >= 3) {
            player.sendMessage(Prefix.ERROR + "Deine Fraktion kann maximal 3 Gebiete besitzen!");
            return;
        }
        if (!Utils.getTime().getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
            player.sendMessage(Prefix.ERROR + "Gangwar kann an Sonntagen bespielt werden!");
        }
        if ((Utils.getTime().getHour() >= 18 && Utils.getTime().getHour() < 22 || (playerData.isAduty() && playerData.getPermlevel() >= 80))) {
            IGangzone gangzone = getGangzoneByName(zone);
            FactionData factionData = factionManager.getFactionData(playerData.getFaction());
            if (!factionData.canDoGangwar()) {
                player.sendMessage(Main.error + "Deine Fraktion kann kein Gangwar-Gebiet angreifen.");
                return;
            }
            Timestamp timestamp = Timestamp.valueOf(gangzone.getLastAttack().toLocalDateTime());
            LocalDateTime currentDateTime = LocalDateTime.now();
            LocalDateTime twoDaysAfterTimestamp = timestamp.toLocalDateTime().plusHours(40);
            boolean isTwoDaysAfter = currentDateTime.isAfter(twoDaysAfterTimestamp);
            if (!isTwoDaysAfter || (playerData.isAduty() && playerData.getPermlevel() < 80)) {
                player.sendMessage(Main.error + "Dieses Gebiet kann noch nicht angegriffen werden.");
                return;
            }
            if (factionData.getCurrent_gangwar() != null) {
                player.sendMessage(Main.error + "Deine Fraktion ist bereits im Gangwar.");
                return;
            }
            if (factionData.getName().equals(gangzone.getOwner())) {
                player.sendMessage(Main.error + "Du kannst dein eigenes Gebiet nicht angreifen.");
                return;
            }
            FactionData defenderData = factionManager.getFactionData(gangzone.getOwner());
            if (defenderData.getCurrent_gangwar() != null) {
                player.sendMessage(Main.error + "Diese Fraktion ist bereits im Gangwar.");
                return;
            }
            player.closeInventory();
            for (Player players : Bukkit.getOnlinePlayers()) {
                String playersFaction = playerManager.getPlayerData(players.getUniqueId()).getFaction();
                if (playersFaction == null) continue;
                if (playersFaction.equals(gangzone.getOwner())) {
                    players.sendMessage("§8[§cGangwar§8]§c Euer Gebiet §l§n" + gangzone.getName() + "§c wird von §l" + factionData.getName() + "§c angegriffen!");
                    continue;
                }
                if (playersFaction.equals(factionData.getName())) {
                    players.sendMessage("§8[§cGangwar§8]§c Ihr greift das Gebiet §l§n" + gangzone.getName() + "§c von §l" + defenderData.getName() + "§c an!");
                }
            }
            defenderData.setCurrent_gangwar(gangzone.getName());
            factionData.setCurrent_gangwar(gangzone.getName());
            Gangwar gangwar = new Gangwar();
            gangwar.setGangZone(gangzone);
            gangwar.setDefender(defenderData.getName());
            gangwar.setAttacker(factionData.getName());
            gangwar.setAttackerPoints(0);
            gangwar.setDefenderPoints(0);
            gangwar.setMinutes(25);
            gangwar.setSeconds(0);
            gangwar.start();
            gangWars.add(gangwar);
            joinGangwar(player, gangzone.getName());
        } else {
            player.sendMessage(Main.error + "Gangwar ist nur Dienstag & Donnerstag von 18-22 Uhr verfügbar.");
        }
    }

    public Gangwar getGangwarByZone(String zone) {
        for (Gangwar gangwar : gangWars) {
            if (gangwar.getGangZone().getName().equalsIgnoreCase(zone)) return gangwar;
        }
        return null;
    }

    public IGangzone getGangzoneByName(String name) {
        for (IGangzone gangzone : gangZones) {
            if (gangzone.getName().equalsIgnoreCase(name)) return gangzone;
        }
        return null;
    }

    public synchronized void endGangwar(String zone) {
        Gangwar gangwarData = getGangwarByZone(zone);
        FactionData attackerData = factionManager.getFactionData(gangwarData.getAttacker());
        FactionData defenderData = factionManager.getFactionData(gangwarData.getGangZone().getOwner());
        if (gangwarData.getDefenderPoints() >= gangwarData.getAttackerPoints()) {
            for (Player players : Bukkit.getOnlinePlayers()) {
                String playersFaction = playerManager.getPlayerData(players.getUniqueId()).getFaction();
                PlayerData playersData = playerManager.getPlayerData(players);
                if (playersFaction != null) {
                    if (playersFaction.equals(defenderData.getName())) {
                        players.sendMessage("§8[§cGangwar§8]§a Ihr habt das Gebiet §l§n" + gangwarData.getGangZone().getName() + "§a gegen §l" + attackerData.getName() + "§a verteitigt!");
                        leaveGangwar(players);
                    }
                    if (playersFaction.equals(attackerData.getName())) {
                        players.sendMessage("§8[§cGangwar§8]§c Ihr habt den Angriff des Gebietes §l§n" + gangwarData.getGangZone().getName() + "§c von §l" + defenderData.getName() + "§c verloren!");
                        leaveGangwar(players);
                    }
                }
            }
            try {
                Connection connection = Main.getInstance().mySQL.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement("UPDATE gangwar SET lastAttack = NOW() WHERE zone = ?");
                preparedStatement.setString(1, gangwarData.getGangZone().getName());
                preparedStatement.execute();
                preparedStatement.close();
                connection.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else {
            for (Player players : Bukkit.getOnlinePlayers()) {
                String playersFaction = playerManager.getPlayerData(players.getUniqueId()).getFaction();
                if (playersFaction != null) {
                    if (playersFaction.equals(defenderData.getName())) {
                        players.sendMessage("§8[§cGangwar§8]§c Ihr habt die Vertetigung des Gebietes §l§n" + gangwarData.getGangZone().getName() + "§c gegen §l" + attackerData.getName() + "§c verloren!");
                        leaveGangwar(players);
                    }
                    if (playersFaction.equals(attackerData.getName())) {
                        players.sendMessage("§8[§cGangwar§8]§a Ihr habt den Angriff des Gebietes §l§n" + gangwarData.getGangZone().getName() + "§a gegen §l" + defenderData.getName() + "§a gewonnen!");
                        Main.getInstance().seasonpass.didQuest(players, 19);
                        leaveGangwar(players);
                    }
                }
            }
            gangwarData.getGangZone().setOwner(attackerData.getName());
            Main.getInstance().blockManager.updateBlocksAtScenario("gangwar-" + gangwarData.getGangZone().getName(), attackerData);
            try {
                Connection connection = Main.getInstance().mySQL.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement("UPDATE gangwar SET lastAttack = NOW(), owner = ? WHERE zone = ?");
                preparedStatement.setString(1, attackerData.getName());
                preparedStatement.setString(2, gangwarData.getGangZone().getName());
                preparedStatement.execute();
                preparedStatement.close();
                connection.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        gangwarData.setAttacker(null);
        attackerData.setCurrent_gangwar(null);
        defenderData.setCurrent_gangwar(null);
        gangwarData.getGangZone().setLastAttack(new Timestamp(System.currentTimeMillis()));
        gangWars.remove(gangwarData);
    }

    @Nullable
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> suggestions = new ObjectArrayList<>();
            suggestions.add("info");
            suggestions.add("attack");
            //suggestions.add("join");
            suggestions.add("leave");

            return suggestions;
        }
        if (args.length == 2) {
            List<String> suggestions = new ObjectArrayList<>();
            for (IGangzone gangZone : gangZones) {
                suggestions.add(gangZone.getName());
            }

            return suggestions;
        }
        return null;
    }

}
