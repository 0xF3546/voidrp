package de.polo.metropiacity.commands;

import de.polo.metropiacity.Main;
import de.polo.metropiacity.database.MySQL;
import de.polo.metropiacity.dataStorage.FactionData;
import de.polo.metropiacity.dataStorage.PlayerData;
import de.polo.metropiacity.utils.FactionManager;
import de.polo.metropiacity.utils.ItemManager;
import de.polo.metropiacity.utils.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import java.util.UUID;

public class OpenBossMenuCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        if (playerData.getFactionGrade() >= 8) {
            try {
                openBossMenu(player, 1);
            } catch (SQLException e) {
                player.sendMessage(Main.error + "Ein Fehler ist aufgetreten. Bitte melde diesen bei den Entwicklern.");
                throw new RuntimeException(e);
            }
        } else {
            player.sendMessage(Main.error_nopermission);
        }
        return false;
    }

    public static void openBossMenu(Player player, int page) throws SQLException {
        if (page <= 0) return;
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        FactionData factionData = FactionManager.factionDataMap.get(playerData.getFaction());
        playerData.setVariable("current_inventory", "bossmenu_" + playerData.getFaction());
        playerData.setIntVariable("current_page", page);
        Statement statement = Main.getInstance().mySQL.getStatement();
        ResultSet result = statement.executeQuery("SELECT `uuid`, `player_name`, `faction`, `faction_grade` FROM `players` WHERE `faction` = '" + playerData.getFaction() + "'");
        Inventory inv = Bukkit.createInventory(player, 27, "§8» §" + factionData.getSecondaryColor() + "BossMenü §l" + factionData.getFullname() + "§8 - §" + factionData.getSecondaryColor() + "Seite§8:§7 " + page);
        int i = 0;
        while (result.next()) {
            if (i == 26 && i == 18) {
                i++;
            } else if (result.getRow() >= (25 * (page - 1)) && result.getRow() <= (25 * page)) {
                inv.setItem(i, ItemManager.createItemHead(result.getString(1), 1, 0, "§8» §6" + result.getString(2), "§8 ➥ §eRang§8:§7 " + result.getInt(4)));
                i++;
            }
            inv.setItem(26, ItemManager.createItem(Material.GOLD_NUGGET, 1, 0, "§cNächste Seite", null));
            inv.setItem(18, ItemManager.createItem(Material.NETHER_WART, 1, 0, "§cVorherige Seite", null));
        }
        result.close();
        player.openInventory(inv);
    }

    public static void editPlayerViaBoss(Player player, ItemStack stack) throws SQLException {
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        FactionData factionData = FactionManager.factionDataMap.get(playerData.getFaction());
        ItemStack tempItemStack = new ItemStack(stack.getType());
        tempItemStack.setItemMeta(stack.getItemMeta());
        if (tempItemStack.getItemMeta() instanceof SkullMeta) {
            SkullMeta skullMeta = (SkullMeta) tempItemStack.getItemMeta();
            UUID uuid = Objects.requireNonNull(skullMeta.getOwningPlayer()).getUniqueId();
            OfflinePlayer targetplayer = Bukkit.getOfflinePlayer(uuid);
            playerData.setVariable("current_inventory", "edit_factionplayer_" + targetplayer.getUniqueId());
            Statement statement = Main.getInstance().mySQL.getStatement();
            ResultSet result = statement.executeQuery("SELECT `uuid`, `player_name`, `faction`, `faction_grade` FROM `players` WHERE `uuid` = '" + uuid + "'");
            Inventory inv = Bukkit.createInventory(player, 27, "§8» §" +  factionData.getSecondaryColor() + "Mitglied bearbeiten");
            if (result.next()) {
                inv.setItem(4, ItemManager.createItemHead(result.getString(1), 1, 0, "§8» §6" + result.getString(2), "§8 ➥ §eRang§8:§7 " + result.getInt(4)));
                inv.setItem(11, ItemManager.createItem(Material.GLOWSTONE_DUST, 1, 0, "§cDegradieren", null));
                inv.setItem(15, ItemManager.createItem(Material.DIAMOND, 1, 0, "§aBefördern", null));
                inv.setItem(18, ItemManager.createItem(Material.NETHER_WART, 1, 0, "§cZurück", null));
                inv.setItem(22, ItemManager.createItem(Material.REDSTONE, 1, 0, "§cKicken", null));
                for (int i = 0; i < 27; i++) {
                    if (inv.getItem(i) == null) inv.setItem(i, ItemManager.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, 0, "§8", null));
                }
            } else {
                player.closeInventory();
                player.sendMessage(Main.error + "Spieler konnte nicht geladen werden.");
            }
            player.openInventory(inv);
        } else {
            player.closeInventory();
            player.sendMessage(Main.error + "Spieler konnte nicht geladen werden.");
        }
    }
}
