package de.polo.voidroleplay.utils.GamePlay;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.dataStorage.WeaponType;
import de.polo.voidroleplay.utils.InventoryManager.InventoryManager;
import de.polo.voidroleplay.utils.ItemManager;
import de.polo.voidroleplay.utils.enums.CaseType;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class Case {
    private final Player player;
    private InventoryManager inventoryManager;
    private final CaseType caseType;
    boolean spinning = false;
    public Case(Player player, CaseType caseType) {
        this.player = player;
        this.caseType = caseType;
        init();
    }

    private void init() {
        inventoryManager = new InventoryManager(player, 27, caseType.getDisplayName(), true, true);
        spinWheel();
    }

    private void spinWheel() {
        spinning = true;
        int durationTicks = 60;
        int ticksPerRotation = 2;

        List<ItemStack> items = Arrays.asList(
                ItemManager.createItem(Material.SUNFLOWER, 1, 0, "§e100 Coins"),
                ItemManager.createItem(Material.CHEST, 1, 0, "§b§lXP-Case"),
                ItemManager.createItem(Material.DIAMOND_HORSE_ARMOR, 1, 0, "§cSturmgewehr"),
                ItemManager.createItem(Material.GOLD_NUGGET, 5, 0, "§eWertanlage (500$)"),
                ItemManager.createItem(Material.GOLD_NUGGET, 1, 0, "§eWertanlage (1750$)"),
                ItemManager.createItem(Material.GOLD_NUGGET, 2, 0, "§eWertanlage (800$)"),
                ItemManager.createItem(Material.GOLD_INGOT, 1, 0, "§61 Tag Premium")
        );

        Collections.shuffle(items);

        new BukkitRunnable() {
            int ticksPassed = 0;
            List<ItemStack> contents = new ArrayList<>(Collections.nCopies(27, new ItemStack(Material.AIR)));
            Random random = new Random();

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
        PlayerData playerData = Main.getInstance().playerManager.getPlayerData(player);
        ItemStack winningItem = inventoryManager.getInventory().getItem(13);
        if (winningItem != null && winningItem.getType() != Material.AIR) {
            player.sendMessage("§8 » §bDu hast " + winningItem.getAmount() + "x " + winningItem.getItemMeta().getDisplayName() + "§b gewonnen!");
            switch (winningItem.getType()) {
                case SUNFLOWER:
                    Main.getInstance().playerManager.addCoins(player, 100);
                    break;
                case DIAMOND_HORSE_ARMOR:
                    Main.getInstance().weapons.giveWeaponToPlayer(player, winningItem.getType(), WeaponType.NORMAL);
                    break;
                case GOLD_INGOT:
                    if (playerData.getPermlevel() >= 40) {
                        player.sendMessage(Main.prefix + "Da du bereits einen höheren Rang hast, erhälst du 125 Coins.");
                        Main.getInstance().playerManager.addCoins(player, 125);
                    } else {
                        Main.getInstance().playerManager.redeemRank(player, "premium", 1, "d");
                    }
                    break;
                case GOLD_NUGGET:
                    try {
                        String amountString = winningItem.getItemMeta().getDisplayName().replaceAll("[^0-9]", "");
                        int amount = Integer.parseInt(amountString);
                        playerData.addMoney(amount * winningItem.getAmount());
                    } catch (Exception e) {
                        player.getInventory().addItem(winningItem);
                    }
                    break;
                default:
                    player.getInventory().addItem(winningItem);
                    break;
            }
        } else {
            player.sendMessage("Leider ist beim Drehen des Glücksrads ein Fehler aufgetreten.");
        }
        player.closeInventory();
    }
}
