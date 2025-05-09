package de.polo.core.listeners;

import de.polo.core.player.entities.PlayerData;
import de.polo.core.utils.Event;
import de.polo.core.utils.enums.PickaxeType;
import de.polo.core.utils.enums.RoleplayItem;
import de.polo.core.utils.enums.Weapon;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

import static de.polo.core.Main.playerManager;
import static de.polo.core.Main.weaponManager;

@Event
public class ItemDropListener implements Listener {

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        ItemStack droppedItem = event.getItemDrop().getItemStack();
        List<Material> blockedItems = new ObjectArrayList<>();
        blockedItems.add(RoleplayItem.CUFF.getMaterial());
        blockedItems.add(RoleplayItem.SWAT_SHIELD.getMaterial());
        blockedItems.add(RoleplayItem.TAZER.getMaterial());
        blockedItems.add(RoleplayItem.URAN.getMaterial());
        blockedItems.add(Material.WOODEN_AXE);
        if (event.getPlayer().getEquipment().getItem(EquipmentSlot.OFF_HAND).equals(droppedItem)) {
            event.setCancelled(true);
            return;
        }
        if (event.getPlayer().getGameMode().equals(GameMode.CREATIVE)) {
            event.setCancelled(true);
            return;
        }
        if (blockedItems.contains(event.getItemDrop().getItemStack().getType())) {
            event.setCancelled(true);
            return;
        }
        if (Arrays.stream(PickaxeType.values()).anyMatch(x -> x.getMaterial() == event.getItemDrop().getItemStack().getType())) {
            event.setCancelled(true);
            return;
        }
        PlayerData playerData = playerManager.getPlayerData(event.getPlayer());
        if (playerData.isCuffed()) {
            event.setCancelled(true);
            return;
        }
        if (playerData.getVariable("gangwar") != null) event.setCancelled(true);
        for (Weapon weapon : Weapon.values()) {
            if (weapon.getMaterial() == event.getItemDrop().getItemStack().getType()) {
                event.setCancelled(true);
                weaponManager.reloadWeapon(event.getPlayer(), event.getItemDrop().getItemStack());
            }
        }
    }
}
