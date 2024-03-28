package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.dataStorage.WeaponData;
import de.polo.voidroleplay.utils.InventoryManager.CustomItem;
import de.polo.voidroleplay.utils.InventoryManager.InventoryManager;
import de.polo.voidroleplay.utils.ItemManager;
import de.polo.voidroleplay.utils.Utils;
import de.polo.voidroleplay.utils.Weapons;
import de.polo.voidroleplay.utils.enums.RoleplayItem;
import de.polo.voidroleplay.utils.playerUtils.ChatUtils;
import de.polo.voidroleplay.utils.PlayerManager;
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

    public FriskCommand(PlayerManager playerManager) {
        this.playerManager = playerManager;
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
            for (WeaponData weaponData : Weapons.weaponDataMap.values()) {
                if (weaponData.getMaterial().equals(stack.getType())) {
                    items.add(weaponData);
                }
            }
            //todo: check for roleplay items
        }

        int iCount = Utils.roundUpToMultipleOfNine(items.size());

        InventoryManager inventoryManager = new InventoryManager(player, iCount, "§8 » §b" + targetplayer.getName() + " (" + items.size() + " Gegenstände)", true, true);
        int i = 0;
        for (ItemStack stack : items) {
            inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(stack.getType(), stack.getAmount(), 0, stack.getItemMeta().getDisplayName())) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    targetplayer.sendMessage("§8 » §7" + player.getName() + " hat dir " + stack.getItemMeta().getDisplayName() + " konfisziert.");
                    player.sendMessage("§8 » §7Du hast " + targetplayer.getName() + " " + stack.getItemMeta().getDisplayName() + " konfisziert.");
                    for (WeaponData data : Weapons.weaponDataMap.values()) {
                        if (stack.getType().equals(data.getMaterial()) && stack.getItemMeta().getDisplayName().equalsIgnoreCase(data.getName())) {
                            weapons.removeWeapon(player, stack);
                        } else {
                            targetplayer.getInventory().remove(stack);
                        }
                    }
                    openFriskInventory(player, targetplayer);
                }
            });
            i++;
        }
    }
}
