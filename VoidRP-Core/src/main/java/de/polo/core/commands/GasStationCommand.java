package de.polo.core.commands;

import de.polo.api.Utils.ItemBuilder;
import de.polo.api.Utils.inventorymanager.CustomItem;
import de.polo.api.Utils.inventorymanager.InventoryManager;
import de.polo.api.VoidAPI;
import de.polo.core.Main;
import de.polo.core.game.events.SubmitChatEvent;
import de.polo.core.location.services.LocationService;
import de.polo.core.manager.CompanyManager;
import de.polo.core.manager.ItemManager;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.storage.GasStationData;
import de.polo.core.utils.Prefix;
import de.polo.core.utils.Utils;
import de.polo.core.utils.enums.RoleplayItem;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class GasStationCommand implements CommandExecutor, Listener {
    private final PlayerManager playerManager;
    private final CompanyManager companyManager;

    public GasStationCommand(PlayerManager playerManager, CompanyManager companyManager) {
        this.playerManager = playerManager;
        this.companyManager = companyManager;

        Main.registerCommand("gasstation", this);
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player);
        LocationService locationService = VoidAPI.getService(LocationService.class);
        GasStationData gasStationData = locationService.getGasStationInRadius(player);
        if (gasStationData == null) {
            player.sendMessage(Prefix.ERROR + "Du bist nicht in der nähe einer Tankstelle.");
            return false;
        }
        openGasStation(player, gasStationData);
        return false;
    }

    private void openGasStation(Player player, GasStationData gasStationData) {
        PlayerData playerData = playerManager.getPlayerData(player);
        InventoryManager inventoryManager = new InventoryManager(player, 27, Component.text("§8 » §cTankstelle " + gasStationData.getName()), true, true);
        if (playerData.getCompany() != null) {
            inventoryManager.setItem(new CustomItem(18, ItemManager.createItem(Material.YELLOW_DYE, 1, 0, "§eBusiness-Übersicht")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    openBusinessOverview(player, gasStationData);
                }
            });
        }
        int oil = ItemManager.getCustomItemCount(player, RoleplayItem.OIL);
        inventoryManager.setItem(new CustomItem(19, new ItemBuilder(RoleplayItem.OIL.getMaterial())
                .setName(oil == 0 ? "§7§mÖl abliefern" : "§7Öl abliefern")
                .setLore(oil == 0 ? List.of("§8 » §cDu hast kein Öl dabei") : List.of("§8 » §7Du hast §a" + oil + "x Öl §7dabei", "§8 » §aKlicke um es abzuliefern"))
                .build()) {
            @Override
            public void onClick(InventoryClickEvent event) {
                ItemManager.removeCustomItem(player, RoleplayItem.OIL, oil);
                gasStationData.addLiter(oil);
            }
        });
    }

    private void openBusinessOverview(Player player, GasStationData gasStationData) {
        PlayerData playerData = playerManager.getPlayerData(player);
        if (gasStationData.getCompany() != playerData.getCompany()) {
            openBusinessBuyOverview(player, gasStationData);
            return;
        }
        InventoryManager inventoryManager = new InventoryManager(player, 27, Component.text("§8» §c" + gasStationData.getName() + " (Business)"), true, true);
        inventoryManager.setItem(new CustomItem(18, ItemManager.createItem(Material.NETHER_WART, 1, 0, "§cZurück")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openGasStation(player, gasStationData);
            }
        });
        inventoryManager.setItem(new CustomItem(4, ItemManager.createItem(Material.PAPER, 1, 0, "§3Information", Arrays.asList("§8 ➥ §bKasse§8:§7 " + gasStationData.getBank() + "$", "§8 ➥ §bLiter-Preis§8:§7 " + gasStationData.getLiterprice() + "$", "§8 ➥ §bLiter§8:§7 " + gasStationData.getLiter() + "$"))) {
            @Override
            public void onClick(InventoryClickEvent event) {

            }
        });
        if (playerData.getCompanyRole() != null && (playerData.getCompanyRole().hasPermission("*") || playerData.getCompany().getOwner().equals(player.getUniqueId()) || playerData.getCompanyRole().hasPermission("manage_gas_" + gasStationData.getId()))) {
            inventoryManager.setItem(new CustomItem(11, ItemManager.createItem(Material.CHEST, 1, 0, "§6Liter-Preis anpassen")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    player.closeInventory();
                    playerData.setVariable("chatblock", "gasstation::changeliterprice");
                    playerData.setVariable("gasstation", gasStationData);
                    player.sendMessage("§8[§cTankstelle§8]§7 Gib nun den neuen Liter-Preis an.");
                }
            });
        }
        if (playerData.getCompanyRole() != null && (playerData.getCompanyRole().hasPermission("*") || playerData.getCompany().getOwner().equals(player.getUniqueId()) || playerData.getCompanyRole().hasPermission("manage_bank"))) {
            inventoryManager.setItem(new CustomItem(15, ItemManager.createItem(Material.GOLD_INGOT, 1, 0, "§3Kasse leeren")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    player.closeInventory();
                    companyManager.sendCompanyMessage(playerData.getCompany(), "§8[§6" + playerData.getCompany().getName() + "§8]§7 " + player.getName() + " hat §a" + Utils.toDecimalFormat(gasStationData.getBank()) + "$ §7aus dem §eTankstelle " + gasStationData.getName() + "§7 Business entnommen.");
                    playerData.addMoney(gasStationData.getBank(), "Kasse Tankstelle-" + gasStationData.getId());
                    playerData.getCompany().setBank(0);
                    playerData.getCompany().save();
                }
            });
        }
    }

    private void openBusinessBuyOverview(Player player, GasStationData gasStationData) {
        InventoryManager inventoryManager = new InventoryManager(player, 27, Component.text("§8» §c" + gasStationData.getName() + " (Business kaufen)"), true, true);
        PlayerData playerData = playerManager.getPlayerData(player);
        inventoryManager.setItem(new CustomItem(12, ItemManager.createItem(Material.PAPER, 1, 0, "§3Statistiken", Arrays.asList("§8 ➥§bTyp§8:§7 Tankstelle", "§8 ➥§bPreis§8:§7 3.250.000$"))) {
            @Override
            public void onClick(InventoryClickEvent event) {

            }
        });
        if (playerData.getCompanyRole() != null && playerData.getCompanyRole().hasPermission("*")) {
            inventoryManager.setItem(new CustomItem(14, ItemManager.createItem(Material.LIME_DYE, 1, 0, "§aKaufen")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    if (playerData.getCompany().getBank() < 3250000) {
                        player.sendMessage(Prefix.ERROR + "Deine Firma hat nicht genug Kapital um sich dieses Business zu leisten.");
                        return;
                    }
                    player.closeInventory();
                    player.sendMessage("§8[§6" + playerData.getCompany().getName() + "§8]§a Ihr habt das Business \"Tankstelle " + gasStationData.getName() + "\" gekauft.");
                    gasStationData.setCompany(playerData.getCompany().getId());
                    gasStationData.save();
                }
            });
        } else {
            inventoryManager.setItem(new CustomItem(14, ItemManager.createItem(Material.LIME_DYE, 1, 0, "§a§mKaufen", "§8 ➥ §cDafür bist du nicht berechtigt.")) {
                @Override
                public void onClick(InventoryClickEvent event) {

                }
            });
        }
        inventoryManager.setItem(new CustomItem(18, ItemManager.createItem(Material.NETHER_WART, 1, 0, "§cZurück")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openGasStation(player, gasStationData);
            }
        });
    }

    @EventHandler
    public void onChatSubmit(SubmitChatEvent event) {
        if (!event.getSubmitTo().equalsIgnoreCase("gasstation::changeliterprice")) {
            return;
        }
        if (event.isCancel()) {
            event.sendCancelMessage();
            event.end();
            return;
        }
        try {
            int price = Integer.parseInt(event.getMessage());
            GasStationData gasStationData = event.getPlayerData().getVariable("gasstation");
            companyManager.sendCompanyMessage(event.getPlayerData().getCompany(), "§8[§6" + event.getPlayerData().getCompany().getName() + "§8]§7 " + event.getPlayer().getName() + " hat den Liter-Preis von §eTankstelle " + gasStationData.getName() + "§7 auf §a" + price + "$§7 angepasst.");
            gasStationData.setLiterprice(price);
            gasStationData.save();
        } catch (Exception e) {
            event.getPlayer().sendMessage(Prefix.ERROR + "Die Zahl muss numerisch sein.");
        }
    }
}
