package de.polo.core.jobs.commands;

import de.polo.api.Utils.inventorymanager.CustomItem;
import de.polo.api.Utils.inventorymanager.InventoryManager;
import de.polo.api.VoidAPI;
import de.polo.api.jobs.MiningJob;
import de.polo.api.jobs.TransportJob;
import de.polo.api.jobs.enums.MiniJob;
import de.polo.api.player.VoidPlayer;
import de.polo.core.Main;
import de.polo.core.handler.CommandBase;
import de.polo.core.location.services.LocationService;
import de.polo.core.location.services.NavigationService;
import de.polo.core.manager.ItemManager;
import de.polo.core.manager.ServerManager;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.player.services.PlayerService;
import de.polo.core.utils.Prefix;
import de.polo.core.utils.Utils;
import de.polo.core.utils.player.SoundManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

@CommandBase.CommandMeta(
        name = "farmer",
        usage = "/farmer"
)
public class FarmerCommand extends CommandBase implements MiningJob, TransportJob {

    public FarmerCommand(@NotNull CommandMeta meta) {
        super(meta);
    }

    private static void scheduleHayRespawn(Location location) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Block block = location.getBlock();
                if (block.getType() == Material.AIR) {
                    block.setType(Material.HAY_BLOCK);
                }
            }
        }.runTaskLater(Main.getInstance(), 120 * 20);
    }

    @Override
    public void execute(@NotNull VoidPlayer player, @NotNull PlayerData playerData, @NotNull String[] args) throws Exception {
        if (ServerManager.canDoJobs()) {
            LocationService locationService = VoidAPI.getService(LocationService.class);
            if (locationService.getDistanceBetweenCoords(player, "farmer") <= 5) {
                PlayerService playerService = VoidAPI.getService(PlayerService.class);
                InventoryManager inventoryManager = new InventoryManager(player.getPlayer(), 27, Component.text("§8 » §eFarmer"), true, true);

                // Start Job Option
                if (!playerService.isInJobCooldown(player, MiniJob.FARMER) && player.getActiveJob() == null) {
                    inventoryManager.setItem(new CustomItem(11, ItemManager.createItem(Material.LIME_DYE, 1, 0, "§aFarmer starten")) {
                        @Override
                        public void onClick(InventoryClickEvent event) {
                            startJob(player);
                            player.getPlayer().closeInventory();
                        }
                    });
                    inventoryManager.setItem(new CustomItem(22, ItemManager.createItem(Material.WHEAT, 1, 0, "§eWeizenlieferant starten")) {
                        @Override
                        public void onClick(InventoryClickEvent event) {
                            startTransport(player);
                            player.getPlayer().closeInventory();
                        }
                    });
                } else {
                    if (player.getActiveJob() == null) {
                        inventoryManager.setItem(new CustomItem(11, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§a§mFarmer starten", "§8 ➥§7 Warte noch " + Utils.getTime(playerService.getJobCooldown(player, MiniJob.FARMER)) + "§7.")) {
                            @Override
                            public void onClick(InventoryClickEvent event) {
                            }
                        });
                        inventoryManager.setItem(new CustomItem(22, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§e§mWeizenlieferant starten", "§8 ➥§7 Warte noch " + Utils.getTime(playerService.getJobCooldown(player, MiniJob.FARMER)) + "§7.")) {
                            @Override
                            public void onClick(InventoryClickEvent event) {
                            }
                        });
                    } else {
                        inventoryManager.setItem(new CustomItem(11, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§a§mFarmer starten", "§8 ➥§7 Du hast bereits den §f" + player.getMiniJob().getName() + "§7 Job angenommen.")) {
                            @Override
                            public void onClick(InventoryClickEvent event) {
                            }
                        });
                        inventoryManager.setItem(new CustomItem(22, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§e§mWeizenlieferant starten", "§8 ➥§7 Du hast bereits den §f" + player.getMiniJob().getName() + "§7 Job angenommen.")) {
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
                } else if (!player.getMiniJob().equals(MiniJob.FARMER)) {
                    inventoryManager.setItem(new CustomItem(15, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§e§mJob beenden", "§8 ➥§7 Du hast den Job nicht angenommen")) {
                        @Override
                        public void onClick(InventoryClickEvent event) {
                        }
                    });
                } else {
                    inventoryManager.setItem(new CustomItem(15, ItemManager.createItem(Material.YELLOW_DYE, 1, 0, "§eJob beenden", "§8 ➥ §7Du erhälst §a" + ServerManager.getPayout("heuballen") * (int) player.getVariable("heuballen") + "$")) {
                        @Override
                        public void onClick(InventoryClickEvent event) {
                            endJob(player);
                            player.getPlayer().closeInventory();
                        }
                    });
                }
            } else {
                player.sendMessage(Prefix.ERROR + "Du bist §cnicht§7 in der nähe der Farm§7!");
            }
        } else {
            player.sendMessage(ServerManager.error_cantDoJobs);
        }
    }

    @Override
    public void endJob(VoidPlayer player) {
        Main.beginnerpass.didQuest(player.getPlayer(), 5);

        if (player.getMiniJob() == MiniJob.WHEAT_TRANSPORT) {
            player.setMiniJob(null);
            player.setActiveJob(null);
            player.sendMessage("§8[§eLieferant§8]§7 Du hast den Job beendet.");
            return;
        }

        PlayerService playerService = VoidAPI.getService(PlayerService.class);
        int payout = ServerManager.getPayout("heuballen") * (int) player.getVariable("heuballen");
        player.sendMessage("§8[§eFarmer§8]§7 Vielen Dank für die geleistete Arbeit. §a+" + payout + "$");
        SoundManager.successSound(player.getPlayer());
        playerService.handleJobFinish(player, MiniJob.FARMER, 3600, Utils.random(12, 20));
        player.getPlayer().removePotionEffect(PotionEffectType.SLOW_DIGGING);
        player.getData().addBankMoney(payout, "Auszahlung Farmer");

        player.setMiniJob(null);
        player.setActiveJob(null);
        player.setVariable("heuballen", null);
        player.setVariable("heuballen_remaining", null);
    }

    @Override
    public void startJob(VoidPlayer player) {
        PlayerService playerService = VoidAPI.getService(PlayerService.class);
        if (!playerService.isInJobCooldown(player, MiniJob.FARMER)) {
            player.setMiniJob(MiniJob.FARMER);
            player.setActiveJob(this);

            player.sendMessage("§8[§eFarmer§8]§7 Du bist nun Farmer.");
            int hayBales = 9 + (player.getData().getJobSkill(MiniJob.FARMER).getLevel() / 2);
            player.sendMessage("§8[§eFarmer§8]§7 Baue §e" + hayBales + " Heuballen§7 ab.");
            player.setVariable("heuballen_remaining", hayBales);
            player.setVariable("heuballen", 0);
            player.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, Integer.MAX_VALUE, 0, true, false));
        } else {
            player.sendMessage("§8[§eFarmer§8]§7 Du kannst den Job erst in §f" + Utils.getTime(playerService.getJobCooldown(player, MiniJob.FARMER)) + "§7 beginnen.");
        }
    }

    public void startTransport(VoidPlayer player) {
        PlayerService playerService = VoidAPI.getService(PlayerService.class);
        if (!playerService.isInJobCooldown(player, MiniJob.FARMER)) {
            player.setMiniJob(MiniJob.WHEAT_TRANSPORT);
            player.setActiveJob(this);
            player.setVariable("weizen", Utils.random(2, 5));
            player.sendMessage("§8[§eLieferant§8]§7 Bringe das Weizen zur Mühle.");
            player.sendMessage("§8 ➥ §7Nutze §8/§edrop§7 um das Weizen abzugeben.");
            NavigationService navigationService = VoidAPI.getService(NavigationService.class);
            navigationService.createNavi(player.getPlayer(), "Mühle", true);
        }
    }

    @Override
    public void handleBlockBreak(VoidPlayer player, BlockBreakEvent event) {
        if (event.getBlock().getType() == Material.HAY_BLOCK) {
            if ((int) player.getVariable("heuballen_remaining") <= 0) {
                player.sendMessage("§8[§eFarmer§8]§7 Du hast alle Heuballen abgebaut.");
                return;
            }

            event.getBlock().setType(Material.AIR);
            player.setVariable("heuballen_remaining", (int) player.getVariable("heuballen_remaining") - 1);
            int amount = Utils.random(2, 4);
            player.setVariable("heuballen", (int) player.getVariable("heuballen") + amount);
            player.sendMessage("§8[§eFarmer§8]§7 +" + amount + " Heuballen");

            if ((int) player.getVariable("heuballen_remaining") <= 0) {
                player.sendMessage("§8[§eFarmer§8]§7 Du hast alle Heuballen abgebaut, begib dich wieder zum Farmer.");
            }
            scheduleHayRespawn(event.getBlock().getLocation());
        }
    }

    @Override
    public void handleDrop(VoidPlayer player) {
        LocationService locationService = VoidAPI.getService(LocationService.class);
        if (locationService.getDistanceBetweenCoords(player, "Mühle") < 5) {
            PlayerService playerService = VoidAPI.getService(PlayerService.class);
            int payout = Utils.random(ServerManager.getPayout("weizenlieferant"), ServerManager.getPayout("weizenlieferant2"));
            player.sendMessage("§8[§eLieferant§8]§7 Danke für's abliefern. §a+" + payout + "$");
            SoundManager.successSound(player.getPlayer());
            playerService.addExp(player.getPlayer(), Utils.random(1, 3));

            int remainingWheat = (int) player.getVariable("weizen") - 1;
            player.setVariable("weizen", remainingWheat);
            player.getData().addBankMoney(payout, "Auszahlung Weizentransport");

            if (remainingWheat <= 0) {
                player.sendMessage("§8[§eLieferant§8]§7 Du hast alles abgegeben. Danke!");
                playerService.handleJobFinish(player, MiniJob.FARMER, 3600, Utils.random(12, 20));
                player.setMiniJob(null);
                player.setActiveJob(null);
            }
        } else {
            player.sendMessage(Prefix.ERROR + "Du bist nicht in der nähe der Mühle.");
        }
    }
}