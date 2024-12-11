package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.handler.CommandBase;
import de.polo.voidroleplay.manager.ItemManager;
import de.polo.voidroleplay.manager.inventory.CustomItem;
import de.polo.voidroleplay.manager.inventory.InventoryManager;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.utils.Prefix;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

import static de.polo.voidroleplay.Main.locationManager;

@CommandBase.CommandMeta(name = "adventskalender")
public class AdventskalenderCommand extends CommandBase {

    private final File dataFile;
    private final YamlConfiguration configuration;
    private final String PREFIX = "§8[§cAdventskalender§8]§7 ";

    public AdventskalenderCommand(@NotNull CommandMeta meta) {
        super(meta);

        dataFile = new File(Bukkit.getServer().getPluginManager().getPlugin("VoidRoleplay").getDataFolder(), "adventskalender.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.getParentFile().mkdirs();
                dataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        configuration = YamlConfiguration.loadConfiguration(dataFile);
    }

    @Override
    public void execute(@NotNull Player player, @NotNull PlayerData playerData, @NotNull String[] args) throws Exception {
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§cAdventskalender", true, false);
        if (locationManager.getDistanceBetweenCoords(player, "adventskalender") > 5) {
            player.sendMessage(Prefix.ERROR + "Du bist nicht in der nähe vom Weihnachtsmann.");
            return;
        }
        if (playerData.getLastPayDay().getDayOfMonth() != LocalDateTime.now().getDayOfMonth()) {
            player.sendMessage(Prefix.ERROR + "Du musst mindestens einen PayDay pro Tag erhalten haben um den Adventskalender zu öffnen abzuholen.");
            player.closeInventory();
            return;
        }
        for (int i = 0; i < 24; i++) {
            int day = i + 1;
            int finalI = i;
            CompletableFuture.runAsync(() -> {
                boolean alreadyOpened = configuration.getBoolean(player.getUniqueId() + ".day" + day, false);
                Material material = alreadyOpened ? Material.ENDER_CHEST : Material.CHEST;

                Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
                    inventoryManager.setItem(new CustomItem(finalI, ItemManager.createItem(material, 1, 0, "§cTag " + day)) {
                        @Override
                        public void onClick(InventoryClickEvent event) {
                            LocalDate today = LocalDate.now();
                            int currentDay = today.getDayOfMonth();

                            if (day > currentDay) {
                                player.sendMessage(PREFIX + "§cDu kannst dieses Türchen erst am " + day + ". Dezember öffnen!");
                                return;
                            }
                            if (alreadyOpened) {
                                player.sendMessage("§cDieses Türchen wurde bereits geöffnet!");
                                return;
                            }

                            configuration.set(player.getUniqueId() + ".day" + day, true);
                            saveConfigAsync();

                            event.getWhoClicked().closeInventory();
                            checkedDate(player, day);
                        }
                    });
                });
            });
        }}

    private void checkedDate(Player player, int day) {
        switch (day) {
            case 1:
                int xp = Main.random(400, 500);
                player.sendMessage(PREFIX + "Du hast das §6" + day + " Türchen§7 geöffnet und &6" + xp + " XP§7 erhalten!");
                Main.getInstance().playerManager.addExp(player, xp);
                break;
            case 2:
                break;
            case 3:
                break;
            case 4:
                break;
            case 5:
                break;
            case 6:
                break;
            case 7:
                break;
            case 8:
                break;
            case 9:
                break;
            case 10:
                break;
            case 11:
                break;
            case 12:
                break;
            case 13:
                break;
            case 14:
                break;
        }
    }

    private void saveConfigAsync() {
        CompletableFuture.runAsync(() -> {
            try {
                configuration.save(dataFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
