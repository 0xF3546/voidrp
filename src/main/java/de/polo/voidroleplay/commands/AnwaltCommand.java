package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.manager.InventoryManager.CustomItem;
import de.polo.voidroleplay.manager.InventoryManager.InventoryManager;
import de.polo.voidroleplay.manager.ItemManager;
import de.polo.voidroleplay.manager.LocationManager;
import de.polo.voidroleplay.manager.PlayerManager;
import de.polo.voidroleplay.utils.Prefix;
import lombok.SneakyThrows;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class AnwaltCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final LocationManager locationManager;
    public AnwaltCommand(PlayerManager playerManager, LocationManager locationManager) {
        this.playerManager = playerManager;
        this.locationManager = locationManager;

        Main.registerCommand("anwalt", this);
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player);
        if (locationManager.getDistanceBetweenCoords(player, "anwalt") > 5) {
            player.sendMessage(Prefix.ERROR + "Du bist nicht in der nähe des Anwalts.");
            return false;
        }
        if (!playerData.hasAnwalt()) {
            player.sendMessage(Prefix.ERROR + "Du hast keinen Anwalt.");
            return false;
        }
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8 » §7Anwalt");
        inventoryManager.setItem(new CustomItem(12, ItemManager.createItem(Material.BLUE_DYE, 1, 0, "§9Aktenübersicht")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openPlayerAkte(player, 1);
            }
        });
        inventoryManager.setItem(new CustomItem(14, ItemManager.createItem(Material.BLUE_DYE, 1, 0, "§9Akten entfernen", Arrays.asList("§8 ➥ §e50% Chance alle Akten entfernt zu bekommen", "§8 ➥ §a20.000$"))) {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (playerData.getBargeld() < 20000) {
                    player.sendMessage(Prefix.ERROR + "Du hast nicht genug Geld.");
                    return;
                }
                if (Main.random(1, 2) == 2) {
                    player.sendMessage("§8[§7Anwalt§8]§7 Ich habe es nicht geschafft...");
                    return;
                }
                player.sendMessage("§8[§7Anwalt§8]§7 Ich habe es geschafft, alle Akten sind entfernt.");
                Main.getInstance().utils.staatUtil.clearPlayerAkte(player);
            }
        });
        return false;
    }

    @SneakyThrows
    public void openPlayerAkte(Player player, int page) {
        if (page <= 0) return;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8» §9Aktenübersicht §8- §9Seite§8:§7 " + page, true, false);
        Statement statement = Main.getInstance().mySQL.getStatement();
        ResultSet result = statement.executeQuery("SELECT `id`, `akte`, `hafteinheiten`, `geldstrafe`, `vergebendurch`, DATE_FORMAT(datum, '%d.%m.%Y | %H:%i:%s') AS formatted_timestamp FROM `player_akten` WHERE `uuid` = '" + player.getUniqueId() + "'");
         int i = 0;
        while (result.next()) {
            if (i == 26 && i == 18 && i == 22) {
                i++;
            } else if (result.getRow() >= (25 * (page - 1)) && result.getRow() <= (25 * page)) {
                inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(Material.WRITTEN_BOOK, 1, 0, "§8» §3" + result.getString(2), Arrays.asList("§8 ➥ §bHafteinheiten§8:§7 " + result.getInt(3), "§8 ➥ §bGeldstrafe§8:§7 " + result.getInt(4) + "$", "§8 ➥ §bDurch§8:§7 " + result.getString(5), "§8 ➥ §bDatum§8:§7 " + result.getString("formatted_timestamp")))) {
                    @SneakyThrows
                    @Override
                    public void onClick(InventoryClickEvent event) {

                    }
                });
                i++;
            }
        }
        inventoryManager.setItem(new CustomItem(26, ItemManager.createItem(Material.GOLD_NUGGET, 1, 0, "§cNächste Seite")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openPlayerAkte(player, page + 1);
            }
        });
        inventoryManager.setItem(new CustomItem(18, ItemManager.createItem(Material.NETHER_WART, 1, 0, "§cVorherige Seite")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openPlayerAkte(player, page + 1);
            }
        });
        result.close();
    }
}
