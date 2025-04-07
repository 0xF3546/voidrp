package de.polo.voidroleplay.utils.player;

import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.utils.Utils;
import de.polo.voidroleplay.utils.inventory.CustomItem;
import de.polo.voidroleplay.utils.inventory.InventoryManager;
import de.polo.voidroleplay.manager.ItemManager;
import de.polo.voidroleplay.player.services.impl.PlayerManager;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.SQLException;
import java.util.concurrent.ThreadLocalRandom;

public class Rubbellose {
    private final PlayerManager playerManager;

    public Rubbellose(PlayerManager playerManager) {
        this.playerManager = playerManager;
    }

    public void startGame(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        player.closeInventory();
        InventoryManager inventoryManager = new InventoryManager(player, 54, "§6§lRubbellos", true, false);
        playerData.setIntVariable("rubbellose_gemacht", 0);
        playerData.setIntVariable("rubbellose_wins", 0);
        int greenBlocksPlaced = 0;
        for (int i = 0; i < 54; i++) {
            if (i % 9 == 0 || i % 9 == 8 || i < 9 || i > 44) {
                inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, 0, "§8")) {
                    @Override
                    public void onClick(InventoryClickEvent event) {

                    }
                });
            }
        }
        for (int i = 0; i < 54; i++) {
            if (inventoryManager.getInventory().getItem(i) == null) {
                if (greenBlocksPlaced < 4 && ThreadLocalRandom.current().nextBoolean()) {
                    inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§8")) {
                        @Override
                        public void onClick(InventoryClickEvent event) {
                            ItemMeta meta = event.getCurrentItem().getItemMeta();
                            meta.setDisplayName("§aGewonnen!");
                            event.getCurrentItem().setItemMeta(meta);
                            event.getCurrentItem().setType(Material.LIME_DYE);
                            playerData.setIntVariable("rubbellose_wins", playerData.getIntVariable("rubbellose_wins") + 1);
                            playerData.setIntVariable("rubbellose_gemacht", playerData.getIntVariable("rubbellose_gemacht") + 1);
                            if (playerData.getIntVariable("rubbellose_gemacht") >= 5) {
                                endGame(player);
                                player.closeInventory();
                            }
                        }
                    });
                } else {
                    inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§8")) {
                        @Override
                        public void onClick(InventoryClickEvent event) {
                            ItemMeta meta = event.getCurrentItem().getItemMeta();
                            meta.setDisplayName("§cVerloren!");
                            event.getCurrentItem().setItemMeta(meta);
                            event.getCurrentItem().setType(Material.RED_DYE);
                            playerData.setIntVariable("rubbellose_gemacht", playerData.getIntVariable("rubbellose_gemacht") + 1);
                            if (playerData.getIntVariable("rubbellose_gemacht") >= 5) {
                                endGame(player);
                                player.closeInventory();
                            }
                        }
                    });
                }
            }
        }
        inventoryManager.setItem(new CustomItem(49, ItemManager.createItem(Material.STRUCTURE_VOID, 1, 0, "§c§lAbbrechen")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                player.closeInventory();
                player.sendMessage("§8[§6Rubbellos§8]§c Du hast das Spiel abgebrochen!");
            }
        });
    }

    public void endGame(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        int payout = 0;
        System.out.println(playerData.getIntVariable("rubbellose_wins"));
        for (int i = 0; i < playerData.getIntVariable("rubbellose_wins"); i++) {
            payout = payout + (int) (200 * 0.35);
        }
        if (payout > 0) {
            player.sendMessage("§8[§6Rubbellos§8]§a Du hast " + payout + "$ gewonnen!");
        } else {
            player.sendMessage("§8[§6Rubbellos§8]§c Leider verloren!");
        }
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 0);
        playerManager.addExp(player, Utils.random(1, 3));
        try {
            playerManager.addMoney(player, payout, "Rubellos");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
