package de.polo.voidroleplay.listeners;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.storage.WeaponData;
import de.polo.voidroleplay.manager.PlayerManager;
import de.polo.voidroleplay.manager.WeaponManager;
import de.polo.voidroleplay.utils.enums.PickaxeType;
import de.polo.voidroleplay.utils.enums.RoleplayItem;
import de.polo.voidroleplay.utils.enums.Weapon;
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

public class ItemDropListener implements Listener {
    private final WeaponManager weaponManager;
    private final PlayerManager playerManager;

    public ItemDropListener(WeaponManager weaponManager, PlayerManager playerManager) {
        this.weaponManager = weaponManager;
        this.playerManager = playerManager;
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        ItemStack droppedItem = event.getItemDrop().getItemStack();
        List<Material> blockedItems = new ObjectArrayList<>();
        blockedItems.add(RoleplayItem.CUFF.getMaterial());
        blockedItems.add(RoleplayItem.SWAT_SHIELD.getMaterial());
        blockedItems.add(RoleplayItem.TAZER.getMaterial());
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
        if (playerData.getVariable("gangwar") != null) event.setCancelled(true);
        for (Weapon weapon : Weapon.values()) {
            if (weapon.getMaterial() == event.getItemDrop().getItemStack().getType()) {
                event.setCancelled(true);
                weaponManager.reloadWeapon(event.getPlayer(), event.getItemDrop().getItemStack());
            }
        }
    }
}
