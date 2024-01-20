package de.polo.metropiacity.utils.Game;

import de.polo.metropiacity.Main;
import de.polo.metropiacity.dataStorage.FactionData;
import de.polo.metropiacity.dataStorage.PlayerData;
import de.polo.metropiacity.dataStorage.PlayerLaboratory;
import de.polo.metropiacity.utils.FactionManager;
import de.polo.metropiacity.utils.InventoryManager.CustomItem;
import de.polo.metropiacity.utils.InventoryManager.InventoryManager;
import de.polo.metropiacity.utils.ItemManager;
import de.polo.metropiacity.utils.LocationManager;
import de.polo.metropiacity.utils.PlayerManager;
import de.polo.metropiacity.utils.enums.RoleplayItem;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class Laboratory implements CommandExecutor {

    private final PlayerManager playerManager;
    private final FactionManager factionManager;
    private final LocationManager locationManager;
    private final List<PlayerLaboratory> playerLaboratories = new ArrayList<>();
    public Laboratory(PlayerManager playerManager, FactionManager factionManager, LocationManager locationManager) {
        this.playerManager = playerManager;
        this.factionManager = factionManager;
        this.locationManager = locationManager;
        Main.registerCommand("labor", this);
    }

    public Collection<PlayerLaboratory> getPlayerLaboratorries() {
        return playerLaboratories;
    }

    public void addPlayerLaboratory(PlayerLaboratory laboratory) {
        playerLaboratories.add(laboratory);
    }
    public void removePlayerLaboratory(PlayerLaboratory playerLaboratory) {
        playerLaboratories.remove(playerLaboratory);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        openInventory(player);
        return false;
    }

    private void openInventory(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player);
        if (playerData.getFaction() == null) {
            player.sendMessage(Main.error + "Du bist in keiner Fraktion.");
            return;
        }
        FactionData factionData = factionManager.getFactionData(playerData.getFaction());
        if (!factionData.hasLaboratory()) {
            player.sendMessage(Main.error + "Deine Fraktion hat kein Labor.");
            return;
        }

        if (locationManager.getDistanceBetweenCoords(player, playerData.getFaction() + "_laboratory") > 5) {
            player.sendMessage(Main.error + "Du bist nicht in der nähe deines Labors.");
            return;
        }

        if (playerData.getLaboratory() == null) {
            PlayerLaboratory laboratory = new PlayerLaboratory(this);
            laboratory.create(player.getUniqueId());
            playerData.setLaboratory(laboratory);
        }

        InventoryManager inventoryManager = new InventoryManager(player, 27, "§7 » §" + factionData.getPrimaryColor() + "Labor", true, true);
        if (playerData.getLaboratory().isStarted()) {
            inventoryManager.setItem(new CustomItem(15, ItemManager.createItem(Material.REDSTONE, 1, 0, "§cLabor stoppen")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    playerData.getLaboratory().stop();
                    player.sendMessage("§8[§" + factionData.getPrimaryColor() + "Labor§8]§c Du hast dein Labor gestoppt.");
                    player.closeInventory();
                }
            });
        } else {
            inventoryManager.setItem(new CustomItem(15, ItemManager.createItem(Material.EMERALD, 1, 0, "§aLabor starten")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    playerData.getLaboratory().start();
                    player.sendMessage("§8[§" + factionData.getPrimaryColor() + "Labor§8]§a Du hast dein Labor gestartet.");
                    player.closeInventory();
                }
            });
        }

        inventoryManager.setItem(new CustomItem(13, ItemManager.createItem(Material.CHEST, 1, 0, "§fLabor öffnen", Arrays.asList("§8 ➥ §a" + playerData.getLaboratory().getJointAmount() + " Joints§", "§8 ➥ §a" + playerData.getLaboratory().getWeedAmount() + " Marihuana§"))) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openLabotory(player);
            }
        });

        inventoryManager.setItem(new CustomItem(11, ItemManager.createItem(Material.CHEST, 1, 0, "§bLabor befüllen")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openFillInventory(player);
            }
        });
    }
    private void openLabotory(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player);
        FactionData factionData = factionManager.getFactionData(playerData.getFaction());
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§7 » §aLabor", true, true);
        inventoryManager.setItem(new CustomItem(12, ItemManager.createItem(RoleplayItem.MARIHUANA.getMaterial(), 1, 0, RoleplayItem.MARIHUANA.getDisplayName(), Arrays.asList("§8 » §7" + playerData.getLaboratory().getWeedAmount() + " Stück", "", "§cKlicke zum entfernen"))) {
            @Override
            public void onClick(InventoryClickEvent event) {
                ItemManager.addCustomItem(player, RoleplayItem.MARIHUANA, playerData.getLaboratory().getWeedAmount());
                player.sendMessage("§8[§" + factionData.getPrimaryColor() + "Labor§8]§a Du hast " + playerData.getLaboratory().getWeedAmount() + " Marihuana aus dem Labor genommen.");
                playerData.getLaboratory().setWeedAmount(0);
                playerData.getLaboratory().save();
                player.closeInventory();
            }
        });

        inventoryManager.setItem(new CustomItem(14, ItemManager.createItem(RoleplayItem.JOINT.getMaterial(), 1, 0, RoleplayItem.JOINT.getDisplayName(), Arrays.asList("§8 » §7" + playerData.getLaboratory().getJointAmount() + " Stück", "", "§cKlicke zum entfernen"))) {
            @Override
            public void onClick(InventoryClickEvent event) {
                ItemManager.addCustomItem(player, RoleplayItem.JOINT, playerData.getLaboratory().getJointAmount());
                player.sendMessage("§8[§" + factionData.getPrimaryColor() + "Labor§8]§a Du hast " + playerData.getLaboratory().getJointAmount() + " Joints aus dem Labor genommen.");
                playerData.getLaboratory().setJointAmount(0);
                playerData.getLaboratory().save();
                player.closeInventory();
            }
        });

        inventoryManager.setItem(new CustomItem(18, ItemManager.createItem(Material.NETHER_WART, 1, 0, "§cZurück")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openInventory(player);
            }
        });
    }

    private void openFillInventory(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player);
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§7 » §aLabor befüllen", true, true);
        int weedAmount = ItemManager.getCustomItemCount(player, RoleplayItem.MARIHUANA);
        int amountFor64 = 1;
        if (weedAmount >= 64) {
            amountFor64 = 64;
        } else {
            amountFor64 = weedAmount;
        }
        inventoryManager.setItem(new CustomItem(15, ItemManager.createItem(RoleplayItem.MARIHUANA.getMaterial(), amountFor64, 0, "§aAlles einlagern", "§8 ➥ §7" + weedAmount + " Stück")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                ItemManager.removeCustomItem(player, RoleplayItem.MARIHUANA, weedAmount);
                playerData.getLaboratory().add(weedAmount);
                player.closeInventory();
                playerData.getLaboratory().save();
            }
        });
        inventoryManager.setItem(new CustomItem(18, ItemManager.createItem(Material.NETHER_WART, 1, 0, "§cZurück")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openInventory(player);
            }
        });
    }

    public void openLaboratoryAsAttacker(Player player, FactionData defenderFaction) {
        PlayerData playerData = playerManager.getPlayerData(player);
        FactionData factionData = factionManager.getFactionData(playerData.getFaction());
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§7 » §" + defenderFaction.getPrimaryColor() + "Labor §8× §cAngriff", true, true);
        inventoryManager.setItem(new CustomItem(13, ItemManager.createItem(Material.REDSTONE, 1, 0, "§cRaube das Labor aus")) {
            @Override
            public void onClick(InventoryClickEvent event) {

            }
        });
    }

    public void pushTick() {
        for (PlayerLaboratory laboratory : playerLaboratories) {
            if (laboratory.getWeedAmount() >= 2) {
                laboratory.setWeedAmount(laboratory.getWeedAmount() - 2);
                laboratory.setJointAmount(laboratory.getJointAmount() + 1);
            } else {
                laboratory.stop();
            }
        }
    }
}
