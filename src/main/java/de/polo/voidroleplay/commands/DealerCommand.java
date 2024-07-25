package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.Dealer;
import de.polo.voidroleplay.dataStorage.FactionData;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.utils.*;
import de.polo.voidroleplay.utils.GamePlay.GamePlay;
import de.polo.voidroleplay.utils.InventoryManager.CustomItem;
import de.polo.voidroleplay.utils.InventoryManager.InventoryManager;
import de.polo.voidroleplay.utils.enums.RoleplayItem;
import lombok.SneakyThrows;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class DealerCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final GamePlay gamePlay;
    private final LocationManager locationManager;
    private final FactionManager factionManager;
    public DealerCommand(PlayerManager playerManager, GamePlay gamePlay, LocationManager locationManager, FactionManager factionManager) {
        this.playerManager = playerManager;
        this.gamePlay = gamePlay;
        this.locationManager = locationManager;
        this.factionManager = factionManager;
        Main.registerCommand("dealer", this);
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player);
        for (Dealer dealer : gamePlay.getDealer()) {
            if (locationManager.getDistanceBetweenCoords(player, "dealer-" + dealer.getId()) < 5) {
                open(player, dealer);
            }
        }
        return false;
    }
    private void open(Player player, Dealer dealer) {
        PlayerData playerData = playerManager.getPlayerData(player);
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8 » §cDealer", true, true);
        inventoryManager.setItem(new CustomItem(11, ItemManager.createItem(RoleplayItem.COCAINE.getMaterial(), 1, 0, RoleplayItem.COCAINE.getDisplayName(), "§8 ➥ §eBenötigt§8: §71 Joint")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (ItemManager.getCustomItemCount(player, RoleplayItem.JOINT) < 1 && ItemManager.getCustomItemCount(player, RoleplayItem.FACTION_JOINT) < 1) {
                    player.sendMessage(Main.error + "Du hast davon nicht genug.");
                    player.closeInventory();
                    return;
                }
                if (ItemManager.getCustomItemCount(player, RoleplayItem.JOINT) >= 1) {
                    ItemManager.removeCustomItem(player, RoleplayItem.JOINT, 1);
                } else {
                    ItemManager.removeCustomItem(player, RoleplayItem.FACTION_JOINT, 1);
                }
                ItemManager.addCustomItem(player, RoleplayItem.COCAINE, 1);
            }
        });
        inventoryManager.setItem(new CustomItem(13, ItemManager.createItem(RoleplayItem.NOBLE_JOINT.getMaterial(), 1, 0, RoleplayItem.NOBLE_JOINT.getDisplayName(), "§8 ➥ §eBenötigt§8: §71 Joint")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (ItemManager.getCustomItemCount(player, RoleplayItem.JOINT) < 1 && ItemManager.getCustomItemCount(player, RoleplayItem.FACTION_JOINT) < 1) {
                    player.sendMessage(Main.error + "Du hast davon nicht genug.");
                    player.closeInventory();
                    return;
                }
                if (ItemManager.getCustomItemCount(player, RoleplayItem.JOINT) >= 1) {
                    ItemManager.removeCustomItem(player, RoleplayItem.JOINT, 1);
                } else {
                    ItemManager.removeCustomItem(player, RoleplayItem.FACTION_JOINT, 1);
                }
                ItemManager.addCustomItem(player, RoleplayItem.NOBLE_JOINT, 1);
            }
        });
        inventoryManager.setItem(new CustomItem(15, ItemManager.createItem(RoleplayItem.BOX_WITH_JOINTS.getMaterial(), 1, 0, "§a+" + ServerManager.getPayout("box_dealer") + "$", "§8 ➥ §eBenötigt§8: §71 Kiste mit Joints")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (ItemManager.getCustomItemCount(player, RoleplayItem.BOX_WITH_JOINTS) < 1) {
                    player.sendMessage(Main.error + "Du hast davon nicht genug.");
                    player.closeInventory();
                    return;
                }
                FactionData factionData = factionManager.getFactionData(playerData.getFaction());
                double amount = ServerManager.getPayout("box_dealer");
                if (Utils.getTime().getHour() >= 18 && Utils.getTime().getHour() < 22) {
                    amount = amount * 1.08;
                }
                long percentage = Math.round(amount * 0.1);
                factionData.addBankMoney((int) percentage, "Verkauf (Dealer - " + player.getName() + ")");
                amount = amount - (int) percentage;
                int cashOutAmount = (int) amount;
                ItemManager.removeCustomItem(player, RoleplayItem.BOX_WITH_JOINTS, 1);
                player.sendMessage("§8[§cDealer§8]§7 Aus dem Verkauf einer Box erhälst du §a" + cashOutAmount + "$§7. Es gehen §a" + percentage + "$§7 an deine Fraktion.");
                playerData.addMoney(cashOutAmount);
                player.closeInventory();
                dealer.setLocation(player.getLocation());
                soldAtDealer(dealer);
            }
        });
        inventoryManager.setItem(new CustomItem(26, ItemManager.createItem(Material.GOLD_NUGGET, 1, 0, "§6Ankauf")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openPurchase(player, dealer);
            }
        });
    }

    private void openPurchase(Player player, Dealer dealer) {
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8 » §cDealer (Ankauf)", true, true);
        inventoryManager.setItem(new CustomItem(18, ItemManager.createItem(Material.NETHER_WART, 1, 0, "§cZurück")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                open(player, dealer);
            }
        });
        int pearlPrice = ServerManager.getPayout("dealer_pearl");
        inventoryManager.setItem(new CustomItem(12, ItemManager.createItem(Material.GHAST_TEAR, 1, 0, "§bPerle", "§8 ➥ §a" + pearlPrice + "$")) {
            @SneakyThrows
            @Override
            public void onClick(InventoryClickEvent event) {
                player.closeInventory();
                if (ItemManager.getCustomItemCount(player, RoleplayItem.PEARL) < 1) {
                    player.sendMessage(Main.error + "Du hast nicht gengu Perlen dabei.");
                    return;
                }
                player.sendMessage("§8[§cDealer§8]§a +" + pearlPrice + "$");
                ItemManager.removeCustomItem(player, RoleplayItem.PEARL, 1);
                playerManager.addMoney(player, pearlPrice);
            }
        });
        int diamondPrice = ServerManager.getPayout("dealer_diamond");
        inventoryManager.setItem(new CustomItem(14, ItemManager.createItem(Material.DIAMOND, 1, 0, "§bDiamant", "§8 ➥ §a" + diamondPrice + "$")) {
            @SneakyThrows
            @Override
            public void onClick(InventoryClickEvent event) {
                player.closeInventory();
                if (ItemManager.getItem(player, Material.DIAMOND) < 1) {
                    player.sendMessage(Main.error + "Du hast nicht gengu Diamanten dabei.");
                    return;
                }
                player.sendMessage("§8[§cDealer§8]§a +" + diamondPrice + "$");
                ItemManager.removeCustomItem(player, RoleplayItem.DIAMOND, 1);
                playerManager.addMoney(player, diamondPrice);
            }
        });
    }

    private void soldAtDealer(Dealer dealer) {
        boolean isSnitch = Utils.isRandom(7);
        if (!isSnitch) return;
        TextComponent message = new TextComponent("§8[§cInformant§8]§7 Jemand hat mir hier gerade Drogen verkauft. §8[§7Klick§8]");
        message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/navi " + (int) dealer.getLocation().getX() + " " + (int) dealer.getLocation().getY() + " " + (int) dealer.getLocation().getZ()));
        message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§a§oRoute verfolgen")));
        for (PlayerData playerData : playerManager.getPlayers()) {
            if (playerData.getFaction().equalsIgnoreCase("FBI")) {
                playerData.getPlayer().spigot().sendMessage(message);
            }
        }
    }
}
