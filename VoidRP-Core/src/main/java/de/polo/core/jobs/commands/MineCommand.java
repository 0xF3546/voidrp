package de.polo.core.jobs.commands;

import de.polo.api.Utils.inventorymanager.CustomItem;
import de.polo.api.Utils.inventorymanager.InventoryManager;
import de.polo.api.VoidAPI;
import de.polo.api.jobs.MiningJob;
import de.polo.api.player.VoidPlayer;
import de.polo.core.Main;
import de.polo.api.jobs.enums.MiniJob;
import de.polo.core.handler.CommandBase;
import de.polo.core.location.services.LocationService;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.player.services.PlayerService;
import de.polo.core.utils.Utils;
import de.polo.core.manager.ItemManager;
import de.polo.core.manager.ServerManager;
import de.polo.core.utils.Prefix;
import de.polo.core.utils.enums.EXPType;
import de.polo.core.utils.player.SoundManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

@CommandBase.CommandMeta(
        name = "minenarbeiter",
        usage = "/minenarbeiter"
)
public class MineCommand extends CommandBase implements MiningJob {
    public final Material[] blocks = new Material[]{
            Material.DIAMOND_ORE, Material.EMERALD_ORE, Material.IRON_ORE,
            Material.GOLD_ORE, Material.LAPIS_ORE, Material.REDSTONE_ORE
    };
    public final String prefix = "§8[§7Mine§8] §7";

    public MineCommand(@NotNull CommandMeta meta) {
        super(meta);
    }

