package de.polo.core.jobs.commands;

import de.polo.api.Utils.inventorymanager.CustomItem;
import de.polo.api.Utils.inventorymanager.InventoryManager;
import de.polo.api.VoidAPI;
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
import de.polo.core.storage.RegisteredBlock;
import de.polo.core.utils.Prefix;
import de.polo.core.utils.Utils;
import de.polo.core.utils.enums.RoleplayItem;
import de.polo.core.utils.player.SoundManager;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static de.polo.core.Main.blockManager;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
@CommandBase.CommandMeta(
        name = "uranmine",
        usage = "/uranmine"
)
public class UranMineCommand extends CommandBase implements TransportJob, Listener {
    public final String prefix = "§8[§cAKW§8] §7";
    private List<Location> rollOutLocations = new ObjectArrayList<>();

    public UranMineCommand(@NotNull CommandMeta meta) {
        super(meta);
        rollOutLocations = blockManager.getBlocks()
                .stream()
                .filter(x -> x.getInfoValue() != null && x.getInfoValue().equalsIgnoreCase("mine"))
                .map(RegisteredBlock::getLocation)
                .toList();
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }

    @Override
    public void execute(@NotNull VoidPlayer player, @NotNull PlayerData playerData, @NotNull String[] args) throws Exception {
        if (ServerManager.canDoJobs()) {
            LocationService locationService = VoidAPI.getService(LocationService.class);
            boolean nearMine = locationService.getDistanceBetweenCoords(player, "uranmine") <= 5;
            boolean nearPowerPlant = locationService.getDistanceBetweenCoords(player, "atomkraftwerk") <= 5;

            if (!nearMine && !nearPowerPlant) {
                player.sendMessage(Prefix.ERROR + "Du bist weder in der Nähe der Uranmine noch des Atomkraftwerks.");
                return;
            }
            PlayerService playerService = VoidAPI.getService(PlayerService.class);
            InventoryManager inventoryManager = new InventoryManager(player.getPlayer(), 27, Component.text("§8 » §cUranmine"), true, true);

            // Start Job Option
            if (!playerService.isInJobCooldown(player, MiniJob.URANIUM_MINER) && player.getActiveJob() == null) {
                inventoryManager.setItem(new CustomItem(11, ItemManager.createItem(Material.LIME_DYE, 1, 0, "§aUrantransport starten")) {
                    @Override
                    public void onClick(InventoryClickEvent event) {
                        startJob(player);
                        player.getPlayer().closeInventory();
                    }
                });
            } else {
                if (player.getActiveJob() == null) {
                    inventoryManager.setItem(new CustomItem(11, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§a§mUrantransport starten", "§8 ➥§7 Warte noch " + Utils.getTime(playerService.getJobCooldown(player, MiniJob.URANIUM_MINER)) + "§7.")) {
                        @Override
                        public void onClick(InventoryClickEvent event) {
                        }
                    });
                } else {
                    inventoryManager.setItem(new CustomItem(11, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§a§mUrantransport starten", "§8 ➥§7 Du hast bereits den §f" + player.getMiniJob().getName() + "§7 Job angenommen.")) {
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
            } else if (!player.getMiniJob().equals(MiniJob.URANIUM_MINER)) {
                inventoryManager.setItem(new CustomItem(15, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§e§mJob beenden", "§8 ➥§7 Du hast den Job nicht angenommen")) {
                    @Override
                    public void onClick(InventoryClickEvent event) {
                    }
                });
            } else {
                int uranCount = ItemManager.getCustomItemCount(player.getPlayer(), RoleplayItem.URAN);
                String payoutText = uranCount > 0 ? "§8 ➥ §7Du erhälst §a" + ServerManager.getPayout("uran") + "$" : "§8 ➥ §7Du hast kein Uran dabei";
                inventoryManager.setItem(new CustomItem(15, ItemManager.createItem(Material.YELLOW_DYE, 1, 0, "§eJob beenden", payoutText)) {
                    @Override
                    public void onClick(InventoryClickEvent event) {
                        endJob(player);
                        player.getPlayer().closeInventory();
                    }
                });
            }

            // Drop Option bei Atomkraftwerk
            if (nearPowerPlant && player.getMiniJob() == MiniJob.URANIUM_MINER) {
                inventoryManager.setItem(new CustomItem(22, ItemManager.createItem(Material.EMERALD, 1, 0, "§aUran abgeben")) {
                    @Override
                    public void onClick(InventoryClickEvent event) {
                        handleDrop(player); // 0 als Platzhalter, da keine Hausnummer benötigt
                        player.getPlayer().closeInventory();
                    }
                });
            }
        } else {
            player.sendMessage(ServerManager.error_cantDoJobs);
        }
    }

    @Override
    public void startJob(VoidPlayer player) {
        PlayerService playerService = VoidAPI.getService(PlayerService.class);
        if (!playerService.isInJobCooldown(player, MiniJob.URANIUM_MINER)) {
            player.setMiniJob(MiniJob.URANIUM_MINER);
            player.setActiveJob(this);
            player.sendMessage(prefix + "Finde die Uranquelle (Smaragderz) und baue sie ab. Bringe diese anschließend zum Atomkraftwerk");
            equip(player);
            checkForRollout();
        } else {
            player.sendMessage(prefix + "Du kannst den Job erst in §f" + Utils.getTime(playerService.getJobCooldown(player, MiniJob.URANIUM_MINER)) + "§7 beginnen.");
        }
    }

    @Override
    public void endJob(VoidPlayer player) {
        int uranCount = ItemManager.getCustomItemCount(player.getPlayer(), RoleplayItem.URAN);
        LocationService locationService = VoidAPI.getService(LocationService.class);
        if (uranCount > 0 && locationService.getDistanceBetweenCoords(player, "atomkraftwerk") <= 5) {
            handleDrop(player);
        } else {
            PlayerService playerService = VoidAPI.getService(PlayerService.class);
            player.sendMessage(prefix + "Du hast den Job beendet.");
            removeEquip(player);
            playerService.handleJobFinish(player, MiniJob.URANIUM_MINER, 3600, Utils.random(12, 20));
            player.setMiniJob(null);
            player.setActiveJob(null);
        }
    }

    private void equip(VoidPlayer player) {
        ItemStack item = ItemManager.createItem(Material.IRON_PICKAXE, 1, 0, "§6Uran-Spitzhacke");
        player.getPlayer().getInventory().addItem(item);
    }

    private void removeEquip(VoidPlayer player) {
        for (ItemStack item : player.getPlayer().getInventory().getContents()) {
            if (item != null && item.getType() == Material.IRON_PICKAXE &&
                    item.hasItemMeta() && item.getItemMeta().getDisplayName().equals("§6Uran-Spitzhacke")) {
                player.getPlayer().getInventory().removeItem(item);
            }
        }
    }

    @Override
    public void handleDrop(VoidPlayer player) {
        LocationService locationService = VoidAPI.getService(LocationService.class);
        if (locationService.getDistanceBetweenCoords(player, "atomkraftwerk") > 5) {
            player.sendMessage(Prefix.ERROR + "Du bist nicht in der Nähe des Atomkraftwerks.");
            return;
        }

        int uranCount = ItemManager.getCustomItemCount(player.getPlayer(), RoleplayItem.URAN);
        if (uranCount < 1) {
            player.sendMessage(Prefix.ERROR + "Du hast kein Uran dabei.");
            return;
        }

        ItemManager.removeCustomItem(player.getPlayer(), RoleplayItem.URAN, 1);
        PlayerService playerService = VoidAPI.getService(PlayerService.class);
        int payout = ServerManager.getPayout("uran");
        player.getData().addMoney(payout, "Urantransport");
        playerService.addExp(player.getPlayer(), Utils.random(12, 20));
        player.sendMessage(prefix + "Danke für das Uran! §a+" + payout + "$");
        SoundManager.successSound(player.getPlayer());

        removeEquip(player);
        playerService.handleJobFinish(player, MiniJob.URANIUM_MINER, 3600, Utils.random(12, 20));
        player.setMiniJob(null);
        player.setActiveJob(null);
    }

    private void rollOutMine() {
        Location randomLocation = rollOutLocations.get(Utils.random(0, rollOutLocations.size()));
        randomLocation.getBlock().setType(Material.EMERALD_ORE);
    }

    private void checkForRollout() {
        for (Location location : rollOutLocations) {
            if (location.getBlock().getType().equals(Material.EMERALD_ORE)) return;
        }
        rollOutMine();
    }

    private boolean isBlock(Block block) {
        LocationService locationService = VoidAPI.getService(LocationService.class);
        return rollOutLocations.stream().anyMatch(loc -> locationService.isLocationEqual(block.getLocation(), loc));
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        VoidPlayer player = VoidAPI.getPlayer(event.getPlayer());
        if (player.getMiniJob() != MiniJob.URANIUM_MINER) return;

        if (ItemManager.getCustomItemCount(player.getPlayer(), RoleplayItem.URAN) >= 1) {
            player.sendMessage(Prefix.ERROR + "Du hast bereits ein Uran dabei.");
            event.setCancelled(true);
            return;
        }

        if (!isBlock(event.getBlock())) return;

        event.setCancelled(true);
        ItemManager.addCustomItem(player.getPlayer(), RoleplayItem.URAN, 1);
        NavigationService navigationService = VoidAPI.getService(NavigationService.class);
        PlayerService playerService = VoidAPI.getService(PlayerService.class);
        player.sendMessage(prefix + "Du hast ein Uran abgebaut. Bringe es nun zum Atomkraftwerk");
        navigationService.createNavi(player.getPlayer(), "Atomkraftwerk", true);
        event.getBlock().setType(Material.STONE);
        removeEquip(player);
        rollOutMine();
        playerService.addExp(player.getPlayer(), Utils.random(5, 10));
    }
}