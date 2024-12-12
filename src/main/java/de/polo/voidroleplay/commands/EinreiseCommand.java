package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.manager.inventory.CustomItem;
import de.polo.voidroleplay.manager.inventory.InventoryManager;
import de.polo.voidroleplay.manager.ItemManager;
import de.polo.voidroleplay.manager.LocationManager;
import de.polo.voidroleplay.manager.PlayerManager;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.utils.enums.Gender;
import lombok.SneakyThrows;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;

public class EinreiseCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final LocationManager locationManager;

    public EinreiseCommand(PlayerManager playerManager, LocationManager locationManager) {
        this.playerManager = playerManager;
        this.locationManager = locationManager;
        Main.registerCommand("einreise", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (playerData.getFirstname() == null || playerData.getLastname() == null) {
            LocalDate date = LocalDate.of(2000, 1, 1);
            if (playerData.getVariable("einreise_firstname") == null)
                playerData.setVariable("einreise_firstname", "Vorname");
            if (playerData.getVariable("einreise_lastname") == null)
                playerData.setVariable("einreise_lastname", "Nachname");
            if (playerData.getVariable("einreise_gender") == null)
                playerData.setVariable("einreise_gender", Gender.MALE);
            if (playerData.getVariable("einreise_dob") == null) playerData.setVariable("einreise_dob", date);
            openEinrese(player);
        } else {
            player.sendMessage(Prefix.ERROR + "Du hast bereits deine Papiere erhalten.");
        }
        return false;
    }

    public void openEinrese(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (locationManager.getDistanceBetweenCoords(player, "einreise") > 10) {
            player.sendMessage(Prefix.ERROR + "Du bist nicht in der nähe der Einreise.");
            return;
        }
        InventoryManager inventoryManager = new InventoryManager(player, 9, "§8 » §6Void Roleplay Einreiseamt", true, true);
        inventoryManager.setItem(new CustomItem(1, ItemManager.createItem(Material.PAPER, 1, 0, "§e" + playerData.getVariable("einreise_firstname"))) {
            @Override
            public void onClick(InventoryClickEvent event) {
                playerData.setVariable("chatblock", "firstname");
                player.sendMessage(Prefix.MAIN + "Gib nun deinen §6Vornamen§7 in den §6Chat§7 ein!");
                player.closeInventory();
            }
        });
        inventoryManager.setItem(new CustomItem(2, ItemManager.createItem(Material.PAPER, 1, 0, "§e" + playerData.getVariable("einreise_lastname"))) {
            @Override
            public void onClick(InventoryClickEvent event) {
                playerData.setVariable("chatblock", "lastname");
                player.sendMessage(Prefix.MAIN + "Gib nun deinen §6Nachnamen§7 in den §6Chat§7 ein!");
                player.closeInventory();
            }
        });
        LocalDate einreiseDob = playerData.getVariable("einreise_dob");
        Date einreiseDate = Date.from(einreiseDob.atStartOfDay(ZoneId.systemDefault()).toInstant());
        inventoryManager.setItem(new CustomItem(4, ItemManager.createItem(Material.PAPER, 1, 0, "§e" + playerData.getVariable("einreise_gender"), Arrays.asList("§7 ➥ §8[§6Linksklick§8]§7 Männlich", "§7 ➥ §8[§6Rechtsklick§8]§7 Weiblich"))) {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (event.getClick().isLeftClick()) {
                    playerData.setVariable("einreise_gender", Gender.MALE);
                    openEinrese(player);
                } else if (event.getClick().isRightClick()) {
                    playerData.setVariable("einreise_gender", Gender.FEMALE);
                    openEinrese(player);
                }
            }
        });
        inventoryManager.setItem(new CustomItem(5, ItemManager.createItem(Material.PAPER, 1, 0, "§e" + playerData.getVariable("einreise_dob"))) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openDOBChanger(player);
            }
        });
        inventoryManager.setItem(new CustomItem(8, ItemManager.createItem(Material.EMERALD, 1, 0, "§2Bestätigen")) {
            @SneakyThrows
            @Override
            public void onClick(InventoryClickEvent event) {
                if (playerData.getVariable("einreise_firstname").toString().equalsIgnoreCase("Vorname")) {
                    player.sendMessage(Prefix.ERROR + "Die Eingaben sind Ungültig (Vorname).");
                    return;
                }
                if (playerData.getVariable("einreise_lastname").toString().equalsIgnoreCase("Nachname")) {
                    player.sendMessage(Prefix.ERROR + "Die Eingaben sind Ungültig (Nachname).");
                    return;
                }
                player.closeInventory();
                playerData.setFirstname(playerData.getVariable("einreise_firstname"));
                playerData.setLastname(playerData.getVariable("einreise_lastname"));
                playerData.setGender(playerData.getVariable("einreise_gender"));
                Connection connection = Main.getInstance().mySQL.getConnection();
                PreparedStatement statement = connection.prepareStatement("UPDATE `players` SET `firstname` = ?, `lastname` = ?, `birthday` = ?, `gender` = ? WHERE `uuid` = ?");
                statement.setString(1, playerData.getFirstname());
                statement.setString(2, playerData.getLastname());
                LocalDate einreiseDob = playerData.getVariable("einreise_dob");
                Instant instant = einreiseDob.atStartOfDay(ZoneId.systemDefault()).toInstant();
                playerData.setBirthday(Date.from(instant));
                java.util.Date utilDate = playerData.getBirthday();
                java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());
                statement.setDate(3, sqlDate);
                statement.setString(4, playerData.getGender().name());
                statement.setString(5, player.getUniqueId().toString());
                statement.executeUpdate();

                player.sendMessage(Prefix.MAIN + "Du bist nun §6Staatsbürger§7, nutze §l/perso§7 um dir deinen Personalausweis anzuschauen!");
                playerManager.addExp(player, Main.random(100, 200));
                Main.getInstance().utils.tutorial.createdAusweis(player);
                player.playSound(player.getLocation(), Sound.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, 1, 0);
            }
        });
    }

    private void openDOBChanger(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player);
        InventoryManager inventoryManager = new InventoryManager(player, 9, "§8 » §6Void Roleplay Einreiseamt", true, true);
        LocalDate date = playerData.getVariable("einreise_dob");
        inventoryManager.setItem(new CustomItem(0, ItemManager.createItem(Material.NETHER_WART, 1, 0, "§cZurück")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openEinrese(player);
            }
        });
        inventoryManager.setItem(new CustomItem(2, ItemManager.createItem(Material.STONE_BUTTON, 1, 0, "§e" + date.getDayOfMonth(), Arrays.asList("§8 ➥ §8[§6Linksklick§8]§7 +1 Tag", "§8 ➥ §8[§6Rechtsklick§8]§7 -1 Tag"))) {
            @Override
            public void onClick(InventoryClickEvent event) {
                LocalDate newDate;
                if (event.isLeftClick()) {
                    newDate = date.plusDays(1);
                } else {
                    newDate = date.minusDays(1);
                }
                playerData.setVariable("einreise_dob", newDate);
                openDOBChanger(player);
            }
        });
        inventoryManager.setItem(new CustomItem(4, ItemManager.createItem(Material.STONE_BUTTON, 1, 0, "§e" + date.getMonth(), Arrays.asList("§8 ➥ §8[§6Linksklick§8]§7 +1 Tag", "§8 ➥ §8[§6Rechtsklick§8]§7 -1 Tag"))) {
            @Override
            public void onClick(InventoryClickEvent event) {
                LocalDate newDate;
                if (event.isLeftClick()) {
                    newDate = date.plusMonths(1);
                } else {
                    newDate = date.minusMonths(1);
                }
                playerData.setVariable("einreise_dob", newDate);
                openDOBChanger(player);
            }
        });
        inventoryManager.setItem(new CustomItem(6, ItemManager.createItem(Material.STONE_BUTTON, 1, 0, "§e" + date.getYear(), Arrays.asList("§8 ➥ §8[§6Linksklick§8]§7 +1 Tag", "§8 ➥ §8[§6Rechtsklick§8]§7 -1 Tag"))) {
            @Override
            public void onClick(InventoryClickEvent event) {
                LocalDate newDate;
                if (event.isLeftClick()) {
                    newDate = date.plusYears(1);
                } else {
                    newDate = date.minusYears(1);
                }
                playerData.setVariable("einreise_dob", newDate);
                openDOBChanger(player);
            }
        });
    }
}
