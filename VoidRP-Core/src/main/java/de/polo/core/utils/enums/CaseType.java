package de.polo.core.utils.enums;

import de.polo.core.manager.ItemManager;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

@AllArgsConstructor
@Getter
public enum CaseType {
    BASIC("§6§lCase", Arrays.asList(
            ItemManager.createItem(Material.SUNFLOWER, 1, 0, "§e100 Coins"),
            ItemManager.createItem(Material.CHEST, 1, 0, "§b§lXP-Case"),
            ItemManager.createItem(Material.DIAMOND_HORSE_ARMOR, 1, 0, Weapon.ASSAULT_RIFLE.getName()),
            ItemManager.createItem(Material.GOLD_NUGGET, 5, 0, "§eWertanlage (500$)"),
            ItemManager.createItem(Material.GOLD_NUGGET, 1, 0, "§eWertanlage (1750$)"),
            ItemManager.createItem(Material.GOLD_NUGGET, 2, 0, "§eWertanlage (800$)"),
            ItemManager.createItem(Material.GOLD_INGOT, 1, 0, "§61 Tag Premium")
    )),
    DAILY("§b§lDaily-Case", Arrays.asList(
            ItemManager.createItem(Material.SUNFLOWER, 1, 0, "§e100 Coins"),
            ItemManager.createItem(Material.CHEST, 1, 0, "§b§lXP-Case"),
            ItemManager.createItem(Material.DIAMOND_HORSE_ARMOR, 1, 0, Weapon.ASSAULT_RIFLE.getName()),
            ItemManager.createItem(Material.GOLD_NUGGET, 5, 0, "§eWertanlage (500$)"),
            ItemManager.createItem(Material.GOLD_NUGGET, 1, 0, "§eWertanlage (1750$)"),
            ItemManager.createItem(Material.GOLD_NUGGET, 2, 0, "§eWertanlage (800$)"),
            ItemManager.createItem(Material.GOLD_INGOT, 1, 0, "§61 Tag Premium")
    )),
    VOTE("§b§lVote-Case", Arrays.asList(
            ItemManager.createItem(Material.SUNFLOWER, 1, 0, "§e100 Coins"),
            ItemManager.createItem(Material.CHEST, 1, 0, "§b§lXP-Case"),
            ItemManager.createItem(Material.DIAMOND_HORSE_ARMOR, 1, 0, Weapon.ASSAULT_RIFLE.getName()),
            ItemManager.createItem(Material.GOLD_NUGGET, 5, 0, "§eWertanlage (500$)"),
            ItemManager.createItem(Material.GOLD_NUGGET, 1, 0, "§eWertanlage (1750$)"),
            ItemManager.createItem(Material.GOLD_NUGGET, 2, 0, "§eWertanlage (800$)"),
            ItemManager.createItem(Material.GOLD_INGOT, 1, 0, "§61 Tag Premium")
    )),
    CHRISTMAS("§cWeihnachts-Case", Arrays.asList(
            ItemManager.createItem(Material.SUNFLOWER, 1, 0, "§e100 Coins"),
            ItemManager.createItem(Material.CHEST, 1, 0, "§b§lXP-Case"),
            ItemManager.createItem(Material.DIAMOND_HORSE_ARMOR, 1, 0, Weapon.ASSAULT_RIFLE.getName()),
            ItemManager.createItem(Material.GOLD_NUGGET, 5, 0, "§eWertanlage (500$)"),
            ItemManager.createItem(Material.GOLD_NUGGET, 1, 0, "§eWertanlage (1750$)"),
            ItemManager.createItem(Material.GOLD_NUGGET, 2, 0, "§eWertanlage (800$)"),
            ItemManager.createItem(Material.GOLD_INGOT, 1, 0, "§61 Tag Premium"),
            ItemManager.createItem(Weapon.MARKSMAN.getMaterial(), 1, 0, Weapon.MARKSMAN.getName())
    ));

    private final String DisplayName;
    private List<ItemStack> items;
}
