package de.polo.core.laboratory.gui;

import de.polo.api.utils.GUI;
import de.polo.api.utils.ItemBuilder;
import de.polo.api.utils.inventorymanager.CustomItem;
import de.polo.api.utils.inventorymanager.InventoryManager;
import de.polo.api.laboratory.Laboratory;
import de.polo.api.player.VoidPlayer;
import de.polo.core.utils.Utils;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class LaboratoryGUI implements GUI {
    private final VoidPlayer player;
    private final Laboratory laboratory;
    public LaboratoryGUI(VoidPlayer player, Laboratory laboratory) {
        this.player = player;
        this.laboratory = laboratory;
    }
    @Override
    public void open() {
        InventoryManager inventoryManager = new InventoryManager(player.getPlayer(), 9, Component.text("§8 » §cLabor §8| §a" + laboratory.getType().getName()));
        inventoryManager.setItem(new CustomItem(0, new ItemBuilder(Material.PAPER)
                .setName("§aBesitzer")
                .setLore(
                        List.of("§8▎ §7Besitzer: " + laboratory.getFaction().getFullname(),
                                "§8▎ §7Letzter Angriff: " + Utils.localDateTimeToReadableString(laboratory.getLastAttack()))
                )
                .build()) {
            @Override
            public void onClick(InventoryClickEvent event) {

            }
        });
        switch (laboratory.getType()) {
            case DRUG_LAB -> {
                inventoryManager.setItem(new CustomItem(1, new ItemBuilder(Material.PAPER)
                        .setName("§aDrogenlabor öffnen")
                        .build()) {
                    @Override
                    public void onClick(InventoryClickEvent event) {
                        openDrugLab();
                    }
                });
            }
            case MONEY_LAB -> {
                inventoryManager.setItem(new CustomItem(1, new ItemBuilder(Material.PAPER)
                        .setName("§aGeldlabor öffnen")
                        .build()) {
                    @Override
                    public void onClick(InventoryClickEvent event) {
                        openMoneyLab();
                    }
                });
            }
            case WEAPON_LAB -> {
                inventoryManager.setItem(new CustomItem(1, new ItemBuilder(Material.PAPER)
                        .setName("§aWaffenlabor öffnen")
                        .build()) {
                    @Override
                    public void onClick(InventoryClickEvent event) {
                        openWeaponLab();
                    }
                });
            }
        }
    }

    private void openMoneyLab() {
        InventoryManager inventoryManager = new InventoryManager(player.getPlayer(), 9, Component.text("§8 » §cLabor §8| §a" + laboratory.getType().getName()));
        if (player.getData().getFaction() == null) {
            if (player.getData().getCompany() == null) {
                displayUnaccessable("§cDu bist in keiner Firma", inventoryManager);
                return;
            } else {
                if (player.getData().getCompanyRole().hasPermission("bank")) {
                    displayUnaccessable("§cDu hast keine Berechtigung", inventoryManager);
                    return;
                }
                // TODO: MoneyLab
            }
        }
    }

    private void openDrugLab() {
        InventoryManager inventoryManager = new InventoryManager(player.getPlayer(), 9, Component.text("§8 » §cLabor §8| §a" + laboratory.getType().getName()));
        inventoryManager.setItem(new CustomItem(0, new ItemBuilder(Material.PAPER)
                .setName("§aBesitzer")
                .setLore("§8▎ §7" + laboratory.getFaction().getFullname())
                .build()) {
            @Override
            public void onClick(InventoryClickEvent event) {

            }
        });
    }

    private void openWeaponLab() {
        InventoryManager inventoryManager = new InventoryManager(player.getPlayer(), 9, Component.text("§8 » §cLabor §8| §a" + laboratory.getType().getName()));
        inventoryManager.setItem(new CustomItem(0, new ItemBuilder(Material.PAPER)
                .setName("§aBesitzer")
                .setLore("§8▎ §7" + laboratory.getFaction().getFullname())
                .build()) {
            @Override
            public void onClick(InventoryClickEvent event) {

            }
        });
    }

    private void displayUnaccessable(String message, InventoryManager inventoryManager) {
        for (int i = 0; i < inventoryManager.getSize(); i++) {
            inventoryManager.setItem(new CustomItem(i, new ItemBuilder(Material.RED_STAINED_GLASS_PANE)
                    .setName("§c" + message)
                    .build()
            ) {
                @Override
                public void onClick(InventoryClickEvent event) {

                }
            });
        }
    }
}
