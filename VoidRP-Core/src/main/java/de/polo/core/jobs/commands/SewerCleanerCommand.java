package de.polo.core.jobs.commands;

import de.polo.api.Utils.ItemBuilder;
import de.polo.api.Utils.inventorymanager.CustomItem;
import de.polo.api.Utils.inventorymanager.InventoryManager;
import de.polo.core.Main;
import de.polo.api.VoidAPI;
import de.polo.core.game.events.MinuteTickEvent;
import de.polo.core.handler.CommandBase;
import de.polo.api.jobs.enums.MiniJob;
import de.polo.core.manager.ItemManager;
import de.polo.api.player.VoidPlayer;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.utils.Utils;
import de.polo.api.Utils.enums.Prefix;
import de.polo.core.utils.player.SoundManager;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static de.polo.core.Main.locationService;
import static de.polo.core.Main.playerManager;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
@CommandBase.CommandMeta(
        name = "sewercleaner",
        usage = "/sewercleaner"
)

public class SewerCleanerCommand extends CommandBase implements Listener {
    private final String PREFIX = "§9 " + MiniJob.SEWER_CLEANER.getName() + " §8┃ §8➜ §7";

    private final Material sewerCleanerMaterial = Material.DIRT;
    private final Material cleaningItem = Material.BRUSH;
    private final List<Block> cleanedBlocks = new ObjectArrayList<>();

    public SewerCleanerCommand(@NotNull CommandMeta meta) {
        super(meta);
        Main.registerListener(this);
    }

    @Override
    public void execute(@NotNull VoidPlayer player, @NotNull PlayerData playerData, @NotNull String[] args) throws Exception {
        if (locationService.getDistanceBetweenCoords(player, "sewercleaner") > 5) {
            player.sendMessage("Du bist nicht in der Nähe des Abwasserkanals.", Prefix.ERROR);
            return;
        }
        if (player.getMiniJob() != null) {
            player.sendMessage("Du hast bereits einen Minijob.", Prefix.ERROR);
            return;
        }
        openJobInventory(player);
    }

    private void openJobInventory(VoidPlayer player) {
        InventoryManager inventoryManager = new InventoryManager(player.getPlayer(), 27, Component.text("§8 » §3" + MiniJob.SEWER_CLEANER.getName()), true, true);
        ItemStack item = new ItemBuilder(Material.BRUSH).setName("§7Job starten").build();
        if (player.getMiniJob() != null) {
            item = new ItemBuilder(Material.BRUSH).setName("§7Job beenden").build();
        }
        inventoryManager.setItem(new CustomItem(13, item) {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (player.getMiniJob() != null) {
                    player.setMiniJob(null);
                    player.sendMessage(PREFIX + "Du hast den Job beendet.");
                    return;
                }
                startJob(player);
                event.getWhoClicked().closeInventory();
            }
        });
    }

    private void startJob(VoidPlayer player) {
        player.sendMessage(PREFIX + "Du hast den Job gestartet.");
        player.setMiniJob(MiniJob.SEWER_CLEANER);
        player.getData().setVariable("job::cleaning::blocks", Utils.random(3, 6));
        equip(player);
    }

    private void endJob(VoidPlayer player) {
        player.sendMessage(PREFIX + "Du hast den Job beendet.");
        player.sendMessage("Du hast deinen Minijob beendet.", Prefix.INFO);
        player.setMiniJob(null);
        unEquip(player);
    }

    private void openCleaningInventory(VoidPlayer player, Block block) {
        InventoryManager inventoryManager = new InventoryManager(player.getPlayer(), 54, Component.text("§7Reinigung"), true, true);
        for (int i = 0; i < Utils.random(12, 20); i++) {
            inventoryManager.setItem(new CustomItem(Utils.random(0, 53), new ItemBuilder(Material.DIRT).setName("§7Dreck").build()) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    ItemStack item = event.getCurrentItem();
                    item.setType(Material.BLACK_STAINED_GLASS_PANE);
                    ItemMeta meta = item.getItemMeta();
                    meta.displayName(Component.text("§2"));
                    item.setItemMeta(meta);
                    event.setCurrentItem(item);
                    int amount = 0;
                    for (ItemStack inventoryItem : event.getInventory().getContents()) {
                        if (inventoryItem.getType() == Material.DIRT) {
                            amount++;
                        }
                    }
                    if (amount > 0) {
                        SoundManager.openSound(player.getPlayer());
                        return;
                    }
                    block.setType(Material.STONE);
                    cleanedBlocks.add(block);
                    int newAmount = (int) player.getData().getVariable("job::cleaning::blocks") - 1;
                    player.getData().setVariable("job::cleaning::blocks", newAmount);
                    if (newAmount <= 0) endJob(player);
                    player.getPlayer().closeInventory();
                    SoundManager.successSound(player.getPlayer());
                }
            });
        }
    }

    private void equip(VoidPlayer voidPlayer) {
        ItemStack item = new ItemBuilder(cleaningItem).setName("§7Reinigungsgerät").build();
        voidPlayer.getPlayer().getInventory().addItem(item);
    }

    private void unEquip(VoidPlayer voidPlayer) {
        ItemManager.removeItem(voidPlayer.getPlayer(), cleaningItem, 1);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        VoidPlayer player = VoidAPI.getPlayer(event.getPlayer());
        if (!player.getMiniJob().equals(MiniJob.SEWER_CLEANER)) return;
        if (event.getClickedBlock().getType().equals(sewerCleanerMaterial)) {
            openCleaningInventory(player, event.getClickedBlock());
        }
    }

    @EventHandler
    public void onMinute(MinuteTickEvent event) {
        if (event.getMinute() % 2 == 0) {
            for (Block block : cleanedBlocks) {
                block.setType(sewerCleanerMaterial);
            }
            cleanedBlocks.clear();
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onQuit(PlayerQuitEvent event) {
        VoidPlayer player = VoidAPI.getPlayer(event.getPlayer());
        if (player.getMiniJob() != null) {
            endJob(player);
        }
    }
}
