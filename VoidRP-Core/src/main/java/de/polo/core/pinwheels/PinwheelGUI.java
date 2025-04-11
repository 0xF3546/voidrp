package de.polo.core.pinwheels;

import de.polo.api.Utils.ItemBuilder;
import de.polo.api.Utils.inventorymanager.CustomItem;
import de.polo.api.Utils.inventorymanager.InventoryManager;
import de.polo.api.VoidAPI;
import de.polo.api.jobs.enums.MiniJob;
import de.polo.api.pinwheels.Pinwheel;
import de.polo.api.player.VoidPlayer;
import de.polo.core.player.services.PlayerService;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class PinwheelGUI {
    private final String PREFIX = "§aWindrad §8┃ §8➜ §7";
    private final VoidPlayer player;
    private final Pinwheel pinwheel;

    public PinwheelGUI(VoidPlayer player, Pinwheel pinwheel) {
        this.player = player;
        this.pinwheel = pinwheel;
    }

    public void open() {
        InventoryManager inventoryManager = new InventoryManager(player.getPlayer(), 9, Component.text("§8 » §aWindrad " + pinwheel.getName()));
        if (pinwheel.isBroken()) {
            for (int i = 0; i < 9; i++) {
                inventoryManager.setItem(new CustomItem(i, new ItemBuilder(Material.RED_STAINED_GLASS_PANE)
                        .setName(Component.text("§8 » §cWindrad ist defekt"))
                        .build()) {
                    @Override
                    public void onClick(InventoryClickEvent event) {
                        PlayerService playerService = VoidAPI.getService(PlayerService.class);
                        if (playerService.isInJobCooldown(player, MiniJob.ELECTRITION)) {
                            player.getPlayer().sendMessage(PREFIX + "Ruhe dich etwas aus und repariere das Windrad später.");
                            return;
                        }
                        openJob();
                    }
                });
            }
        } else {
            for (int i = 0; i < 9; i++) {
                inventoryManager.setItem(new CustomItem(i, new ItemBuilder(Material.GREEN_STAINED_GLASS_PANE)
                        .setName(Component.text("§8 » §aWindrad ist in betrieb"))
                        .build()) {
                    @Override
                    public void onClick(InventoryClickEvent event) {

                    }
                });
            }
        }
    }

    public void openJob() {
        player.setVariable("inventory::base", player.getPlayer().getInventory().getContents());
        player.getPlayer().getInventory().clear();
        InventoryManager inventoryManager = new InventoryManager(player.getPlayer(), 54, Component.text("§8 » §aWindrad " + pinwheel.getName()));
        inventoryManager.setOnClose(() -> {
            player.getPlayer().getInventory().clear();
            player.getPlayer().getInventory().setContents((ItemStack[]) player.getVariable("inventory::base"));
            player.setVariable("inventory::base", null);
        });
        inventoryManager.setOnDrop(event -> {
            event.setCancelled(true);
        });
        for (int i = 0; i < 54; i++) {
            inventoryManager.setItem(new CustomItem(i, new ItemBuilder(Material.RED_STAINED_GLASS_PANE)
                    .setName(Component.text("§8 » §cWindrad reparieren"))
                    .build()) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    if (pinwheel.isBroken()) {
                        pinwheel.setBroken(false);
                        player.getPlayer().sendMessage(Component.text("§8 » §aWindrad repariert"));
                        player.getPlayer().closeInventory();
                        player.sendMessage(PREFIX + "Du hast das Windrad repariert.");
                        PlayerService playerService = VoidAPI.getService(PlayerService.class);
                        playerService.handleJobFinish(player, MiniJob.ELECTRITION, 1200, 20);
                    } else {
                        player.getPlayer().sendMessage(Component.text("§8 » §cWindrad ist bereits repariert"));
                    }
                }
            });
        }
    }
}
