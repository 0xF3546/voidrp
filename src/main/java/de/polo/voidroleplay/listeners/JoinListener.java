package de.polo.voidroleplay.listeners;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.FactionData;
import de.polo.voidroleplay.dataStorage.RankData;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.game.base.vehicle.Vehicles;
import de.polo.voidroleplay.game.faction.plants.Plant;
import de.polo.voidroleplay.utils.*;
import de.polo.voidroleplay.utils.Interfaces.PlayerJoin;
import de.polo.voidroleplay.utils.InventoryManager.CustomItem;
import de.polo.voidroleplay.utils.InventoryManager.InventoryManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.UUID;

public class JoinListener implements Listener {
    private final PlayerManager playerManager;
    private final AdminManager adminManager;
    private final Utils utils;
    private final LocationManager locationManager;
    private final ServerManager serverManager;
    private PlayerJoin playerJoin;

    public JoinListener(PlayerManager playerManager, AdminManager adminManager, Utils utils, LocationManager locationManager, ServerManager serverManager) {
        this.playerManager = playerManager;
        this.adminManager = adminManager;
        this.utils = utils;
        this.locationManager = locationManager;
        this.serverManager = serverManager;
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        event.setJoinMessage("");
        player.setGameMode(GameMode.SURVIVAL);
        if (playerManager.isCreated(player.getUniqueId())) {
            PlayerData playerData = playerManager.loadPlayer(player).join();
            if (playerData.getVariable("tpNewmap")) {
                Main.getInstance().locationManager.useLocation(player, "stadthalle");
                player.sendMessage("§8 ✈ §aWillkommen auf der neuen Map!");
                try {
                    Connection connection = Main.getInstance().mySQL.getConnection();
                    PreparedStatement ps = connection.prepareStatement("UPDATE players SET tpNewmap = true WHERE uuid = ?");
                    ps.setString(1, uuid.toString());
                    ps.execute();
                    ps.close();
                    connection.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
            if (playerData == null) {
                player.kick(Component.text("§cWir mussten deine Verbindung trennen, da deine Spielerdaten nicht geladen werden konnten."));
                return;
            }
            System.out.println(playerData);
            System.out.println(playerData.getPlayer().getName());
            System.out.println(playerData.getFirstname());
            adminManager.send_message(player.getName() + " hat den Server betreten.", ChatColor.GRAY);
            player.sendMessage("§6Willkommen zurück, " + player.getName() + "!");
            if (playerData.getFaction() != null) {
                FactionData factionData = Main.getInstance().factionManager.getFactionData(playerData.getFaction());
                player.sendMessage("§8 ➥ §6[FMOTD] " + factionData.getMotd());
            }
            RankData rankData = ServerManager.rankDataMap.get(playerData.getRang());
            Utils.Tablist.setTablist(player, null);
            playerData.setUuid(player.getUniqueId());
            if (playerData.getPermlevel() >= 40) {
                player.sendMessage("§8 ➥ §cEs sind " + Main.getInstance().supportManager.getTickets().size() + " Tickets offen.");
                int teamCount = 0;
                int deathCount = 0;
                for (Player player1 : Bukkit.getOnlinePlayers()) {
                    PlayerData playerData1 = playerManager.getPlayerData(player1.getUniqueId());
                    if (playerData1.getPermlevel() >= 40) {
                        teamCount++;
                    }
                    if (playerData1.isDead()) {
                        deathCount++;
                    }
                }
            }
            Vehicles.spawnPlayerVehicles(player);
            serverManager.updateTablist(null);

            if (playerData.getVariable("jugendschutz") != null) {
                Main.waitSeconds(1, () -> {
                    InventoryManager inventory = new InventoryManager(player, 27, "§c§lJugendschutz", true, false);
                    playerData.setVariable("originClass", this);
                    inventory.setItem(new CustomItem(11, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTkyZTMxZmZiNTljOTBhYjA4ZmM5ZGMxZmUyNjgwMjAzNWEzYTQ3YzQyZmVlNjM0MjNiY2RiNDI2MmVjYjliNiJ9fX0=", 1, 0, "§a§lIch bestäige", Arrays.asList("§VoidRoleplay simuliert das §fechte Leben§7, weshalb mit §7Gewalt§7,", " §fSexualität§7, §fvulgärer Sprache§7, §fDrogen§7", "§7 und §fAlkohol§7 gerechnet werden muss.", "\n", "§7Bitte bestätige, dass du mindestens §e18 Jahre§7", "§7 alt bist oder die §aErlaubnis§7 eines §fErziehungsberechtigten§7 hast.", "§7Das VoidRoleplay Team behält sich vor", "§7 diesen Umstand ggf. unangekündigt zu prüfen", "\n", "§8 ➥ §7[§6Klick§7]§7 §a§lIch bin 18 Jahre alt oder", "§a§l habe die Erlaubnis meiner Eltern"))) {
                        @Override
                        public void onClick(InventoryClickEvent event) {
                            playerData.setVariable("jugendschutz", null);
                            player.closeInventory();
                            player.sendMessage("§8[§c§lJugendschutz§8]§a Du hast den Jugendschutz aktzeptiert.");
                            Statement statement = null;
                            try {
                                statement = Main.getInstance().mySQL.getStatement();
                                statement.executeUpdate("UPDATE `players` SET `jugendschutz` = true, `jugendschutz_accepted` = NOW() WHERE `uuid` = '" + player.getUniqueId() + "'");
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                            player.closeInventory();
                        }
                    });
                    inventory.setItem(new CustomItem(15, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmViNTg4YjIxYTZmOThhZDFmZjRlMDg1YzU1MmRjYjA1MGVmYzljYWI0MjdmNDYwNDhmMThmYzgwMzQ3NWY3In19fQ==", 1, 0, "§c§lIch bestätige nicht", Arrays.asList("§7Klicke hier, wenn du keine 18 Jahre alt bist", "§7 und nicht die §fZustimmung§7 eines §fErziehungsberechtigten§7", "§7hast, derartige Spiele zu Spielen", "\n", "§8 ➥ §7[§6Klick§7]§c§l Ich bin keine 18 Jahre alt", "§c§l und habe keine Erlaubnis meiner Eltern"))) {
                        @Override
                        public void onClick(InventoryClickEvent event) {
                            player.closeInventory();
                            player.kickPlayer("§cDa du den Jugendschutz nicht aktzeptieren konntest, kannst du auf dem Server §lnicht§c Spielen.\n§cBitte deine Erziehungsberechtigten um Erlabunis oder warte bis du 18 bist.");
                        }
                    });
                    for (int i = 0; i < 27; i++) {
                        if (i != 15 && i != 11) {
                            inventory.setItem(new CustomItem(i, ItemManager.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, 0, "§8")) {
                                @Override
                                public void onClick(InventoryClickEvent event) {

                                }
                            });
                        }
                    }
                });
            }

        } else {
            player.sendMessage(" ");
            player.sendMessage("§6VoidRoleplay §8»§7 Herzlich Wilkommen auf VoidRoleplay, " + player.getName() + ".");
            player.sendMessage(" ");
            locationManager.useLocation(player, "Spawn");
            adminManager.send_message("§c" + player.getName() + "§7 hat sich gerade registriert.", ChatColor.GREEN);
        }
        if (player.getGameMode() == GameMode.CREATIVE) {
            Utils.Tablist.setTablist(player, "§8[§2GM§8]");
        } else {
            Utils.Tablist.setTablist(player, null);
        }
    }
}
