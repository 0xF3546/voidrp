package de.polo.core.faction.gui;

import de.polo.api.utils.ApiUtils;
import de.polo.api.utils.GUI;
import de.polo.api.utils.ItemBuilder;
import de.polo.api.utils.inventorymanager.CustomItem;
import de.polo.api.utils.inventorymanager.InventoryManager;
import de.polo.api.VoidAPI;
import de.polo.api.faction.CharacterRecord;
import de.polo.api.player.VoidPlayer;
import de.polo.core.Main;
import de.polo.core.faction.entity.CoreCharacterRecord;
import de.polo.core.faction.service.LawEnforcementService;
import de.polo.core.utils.Utils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Arrays;
import java.util.UUID;

public class CriminalRecordGUI implements GUI {
    private final VoidPlayer player;
    private final UUID criminal;
    private final CharacterRecord record;
    private final Runnable onBack;

    public CriminalRecordGUI(VoidPlayer player, UUID criminal, CharacterRecord record, Runnable onBack) {
        this.player = player;
        this.criminal = criminal;
        this.record = record;
        this.onBack = onBack;
    }

    public void open() {
        player.setLastGUI(this);
        if (record == null) {
            InventoryManager inventoryManager = new InventoryManager(player.getPlayer(), 27, Component.text("Criminal Record"));
            inventoryManager.setItem(new CustomItem(13, new ItemBuilder(Material.EMERALD)
                    .setName("§aRecord anlegen")
                    .build()) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    LawEnforcementService lawEnforcementService = VoidAPI.getService(LawEnforcementService.class);
                    CharacterRecord newRecord = new CoreCharacterRecord(criminal, "Keine Angabe", player.getUuid(), Utils.getTime());
                    lawEnforcementService.setCharacterRecord(criminal, newRecord);
                    Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
                        openRecord();
                    }, 20L);
                }
            });
        } else openRecord();
    }

    private void openRecord() {
        InventoryManager inventoryManager = new InventoryManager(player.getPlayer(), 27, Component.text("Criminal Record"));
        String lastEditor = (record.getLastEditor() == null ? "Niemand" : Bukkit.getOfflinePlayer(record.getLastEditor()).getName());
        inventoryManager.setItem(new CustomItem(4, new ItemBuilder(Material.PAPER)
                .setName(Component.text("§bInformation"))
                .setLore(
                        Arrays.asList(
                                "§8▎ §aZuletzt bearbeitet §8» §7" + Utils.localDateTimeToReadableString(record.getLastEdit()),
                                "§8▎ §aLetzter bearbeiter §8» §7" + lastEditor
                        )
                )
                .build()) {
            @Override
            public void onClick(InventoryClickEvent event) {

            }
        });

        String infoText = "§8▎ §aInfo §8» §7" + (record.getInfoText() == null ? "Keine" : record.getInfoText());
        if (player.getVariable("criminalrecord::edit::infoText") != null) {
            infoText = "§8▎ §aInfo §8» §7" + player.getVariable("criminalrecord::edit::infoText");
        }
        inventoryManager.setItem(new CustomItem(13, new ItemBuilder(Material.PAPER)
                .setName(Component.text("§cAnmerkung"))
                .setLore(
                        Arrays.asList(
                                "§8▎ §aInfo §8» §7" + (record.getInfoText() == null ? "Keine" : record.getInfoText())
                        )
                )
                .build()) {
            @Override
            public void onClick(InventoryClickEvent event) {
                player.setVariable("chatblock", "criminalrecord::edit");
                player.sendMessage("§7Bitte gib deine Anmerkung ein.");
                player.getPlayer().closeInventory();
            }
        });

        if (player.getVariable("criminalrecord::edit::infoText") != null) {
            inventoryManager.setItem(new CustomItem(26, new ItemBuilder(Material.EMERALD)
                    .setName("§aEditieren")
                    .build()) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    LawEnforcementService lawEnforcementService = VoidAPI.getService(LawEnforcementService.class);
                    record.setInfoText((String) player.getVariable("criminalrecord::edit::infoText"));
                    record.setLastEdit(Utils.getTime());
                    record.setLastEditor(player.getUuid());
                    lawEnforcementService.setCharacterRecord(criminal, record);
                }
            });
        }
    }
}
