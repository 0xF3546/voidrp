package de.polo.core.commands;

import de.polo.api.Utils.inventorymanager.CustomItem;
import de.polo.api.Utils.inventorymanager.InventoryManager;
import de.polo.api.gangwar.IGangzone;
import de.polo.core.Main;
import de.polo.core.faction.entity.Faction;
import de.polo.core.faction.service.impl.FactionManager;
import de.polo.core.manager.ItemManager;
import de.polo.core.manager.ServerManager;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.storage.Dealer;
import de.polo.core.utils.Prefix;
import de.polo.core.utils.Utils;
import de.polo.core.utils.enums.RoleplayItem;
import de.polo.core.utils.gameplay.GamePlay;
import lombok.SneakyThrows;
import net.kyori.adventure.text.Component;
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
    private final FactionManager factionManager;

    public DealerCommand(PlayerManager playerManager, GamePlay gamePlay, FactionManager factionManager) {
        this.playerManager = playerManager;
        this.gamePlay = gamePlay;
        this.factionManager = factionManager;
        Main.registerCommand("dealer", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player);
        for (Dealer dealer : gamePlay.getCurrentDealer()) {
            if (player.getLocation().distance(dealer.getLocation()) < 5) {
                open(player, dealer);
            }
        }
        return false;
    }

    private void open(Player player, Dealer dealer) {
        PlayerData playerData = playerManager.getPlayerData(player);
        Faction factionData = factionManager.getFactionData(dealer.getOwner());
        InventoryManager inventoryManager = new InventoryManager(player, 27, Component.text("§8 » §cDealer §8(§" + factionData.getPrimaryColor() + factionData.getName() + "§8)"), true, true);
        inventoryManager.setItem(new CustomItem(11, ItemManager.createItem(RoleplayItem.SNUFF.getMaterial(), 1, 0, RoleplayItem.SNUFF.getDisplayName(), "§8 ➥ §eBenötigt§8: §71 Joint")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (ItemManager.getCustomItemCount(player, RoleplayItem.PIPE) < 1 && ItemManager.getCustomItemCount(player, RoleplayItem.FACTION_PIPE) < 1) {
                    player.sendMessage(Prefix.ERROR + "Du hast davon nicht genug.");
                    player.closeInventory();
                    return;
                }
                if (ItemManager.getCustomItemCount(player, RoleplayItem.PIPE) >= 1) {
                    ItemManager.removeCustomItem(player, RoleplayItem.PIPE, 1);
                } else {
                    ItemManager.removeCustomItem(player, RoleplayItem.FACTION_PIPE, 1);
                }
                ItemManager.addCustomItem(player, RoleplayItem.SNUFF, 1);
            }
        });
        inventoryManager.setItem(new CustomItem(13, ItemManager.createItem(RoleplayItem.CIGAR.getMaterial(), 1, 0, RoleplayItem.CIGAR.getDisplayName(), "§8 ➥ §eBenötigt§8: §71 Joint")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (ItemManager.getCustomItemCount(player, RoleplayItem.PIPE) < 1 && ItemManager.getCustomItemCount(player, RoleplayItem.FACTION_PIPE) < 1) {
                    player.sendMessage(Prefix.ERROR + "Du hast davon nicht genug.");
                    player.closeInventory();
                    return;
                }
                if (ItemManager.getCustomItemCount(player, RoleplayItem.PIPE) >= 1) {
                    ItemManager.removeCustomItem(player, RoleplayItem.PIPE, 1);
                } else {
                    ItemManager.removeCustomItem(player, RoleplayItem.FACTION_PIPE, 1);
                }
                ItemManager.addCustomItem(player, RoleplayItem.CIGAR, 1);
            }
        });
        inventoryManager.setItem(new CustomItem(15, ItemManager.createItem(RoleplayItem.BOX_WITH_JOINTS.getMaterial(), 1, 0, "§a+" + dealer.getPrice() + "$", "§8 ➥ §eBenötigt§8: §71 Kiste mit Pfeifen")) {
            @SneakyThrows
            @Override
            public void onClick(InventoryClickEvent event) {
                if (ItemManager.getCustomItemCount(player, RoleplayItem.BOX_WITH_JOINTS) < 1) {
                    player.sendMessage(Prefix.ERROR + "Du hast davon nicht genug.");
                    player.closeInventory();
                    return;
                }
                if (!dealer.canSell()) {
                    player.sendMessage(Prefix.ERROR + "Der Dealer ist aktuell überfüllt.");
                    return;
                }
                Faction factionData = factionManager.getFactionData(playerData.getFaction());
                double amount = dealer.getPrice();
                if (Utils.getTime().getHour() >= 18 && Utils.getTime().getHour() < 22) {
                    amount = amount * 1.08;
                }
                long percentage = Math.round(amount * 0.1);
                factionData.addBankMoney((int) percentage, "Verkauf (Dealer - " + player.getName() + ")");
                amount = amount - (int) percentage;
                int cashOutAmount = (int) amount;
                ItemManager.removeCustomItem(player, RoleplayItem.BOX_WITH_JOINTS, 1);
                player.sendMessage("§8[§cDealer§8]§7 Aus dem Verkauf einer Box erhälst du §a" + cashOutAmount + "$§7. Es gehen §a" + percentage + "$§7 an deine Fraktion.");
                playerData.addMoney(cashOutAmount, "Verkauf-Dealer");
                player.closeInventory();
                soldAtDealer(dealer);

                IGangzone gangzone = Main.utils.gangwarUtils.getGangzoneByName(dealer.getGangzone());
                double ownerPercentages = 0.005;
                if (!gangzone.getOwner().equalsIgnoreCase(dealer.getOwner())) {
                    ownerPercentages = 0.002;
                    factionManager.addFactionMoney(gangzone.getOwner(), (int) (amount * 0.003), "Verkauf von Kisten an Dealer-" + dealer.getId() + " durch " + playerData.getFaction());
                }
                factionManager.addFactionMoney(gangzone.getOwner(), (int) (amount * ownerPercentages), "Verkauf von Kisten an Dealer-" + dealer.getId() + " durch " + playerData.getFaction());
            }
        });
        if (factionData.isBadFrak()) {
            inventoryManager.setItem(new CustomItem(18, ItemManager.createItem(Material.RED_DYE, 1, 0, "§cÜbernehmen")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    if (dealer.getOwner().equalsIgnoreCase(playerData.getFaction())) {
                        player.sendMessage(Prefix.ERROR + "Du kannst deinen eigenen Dealer nicht einschüchtern.");
                        return;
                    }
                    if (gamePlay.rob.containsKey(dealer)) {
                        player.sendMessage(Prefix.ERROR + "Jemand nimmt aktuell den Dealer ein!");
                        return;
                    }
                    if (!Utils.getTime().plusMinutes(60).isAfter(dealer.getLastAttack())) {
                        player.sendMessage(Prefix.ERROR + "Der Dealer wurde erst vor kurzem eingenommen!");
                        return;
                    }
                    dealer.setLastAttack(Utils.getTime());
                    player.closeInventory();
                    factionManager.sendCustomMessageToFaction(dealer.getOwner(), "§8[§cDealer-" + dealer.getGangzone() + "§8]§c Jemand versucht deinen Dealer zu übernehmen.");
                    factionManager.sendCustomMessageToFaction(playerData.getFaction(), "§8[§cDealer-" + dealer.getGangzone() + "§8]§a Ihr fangt an den Dealer zu übernehmen!");
                    player.sendMessage("§8[§cDealer§8]§7 Warte nun 10 Minuten, bleibe dabei in der nähe des Dealers.");
                    dealer.setAttacker(playerData.getFaction());
                    gamePlay.rob.put(dealer, 0);
                }
            });
        }
        inventoryManager.setItem(new CustomItem(26, ItemManager.createItem(Material.GOLD_NUGGET, 1, 0, "§6Ankauf")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openPurchase(player, dealer);
            }
        });
    }

    private void openPurchase(Player player, Dealer dealer) {
        InventoryManager inventoryManager = new InventoryManager(player, 27, Component.text("§8 » §cDealer (Ankauf)"), true, true);
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
                    player.sendMessage(Prefix.ERROR + "Du hast nicht gengu Perlen dabei.");
                    return;
                }
                player.sendMessage("§8[§cDealer§8]§a +" + pearlPrice + "$");
                ItemManager.removeCustomItem(player, RoleplayItem.PEARL, 1);
                playerManager.addMoney(player, pearlPrice, "Verkauf Perle");
            }
        });
        int diamondPrice = ServerManager.getPayout("dealer_diamond");
        inventoryManager.setItem(new CustomItem(14, ItemManager.createItem(Material.DIAMOND, 1, 0, "§bDiamant", "§8 ➥ §a" + diamondPrice + "$")) {
            @SneakyThrows
            @Override
            public void onClick(InventoryClickEvent event) {
                player.closeInventory();
                if (ItemManager.getItem(player, Material.DIAMOND) < 1) {
                    player.sendMessage(Prefix.ERROR + "Du hast nicht gengu Diamanten dabei.");
                    return;
                }
                player.sendMessage("§8[§cDealer§8]§a +" + diamondPrice + "$");
                ItemManager.removeCustomItem(player, RoleplayItem.DIAMOND, 1);
                playerManager.addMoney(player, diamondPrice, "Verkauf Diamant");
            }
        });
    }

    private void soldAtDealer(Dealer dealer) {
        boolean isSnitch = Utils.isRandom(3);
        dealer.setSold(dealer.getSold() + 1);
        if (!isSnitch) return;
        TextComponent message = new TextComponent("§8[§cInformant§8]§7 Jemand hat mir hier gerade Drogen verkauft. §8[§7Klick§8]");
        message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/navi " + (int) dealer.getLocation().getX() + " " + (int) dealer.getLocation().getY() + " " + (int) dealer.getLocation().getZ()));
        message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§a§oRoute verfolgen")));
        for (PlayerData playerData : playerManager.getPlayers()) {
            if (playerData.getFaction() == null) continue;
            if (playerData.getFaction().equalsIgnoreCase("FBI")) {
                playerData.getPlayer().spigot().sendMessage(message);
            }
        }
    }
}
