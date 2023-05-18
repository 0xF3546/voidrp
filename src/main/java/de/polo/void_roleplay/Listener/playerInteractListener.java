package de.polo.void_roleplay.Listener;

import com.jeff_media.customblockdata.CustomBlockData;
import de.polo.void_roleplay.DataStorage.HouseData;
import de.polo.void_roleplay.DataStorage.PlayerData;
import de.polo.void_roleplay.Main;
import de.polo.void_roleplay.PlayerUtils.BankingUtils;
import de.polo.void_roleplay.PlayerUtils.ChatUtils;
import de.polo.void_roleplay.PlayerUtils.rubbellose;
import de.polo.void_roleplay.Utils.Housing;
import de.polo.void_roleplay.Utils.ItemManager;
import de.polo.void_roleplay.Utils.PlayerManager;
import jdk.javadoc.internal.doclint.HtmlTag;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
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

public class playerInteractListener implements Listener {
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (event.getClickedBlock() != null) {
                if (event.getClickedBlock().getType() == Material.CAULDRON) {
                    Material[] items = {Material.POTATO, Material.POISONOUS_POTATO, Material.GLASS_BOTTLE};
                    if (!Main.cooldownManager.isOnCooldown(player, "mülleimer")) {
                        Main.cooldownManager.setCooldown(player, "mülleimer", 30);
                        Material random = items[new Random().nextInt(items.length)];
                        player.getInventory().addItem(new ItemStack(random));
                        ChatUtils.sendGrayMessageAtPlayer(player, player.getName() + " duchwühlt einen Mülleimer.");
                        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1, 0);
                    } else {
                        String actionBarText = "§7Warte noch " + Main.cooldownManager.getRemainingTime(player, "mülleimer") + " Sekunden!";
                        player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, net.md_5.bungee.api.chat.TextComponent.fromLegacyText(actionBarText));
                    }
                }
                TileState state = (TileState) event.getClickedBlock().getState();
                if (state instanceof Sign) {
                    Sign sign = (Sign) event.getClickedBlock().getState();
                    if (sign.getLine(1).contains("Bankautomat")) {
                        BankingUtils.openBankMenu(player);
                    }
                    PersistentDataContainer container = new CustomBlockData(event.getClickedBlock(), Main.plugin);
                    for (HouseData houseData : Housing.houseDataMap.values()) {
                        if (houseData.getNumber() == container.get(new NamespacedKey(Main.plugin, "value"), PersistentDataType.INTEGER)) {
                            if (houseData.getOwner() == null) {
                                player.sendMessage("§8[§6Haus§8]§e Möchtest du Haus " + houseData.getNumber() + " für " + houseData.getPrice() + "$ kaufen?");
                                TextComponent route = new TextComponent("§8 ➥ §aKaufen");
                                route.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/buyhouse " + houseData.getNumber()));
                                route.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§a§oHaus " + houseData.getNumber() + " kaufen")));
                                player.spigot().sendMessage(route);
                            } else {
                                Inventory inv = Bukkit.createInventory(player, 45, "");
                                OfflinePlayer owner = Bukkit.getOfflinePlayer(UUID.fromString(houseData.getOwner()));
                                PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
                                playerData.setVariable("current_inventory", "haus");
                                playerData.setIntVariable("current_house", houseData.getNumber());
                                inv.setItem(13, ItemManager.createItemHead(houseData.getOwner(), 1, 0, "§6Besitzer", "§8 ➥ §7" + owner.getName()));
                                inv.setItem(29, ItemManager.createItem(Material.PAPER, 1, 0, "§bInformationen", "Lädt..."));
                                ItemMeta meta = inv.getItem(29).getItemMeta();
                                meta.setLore(Arrays.asList("§8 ➥ §ePreis§8:§7 " + new DecimalFormat("#,###").format(houseData.getPrice()) + "$", "§8 ➥ §eUmsatz§8: §7" + new DecimalFormat("#,###").format(houseData.getTotalMoney()) + "$", "§8 ➥ §eMieterslots§8:§7 " + houseData.getTotalSlots()));
                                inv.getItem(29).setItemMeta(meta);
                                if (houseData.getOwner().equals(player.getUniqueId().toString())) {
                                    inv.setItem(33, ItemManager.createItem(Material.RED_DYE, 1, 0, "§cHaus verkaufen", "§8 ➥§7 Du erhälst: " + new DecimalFormat("#,###").format(houseData.getPrice() * 0.8) + "$" ));
                                } else {
                                    inv.setItem(33, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§c§mHaus verkaufen", "§8 ➥§7 Dieses Haus gehört dir nicht."));
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
            }


            //hier die items und nicht die blöcke


            if (event.getItem() == null) return;
            if (event.getItem().getItemMeta().getDisplayName().contains("Rubbellos")) {
                rubbellose.startGame(player);
                ItemStack itemStack = event.getItem().clone();
                itemStack.setAmount(1);
                player.getInventory().removeItem(itemStack);
            }
        }
    }
}
