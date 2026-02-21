package de.polo.core.jobs.commands;

import de.polo.api.utils.ItemBuilder;
import de.polo.api.utils.inventorymanager.CustomItem;
import de.polo.api.utils.inventorymanager.InventoryManager;
import de.polo.api.VoidAPI;
import de.polo.api.jobs.Job;
import de.polo.api.jobs.enums.MiniJob;
import de.polo.api.player.VoidPlayer;
import de.polo.core.Main;
import de.polo.core.game.events.MinuteTickEvent;
import de.polo.core.handler.CommandBase;
import de.polo.core.location.services.LocationService;
import de.polo.core.manager.ItemManager;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.player.services.PlayerService;
import de.polo.core.utils.Prefix;
import de.polo.core.utils.Utils;
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

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
@CommandBase.CommandMeta(
        name = "sewercleaner",
        usage = "/sewercleaner"
)
public class SewerCleanerCommand extends CommandBase implements Listener, Job {
    private final String PREFIX = "§9 " + MiniJob.SEWER_CLEANER.getName() + " §8┃ §8➜ §7";
    private final Material sewerCleanerMaterial = Material.ANDESITE;
    private final Material cleaningItem = Material.BRUSH;
    private final List<Block> cleanedBlocks = new ObjectArrayList<>();

    public SewerCleanerCommand(@NotNull CommandMeta meta) {
        super(meta);
        Main.registerListener(this);
    }

    @Override
    public void execute(@NotNull VoidPlayer player, @NotNull PlayerData playerData, @NotNull String[] args) throws Exception {
        LocationService locationService = VoidAPI.getService(LocationService.class);
        PlayerService playerService = VoidAPI.getService(PlayerService.class);
        if (locationService.getDistanceBetweenCoords(player, "sewercleaner") > 5) {
            player.sendMessage(Prefix.ERROR + "Du bist nicht in der Nähe des Abwasserkanals.");
            return;
        }

        InventoryManager inventoryManager = new InventoryManager(player.getPlayer(), 27, Component.text("§8 » §3" + MiniJob.SEWER_CLEANER.getName()), true, true);

        // Start Job Option
        if (!playerService.isInJobCooldown(player, MiniJob.SEWER_CLEANER) && player.getActiveJob() == null) {
            inventoryManager.setItem(new CustomItem(11, ItemManager.createItem(Material.LIME_DYE, 1, 0, "§aJob starten")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    startJob(player);
                    player.getPlayer().closeInventory();
                }
            });
        } else {
            if (player.getActiveJob() == null) {
                inventoryManager.setItem(new CustomItem(11, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§a§mJob starten", "§8 ➥§7 Warte noch " + Utils.getTime(playerService.getJobCooldown(player, MiniJob.SEWER_CLEANER)) + "§7.")) {
                    @Override
                    public void onClick(InventoryClickEvent event) {
                    }
                });
            } else {
                inventoryManager.setItem(new CustomItem(11, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§a§mJob starten", "§8 ➥§7 Du hast bereits den §f" + player.getMiniJob().getName() + "§7 Job angenommen.")) {
                    @Override
                    public void onClick(InventoryClickEvent event) {
                    }
                });
            }
        }

