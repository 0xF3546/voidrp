package de.polo.core.faction.commands;

import de.polo.core.Main;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.storage.WeaponData;
import de.polo.core.faction.service.impl.FactionManager;
import de.polo.core.utils.inventory.CustomItem;
import de.polo.core.utils.inventory.InventoryManager;
import de.polo.core.manager.ItemManager;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.manager.WeaponManager;
import de.polo.core.utils.Prefix;
import de.polo.core.utils.StaatUtil;
import de.polo.core.utils.Utils;
import de.polo.core.utils.enums.RoleplayItem;
import de.polo.core.utils.enums.Weapon;
import de.polo.core.utils.player.ChatUtils;
import de.polo.core.utils.player.PlayerInventoryItem;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class FriskCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final WeaponManager weaponManager;
    private final FactionManager factionManager;

    public FriskCommand(PlayerManager playerManager, WeaponManager weaponManager, FactionManager factionManager) {
        this.playerManager = playerManager;
        this.weaponManager = weaponManager;
        this.factionManager = factionManager;

        Main.registerCommand("frisk", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player);
        if (!playerData.getFaction().equalsIgnoreCase("Polizei") && !playerData.getFaction().equalsIgnoreCase("FBI")) {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
            return false;
        }
        if (args.length < 1) {
            player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /frisk [Spieler]");
            return false;
        }
        Player targetplayer = Bukkit.getPlayer(args[0]);
        if (targetplayer == null) {
            player.sendMessage(Prefix.ERROR + targetplayer.getName() + " ist nicht online.");
            return false;
        }
        if (targetplayer.getName().equals(player.getName())) {
            player.sendMessage(Prefix.ERROR + "Du kannst dich nicht selbst durchsuchen.");
            return false;
        }
        if (player.getLocation().distance(targetplayer.getLocation()) > 5) {
            player.sendMessage(Prefix.ERROR + targetplayer.getName() + " ist nicht in deiner nähe.");
            return false;
        }
        PlayerData targetData = playerManager.getPlayerData(targetplayer);
        if (!targetData.isCuffed()) {
            player.sendMessage(Prefix.ERROR + targetplayer.getName() + " ist nicht gefesselt oder in Handschellen.");
            return false;
        }
        openFriskInventory(player, targetplayer);
        ChatUtils.sendGrayMessageAtPlayer(player, player.getName() + " durchsucht " + targetplayer.getName());
        return false;
    }

    private void openFriskInventory(Player player, Player targetplayer) {
        if (player.getLocation().distance(targetplayer.getLocation()) >= 5) {
            player.closeInventory();
            player.sendMessage(Component.text(Prefix.ERROR + targetplayer.getName() + " ist nicht in deiner nähe."));
            return;
        }
        PlayerData playerData = playerManager.getPlayerData(targetplayer);
        List<ItemStack> items = new ObjectArrayList<>();
        List<PlayerInventoryItem> playerInventoryItems = new ObjectArrayList<>();
        for (ItemStack stack : targetplayer.getInventory().getContents()) {
            if (stack == null) continue;
            for (Weapon weaponData : Weapon.values()) {
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
        for (PlayerInventoryItem item : playerData.getInventory().getItems()) {
            if (!item.getItem().isFriskItem()) continue;
            playerInventoryItems.add(item);
        }

        int iCount = Utils.roundUpToMultipleOfNine(items.size() + playerInventoryItems.size());
        if (iCount == 0) iCount = 9;

        InventoryManager inventoryManager = new InventoryManager(player, iCount, "§8 » §b" + targetplayer.getName() + " (" + items.size() + " Gegenstände)", true, true);
        int i = 0;
        for (ItemStack stack : items) {
            inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(stack.getType(), stack.getAmount(), 0, stack.getItemMeta().getDisplayName())) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    targetplayer.sendMessage("§8 » §7" + factionManager.getTitle(player) + " " + player.getName() + " hat dir " + stack.getItemMeta().getDisplayName() + "§7 konfisziert.");
                    player.sendMessage("§8 » §7Du hast " + targetplayer.getName() + " " + stack.getItemMeta().getDisplayName() + "§7 konfisziert.");
                    boolean isWeapon = false;
                    for (WeaponData data : WeaponManager.weaponDataMap.values()) {
                        if (stack.getType().equals(data.getMaterial()) && stack.getItemMeta().getDisplayName().equalsIgnoreCase(data.getName())) {
                            weaponManager.removeWeapon(player, stack);
                            isWeapon = true;
                        }
                    }
                    targetplayer.getInventory().remove(stack);
                    openFriskInventory(player, targetplayer);
                }
            });
            i++;
        }
        for (PlayerInventoryItem item : playerInventoryItems) {
            inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(item.getItem().getMaterial(), item.getAmount(), 0, item.getItem().getDisplayName())) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    targetplayer.sendMessage("§8 » §7" + factionManager.getTitle(player) + " " + player.getName() + " hat dir " + item.getItem().getDisplayName() + "§7 konfisziert.");
                    player.sendMessage("§8 » §7Du hast " + targetplayer.getName() + " " + item.getItem().getDisplayName() + "§7 konfisziert.");
                    if (item.getItem().equals(RoleplayItem.PIPE)) {
                        StaatUtil.Asservatemkammer.setJoints(StaatUtil.Asservatemkammer.getJoints() + item.getAmount());
                    }
                    if (item.getItem().equals(RoleplayItem.SNUFF)) {
                        StaatUtil.Asservatemkammer.setCocaine(StaatUtil.Asservatemkammer.getCocaine() + item.getAmount());
                    }
                    if (item.getItem().equals(RoleplayItem.PIPE_TOBACCO)) {
                        StaatUtil.Asservatemkammer.setWeed(StaatUtil.Asservatemkammer.getWeed() + item.getAmount());
                    }
                    if (item.getItem().equals(RoleplayItem.CIGAR)) {
                        StaatUtil.Asservatemkammer.setNoble_joints(StaatUtil.Asservatemkammer.getNoble_joints() + item.getAmount());
                    }
                    playerData.getInventory().removeItem(item);
                    openFriskInventory(player, targetplayer);
                    StaatUtil.Asservatemkammer.save();
                }
            });
            i++;
        }
    }
}