    private static void scheduleOreRespawn(Location location, Material originalMaterial) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Block block = location.getBlock();
                if (block.getType() == Material.STONE) {
                    block.setType(originalMaterial);
                }
            }
        }.runTaskLater(Main.getInstance(), 120 * 20);
    }

    @Override
    public void execute(@NotNull VoidPlayer player, @NotNull PlayerData playerData, @NotNull String[] args) throws Exception {
        if (ServerManager.canDoJobs()) {
            PlayerService playerService = VoidAPI.getService(PlayerService.class);
            if (!playerData.canInteract()) {
                player.sendMessage(Prefix.error_cantinteract);
                return;
            }
            LocationService locationService = VoidAPI.getService(LocationService.class);
            if (locationService.getDistanceBetweenCoords(player, "mine") <= 5) {
                InventoryManager inventoryManager = new InventoryManager(player.getPlayer(), 27, Component.text("§8 » §7Minenarbeiter"), true, true);

                // Start Job Option
                if (!playerService.isInJobCooldown(player, MiniJob.MINER) && player.getActiveJob() == null) {
                    inventoryManager.setItem(new CustomItem(11, ItemManager.createItem(Material.LIME_DYE, 1, 0, "§aMinensarbeiter starten")) {
                        @Override
                        public void onClick(InventoryClickEvent event) {
                            startJob(player);
                            player.getPlayer().closeInventory();
                        }
                    });
                } else {
                    if (player.getActiveJob() == null) {
                        inventoryManager.setItem(new CustomItem(11, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§a§mMinensarbeiter starten", "§8 ➥§7 Warte noch " + Utils.getTime(playerService.getJobCooldown(player, MiniJob.MINER)) + "§7.")) {
                            @Override
                            public void onClick(InventoryClickEvent event) {}
                        });
                    } else {
                        inventoryManager.setItem(new CustomItem(11, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§a§mMinensarbeiter starten", "§8 ➥§7 Du hast bereits den §f" + player.getMiniJob().getName() + "§7 Job angenommen.")) {
                            @Override
                            public void onClick(InventoryClickEvent event) {}
                        });
                    }
                }

                // Quit Job Option
                if (player.getActiveJob() == null) {
                    inventoryManager.setItem(new CustomItem(15, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§e§mJob beenden", "§8 ➥§7 Du hast den Job nicht angenommen")) {
                        @Override
                        public void onClick(InventoryClickEvent event) {}
                    });
                } else if (!player.getMiniJob().equals(MiniJob.MINER)) {
                    inventoryManager.setItem(new CustomItem(15, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§e§mJob beenden", "§8 ➥§7 Du hast den Job nicht angenommen")) {
                        @Override
                        public void onClick(InventoryClickEvent event) {}
                    });
                } else {
                    inventoryManager.setItem(new CustomItem(15, ItemManager.createItem(Material.YELLOW_DYE, 1, 0, "§eJob beenden", "§8 ➥ §7Erze verkaufen")) {
                        @Override
                        public void onClick(InventoryClickEvent event) {
                            quitJob(player);
                            player.getPlayer().closeInventory();
                        }
                    });
                }
            } else {
                player.sendMessage(Prefix.ERROR + "Du bist §cnicht§7 in der nähe der Mine§7!");
            }
        } else {
            player.sendMessage(ServerManager.error_cantDoJobs);
        }
    }

    @Override
    public void startJob(VoidPlayer player) {
        PlayerService playerService = VoidAPI.getService(PlayerService.class);
        if (!playerService.isInJobCooldown(player, MiniJob.MINER)) {
            player.setMiniJob(MiniJob.MINER);
            player.setActiveJob(this);

            player.sendMessage(prefix + "Du bist nun Minenarbeiter§7.");
            player.sendMessage(prefix + "Baue nun Erze ab.");

            int pickaxeLevel = player.getData().getJobSkill(MiniJob.MINER).getLevel();
            Material pickaxeType = pickaxeLevel < 5 ? Material.STONE_PICKAXE : Material.IRON_PICKAXE;
            player.getPlayer().getInventory().addItem(ItemManager.createItem(pickaxeType, 1, 0, "§6Spitzhacke"));
        } else {
            player.sendMessage(prefix + "Du kannst den Job erst in §f" + Utils.getTime(playerService.getJobCooldown(player, MiniJob.MINER)) + "§7 beginnen.");
        }
    }

    @Override
    public void endJob(VoidPlayer player) {
        // Wird durch quitJob ersetzt
    }

    public void quitJob(VoidPlayer player) {
        Main.getInstance().beginnerpass.didQuest(player.getPlayer(), 5);

        int iron = ItemManager.getItem(player.getPlayer(), Material.IRON_ORE);
        int redstone = ItemManager.getItem(player.getPlayer(), Material.REDSTONE_ORE);
        int lapis = ItemManager.getItem(player.getPlayer(), Material.LAPIS_ORE);
        int gold = ItemManager.getItem(player.getPlayer(), Material.GOLD_ORE);
        int emerald = ItemManager.getItem(player.getPlayer(), Material.EMERALD_ORE);
        int diamond = ItemManager.getItem(player.getPlayer(), Material.DIAMOND_ORE);

        int earnings = 0;
        int exp = 0;

        if (iron > 0) {
            player.sendMessage(prefix + "Verdienst durch §7Eisenerz§8: §a+" + iron + "$");
            earnings += iron;
            exp += (int)(iron * 0.35);
        }
        if (redstone > 0) {
            player.sendMessage(prefix + "Verdienst durch §cRedstonerz§8: §a+" + redstone + "$");
            earnings += redstone;
            exp += (int)(redstone * 0.47);
        }
        if (lapis > 0) {
            player.sendMessage(prefix + "Verdienst durch §9Lapislazulierz§8: §a+" + lapis + "$");
            earnings += lapis;
            exp += (int)(lapis * 0.60);
        }
        if (gold > 0) {
            player.sendMessage(prefix + "Verdienst durch §6Golderz§8: §a+" + gold + "$");
            earnings += gold;
            exp += (int)(gold * 0.8);
        }
        if (emerald > 0) {
            player.sendMessage(prefix + "Verdienst durch §aSmaragderz§8: §a+" + emerald + "$");
            earnings += emerald;
            exp += emerald;
        }
        if (diamond > 0) {
            player.sendMessage(prefix + "Verdienst durch §bDiamanterz§8: §a+" + diamond + "$");
            earnings += diamond;
            exp += (int)(diamond * 1.25);
        }
        PlayerService playerService = VoidAPI.getService(PlayerService.class);
        if (earnings > 0) {
            player.sendMessage(prefix + "Du hast insgesamt §a+" + earnings + "$§7 verdient.");
            player.getData().addBankMoney(earnings, "Auszahlung Minenarbeiter");
            SoundManager.successSound(player.getPlayer());
            playerService.addExp(player.getPlayer(), EXPType.SKILL_MINER, exp);
        } else {
            player.sendMessage(prefix + "Du hast keine Erze abgebaut.");
        }

        Inventory inv = player.getPlayer().getInventory();
        for (Material material : blocks) {
            for (ItemStack item : inv.getContents()) {
                if (item != null && (item.getType() == material || item.getType().name().contains("PICKAXE"))) {
                    inv.removeItem(item);
                }
            }
        }

        playerService.handleJobFinish(player, MiniJob.MINER, 3600, Utils.random(12, 20));
        player.setMiniJob(null);
        player.setActiveJob(null);
    }

    @Override
    public void handleBlockBreak(VoidPlayer player, BlockBreakEvent event) {
        Block block = event.getBlock();
        PlayerService playerService = VoidAPI.getService(PlayerService.class);
        for (Material material : blocks) {
            if (block.getType() == material) {
                event.setCancelled(true);
                player.getPlayer().getInventory().addItem(ItemManager.createItem(material, 1, 0, block.getType().name()));
                block.setType(Material.STONE);
                scheduleOreRespawn(block.getLocation(), material);
                playerService.addExp(player.getPlayer(), EXPType.SKILL_MINER, Utils.random(5, 10));
                break;
            }
        }
    }
}