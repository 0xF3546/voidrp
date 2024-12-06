package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.manager.InventoryManager.CustomItem;
import de.polo.voidroleplay.manager.InventoryManager.InventoryManager;
import de.polo.voidroleplay.manager.ItemManager;
import de.polo.voidroleplay.manager.PlayerManager;
import de.polo.voidroleplay.utils.Prefix;
import lombok.SneakyThrows;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.Collections;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class JailCommand implements CommandExecutor {
    private final PlayerManager playerManager;

    public JailCommand(PlayerManager playerManager) {
        this.playerManager = playerManager;

        Main.registerCommand("jail", this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player);
        if (!playerData.isJailed()) {
            player.sendMessage(Prefix.ERROR + "Du bist nicht im Gefängnis.");
            return false;
        }
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8 » §cGefängnis");
        inventoryManager.setItem(new CustomItem(13, ItemManager.createItem(Material.PAPER, 1, 0, "§7Bewährung", Arrays.asList("§8 ➥ §7" + playerData.getHafteinheiten() / 2 + " Hafteinheiten", "§8 ➥ §7" + playerData.getHafteinheiten() * 2 + "min Bewährung"))) {
            @SneakyThrows
            @Override
            public void onClick(InventoryClickEvent event) {
                if (!playerData.isJailed()) return;
                if (Main.getInstance().utils.staatUtil.hasParole(player)) {
                    player.sendMessage(Prefix.ERROR + "Du bist bereits auf Bewährung.");
                    return;
                }
                Main.getInstance().utils.staatUtil.setParole(player, playerData.getHafteinheiten() * 2);
                playerData.setHafteinheiten(playerData.getHafteinheiten() / 2);
                player.sendMessage("§8[§cGefängnis§8]§7 Du hast nun " + playerData.getHafteinheiten() + " Hafteinheiten und " + playerData.getHafteinheiten() * 2 + " Minuten bewährung.");
                PreparedStatement statement = Main.getInstance().mySQL.getConnection().prepareStatement("UPDATE Jail SET hafteinheiten_verbleibend = ? WHERE uuid = ?");
                statement.setInt(1, playerData.getHafteinheiten());
                statement.setString(2, player.getUniqueId().toString());
                statement.executeUpdate();
                statement.close();
            }
        });
        inventoryManager.setItem(new CustomItem(15, ItemManager.createItem(Material.PAPER, 1, 0, "§7Freikaufen", Collections.singletonList("§8 ➥ §7" + playerData.getHafteinheiten() * 500 + "$"))) {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (!playerData.isJailed()) return;
                if (playerData.getBank() < (playerData.getHafteinheiten() * 500)) {
                    player.sendMessage(Prefix.ERROR + "Du hast nicht genug Geld.");
                    return;
                }
                playerData.removeBankMoney(playerData.getHafteinheiten() * 500, "Kaution Gefängnis");
                Main.getInstance().utils.staatUtil.unarrestPlayer(player);
            }
        });
        return false;
    }
}
