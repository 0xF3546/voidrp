package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.handler.CommandBase;
import de.polo.voidroleplay.manager.ItemManager;
import de.polo.voidroleplay.player.entities.VoidPlayer;
import de.polo.voidroleplay.utils.inventory.CustomItem;
import de.polo.voidroleplay.utils.inventory.InventoryManager;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.player.enums.License;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import static de.polo.voidroleplay.Main.*;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
@CommandBase.CommandMeta(name = "takelicense", usage = "/takelicense [Spieler]")
public class TakeLicenseCommand extends CommandBase {
    public TakeLicenseCommand(@NotNull CommandMeta meta) {
        super(meta);
    }

    @Override
    public void execute(@NotNull VoidPlayer player, @NotNull PlayerData playerData, @NotNull String[] args) throws Exception {
        if (!playerData.isExecutiveFaction()) {
            player.sendMessage(Component.text(Prefix.ERROR_NOPERMISSION));
            return;
        }
        if (args.length < 1) {
            showSyntax(player);
            return;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(Component.text(Prefix.ERROR + "Der Spieler wurde nicht gefunden."));
            return;
        }
        if (target.getLocation().distance(player.getLocation()) > 5) {
            player.sendMessage(Component.text(Prefix.ERROR + target.getName() + " ist nicht in deiner nähe."));
            return;
        }
        if (playerData.getFactionGrade() < 4) {
            player.sendMessage(Component.text(Prefix.ERROR + "Das geht erst ab Rang 4!"));
            return;
        }
        PlayerData targetData = playerManager.getPlayerData(target);
        InventoryManager inventoryManager = new InventoryManager(player.getPlayer(), 27, "§8 » §6Lizenzentnahme");
        int i = 0;
        for (License license : targetData.getLicenses()) {
            inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(Material.PAPER, 1, 0, "§cWaffenschein")) {

                @Override
                public void onClick(InventoryClickEvent event) {
                    player.getPlayer().closeInventory();
                    player.sendMessage(Component.text("§8[§cWaffenschein§8]§c Du hast " + target.getName() + "'s Waffenschein abgenommen."));
                    target.sendMessage(Component.text("§8[§cWaffenschein§8]§c " + player.getName() + " hat dir den Waffenschein abgenommen"));
                    targetData.removeLicenseFromDatabase(License.WEAPON);
                    factionManager.sendCustomMessageToFactions("§9HQ: " + factionManager.getTitle(player.getPlayer()) + " " + player.getName() + " hat " + target.getName() + "'s Waffenschein abgenommen.", "Polizei", "FBI");

                }
            });
            i++;
        }
    }
}
