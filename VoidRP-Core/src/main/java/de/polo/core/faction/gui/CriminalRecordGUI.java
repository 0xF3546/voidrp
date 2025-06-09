package de.polo.core.faction.gui;

import de.polo.api.Utils.GUI;
import de.polo.api.Utils.ItemBuilder;
import de.polo.api.Utils.inventorymanager.CustomItem;
import de.polo.api.Utils.inventorymanager.InventoryManager;
import de.polo.api.VoidAPI;
import de.polo.api.faction.CharacterRecord;
import de.polo.api.player.VoidPlayer;
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
                    CharacterRecord newRecord = new CoreCharacterRecord("Keine Angabe", player.getUuid(), Utils.getTime());
                    lawEnforcementService.setCharacterRecord(criminal, newRecord);
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
                                "§7Last edit: " + record.getLastEdit(),
                                "§7Last editor: " + lastEditor
                        )
                )
                .build()) {
            @Override
            public void onClick(InventoryClickEvent event) {

            }
        });

        String infoText = "§7Info: " + (record.getInfoText() == null ? "Keine" : record.getInfoText());
        if (player.getVariable("criminalrecord::edit::infoText") != null) {
            infoText = "§7Info: " + player.getVariable("criminalrecord::edit::infoText");
        }
        inventoryManager.setItem(new CustomItem(13, new ItemBuilder(Material.PAPER)
                .setName(Component.text("§cAnmerkung"))
                .setLore(
                        Arrays.asList(
                                "§7Info: " + (record.getInfoText() == null ? "Keine" : record.getInfoText())
                        )
                )
                .build()) {
            @Override
            public void onClick(InventoryClickEvent event) {
                player.setVariable("chatblock", "criminalrecord::edit");
                player.sendMessage("§7Bitte gib deine Anmerkung ein.");
            }
        });

        if (player.getVariable("criminalrecord::edit::infoText") != null) {
            inventoryManager.setItem(new CustomItem(27, new ItemBuilder(Material.EMERALD)
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
