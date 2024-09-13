package de.polo.voidroleplay.listeners;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.dataStorage.WeaponData;
import de.polo.voidroleplay.utils.PlayerManager;
import de.polo.voidroleplay.utils.Weapons;
import de.polo.voidroleplay.utils.enums.PickaxeType;
import de.polo.voidroleplay.utils.enums.RoleplayItem;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemDropListener implements Listener {
    private final Weapons weapons;
    private final PlayerManager playerManager;
    public ItemDropListener(Weapons weapons, PlayerManager playerManager) {
        this.weapons = weapons;
        this.playerManager = playerManager;
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }
    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        ItemStack droppedItem = event.getItemDrop().getItemStack();
        List<Material> blockedItems = new ArrayList<>();
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
        if (Arrays.stream(PickaxeType.values()).anyMatch(x -> x.getMaterial().equals(event.getPlayer().getInventory().getItemInMainHand().getType()))) {
            event.setCancelled(true);
            return;
        }
        PlayerData playerData = playerManager.getPlayerData(event.getPlayer());
        if (playerData.getVariable("gangwar") != null) event.setCancelled(true);
        WeaponData weaponData = Weapons.weaponDataMap.get(event.getItemDrop().getItemStack().getType());
        if (weaponData != null && event.getItemDrop().getItemStack().getItemMeta().getDisplayName().equalsIgnoreCase(weaponData.getName())) {
            event.setCancelled(true);
            weapons.reloadWeapon(event.getPlayer(), event.getItemDrop().getItemStack());
        }
    }
}
