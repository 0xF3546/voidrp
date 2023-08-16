package de.polo.metropiacity.commands;

import de.polo.metropiacity.Main;
import de.polo.metropiacity.dataStorage.PlayerData;
import de.polo.metropiacity.utils.ItemManager;
import de.polo.metropiacity.utils.LocationManager;
import de.polo.metropiacity.utils.PlayerManager;
import de.polo.metropiacity.utils.ServerManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;

public class MineCommand implements CommandExecutor {
    public static final Material[] blocks = new Material[]{Material.DIAMOND_ORE, Material.EMERALD_ORE, Material.IRON_ORE, Material.GOLD_ORE, Material.LAPIS_ORE, Material.REDSTONE_ORE};
    public static final String prefix ="§8[§7Mine§8] §7";
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        String uuid = player.getUniqueId().toString();
        PlayerData playerData = PlayerManager.playerDataMap.get(uuid);
        if (ServerManager.canDoJobs()) {
            if (playerData.canInteract()) {
                if (playerData.getVariable("job") == null) {
                    if (!Main.getInstance().getCooldownManager().isOnCooldown(player, "mine")) {
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
                        player.sendMessage("§8[§7Mine§8]§7 Du kannst den Job erst in §f" + Main.getTime(Main.getInstance().getCooldownManager().getRemainingTime(player, "farmer")) + "§7 beginnen.");
                    }
                } else {
                    if (playerData.getVariable("job").equals("mine")) {
                        if (LocationManager.getDistanceBetweenCoords(player, "mine") <= 5) {
                            player.sendMessage(prefix + "Du hast den Job Minenarbeiter beendet.");
                            playerData.setVariable("job", null);
                            playerData.getScoreboard().killScoreboard();
                            quitJob(player);
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
                Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
                    if (block.getType() == Material.STONE) {
                        block.setType(blocks[(int) (Math.random() * blocks.length)]);
                    }
                }, 2 * 60 * 20);
            }
        }
    }

    public static void quitJob(Player player) {
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
        player.sendMessage(prefix + "Verdienst durch §7Eisenerz§8: §a+" + iron + "$");
        verdienst = verdienst + (iron);
        exp = (int) (exp + iron * 0.35);
        player.sendMessage(prefix + "Verdienst durch §cRedstonerz§8: §a+" + redstone + "$");
        verdienst = verdienst + (redstone);
        exp = (int) (exp + redstone * 0.47);
        player.sendMessage(prefix + "Verdienst durch §9Lapislazulierz§8: §a+" + lapis + "$");
        verdienst = verdienst + (lapis);
        exp = (int) (exp + lapis * 0.60);
        player.sendMessage(prefix + "Verdienst durch §6Golderz§8: §a+" + gold + "$");
        verdienst = verdienst + (gold);
        exp = (int) (exp + gold * 0.8);
        player.sendMessage(prefix + "Verdienst durch §aSmaragderz§8: §a+" + smaragd + "$");
        verdienst = verdienst + (smaragd);
        exp = (exp + smaragd);
        player.sendMessage(prefix + "Verdienst durch §bDiamanterz§8: §a+" + diamant + "$");
        verdienst = verdienst + (diamant);
        exp = (int) (exp + diamant * 1.25);
        try {
            PlayerManager.addBankMoney(player, verdienst, "Auszahlung Minenarbeiter");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
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
        Main.getInstance().getCooldownManager().setCooldown(player, "mine", 600);
    }
}
