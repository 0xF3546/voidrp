package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.handler.CommandBase;
import de.polo.voidroleplay.manager.ItemManager;
import de.polo.voidroleplay.utils.inventory.CustomItem;
import de.polo.voidroleplay.utils.inventory.InventoryManager;
import de.polo.voidroleplay.storage.Agreement;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.utils.Utils;
import de.polo.voidroleplay.player.enums.License;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import static de.polo.voidroleplay.Main.*;
import static de.polo.voidroleplay.Main.factionManager;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
@CommandBase.CommandMeta(name = "givelicense", usage = "/givelicense [Spieler]")
public class GiveLicenseCommand extends CommandBase {
    public GiveLicenseCommand(@NotNull CommandMeta meta) {
        super(meta);
    }

    @Override
    public void execute(@NotNull Player player, @NotNull PlayerData playerData, @NotNull String[] args) throws Exception {
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
        PlayerData targetData = playerManager.getPlayerData(target);
        InventoryManager inventoryManager = new InventoryManager(player, 27,"§8 » §6Lizenzvergabe");
        int i = 0;
        if (playerData.getFactionGrade() >= 4 && !targetData.hasLicense(License.WEAPON)) {
            int price = 12500;
            if (targetData.getVisum() < 3) price = 8000;
            int finalPrice = price;
            inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(Material.PAPER, 1, 0, "§cWaffenschein", "§8 ➥ §c" + Utils.toDecimalFormat(finalPrice) + "$")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    if (targetData.hasLicense(License.WEAPON)) return;
                    if (targetData.getBargeld() < finalPrice) {
                        player.sendMessage(Component.text(Prefix.ERROR + "Der Spieler hat nicht genug Geld dabei."));
                        return;
                    }
                    player.sendMessage(Component.text("§8[§cWaffenschein§8]§a Du hast " + target.getName() + " einen Waffenschein-Antrag ausgestellt."));
                    target.sendMessage(Component.text("§8[§cWaffenschein§8]§a " + player.getName() + " hat dir einen Waffenschein-Antrag in höhe von " + Utils.toDecimalFormat(finalPrice) + "$ ausgestellt."));
                    utils.vertragUtil.sendInfoMessage(target);
                    Agreement agreement = new Agreement(player, target, "waffenschein",
                            () -> {
                                player.sendMessage(Component.text("§8[§cWaffenschein§8]§a " + target.getName() + " hat den Antrag angenommen."));
                                target.sendMessage(Component.text("§8[§cWaffenschein§8]§a Dir wurde ein Waffenschein ausgestellt."));
                                targetData.removeMoney(finalPrice, "Waffenschein");
                                targetData.addLicenseToDatabase(License.WEAPON);
                                factionManager.sendCustomMessageToFactions("§9HQ: " + factionManager.getTitle(player) + " " + player.getName() + " hat " + target.getName() + " einen Waffenschein ausgestellt.", "Polizei", "FBI");
                            },
                            () -> {
                                player.sendMessage(Component.text("§8[§cWaffenschein§8]§c " + target.getName() + " hat den Antrag abgelehnt."));
                                target.sendMessage(Component.text("§8[§cWaffenschein§8]§c Du hast den Antrag abgelehnt."));
                            });
                    utils.vertragUtil.setAgreement(player, target, agreement);
                }
            });
            i++;
        }
    }
}
