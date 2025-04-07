package de.polo.voidroleplay.jobs.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.VoidAPI;
import de.polo.voidroleplay.game.events.MinuteTickEvent;
import de.polo.voidroleplay.handler.CommandBase;
import de.polo.voidroleplay.jobs.enums.MiniJob;
import de.polo.voidroleplay.manager.ItemManager;
import de.polo.voidroleplay.player.entities.VoidPlayer;
import de.polo.voidroleplay.storage.ATM;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.utils.Utils;
import de.polo.voidroleplay.utils.enums.Prefix;
import de.polo.voidroleplay.utils.inventory.CustomItem;
import de.polo.voidroleplay.utils.inventory.InventoryManager;
import de.polo.voidroleplay.utils.player.SoundManager;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static de.polo.voidroleplay.Main.locationService;

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

    private final Material sewerCleanerMaterial = Material.DIRT;
    private final Material cleaningItem = Material.BUCKET;
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
        startJob(player);
    }

    private void startJob(VoidPlayer player) {
        player.setMiniJob(MiniJob.SEWER_CLEANER);
        player.getData().setVariable("job::cleaning::blocks", Utils.random(3, 6));
    }

    private void endJob(VoidPlayer player) {
        player.sendMessage("Du hast deinen Minijob beendet.", Prefix.INFO);
        player.setMiniJob(null);
    }

    private void openCleaningInventory(VoidPlayer player, Block block) {
        InventoryManager inventoryManager = new InventoryManager(player.getPlayer(), 54, "§7Reinigung ", true, true);
        for (int i = 0; i < Utils.random(12, 20); i++) {
            int cash = Utils.random(25, 50);
            inventoryManager.setItem(new CustomItem(Utils.random(0, 53), ItemManager.createItem(Material.DIRT, 1, 0, "§7Dreck")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    ItemStack item = event.getCurrentItem();
                    item.setType(Material.BLACK_STAINED_GLASS_PANE);
                    ItemMeta meta = item.getItemMeta();
                    meta.setDisplayName("§2");
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
                        block.setType(Material.STONE);
                        cleanedBlocks.add(block);
                        int newAmount = (int) player.getData().getVariable("job::cleaning::blocks") - 1;
                        player.getData().setVariable("job::cleaning::blocks", newAmount);
                        if (newAmount <= 0) endJob(player);
                        return;
                    }
                    player.getPlayer().closeInventory();
                    SoundManager.successSound(player.getPlayer());
                }
            });
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        VoidPlayer player = VoidAPI.getPlayer(event.getPlayer());
        if (!player.getMiniJob().equals(MiniJob.SEWER_CLEANER)) return;
        if (event.getClickedBlock().getType().equals(sewerCleanerMaterial)) {
            player.sendMessage("Du hast den Abwasserkanal gereinigt.", Prefix.INFO);
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
}
