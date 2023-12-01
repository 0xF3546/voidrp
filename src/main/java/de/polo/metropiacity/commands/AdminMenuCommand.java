package de.polo.metropiacity.commands;

import de.polo.metropiacity.dataStorage.FactionData;
import de.polo.metropiacity.dataStorage.PlayerData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.database.MySQL;
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

public class AdminMenuCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final FactionManager factionManager;
    public AdminMenuCommand(PlayerManager playerManager, FactionManager factionManager) {
        this.playerManager = playerManager;
        this.factionManager = factionManager;
        Main.registerCommand("adminmenu", this);
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (playerData.getPermlevel() >= 70) {
            try {
                openAdminMenu(player, 1, false);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else {
            player.sendMessage(Main.error_nopermission);
        }
        return false;
    }

    public void openAdminMenu(Player player, int page, boolean offlinePlayers) throws SQLException {
        if (page <= 0) return;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        FactionData factionData = factionManager.getFactionData(playerData.getFaction());
        playerData.setVariable("current_inventory", "adminmenu");
        playerData.setIntVariable("current_page", page);
        playerData.setVariable("offlinePlayers", "nein");
        System.out.println("adminmenü geöffnet");
        System.out.println(page);
        System.out.println(offlinePlayers);
        if (offlinePlayers) {
            playerData.setVariable("offlinePlayers", "ja");
            Statement statement = Main.getInstance().mySQL.getStatement();
            ResultSet result = statement.executeQuery("SELECT `uuid`, `player_name`, `faction`, `faction_grade` FROM `players`");
            Inventory inv = Bukkit.createInventory(player, 27, "§8» §cAdminMenü §8- §cSeite§8:§7 " + page);
            int i = 0;
            while (result.next()) {
                if (i == 26 && i == 18) {
                    i++;
                } else if (result.getRow() >= (25 * (page - 1)) && result.getRow() <= (25 * page)) {
                    Player iplayer = Bukkit.getPlayer(UUID.fromString(result.getString(1)));
                    String state = "§cOffline";
                    if (iplayer != null && iplayer.isOnline()) state = "§aOnline";
                    inv.setItem(i, ItemManager.createItemHead(result.getString(1), 1, 0, "§8» §6" + result.getString(2), "§8 ➥ §eStatus§8:§7 " + state));
                    i++;
                }
                inv.setItem(26, ItemManager.createItem(Material.GOLD_NUGGET, 1, 0, "§cNächste Seite", null));
                inv.setItem(22, ItemManager.createItem(Material.DIAMOND, 1, 0, "§cNur Online-Spieler anzeigen", null));
                inv.setItem(18, ItemManager.createItem(Material.NETHER_WART, 1, 0, "§cVorherige Seite", null));
            }
            result.close();
            player.openInventory(inv);
        } else {
            playerData.setVariable("offlinePlayers", "nein");
            Inventory inv = Bukkit.createInventory(player, 27, "§8» §cAdminMenü §8- §cSeite§8:§7 " + page);
            int i = 0;
            int j = 0;
            for (Player players : Bukkit.getOnlinePlayers()) {
                if (i == 26 && i == 18) {
                    i++;
                } else if (j >= (25 * (page - 1)) && j <= (25 * page)) {
                    inv.setItem(i, ItemManager.createItemHead(players.getUniqueId().toString(), 1, 0, "§8» §6" + players.getName(), null));
                    i++;
                }
                inv.setItem(26, ItemManager.createItem(Material.GOLD_NUGGET, 1, 0, "§cNächste Seite", null));
                inv.setItem(22, ItemManager.createItem(Material.DIAMOND, 1, 0, "§aAlle Spieler anzeigen", null));
                inv.setItem(18, ItemManager.createItem(Material.NETHER_WART, 1, 0, "§cVorherige Seite", null));
                j++;
            }
            player.openInventory(inv);
        }
    }
    public void editPlayerViaAdmin(Player player, ItemStack stack) throws SQLException {
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        FactionData factionData = factionManager.getFactionData(playerData.getFaction());
        ItemStack tempItemStack = new ItemStack(stack.getType());
        tempItemStack.setItemMeta(stack.getItemMeta());
        if (tempItemStack.getItemMeta() instanceof SkullMeta) {
            SkullMeta skullMeta = (SkullMeta) tempItemStack.getItemMeta();
            UUID uuid = Objects.requireNonNull(skullMeta.getOwningPlayer()).getUniqueId();
            OfflinePlayer targetplayer = Bukkit.getOfflinePlayer(uuid);
            playerData.setVariable("current_inventory", "edit_player_" + targetplayer.getUniqueId());
            Statement statement = Main.getInstance().mySQL.getStatement();
            ResultSet result = statement.executeQuery("SELECT `uuid`, `player_name`, `faction`, `faction_grade` FROM `players`");
            Inventory inv = Bukkit.createInventory(player, 27, "§8» §c" + targetplayer.getName());
            if (result.next()) {
                inv.setItem(4, ItemManager.createItemHead(result.getString(1), 1, 0, "§8» §6" + result.getString(2), "§8 ➥ §eRang§8:§7 " + result.getInt(4)));
                inv.setItem(11, ItemManager.createItem(Material.SNOWBALL, 1, 0, "§cSpieler Freezen/Unfreezen", null));
                inv.setItem(15, ItemManager.createItem(Material.DIAMOND_BLOCK, 1, 0, "§aZu Spieler teleportieren", null));
                inv.setItem(16, ItemManager.createItem(Material.EMERALD_BLOCK, 1, 0, "§aSpieler zu mir teleportieren", null));
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
