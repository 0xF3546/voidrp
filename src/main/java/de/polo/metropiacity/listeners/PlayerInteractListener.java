package de.polo.metropiacity.listeners;

import com.jeff_media.customblockdata.CustomBlockData;
import de.polo.metropiacity.dataStorage.ATM;
import de.polo.metropiacity.dataStorage.HouseData;
import de.polo.metropiacity.dataStorage.PlayerData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.playerUtils.BankingUtils;
import de.polo.metropiacity.playerUtils.ChatUtils;
import de.polo.metropiacity.playerUtils.Rubbellose;
import de.polo.metropiacity.utils.Game.Housing;
import de.polo.metropiacity.utils.ItemManager;
import de.polo.metropiacity.utils.PlayerManager;
import de.polo.metropiacity.commands.MuellmannCommand;
import de.polo.metropiacity.commands.PostboteCommand;
import de.polo.metropiacity.utils.Utils;
import org.bukkit.*;
import org.bukkit.block.Sign;
import org.bukkit.block.TileState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Random;
import java.util.UUID;

public class PlayerInteractListener implements Listener {
    private final PlayerManager playerManager;
    private final Utils utils;
    private final Main.Commands commands;
    public PlayerInteractListener(PlayerManager playerManager, Utils utils, Main.Commands commands) {
        this.playerManager = playerManager;
        this.utils = utils;
        this.commands = commands;
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        playerData.setIntVariable("afk", 0);
        if (playerData.isAFK()) {
            utils.setAFK(player, false);
        }
        if (playerData.isDead()) {
            event.setCancelled(true);
            return;
        }
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (event.getClickedBlock() != null) {
                if (event.getClickedBlock().getType() == Material.CAULDRON) {
                    Material[] items = {Material.POTATO, Material.POISONOUS_POTATO, Material.GLASS_BOTTLE};
                    if (!Main.getInstance().getCooldownManager().isOnCooldown(player, "mülleimer")) {
                        Main.getInstance().getCooldownManager().setCooldown(player, "mülleimer", 30);
                        Material random = items[new Random().nextInt(items.length)];
                        player.getInventory().addItem(new ItemStack(random));
                        ChatUtils.sendGrayMessageAtPlayer(player, player.getName() + " duchwühlt einen Mülleimer.");
                        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1, 0);
                    } else {
                        String actionBarText = "§7Warte noch " + Main.getInstance().getCooldownManager().getRemainingTime(player, "mülleimer") + " Sekunden!";
                        player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, net.md_5.bungee.api.chat.TextComponent.fromLegacyText(actionBarText));
                    }
                }
                if (!(event.getClickedBlock().getState() instanceof TileState)) return;
                TileState state = (TileState) event.getClickedBlock().getState();
                if (state instanceof Sign) {
                    event.setCancelled(true);
                    Sign sign = (Sign) event.getClickedBlock().getState();
                    if (sign.getLine(1).contains("Bankautomat")) {
                        for (ATM atm : utils.bankingUtils.getATMs()) {
                            if (atm.getLocation().getX() == sign.getLocation().getX()
                                    && atm.getLocation().getY() == sign.getLocation().getY()
                                    && atm.getLocation().getZ() == sign.getLocation().getZ()) {
                                utils.bankingUtils.openBankMenu(player, atm);
                                return;
                            }
                        }
                        player.sendMessage(Main.error + "Dieser Automat wurde noch nicht registriert.");
                    }
                    PersistentDataContainer container = new CustomBlockData(event.getClickedBlock(), Main.plugin);
                    for (HouseData houseData : Housing.houseDataMap.values()) {
                        if (houseData.getNumber() == container.get(new NamespacedKey(Main.plugin, "value"), PersistentDataType.INTEGER)) {
                            Inventory inv = Bukkit.createInventory(player, 45, "");
                            playerData.setVariable("current_inventory", "haus");
                            playerData.setIntVariable("current_house", houseData.getNumber());
                            if (houseData.getOwner() != null) {
                                OfflinePlayer owner = Bukkit.getOfflinePlayer(UUID.fromString(houseData.getOwner()));
                                inv.setItem(13, ItemManager.createItemHead(houseData.getOwner(), 1, 0, "§6Besitzer", "§8 ➥ §7" + owner.getName()));
                                if (houseData.getOwner().equals(player.getUniqueId().toString())) {
                                    inv.setItem(33, ItemManager.createItem(Material.RED_DYE, 1, 0, "§cHaus verkaufen", "§8 ➥§7 Du erhälst: " + new DecimalFormat("#,###").format(houseData.getPrice() * 0.8) + "$"));
                                } else {
                                    inv.setItem(33, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§c§mHaus verkaufen", "§8 ➥§7 Dieses Haus gehört dir nicht."));
                                }
                            } else {
                                inv.setItem(13, ItemManager.createItem(Material.SKELETON_SKULL, 1, 0, "§7Kein Besitzer", null));
                                inv.setItem(33, ItemManager.createItem(Material.LIME_DYE, 1, 0, "§aHaus kaufen", "§8 ➥§e " + new DecimalFormat("#,###").format(houseData.getPrice()) + "$"));
                            }
                            inv.setItem(29, ItemManager.createItem(Material.PAPER, 1, 0, "§bInformationen", "Lädt..."));
                            ItemMeta meta = inv.getItem(29).getItemMeta();
                            meta.setLore(Arrays.asList("§8 ➥ §ePreis§8:§7 " + new DecimalFormat("#,###").format(houseData.getPrice()) + "$", "§8 ➥ §eUmsatz§8: §7" + new DecimalFormat("#,###").format(houseData.getTotalMoney()) + "$", "§8 ➥ §eMieterslots§8:§7 " + houseData.getTotalSlots()));
                            inv.getItem(29).setItemMeta(meta);
                            if (playerData.getVariable("job") == null) {
                                inv.setItem(31, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§7Kein Job", "§8 ➥§7 Du hast keinen passenden Job angenommen"));
                            } else {
                                if (!playerData.getVariable("job").toString().equalsIgnoreCase("postbote") && !playerData.getVariable("job").toString().equalsIgnoreCase("müllmann")) {
                                    inv.setItem(31, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§7Kein Job", "§8 ➥§7 Du hast keinen passenden Job angenommen"));
                                } else if (playerData.getVariable("job").toString().equalsIgnoreCase("postbote")) {
                                    if (commands.postboteCommand.canGive(houseData.getNumber())) {
                                        inv.setItem(31, ItemManager.createItem(Material.BOOK, 1, 0, "§ePost abgeben", null));
                                    } else {
                                        inv.setItem(31, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§7Haus bereits beliefert", null));
                                    }
                                } else if (playerData.getVariable("job").toString().equalsIgnoreCase("müllmann")) {
                                    if (commands.muellmannCommand.canGet(houseData.getNumber())) {
                                        inv.setItem(31, ItemManager.createItem(Material.CAULDRON, 1, 0, "§bMüll einsammeln", null));
                                    } else {
                                        inv.setItem(31, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§7Haus bereits geleert", null));
                                    }
                                }
                            }
                            for (int i = 0; i < 45; i++) {
                                if (inv.getItem(i) == null) {
                                    inv.setItem(i, ItemManager.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, 0, "§8", null));
                                }
                            }
                            player.openInventory(inv);
                        }
                    }
                }
            }


            //hier die items und nicht die blöcke


            if (event.getItem() == null) return;
            if (event.getItem().getItemMeta().getDisplayName().contains("Rubbellos")) {
                Rubbellose rubbellose = new Rubbellose(Main.getInstance().playerManager);
                rubbellose.startGame(player);
                ItemStack itemStack = event.getItem().clone();
                itemStack.setAmount(1);
                player.getInventory().removeItem(itemStack);
            } else if (event.getItem().getItemMeta().getDisplayName().contains("XP-Case")) {
                ItemStack itemStack = event.getItem().clone();
                itemStack.setAmount(1);
                player.getInventory().removeItem(itemStack);
                playerManager.addExp(player, Main.random(50, 200));
            } else if (event.getItem().getItemMeta().getDisplayName().equals("§6§lCase")) {
                //todo
            }
        }
    }
}
