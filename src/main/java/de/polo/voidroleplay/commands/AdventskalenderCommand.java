package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.handler.CommandBase;
import de.polo.voidroleplay.manager.ItemManager;
import de.polo.voidroleplay.utils.inventory.CustomItem;
import de.polo.voidroleplay.utils.inventory.InventoryManager;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.utils.Utils;
import de.polo.voidroleplay.utils.enums.CaseType;
import de.polo.voidroleplay.utils.enums.RoleplayItem;
import de.polo.voidroleplay.utils.enums.Weapon;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
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
            player.sendMessage(Prefix.ERROR + "Du musst mindestens einen PayDay pro Tag erhalten haben um den Adventskalender öffnen zu können.");
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
                            int currentDay = Utils.getTime().getDayOfMonth();

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

    @SneakyThrows
    private void checkedDate(Player player, int day) {
        PlayerData playerData = Main.getInstance().playerManager.getPlayerData(player);
        switch (day) {
            case 1 -> {
                player.sendMessage(PREFIX + "Du hast das §6" + day + " Türchen§7 geöffnet und §61.000$§7 erhalten!");
                playerData.addMoney(1000, "Advent #1");
            }
            case 2 -> {
                player.sendMessage(PREFIX + "Du hast das §6" + day + " Türchen§7 geöffnet und §61 Schutzweste§7 erhalten!");
                ItemManager.addCustomItem(player, RoleplayItem.BULLETPROOF, 1);
            }
            case 3 -> {
                player.sendMessage(PREFIX + "Du hast das §6" + day + " Türchen§7 geöffnet und §6100 XP§7 erhalten!");
                Main.getInstance().playerManager.addExp(player, 100);
            }
            case 4 -> {
                player.sendMessage(PREFIX + "Du hast das §6" + day + " Türchen§7 geöffnet und §61 Tag Premium§7 erhalten!");
                Main.getInstance().playerManager.redeemRank(player, "Premium", 1, "d");
            }
            case 14, 5 -> {
                player.sendMessage(PREFIX + "Du hast das §6" + day + " Türchen§7 geöffnet und §6300 Coins§7 erhalten!");
                Main.getInstance().playerManager.addCoins(player, 300);
            }
            case 6, 9 -> {
                player.sendMessage(PREFIX + "Du hast das §6" + day + " Türchen§7 geöffnet und §61.250$§7 erhalten!");
                playerData.addMoney(1250, "Advent #6");
            }
            case 7, 17 -> {
                player.sendMessage(PREFIX + "Du hast das §6" + day + " Türchen§7 geöffnet und §63 Stunden Gameboost§7 erhalten!");
                Main.getInstance().playerManager.addEXPBoost(player, 3);
            }
            case 8 -> {
                player.sendMessage(PREFIX + "Du hast das §6" + day + " Türchen§7 geöffnet und §cWeihnachtsmann-Mütze§7 erhalten!");
                ItemStack helmet = new ItemStack(Material.LEATHER_HELMET);
                LeatherArmorMeta meta = (LeatherArmorMeta) helmet.getItemMeta();
                meta.setColor(Color.RED);
                meta.setDisplayName("§cWeihnachtsmütze");
                helmet.setItemMeta(meta);
                player.getInventory().addItem(helmet);
            }
            case 10 -> {
                player.sendMessage(PREFIX + "Du hast das §6" + day + " Türchen§7 geöffnet und §61.500$§7 erhalten!");
                playerData.addMoney(1500, "Advent #10");
            }
            case 11 -> {
                player.sendMessage(PREFIX + "Du hast das §6" + day + " Türchen§7 geöffnet und §6150 XP§7 erhalten!");
                Main.getInstance().playerManager.addExp(player, 150);
            }
            case 12 -> {
                player.sendMessage(PREFIX + "Du hast das §6" + day + " Türchen§7 geöffnet und §6" + Weapon.ASSAULT_RIFLE.getClearName() + "§7 erhalten!");
                Main.getInstance().getWeaponManager().giveWeaponToCabinet(player, Weapon.ASSAULT_RIFLE, 0, 10);
            }
            case 21, 13 -> {
                player.sendMessage(PREFIX + "Du hast das §6" + day + " Türchen§7 geöffnet und §6Schwere Schutzweste§7 erhalten!");
                ItemManager.addCustomItem(player, RoleplayItem.HEAVY_BULLETPROOF, 1);
            }
            case 19, 15 -> {
                player.sendMessage(PREFIX + "Du hast das §6" + day + " Türchen§7 geöffnet und §61000 XP§7 erhalten!");
                Main.getInstance().playerManager.addExp(player, 1000);
            }
            case 16 -> {
                player.sendMessage(PREFIX + "Du hast das §6" + day + " Türchen§7 geöffnet und §61 1 Tag Premium§7 erhalten!");
                Main.getInstance().playerManager.redeemRank(player, "Premium", 3, "d");
            }
            case 18 -> {
                player.sendMessage(PREFIX + "Du hast das §6" + day + " Türchen§7 geöffnet und §6" + Weapon.SHOTGUN.getClearName() + " (A: 20, V: 4)§7 erhalten!");
                Main.getInstance().getWeaponManager().giveWeaponToCabinet(player, Weapon.SHOTGUN, 20, 4);
            }
            case 20, 22 -> {
                player.sendMessage(PREFIX + "Du hast das §6" + day + " Türchen§7 geöffnet und §61 Weihnachts-Case§7 erhalten!");
                player.getInventory().addItem(ItemManager.createItem(Material.CHEST, 1, 0, CaseType.CHRISTMAS.getDisplayName()));
            }
            case 23 -> {
                player.sendMessage(PREFIX + "Du hast das §6" + day + " Türchen§7 geöffnet und §610 Votepunkte§7 erhalten!");
                playerData.setVotes(playerData.getVotes() + 10);
                Main.getInstance().getCoreDatabase().updateAsync("UPDATE players SET votes = ? WHERE uuid = ?", playerData.getVotes(), player.getUniqueId().toString());
            }
            case 24 -> {
                player.sendMessage(PREFIX + "Du hast das §c§l" + day + " Türchen§7 geöffnet und §61 Levelup, Schwere Schutzweste, " + Weapon.MARKSMAN.getClearName() + " (A: 30, V: 3), 5000$ & 3 Tage Premium§7 erhalten! Frohe Weihnachten!");
                Main.getInstance().playerManager.addExp(player, playerData.getNeeded_exp() - playerData.getExp());
                ItemManager.addCustomItem(player, RoleplayItem.HEAVY_BULLETPROOF, 1);
                Main.getInstance().playerManager.redeemRank(player, "Premium", 3, "d");
                Main.getInstance().getWeaponManager().giveWeaponToCabinet(player, Weapon.MARKSMAN, 30, 3);
                playerData.addMoney(5000, "Advent #24");
            }
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
