package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.dataStorage.WeaponData;
import de.polo.voidroleplay.utils.*;
import de.polo.voidroleplay.utils.Game.EvidenceChamber;
import de.polo.voidroleplay.utils.InventoryManager.CustomItem;
import de.polo.voidroleplay.utils.InventoryManager.InventoryManager;
import de.polo.voidroleplay.utils.enums.RoleplayItem;
import de.polo.voidroleplay.utils.playerUtils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class FriskCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final Weapons weapons;
    private final FactionManager factionManager;

    public FriskCommand(PlayerManager playerManager, Weapons weapons, FactionManager factionManager) {
        this.playerManager = playerManager;
        this.weapons = weapons;
        this.factionManager = factionManager;

        Main.registerCommand("frisk", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player);
        if (!playerData.getFaction().equalsIgnoreCase("Polizei") && !playerData.getFaction().equalsIgnoreCase("FBI")) {
            player.sendMessage(Main.error_nopermission);
            return false;
        }
        if (args.length < 1) {
            player.sendMessage(Main.error + "Syntax-Fehler: /frisk [Spieler]");
            return false;
        }
        Player targetplayer = Bukkit.getPlayer(args[0]);
        if (targetplayer == null) {
            player.sendMessage(Main.error + targetplayer.getName() + " ist nicht online.");
            return false;
        }
        if (targetplayer.getName().equals(player.getName())) {
            player.sendMessage(Main.error + "Du kannst dich nicht selbst durchsuchen.");
            return false;
        }
        if (player.getLocation().distance(targetplayer.getLocation()) > 5) {
            player.sendMessage(Main.error + targetplayer.getName() + " ist nicht in deiner nähe.");
            return false;
        }
        PlayerData targetData = playerManager.getPlayerData(targetplayer);
        if (!targetData.isCuffed()) {
            player.sendMessage(Main.error + targetplayer.getName() + " ist nicht gefesselt oder in Handschellen.");
            return false;
        }
        openFriskInventory(player, targetplayer);
        ChatUtils.sendGrayMessageAtPlayer(player, player.getName() + " durchsucht " + targetplayer.getName());
        return false;
    }

    private void openFriskInventory(Player player, Player targetplayer) {
        List<ItemStack> items = new ArrayList<>();
        for (ItemStack stack : targetplayer.getInventory().getContents()) {
            if (stack == null) continue;
            for (WeaponData weaponData : Weapons.weaponDataMap.values()) {
                if (weaponData.getMaterial() == null) continue;
                if (weaponData.getMaterial().equals(stack.getType())) {
                    items.add(stack);
                }
            }
            for (RoleplayItem item : RoleplayItem.values()) {
                if (item.getDisplayName().equalsIgnoreCase(stack.getItemMeta().getDisplayName()) && item.getMaterial().equals(stack.getType())) {
                    if (item.isFriskItem()) {
                        items.add(stack);
                    }
                }
            }
        }

        int iCount = Utils.roundUpToMultipleOfNine(items.size());
        if (iCount == 0) iCount = 9;

        InventoryManager inventoryManager = new InventoryManager(player, iCount, "§8 » §b" + targetplayer.getName() + " (" + items.size() + " Gegenstände)", true, true);
        int i = 0;
        for (ItemStack stack : items) {
            inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(stack.getType(), stack.getAmount(), 0, stack.getItemMeta().getDisplayName())) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    targetplayer.sendMessage("§8 » §7" + factionManager.getTitle(player) + " "  + player.getName() + " hat dir " + stack.getItemMeta().getDisplayName() + "§7 konfisziert.");
                    player.sendMessage("§8 » §7Du hast " + targetplayer.getName() + " " + stack.getItemMeta().getDisplayName() + "§7 konfisziert.");
                    boolean isWeapon = false;
                    for (WeaponData data : Weapons.weaponDataMap.values()) {
                        if (stack.getType().equals(data.getMaterial()) && stack.getItemMeta().getDisplayName().equalsIgnoreCase(data.getName())) {
                            weapons.removeWeapon(player, stack);
                            isWeapon = true;
                        }
                    }
                    if (stack.getType().equals(RoleplayItem.JOINT.getMaterial()) && stack.getItemMeta().getDisplayName().equalsIgnoreCase(RoleplayItem.JOINT.getDisplayName())) {
                        StaatUtil.Asservatemkammer.setJoints(StaatUtil.Asservatemkammer.getJoints() + stack.getAmount());
                    }
                    if (stack.getType().equals(RoleplayItem.COCAINE.getMaterial()) && stack.getItemMeta().getDisplayName().equalsIgnoreCase(RoleplayItem.COCAINE.getDisplayName())) {
                        StaatUtil.Asservatemkammer.setCocaine(StaatUtil.Asservatemkammer.getCocaine() + stack.getAmount());
                    }
                    if (stack.getType().equals(RoleplayItem.MARIHUANA.getMaterial()) && stack.getItemMeta().getDisplayName().equalsIgnoreCase(RoleplayItem.MARIHUANA.getDisplayName())) {
                        StaatUtil.Asservatemkammer.setWeed(StaatUtil.Asservatemkammer.getWeed() + stack.getAmount());
                    }
                    if (stack.getType().equals(RoleplayItem.NOBLE_JOINT.getMaterial()) && stack.getItemMeta().getDisplayName().equalsIgnoreCase(RoleplayItem.NOBLE_JOINT.getDisplayName())) {
                        StaatUtil.Asservatemkammer.setNoble_joints(StaatUtil.Asservatemkammer.getNoble_joints() + stack.getAmount());
                    }
                    targetplayer.getInventory().remove(stack);
                    openFriskInventory(player, targetplayer);
                    StaatUtil.Asservatemkammer.save();
                }
            });
            i++;
        }
    }
}
