package de.polo.core.player.gui;

import de.polo.api.Utils.ApiUtils;
import de.polo.api.Utils.GUI;
import de.polo.api.Utils.ItemBuilder;
import de.polo.api.Utils.enums.Prefix;
import de.polo.api.Utils.inventorymanager.CustomItem;
import de.polo.api.Utils.inventorymanager.InventoryManager;
import de.polo.api.player.VoidPlayer;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

public class VestColorGUI implements GUI {
    private final VoidPlayer voidPlayer;
    public VestColorGUI(VoidPlayer voidPlayer) {
        this.voidPlayer = voidPlayer;
    }
    @Override
    public void open() {
        InventoryManager inventoryManager = new InventoryManager(voidPlayer.getPlayer(), 27, Component.text("Weste Farbe"));
        int i = 0;
        for (Color color : ApiUtils.getColors()) {
            inventoryManager.setItem(new CustomItem(i, createColoredChestplate(color)) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    if (voidPlayer.getData().getPermlevel() < 20) {
                        voidPlayer.sendMessage("Du hast keine Berechtigung, um die Weste zu färben!", Prefix.ERROR);
                        return;
                    }
                    ItemStack chestplate = voidPlayer.getPlayer().getInventory().getItemInMainHand();
                    if (chestplate.getType() == Material.AIR) {
                        voidPlayer.sendMessage("Du hast keine Weste in der Hand!", Prefix.ERROR);
                    }
                    if (chestplate.getType() != Material.LEATHER_CHESTPLATE) {
                        voidPlayer.sendMessage("Du hast keine Lederweste in der Hand!", Prefix.ERROR);
                        return;
                    }
                    if (chestplate.getType() == Material.LEATHER_CHESTPLATE) {
                        voidPlayer.getPlayer().getInventory().setItemInMainHand(applyChestPlateColor(chestplate, color));
                        voidPlayer.sendMessage("Du hast die Farbe der Weste geändert!", Prefix.MAIN);
                    }
                }
            });
            i++;
        }
    }

    private ItemStack createColoredChestplate(Color color) {
        ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
        LeatherArmorMeta meta = (LeatherArmorMeta) chestplate.getItemMeta();
        meta.setColor(color);
        chestplate.setItemMeta(meta);
        return chestplate;
    }

    private ItemStack applyChestPlateColor(ItemStack chestplate, Color color) {
        LeatherArmorMeta meta = (LeatherArmorMeta) chestplate.getItemMeta();
        meta.setColor(color);
        chestplate.setItemMeta(meta);
        return chestplate;
    }

}
