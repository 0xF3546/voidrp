package de.polo.core.utils.gameplay;

import de.polo.api.Utils.inventorymanager.InventoryManager;
import de.polo.core.Main;
import de.polo.core.manager.ItemManager;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.utils.Prefix;
import de.polo.core.utils.enums.CaseType;
import de.polo.core.utils.enums.Weapon;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collections;
import java.util.List;

public class Case {
    private final Player player;
    private final CaseType caseType;
    boolean spinning = false;
    private InventoryManager inventoryManager;

    public Case(Player player, CaseType caseType) {
        this.player = player;
        this.caseType = caseType;
        init();
    }

    private void init() {
        inventoryManager = new InventoryManager(player, 27, Component.text(caseType.getDisplayName()), true, true);
        spinWheel();
    }

    private void spinWheel() {
        spinning = true;
        int durationTicks = 60;
        int ticksPerRotation = 2;

        List<ItemStack> items = caseType.getItems();

        Collections.shuffle(items);

        new BukkitRunnable() {
            final List<ItemStack> contents = new ObjectArrayList<>(Collections.nCopies(27, new ItemStack(Material.AIR)));
            int ticksPassed = 0;

            @Override
            public void run() {
                if (ticksPassed >= durationTicks) {
                    spinning = false;
                    this.cancel();
                    endSpin();
                    return;
                }

                // Fülle das Glücksrad mit schwarzen Glasscheiben
                for (int i = 0; i < 27; i++) {
                    if (i < 9 || i > 16) { // Slot 9 bis Slot 16 bleiben leer für die Items
                        contents.set(i, ItemManager.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, 0, "§c"));
                        if (i == 4) {
                            contents.set(i, ItemManager.createItem(Material.ORANGE_STAINED_GLASS_PANE, 1, 0, "§6⬇"));
                        }
                        if (i == 22) {
                            contents.set(i, ItemManager.createItem(Material.ORANGE_STAINED_GLASS_PANE, 1, 0, "§6⬆"));
                        }
                    }
                }

                // Setze das zufällige Item in die Slots 17 bis 9 des Glücksrads
                for (int i = 17; i >= 9; i--) {
                    int offset = 17 - i; // Offset für die Reihenfolge der Items
                    ItemStack currentItem = items.get((ticksPassed + offset) % items.size());
                    contents.set(i, currentItem);
                }

                inventoryManager.getInventory().setContents(contents.toArray(new ItemStack[0]));
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);

                ticksPassed += ticksPerRotation;
            }
        }.runTaskTimer(Main.getInstance(), 0, ticksPerRotation);
    }

    private void endSpin() {
        PlayerData playerData = Main.playerManager.getPlayerData(player);
        ItemStack winningItem = inventoryManager.getInventory().getItem(13);
        if (winningItem != null && winningItem.getType() != Material.AIR) {
            player.sendMessage("§8 » §bDu hast " + winningItem.getAmount() + "x " + winningItem.getItemMeta().getDisplayName() + "§b gewonnen!");
            switch (winningItem.getType()) {
                case SUNFLOWER:
                    Main.playerManager.addCoins(player, 100);
                    break;
                case DIAMOND_HORSE_ARMOR:
                    Main.weaponManager.giveWeaponToCabinet(player, Weapon.ASSAULT_RIFLE, 0, 25);
                    break;
                case GOLD_INGOT:
                    if (playerData.getPermlevel() >= 40) {
                        player.sendMessage(Prefix.MAIN + "Da du bereits einen höheren Rang hast, erhälst du 125 Coins.");
                        Main.playerManager.addCoins(player, 125);
                    } else {
                        Main.playerManager.redeemRank(player, "premium", 1, "d");
                    }
                    break;
                case GOLD_NUGGET:
                    try {
                        String amountString = winningItem.getItemMeta().getDisplayName().replaceAll("[^0-9]", "");
                        int amount = Integer.parseInt(amountString);
                        playerData.addMoney(amount * winningItem.getAmount(), "Gewinn Case");
                    } catch (Exception e) {
                        player.getInventory().addItem(winningItem);
                    }
                    break;
                case LEATHER_HORSE_ARMOR:
                    Main.weaponManager.giveWeaponToCabinet(player, Weapon.MARKSMAN, 0, 1);
                    break;
                default:
                    player.getInventory().addItem(winningItem);
                    break;
            }
        } else {
            player.sendMessage("Leider ist beim spinnen der Case ein Fehler aufgetreten.");
        }
        player.closeInventory();
    }
}
