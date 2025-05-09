package de.polo.core.game.faction.plants;

import de.polo.api.Utils.inventorymanager.CustomItem;
import de.polo.api.Utils.inventorymanager.InventoryManager;
import de.polo.api.VoidAPI;
import de.polo.core.Main;
import de.polo.core.database.impl.CoreDatabase;
import de.polo.core.faction.entity.Faction;
import de.polo.core.faction.service.impl.FactionManager;
import de.polo.core.game.events.MinuteTickEvent;
import de.polo.core.location.services.LocationService;
import de.polo.core.manager.ItemManager;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.utils.Prefix;
import de.polo.core.utils.Utils;
import de.polo.core.utils.enums.PlantType;
import de.polo.core.utils.enums.RoleplayItem;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.SneakyThrows;
import net.kyori.adventure.text.Component;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class PlantFunctions implements Listener {
    private final List<Plant> plants = new ObjectArrayList<>();
    private final CoreDatabase coreDatabase;
    private final Utils utils;
    private final FactionManager factionManager;
    private final PlayerManager playerManager;
    private final HashMap<Plant, Integer> rob = new HashMap<>();

    @SneakyThrows
    public PlantFunctions(CoreDatabase coreDatabase, Utils utils, FactionManager factionManager, PlayerManager playerManager) {
        this.coreDatabase = coreDatabase;
        this.utils = utils;
        this.factionManager = factionManager;
        this.playerManager = playerManager;
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }

    private Plant getPlantByBlock(Block block) {
        LocationService locationService = VoidAPI.getService(LocationService.class);
        for (Plant plant : getPlants()) {
            if (locationService.isLocationEqual(plant.getBlock().getLocation(), block.getLocation())) return plant;
        }
        return null;
    }

    private Collection<Plant> getPlantsByFaction(Faction factionData) {
        return plants.stream().filter(x -> x.getPlanter() == factionData).collect(Collectors.toList());
    }

    public void cleanup() {
        for (Plant plant : plants) {
            plant.getBlock().setType(Material.AIR);
            plant.getBlock().getLocation().subtract(0, 1, 0).getBlock().setType(Material.GRASS_BLOCK);
        }
    }

    public String plant(Player player, PlantType plantType, Block block) {
        PlayerData playerData = playerManager.getPlayerData(player);
        Faction factionData = factionManager.getFactionData(playerData.getFaction());
        if (!factionData.isBadFrak()) {
            return "Deine Fraktion kann keine Plantagen legen.";
        }
        if (getPlantsByFaction(factionData).size() >= 10) {
            return "Deine Fraktion hat das limit von 10 Plantagen erreicht.";
        }
        Block aboveBlock = block.getLocation().add(0, 1, 0).getBlock();
        if (aboveBlock.getType() != Material.AIR) {
            return "Es ist kein freier Punkt gefunden wurden.";
        }
        Plant plant = new Plant(factionData, Utils.getTime(), aboveBlock, plantType);
        aboveBlock.setType(Material.FERN);
        block.setType(Material.PODZOL);
        plants.add(plant);
        factionManager.sendCustomMessageToFaction("§8[§6Plantage§8]§2 " + player.getName() + " hat eine " + plantType.getName() + "§2 Plantage gelegt.", factionData.getName());
        ItemManager.removeCustomItem(player, plantType.getPlantItem(), 1);
        return "Du hast eine " + plantType.getName() + " Plantage gelegt.";
    }

    public void openPlant(Player player, Plant plant) {
        if (plant == null) return;
        Faction factionData = plant.getPlanter();
        PlayerData playerData = playerManager.getPlayerData(player);
        InventoryManager inventoryManager = new InventoryManager(player, 9, Component.text("§8 » §2Plantage"), true, false);
        int i = 0;
        if (plant.getTime() < 30) {
            inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(plant.getType().getDrug().getItem().getMaterial(), 1, 0, "§2Ernten", "§8 ➥ §a" + plant.getYield() + "g")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    int diff = playerData.getInventory().getDiff();
                    if (plant.getYield() > diff) {
                        playerData.getInventory().addItem(plant.getType().getDrug().getItem(), diff);
                        plant.setYield(plant.getYield() - diff);
                        factionManager.sendCustomMessageToFactions("§8[§6Plantage§8]§2 " + player.getName() + " hat eine " + plant.getType().getName() + " Plantage geernet. §8(§6" + diff + "g§8)", factionData.getName());
                    } else {
                        playerData.getInventory().addItem(plant.getType().getDrug().getItem(), plant.getYield());
                        plant.getBlock().setType(Material.AIR);
                        plant.getBlock().getLocation().subtract(0, 1, 0).getBlock().setType(Material.GRASS_BLOCK);
                        plants.remove(plant);
                        factionManager.sendCustomMessageToFactions("§8[§6Plantage§8]§2 " + player.getName() + " hat eine " + plant.getType().getName() + " Plantage geernet. §8(§6" + plant.getYield() + "g§8)", factionData.getName());
                    }
                    player.closeInventory();
                    if ((plant.getYield() >= 10 || diff >= 10) && !plant.isReceivedXP()) {
                        plant.setReceivedXP(true);
                        playerManager.addExp(player, Utils.random(5, 20));
                    }
                }
            });
            i++;
        }
        inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(RoleplayItem.FERTILIZER.getMaterial(), 1, 0, "§2Düngern", "§8 ➥ §a" + plant.getFertilizer() + "§7/§a60")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (plant.getFertilizer() > 10 || ItemManager.getCustomItemCount(player, RoleplayItem.FERTILIZER) < 1) {
                    return;
                }
                ItemManager.removeCustomItem(player, RoleplayItem.FERTILIZER, 1);
                factionManager.sendCustomMessageToFactions("§8[§6Plantage§8]§2 " + player.getName() + " hat eine " + plant.getType().getName() + " Plantage gedüngt.", factionData.getName());
                playerManager.addExp(player, Utils.random(8, 10));
                plant.setFertilizer(60);
                player.closeInventory();
            }
        });
        i++;
        inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(RoleplayItem.WATER.getMaterial(), 1, 0, "§bWässern", "§8 ➥ §3" + plant.getWater() + "§7/§360")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (plant.getWater() > 10 || ItemManager.getCustomItemCount(player, RoleplayItem.WATER) < 1) {
                    return;
                }
                ItemManager.removeCustomItem(player, RoleplayItem.WATER, 1);
                factionManager.sendCustomMessageToFactions("§8[§6Plantage§8]§2 " + player.getName() + " hat eine " + plant.getType().getName() + " Plantage gewässert.", factionData.getName());
                playerManager.addExp(player, Utils.random(8, 10));
                plant.setWater(60);
                player.closeInventory();
            }
        });
        i++;
        inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(Material.PAPER, 1, 0, "§bInformation", Arrays.asList("§8 ➥ §3Ertrag§8:§7 " + plant.getYield(), "§8 ➥ §3Gelegt§8: §7" + Utils.localDateTimeToReadableString(plant.getPlanted()), "§8 ➥ §3Typ§8: §7" + plant.getType().getName()))) {
            @Override
            public void onClick(InventoryClickEvent event) {

            }
        });
        i++;
    }

    public Collection<Plant> getPlants() {
        return plants;
    }

    private Collection<Plant> getNearbyPlants(Location location) {
        return plants.stream()
                .filter(x -> x.getBlock().getLocation().distance(location) < 10)
                .toList();
    }

    private void openBurnPlant(Player player, Plant plant) {
        Collection<Plant> nearbyPlants = getNearbyPlants(player.getLocation());
        if (nearbyPlants.size() == 0) return;
        InventoryManager inventoryManager = new InventoryManager(player, 9, Component.text("§8 » §c" + nearbyPlants.size() + " Plantagen"));
        inventoryManager.setItem(new CustomItem(4, ItemManager.createItem(Material.FLINT_AND_STEEL, 1, 0, "§cVerbrennen")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                Collection<PlayerData> nearbyExecutives = factionManager.getFactionMemberInRange("Polizei", player.getLocation(), 10, false);
                nearbyExecutives.addAll(factionManager.getFactionMemberInRange("FBI", player.getLocation(), 10, false));
                if (nearbyExecutives.size() < 4) {
                    player.sendMessage(Prefix.ERROR + "Es sind nicht genug Beamte in der nähe.");
                    return;
                }
                factionManager.sendCustomMessageToFactions("§9HQ: Es wurden " + nearbyPlants.size() + " Plantagen verbrand.", "Polizei", "FBI");
                int xp = 100 * nearbyPlants.size();
                xp = xp / nearbyExecutives.size();
                for (PlayerData executive : nearbyExecutives) {
                    playerManager.addExp(executive.getPlayer(), xp);
                }
                for (Plant p : nearbyPlants) {
                    p.getBlock().setType(Material.FIRE);
                }
                Utils.waitSeconds(10, () -> {
                    for (Plant p : nearbyPlants) {
                        p.getBlock().setType(Material.AIR);
                        p.getBlock().getLocation().subtract(0, 1, 0).getBlock().setType(Material.GRASS_BLOCK);
                        plants.remove(p);
                    }
                });
            }
        });
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if ((player.getInventory().getItemInMainHand().getType().equals(Material.WATER_BUCKET)
                || player.getInventory().getItemInMainHand().getType().equals(Material.BONE_MEAL)) && player.getGameMode() != GameMode.CREATIVE) {
            event.setCancelled(true);
        }
        PlayerData playerData = playerManager.getPlayerData(player);
        if (!playerData.canInteract() || playerData.isCuffed() || playerData.getFaction() == null) return;
        if (event.getClickedBlock() == null) return;
        if (Main.getInstance().getCooldownManager().isOnCooldown(player, "plant")) return;
        if (event.getClickedBlock().getType().equals(Material.GRASS_BLOCK) || event.getClickedBlock().getType().equals(Material.DIRT)) {
            PlantType drug = null;
            for (PlantType type : PlantType.values()) {
                if (ItemManager.equals(player.getInventory().getItemInMainHand(), type.getPlantItem())) drug = type;
            }
            if (drug == null) {
                return;
            }
            player.sendMessage("§8[§6Plantage§8]§2 " + plant(player, drug, event.getClickedBlock()));
            return;
        }
        Plant plant = getPlantByBlock(event.getClickedBlock());
        if (plant == null) {
            return;
        }
        Main.getInstance().getCooldownManager().setCooldown(player, "plant", 1);
        if (!plant.getPlanter().getName().equalsIgnoreCase(playerData.getFaction())) {
            if (playerData.isExecutiveFaction()) {
                openBurnPlant(player, plant);
                return;
            }
            player.sendMessage(Prefix.ERROR + "Diese Plantage gehört nicht deiner Fraktion.");
            return;
        }
        openPlant(player, plant);
    }

    @EventHandler
    public void MinuteTick(MinuteTickEvent event) {
        for (Plant plant : plants) {
            plant.setTime(plant.getTime() - 1);
            plant.setWater(plant.getWater() - 1);
            plant.setFertilizer(plant.getWater() - 1);
            if (event.getMinute() % 15 == 0) {
                if (plant.getTime() < 1) {
                    if (plant.getTime() < -5) {
                        plant.setYield(plant.getYield() - 20);
                        if (plant.getYield() > 0) continue;
                        plant.getBlock().setType(Material.AIR);
                        plants.remove(plant);
                    }
                    continue;
                }
                if (plant.getWater() >= 1) {
                    plant.setYield(plant.getYield() + 2);
                }
                if (plant.getFertilizer() >= 1) {
                    plant.setYield(plant.getYield() + 2);
                }
            }
        }
        if (event.getMinute() % 15 == 0) {
            for (Faction factionData : factionManager.getFactions().stream().filter(Faction::isBadFrak).toList()) {
                int wateredFertilized = (int) plants.stream().filter(x -> (x.getPlanter() == factionData) && x.getWater() < 5 && x.getFertilizer() < 5).count();
                int inProgress = (int) plants.stream().filter(x -> x.getPlanter() == factionData).count() - wateredFertilized;
                int degenerate = (int) plants.stream().filter(x -> x.getPlanter() == factionData && x.getTime() < 1).count();
                if (inProgress >= 1)
                    factionManager.sendCustomMessageToFactions("§8[§6Plantage§2]§2 " + inProgress + " Plantagen befinden sich im Reifeprozess.", factionData.getName());
                if (wateredFertilized >= 1)
                    factionManager.sendCustomMessageToFactions("§8[§6Plantage§8]§2 " + wateredFertilized + " Plantagen müssen gewässert/gedüngt werden.", factionData.getName());
                if (degenerate >= 1)
                    factionManager.sendCustomMessageToFactions("§8[§6Plantage§2]§2 " + degenerate + " Plantagen verkommen.", factionData.getName());
            }
        }
    }
}
