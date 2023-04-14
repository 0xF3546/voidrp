package de.polo.void_roleplay.commands;

import de.polo.void_roleplay.Main;
import de.polo.void_roleplay.DataStorage.PlayerData;
import de.polo.void_roleplay.Utils.ItemManager;
import de.polo.void_roleplay.Utils.LocationManager;
import de.polo.void_roleplay.Utils.PlayerManager;
import de.polo.void_roleplay.Utils.ServerManager;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;
import java.util.Arrays;

public class mineCommand implements CommandExecutor {
    public static Material[] blocks = new Material[]{Material.DIAMOND_ORE, Material.EMERALD_ORE, Material.IRON_ORE, Material.GOLD_ORE, Material.LAPIS_ORE, Material.REDSTONE_ORE};
    public static String prefix ="§7Mine §8» §7";
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        String uuid = player.getUniqueId().toString();
        PlayerData playerData = PlayerManager.playerDataMap.get(uuid);
        if (ServerManager.canDoJobs()) {
            if (playerData.canInteract()) {
                if (playerData.getVariable("job") == null) {
                    if (LocationManager.getDistanceBetweenCoords(player, "mine") <= 5) {
                        playerData.setVariable("job", "mine");
                        player.sendMessage(prefix + "Du bist nun Minenarbeiter§7.");
                        player.sendMessage(prefix + "Baue nun Erze ab.");
                        playerData.getScoreboard().createMineScoreboard();
                        player.getInventory().addItem(ItemManager.createItem(Material.STONE_PICKAXE, 1, 0, "§6Spitzhacke", null));
                    } else {
                        player.sendMessage(Main.error + "Du bist §cnicht§7 in der nähe der Mine§7!");
                    }
                } else {
                    if (playerData.getVariable("job").equals("mine")) {
                        if (LocationManager.getDistanceBetweenCoords(player, "mine") <= 5) {
                            player.sendMessage(prefix + "Du hast den Job Minenarbeiter beendet.");
                            playerData.setVariable("job", null);
                            playerData.getScoreboard().killScoreboard();
                            try {
                                quitJob(player);
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    } else {
                        player.sendMessage(Main.error + "Du übst bereits den Job " + playerData.getVariable("job") + " aus.");
                    }
                }
            } else {
                player.sendMessage(Main.error_cantinteract);
            }
        } else {
            player.sendMessage(ServerManager.error_cantDoJobs);
        }
        return false;
    }

    public static void blockBroken(Player player, Block block, BlockBreakEvent event) {
        event.setCancelled(true);
        for (Material material : blocks) {
            if (block.getType() == material) {
                PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
                player.getInventory().addItem(ItemManager.createItem(material, 1, 0, block.getType().name(), null));
                block.setType(Material.STONE);
                Bukkit.getScheduler().runTaskLater(Main.getInstance(), new Runnable() {
                    @Override
                    public void run() {
                        if (block.getType() == Material.STONE) {
                            block.setType(blocks[(int) (Math.random() * blocks.length)]);
                        }
                    }
                }, 2 * 60 * 20);
            }
        }
    }

    public static void quitJob(Player player) throws SQLException {
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        playerData.getScoreboard().killScoreboard();
        int iron = ItemManager.getItem(player, Material.IRON_ORE);
        int redstone = ItemManager.getItem(player, Material.REDSTONE_ORE);
        int lapis = ItemManager.getItem(player, Material.LAPIS_ORE);
        int gold = ItemManager.getItem(player, Material.GOLD_ORE);
        int smaragd = ItemManager.getItem(player, Material.EMERALD_ORE);
        int diamant = ItemManager.getItem(player, Material.DIAMOND_ORE);
        int exp = 0;
        int verdienst = 0;
        player.sendMessage(prefix + "Verdienst durch §7Eisenerz§8: §a+" + iron * 1 + "$");
        verdienst = verdienst + (iron * 1);
        exp = (int) (exp + iron * 0.35);
        player.sendMessage(prefix + "Verdienst durch §cRedstonerz§8: §a+" + redstone * 1 + "$");
        verdienst = verdienst + (redstone * 1);
        exp = (int) (exp + redstone * 0.47);
        player.sendMessage(prefix + "Verdienst durch §9Lapislazulierz§8: §a+" + lapis * 1 + "$");
        verdienst = verdienst + (lapis * 1);
        exp = (int) (exp + lapis * 0.60);
        player.sendMessage(prefix + "Verdienst durch §6Golderz§8: §a+" + gold * 1 + "$");
        verdienst = verdienst + (gold * 1);
        exp = (int) (exp + gold * 0.8);
        player.sendMessage(prefix + "Verdienst durch §aSmaragderz§8: §a+" + smaragd * 1 + "$");
        verdienst = verdienst + (smaragd * 1);
        exp = (exp + smaragd);
        player.sendMessage(prefix + "Verdienst durch §bDiamanterz§8: §a+" + diamant * 1 + "$");
        verdienst = verdienst + (diamant * 1);
        exp = (int) (exp + diamant * 1.25);
        PlayerManager.addMoney(player, verdienst);
        player.sendMessage(prefix + "Du hast insgesamt §a+" + verdienst + "$§7 verdient.");
        PlayerManager.addExp(player, exp);
        Inventory inv = player.getInventory();
        for (Material material : blocks) {
            for (ItemStack item : inv.getContents()) {
                if (item != null && (item.getType() == material || item.getType() == Material.STONE_PICKAXE)) {
                    inv.removeItem(item);
                }
            }
        }
    }
}
