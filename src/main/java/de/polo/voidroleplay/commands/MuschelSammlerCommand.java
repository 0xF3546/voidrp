package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.manager.InventoryManager.CustomItem;
import de.polo.voidroleplay.manager.InventoryManager.InventoryManager;
import de.polo.voidroleplay.manager.ItemManager;
import de.polo.voidroleplay.manager.LocationManager;
import de.polo.voidroleplay.manager.PlayerManager;
import de.polo.voidroleplay.manager.ServerManager;
import de.polo.voidroleplay.utils.Interfaces.PlayerQuit;
import de.polo.voidroleplay.utils.Utils;
import de.polo.voidroleplay.utils.enums.RoleplayItem;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

public class MuschelSammlerCommand implements CommandExecutor, PlayerQuit {
    private final PlayerManager playerManager;
    private final LocationManager locationManager;
    private final HashMap<Block, LocalDateTime> blocksBroken = new HashMap<>();

    public MuschelSammlerCommand(PlayerManager playerManager, LocationManager locationManager) {
        this.playerManager = playerManager;
        this.locationManager = locationManager;
        Main.registerCommand("muschelsammler", this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player);
        if (locationManager.getDistanceBetweenCoords(player, "muschelsammler") > 5) {
            player.sendMessage(Main.error + "Du bist nicht in der nähe des Muschelsammlers.");
            return false;
        }
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8 » §eMuschelsammler", true, true);
        inventoryManager.setItem(new CustomItem(4, ItemManager.createItem(Material.PAPER, 1, 0, "§bJobbeschreibung", "§8 ➥§7 Sammle Muscheln & verkaufe oder öffne diese.")) {
            @Override
            public void onClick(InventoryClickEvent event) {

            }
        });
        if (playerData.getVariable("job") == null) {
            inventoryManager.setItem(new CustomItem(11, ItemManager.createItem(Material.LIME_DYE, 1, 0, "§eMuschelsammler starten")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    startJob(player);
                    player.closeInventory();
                }
            });
        } else {
            inventoryManager.setItem(new CustomItem(11, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§e§mMuschelsammler starten", "§8 ➥§7 Du hast bereits den §f" + playerData.getVariable("job") + "§7 Job angenommen.")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    startJob(player);
                    player.closeInventory();
                }
            });
        }
        if (playerData.getVariable("job") == null) {
            inventoryManager.setItem(new CustomItem(15, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§e§mJob beenden", "§8 ➥§7 Du hast den Job nicht angenommen")) {
                @Override
                public void onClick(InventoryClickEvent event) {

                }
            });
        } else {
            if (!playerData.getVariable("job").toString().equalsIgnoreCase("Muschelsammler")) {
                inventoryManager.setItem(new CustomItem(15, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§e§mJob beenden", "§8 ➥§7 Du hast den Job nicht angenommen")) {
                    @Override
                    public void onClick(InventoryClickEvent event) {

                    }
                });
            } else {
                inventoryManager.setItem(new CustomItem(15, ItemManager.createItem(Material.YELLOW_DYE, 1, 0, "§eJob beenden")) {
                    @Override
                    public void onClick(InventoryClickEvent event) {
                        quitJob(player, false);
                        player.closeInventory();
                    }
                });
            }
        }
        int muschelPrice = ServerManager.getPayout("muschel");
        inventoryManager.setItem(new CustomItem(22, ItemManager.createItem(Material.BIRCH_BUTTON, 1, 0, "§eMuscheln verkaufen", "§8 ➥ §e" + muschelPrice + "$/Stück")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                int amount = ItemManager.getCustomItemCount(player, RoleplayItem.SHELL);
                if (amount == 0) {
                    return;
                }
                player.closeInventory();
                ItemManager.removeCustomItem(player, RoleplayItem.SHELL, amount);
                int payout = amount * muschelPrice;
                player.sendMessage("§8[§eMuschelsammler§8]§a +" + Utils.toDecimalFormat(payout) + "$");
                playerData.addMoney(payout, "Muschelsammler");
            }
        });
        return false;
    }

    public void blockBroken(Player player, Block block, BlockBreakEvent event) {
        event.setCancelled(true);
        if (event.getBlock().getType() != Material.BIRCH_BUTTON) {
            return;
        }
        if (!ServerManager.canDoJobs()) {
            player.sendMessage(Main.error + "Vor dem restart kannst du diesen Job nicht mehr ausführen.");
            return;
        }
        ItemStack item = ItemManager.createItem(Material.BIRCH_BUTTON, 1, 0, "§eMuschel", Collections.singletonList("§8 ➥ §8[§6Rechtsklick§8]§7 Muschel öffnen"));
        player.getInventory().addItem(item);
        event.getBlock().setType(Material.AIR);
        Main.waitSeconds(120, () -> {
            event.getBlock().setType(Material.BIRCH_BUTTON);
        });
    }

    private void startJob(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        playerData.setVariable("job", "Muschelsammler");
        player.sendMessage("§8[§eMuschelsammler§8]§7 Du hast den Job gestartet.");
        player.sendMessage("§8[§eMuschelsammler§8]§7 Sammle nun Muscheln (Birkenholzknopf abbauen).");
    }

    public void quitJob(Player player, boolean silent) {
        Main.getInstance().beginnerpass.didQuest(player, 5);
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        playerData.setVariable("job", null);
        player.sendMessage("§8[§eMuschelsammler§8]§7 Du hast den Job beendet.");
        player.closeInventory();
    }

    @Override
    public void playerQuit(Player player) {
        quitJob(player, true);
    }
}
