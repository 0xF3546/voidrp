package de.polo.metropiacity.playerUtils;

import de.polo.metropiacity.dataStorage.PlayerData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.utils.ItemManager;
import de.polo.metropiacity.utils.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.sql.SQLException;
import java.util.Random;

public class Rubbellose {
    private final PlayerManager playerManager;
    public Rubbellose(PlayerManager playerManager) {
        this.playerManager = playerManager;
    }
    public void startGame(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        player.closeInventory();
        Inventory inv = Bukkit.createInventory(player, 54, "§6§lRubbellos");
        playerData.setIntVariable("rubbellose_gemacht", 0);
        playerData.setIntVariable("rubbellose_wins", 0);
        Random random = new Random();
        int greenBlocksPlaced = 0;
        for(int i=0; i<54; i++) {
            if(i%9 == 0 || i%9 == 8 || i<9 || i>44) {
                inv.setItem(i, ItemManager.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, 0, "§8"));
            }
        }
        for (int i=0; i<54; i++) {
            if (inv.getItem(i) == null) {
                if (greenBlocksPlaced < 4 && random.nextBoolean()) {
                    inv.setItem(i, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§8"));
                    ItemMeta meta = inv.getItem(i).getItemMeta();
                    meta.getPersistentDataContainer().set(new NamespacedKey(Main.plugin, "isWin"), PersistentDataType.INTEGER, 1);
                    inv.getItem(i).setItemMeta(meta);
                } else {
                    inv.setItem(i, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§8"));
                    ItemMeta meta = inv.getItem(i).getItemMeta();
                    meta.getPersistentDataContainer().set(new NamespacedKey(Main.plugin, "isWin"), PersistentDataType.INTEGER, 0);
                    inv.getItem(i).setItemMeta(meta);
                }
            }
        }
        inv.setItem(49, ItemManager.createItem(Material.STRUCTURE_VOID, 1, 0, "§c§lAbbrechen"));
        player.openInventory(inv);
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
        playerManager.addExp(player, Main.random(1, 3));
        try {
            playerManager.addMoney(player, payout);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
