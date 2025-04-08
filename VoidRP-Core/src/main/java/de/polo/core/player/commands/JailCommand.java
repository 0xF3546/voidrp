package de.polo.core.commands;

import de.polo.core.Main;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.utils.inventory.CustomItem;
import de.polo.core.utils.inventory.InventoryManager;
import de.polo.core.manager.ItemManager;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.utils.Prefix;
import lombok.SneakyThrows;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;

import static de.polo.core.Main.utils;

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
                if (utils.staatUtil.hasParole(player)) {
                    player.sendMessage(Prefix.ERROR + "Du bist bereits auf Bewährung.");
                    return;
                }
                utils.staatUtil.setParole(player, playerData.getHafteinheiten() * 2);
                playerData.setHafteinheiten(playerData.getHafteinheiten() / 2);
                player.sendMessage("§8[§cGefängnis§8]§7 Du hast nun " + playerData.getHafteinheiten() + " Hafteinheiten und " + playerData.getHafteinheiten() * 2 + " Minuten bewährung.");
                Main.getInstance().getCoreDatabase().updateAsync("UPDATE Jail SET wps = ? WHERE uuid = ?", playerData.getHafteinheiten(), player.getUniqueId().toString());
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
                utils.staatUtil.unarrestPlayer(player);
            }
        });
        return false;
    }
}
