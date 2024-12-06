package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.manager.InventoryManager.CustomItem;
import de.polo.voidroleplay.manager.InventoryManager.InventoryManager;
import de.polo.voidroleplay.manager.ItemManager;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class FAQCommand implements CommandExecutor {
    public FAQCommand() {
        Main.registerCommand("faq", this);
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        open(player);
        return false;
    }

    void open(Player player) {
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8 » §6FAQ");
        inventoryManager.setItem(new CustomItem(0, ItemManager.createItem(Material.PAPER, 1, 0, "§7Support")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openSupport(player);
            }
        });
        inventoryManager.setItem(new CustomItem(1, ItemManager.createItem(Material.PAPER, 2, 0, "§7Allgemein")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openAllgemein(player);
            }
        });
        inventoryManager.setItem(new CustomItem(2, ItemManager.createItem(Material.PAPER, 3, 0, "§7Farming & Jobs")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openFarmingJobs(player);
            }
        });
        inventoryManager.setItem(new CustomItem(3, ItemManager.createItem(Material.PAPER, 4, 0, "§7Fraktionen")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openFraktionen(player);
            }
        });
    }

    private void openFraktionen(Player player) {

    }

    private void openFarmingJobs(Player player) {
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8 » §6FAQ §8» §eFarming & Jobs");
        inventoryManager.setItem(new CustomItem(0, ItemManager.createItem(Material.PAPER, 1, 0, "§7Farming", Arrays.asList("§8 ➥ §7Du kannst auf mehreren Feldern, wie z.B. Aramid, Farmen gehen. Schaue dafür ins Navi, begib dich zum Feld und baue die entsprechenden Blöcke ab."))) {
            @Override
            public void onClick(InventoryClickEvent event) {
            }
        });
        inventoryManager.setItem(new CustomItem(1, ItemManager.createItem(Material.PAPER, 2, 0, "§7Workstations", Arrays.asList("§8 ➥ §7Du kannst dich bei den Workstation NPCs in Workstations einmieten.", "§8 ➥ §7Workstations verarbeiten alle 5 Minuten eine gewisse Anzahl an Input-Items zu Output-Items (z.B. X Aramid zu X Kevlar)"))) {
            @Override
            public void onClick(InventoryClickEvent event) {
            }
        });
        inventoryManager.setItem(new CustomItem(2, ItemManager.createItem(Material.PAPER, 3, 0, "§7Jobs", Arrays.asList("§8 ➥ §7Im Navi findest du alle möglichen Arten von Jobs, diese werden dir beim annehmen des Jobs erklärt."))) {
            @Override
            public void onClick(InventoryClickEvent event) {
            }
        });

        inventoryManager.setItem(new CustomItem(18, ItemManager.createItem(Material.NETHER_WART, 1, 0, "§cZurück")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                open(player);
            }
        });
    }

    private void openAllgemein(Player player) {

    }

    private void openSupport(Player player) {

    }
}