        // Quit Job Option
        if (player.getActiveJob() == null) {
            inventoryManager.setItem(new CustomItem(15, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§e§mJob beenden", "§8 ➥§7 Du hast den Job nicht angenommen")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                }
            });
        } else if (!player.getMiniJob().equals(MiniJob.SEWER_CLEANER)) {
            inventoryManager.setItem(new CustomItem(15, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§e§mJob beenden", "§8 ➥§7 Du hast den Job nicht angenommen")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                }
            });
        } else {
            int remainingBlocks = player.getVariable("job::cleaning::blocks") != null ? (int) player.getVariable("job::cleaning::blocks") : 0;
            String payoutText = remainingBlocks > 0 ?
                    "§8 ➥ §7Noch " + remainingBlocks + " Blöcke zu reinigen" :
                    "§8 ➥ §7Alle Blöcke gereinigt";
            inventoryManager.setItem(new CustomItem(15, ItemManager.createItem(Material.YELLOW_DYE, 1, 0, "§eJob beenden", payoutText)) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    player.getPlayer().closeInventory();
                    endJob(player);
                }
            });
        }
    }

    @Override
    public void startJob(VoidPlayer player) {
        PlayerService playerService = VoidAPI.getService(PlayerService.class);
        if (!playerService.isInJobCooldown(player, MiniJob.SEWER_CLEANER)) {
            player.setMiniJob(MiniJob.SEWER_CLEANER);
            player.setActiveJob(this);

            int blocksToClean = Utils.random(3, 6);
            player.setVariable("job::cleaning::blocks", blocksToClean);

            player.sendMessage(PREFIX + "Du hast den Job gestartet.");
            player.sendMessage(PREFIX + "Reinige §e" + blocksToClean + " Abwasserblöcke§7 mit dem Reinigungsgerät.");
            equip(player);
        } else {
            player.sendMessage(PREFIX + "Du kannst den Job erst in §f" + Utils.getTime(playerService.getJobCooldown(player, MiniJob.SEWER_CLEANER)) + "§7 beginnen.");
        }
    }

    @Override
    public void endJob(VoidPlayer player) {
        int cleanedCount = (int) player.getVariable("job::cleaning::blocks");
        boolean completed = cleanedCount <= 0;
        PlayerService playerService = VoidAPI.getService(PlayerService.class);

        player.sendMessage(PREFIX + "Du hast den Job beendet.");
        SoundManager.successSound(player.getPlayer());

        if (completed) {
            int payout = Utils.random(50, 100);
            player.sendMessage(PREFIX + "Alle Blöcke gereinigt! §a+" + payout + "$");
            player.getData().addBankMoney(payout, "Auszahlung Sewer Cleaner");
            playerService.addExp(player.getPlayer(), Utils.random(12, 20));
            playerService.handleJobFinish(player, MiniJob.SEWER_CLEANER, 720, Utils.random(12, 20));
        } else {
            playerService.handleJobFinish(player, MiniJob.SEWER_CLEANER, 720, 0);
        }

        unEquip(player);
        player.setMiniJob(null);
        player.setActiveJob(null);
        player.setVariable("job::cleaning::blocks", null);
        cleanedBlocks.clear();
    }

    private void openCleaningInventory(VoidPlayer player, Block block) {
        Material[] materials = {Material.DIRT, Material.STRING, Material.DEAD_FIRE_CORAL};
        InventoryManager inventoryManager = new InventoryManager(player.getPlayer(), 54, Component.text("§7Reinigung"), true, true);

        int dirtCount = Utils.random(12, 20);
        for (int i = 0; i < dirtCount; i++) {
            inventoryManager.setItem(new CustomItem(Utils.random(0, 53), new ItemBuilder(materials[Utils.random(0, materials.length - 1)]).setName("§7Dreck").build()) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    ItemStack item = event.getCurrentItem();
                    item.setType(Material.BLACK_STAINED_GLASS_PANE);
                    ItemMeta meta = item.getItemMeta();
                    meta.displayName(Component.text("§2"));
                    item.setItemMeta(meta);
                    event.setCurrentItem(item);

                    int remainingDirt = 0;
                    for (ItemStack inventoryItem : event.getInventory().getContents()) {
                        if (inventoryItem != null && inventoryItem.getType() == Material.DIRT) {
                            remainingDirt++;
                        }
                    }

                    if (remainingDirt > 0) {
                        SoundManager.openSound(player.getPlayer());
                        return;
                    }

                    block.setType(Material.STONE);
                    cleanedBlocks.add(block);
                    int newAmount = (int) player.getVariable("job::cleaning::blocks") - 1;
                    player.setVariable("job::cleaning::blocks", newAmount);

                    player.sendMessage(PREFIX + "Block gereinigt! Noch §e" + newAmount + " Blöcke§7 übrig.");
                    SoundManager.successSound(player.getPlayer());

                    if (newAmount <= 0) {
                        endJob(player);
                    }
                    player.getPlayer().closeInventory();
                }
            });
        }
    }

    private void equip(VoidPlayer player) {
        ItemStack item = new ItemBuilder(cleaningItem).setName("§7Reinigungsgerät").build();
        player.getPlayer().getInventory().addItem(item);
    }

    private void unEquip(VoidPlayer player) {
        for (ItemStack item : player.getPlayer().getInventory().getContents()) {
            if (item != null && item.getType() == cleaningItem &&
                    item.hasItemMeta() && item.getItemMeta().getDisplayName().equals("§7Reinigungsgerät")) {
                player.getPlayer().getInventory().removeItem(item);
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        VoidPlayer player = VoidAPI.getPlayer(event.getPlayer());
        if (player.getMiniJob() != MiniJob.SEWER_CLEANER) return;
        if (event.getClickedBlock() != null && event.getClickedBlock().getType() == sewerCleanerMaterial) {
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

    @EventHandler(priority = EventPriority.LOW)
    public void onQuit(PlayerQuitEvent event) {
        VoidPlayer player = VoidAPI.getPlayer(event.getPlayer());
        if (player.getMiniJob() == MiniJob.SEWER_CLEANER) {
            endJob(player);
        }
    }
}