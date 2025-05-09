package de.polo.core.commands;

import de.polo.api.Utils.inventorymanager.CustomItem;
import de.polo.api.Utils.inventorymanager.InventoryManager;
import de.polo.api.player.VoidPlayer;
import de.polo.core.Main;
import de.polo.core.handler.CommandBase;
import de.polo.core.handler.TabCompletion;
import de.polo.core.manager.ItemManager;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.storage.LoyaltyBonusTimer;
import de.polo.core.utils.Prefix;
import de.polo.core.utils.Utils;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.List;

import static de.polo.core.Main.playerManager;

@CommandBase.CommandMeta(name = "treuebonus")
public class TreuebonusCommand extends CommandBase implements TabCompleter {
    public TreuebonusCommand(@NotNull CommandMeta meta) {
        super(meta);
    }

    @Override
    public void execute(@NotNull VoidPlayer player, @NotNull PlayerData playerData, @NotNull String[] args) throws Exception {
        if (args.length < 1) {
            openShop(player.getPlayer(), playerData);
            return;
        }
        LoyaltyBonusTimer timer = Main.getPlayerManager().getLoyaltyTimer(player.getUuid());
        long diff = Duration.between(timer.getStarted(), Utils.getTime()).toMinutes();
        diff = 120 - diff;
        player.sendMessage(Component.text("§8[§3Treuebonus§8]§b Du erhälst in " + diff + " Minuten deinen Treuebonus."));
    }

    private void openShop(Player player, PlayerData playerData) {
        InventoryManager inventoryManager = new InventoryManager(player, 27, Component.text("§8 » §bTreueshop"));
        int levelUpPrice = playerData.getLevel() * 15;
        inventoryManager.setItem(new CustomItem(0, ItemManager.createItem(Material.EXPERIENCE_BOTTLE, 1, 0, "§cLevelup", "§8 ➥ §b" + levelUpPrice + " Treuepunkte")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (!removeLoyaltyBonus(player, playerData, levelUpPrice)) {
                    player.sendMessage(Component.text(Prefix.ERROR + "Du hast nicht genug Treuepunkte."));
                    return;
                }
                player.sendMessage(Component.text("§8[§bTreueshop§8]§a Du hast dir ein Levelup gekauft!"));
                playerManager.addExp(player, playerData.getNeeded_exp() - playerData.getExp());
            }
        });
        inventoryManager.setItem(new CustomItem(1, ItemManager.createItem(Material.GOLD_INGOT, 1, 0, "§6Premium", "§8 ➥ §b16 Treuepunkte")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (!removeLoyaltyBonus(player, playerData, 16)) {
                    player.sendMessage(Component.text(Prefix.ERROR + "Du hast nicht genug Treuepunkte."));
                    return;
                }
                playerManager.redeemRank(player, "Premium", 1, "d");
                player.sendMessage(Component.text("§8[§bTreueshop§8]§a Du hast dir einen Tag Premium gekauft!"));
            }
        });
        inventoryManager.setItem(new CustomItem(2, ItemManager.createItem(Material.GOLD_INGOT, 1, 0, "§a2.500$", "§8 ➥ §b12 Treuepunkte")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (!removeLoyaltyBonus(player, playerData, 12)) {
                    player.sendMessage(Component.text(Prefix.ERROR + "Du hast nicht genug Treuepunkte."));
                    return;
                }
                playerData.addMoney(2500, "Treueshop");
                player.sendMessage(Component.text("§8[§bTreueshop§8]§a Du hast dir 2500$ gekauft!"));
            }
        });
    }

    private boolean removeLoyaltyBonus(Player player, PlayerData playerData, int amount) {
        if (playerData.getLoyaltyBonus() < amount) return false;
        playerData.setLoyaltyBonus(playerData.getLoyaltyBonus() - amount);
        Main.getInstance().getCoreDatabase().updateAsync("UPDATE players SET loyaltyBonus = ? WHERE uuid = ?", playerData.getLoyaltyBonus(), player.getUniqueId().toString());
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return TabCompletion.getBuilder(args)
                .addAtIndex(1, "info")
                .build();
    }
}
