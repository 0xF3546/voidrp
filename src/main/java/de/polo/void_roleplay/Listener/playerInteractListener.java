package de.polo.void_roleplay.Listener;

import com.jeff_media.customblockdata.CustomBlockData;
import de.polo.void_roleplay.DataStorage.HouseData;
import de.polo.void_roleplay.Main;
import de.polo.void_roleplay.PlayerUtils.rubbellose;
import de.polo.void_roleplay.Utils.Housing;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Sign;
import org.bukkit.block.TileState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.awt.*;
import java.util.Objects;

public class playerInteractListener implements Listener {
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (event.getClickedBlock() != null) {
                TileState state = (TileState) event.getClickedBlock().getState();
                if (state instanceof Sign) {
                    System.out.println("sign geklickt");
                    Sign sign = (Sign) event.getClickedBlock().getState();
                    PersistentDataContainer container = new CustomBlockData(event.getClickedBlock(), Main.plugin);
                    for (HouseData houseData : Housing.houseDataMap.values()) {
                        if (houseData.getNumber() == container.get(new NamespacedKey(Main.plugin, "value"), PersistentDataType.INTEGER)) {
                            System.out.println("sign gefunden");
                            if (houseData.getOwner() == null) {
                                System.out.println("owner ist null");
                                player.sendMessage("§8[§6Haus§8]§e Möchtest du Haus " + houseData.getNumber() + " für " + houseData.getPrice() + "$ kaufen?");
                                TextComponent route = new TextComponent("§8 ➥ §aKaufen");
                                route.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/buyhouse " + houseData.getNumber()));
                                route.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§a§oHaus " + houseData.getNumber() + " kaufen")));
                                player.spigot().sendMessage(route);
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
