package de.polo.voidroleplay.game.faction.plants;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.FactionData;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.database.impl.MySQL;
import de.polo.voidroleplay.game.events.MinuteTickEvent;
import de.polo.voidroleplay.manager.FactionManager;
import de.polo.voidroleplay.manager.InventoryManager.CustomItem;
import de.polo.voidroleplay.manager.InventoryManager.InventoryManager;
import de.polo.voidroleplay.manager.ItemManager;
import de.polo.voidroleplay.manager.LocationManager;
import de.polo.voidroleplay.manager.PlayerManager;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.utils.Utils;
import de.polo.voidroleplay.utils.enums.Drug;
import de.polo.voidroleplay.utils.enums.PlantType;
import de.polo.voidroleplay.utils.enums.RoleplayItem;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.SneakyThrows;
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
    private final MySQL mySQL;
    private final Utils utils;
    private final FactionManager factionManager;
    private final PlayerManager playerManager;
    private final HashMap<Plant, Integer> rob = new HashMap<>();
    private final LocationManager locationManager;

    @SneakyThrows
    public PlantFunctions(MySQL mySQL, Utils utils, FactionManager factionManager, PlayerManager playerManager, LocationManager locationManager) {
        this.mySQL = mySQL;
        this.utils = utils;
        this.factionManager = factionManager;
        this.playerManager = playerManager;
        this.locationManager = locationManager;
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }

    private Plant getPlantByBlock(Block block) {
        for (Plant plant : getPlants()) {
            if (plant.getBlock() == block) return plant;
        }
        return null;
    }

    private Collection<Plant> getPlantsByFaction(FactionData factionData) {
        return plants.stream().filter(x -> x.getPlanter() == factionData).collect(Collectors.toList());
    }

    public void cleanup() {
        for (Plant plant : plants) {
            plant.getBlock().setType(Material.AIR);
        }
    }

    public String plant(Player player, PlantType plantType, Block block) {
        PlayerData playerData = playerManager.getPlayerData(player);
        FactionData factionData = factionManager.getFactionData(playerData.getFaction());
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
        Plant plant = new Plant(factionData, Utils.getTime(), block, plantType);
        aboveBlock.setType(Material.FERN);
        plants.add(plant);
        factionManager.sendCustomMessageToFaction("§8[§6Plantage§8]§2 " + player.getName() + " hat eine " + plantType.getName() + "§2 Plantage gelegt.", factionData.getName());
        return "Du hast eine " + plantType.name() + " Plantage gelegt.";
    }

    public void openPlant(Player player, Plant plant) {
        if (plant == null) return;
        FactionData factionData = plant.getPlanter();
        PlayerData playerData = playerManager.getPlayerData(player);
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8 » §2Plantage)", true, false);
        int i = 0;
        if (plant.getTime() < 30) {
            inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(plant.getType().getDrug().getItem().getMaterial(), 1, 0, "§2Ernten", "§8 ➥ §a" + plant.getYield() + "g")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    plant.getBlock().setType(Material.AIR);
                    ItemManager.addCustomItem(player, plant.getType().getDrug().getItem(), plant.getYield());
                    factionManager.sendCustomLeaderMessageToFactions("§8[§6Plantage§8]§2 " + player.getName() + " hat eine " + plant.getType().getName() + " Plantage geernet. §8(§6" + plant.getYield() + "g§8)", factionData.getName());
                    plants.remove(plant);
                    player.closeInventory();
                    if (plant.getYield() >= 40) {
                        playerManager.addExp(player, Main.random(10, 20));
                    }
                }
            });
            i++;
        }
        inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(RoleplayItem.FERTILIZER.getMaterial(), 1, 0, "§2Düngern", "§8 ➥ §a" + plant.getFertilizer() + "§7/§a60")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (plant.getFertilizer() > 10) {
                    return;
                }
                ItemManager.removeCustomItem(player, RoleplayItem.FERTILIZER, 1);
                factionManager.sendCustomLeaderMessageToFactions("§8[§6Plantage§8]§2 " + player.getName() + " hat eine " + plant.getType().getName() + " Plantage gedüngt.", factionData.getName());
                playerManager.addExp(player, Main.random(8, 10));
                plant.setYield(60);
                player.closeInventory();
            }
        });
        i++;
        inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(RoleplayItem.WATER.getMaterial(), 1, 0, "§bWässern", "§8 ➥ §3" + plant.getWater() + "§7/§360")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (plant.getWater() > 10) {
                    return;
                }
                ItemManager.removeCustomItem(player, RoleplayItem.WATER, 1);
                factionManager.sendCustomLeaderMessageToFactions("§8[§6Plantage§8]§2 " + player.getName() + " hat eine " + plant.getType().getName() + " Plantage gewässert.", factionData.getName());
                playerManager.addExp(player, Main.random(8, 10));
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

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = playerManager.getPlayerData(player);
        if (!playerData.canInteract() || playerData.isCuffed() || playerData.getFaction() == null) return;
        if (event.getClickedBlock() == null) return;
        if (event.getClickedBlock().getType().equals(Material.GRASS_BLOCK) || event.getClickedBlock().getType().equals(Material.DIRT)) {
            PlantType drug = null;
            if (ItemManager.equals(player.getInventory().getItemInMainHand(), RoleplayItem.COCAINE_SEEDS)) {
                drug = PlantType.COCAINE;
            } else if (ItemManager.equals(player.getInventory().getItemInMainHand(), RoleplayItem.WEED_SEEDS)) {
                drug = PlantType.WEED;
            }
            if (drug == null) {
                return;
            }
            player.sendMessage("§8[§6Plantage§8]§2 " + plant(player, drug, event.getClickedBlock()));
            return;
        }
        Plant plant = getPlantByBlock(event.getClickedBlock());
        if (plant == null) return;
        if (!plant.getPlanter().getName().equalsIgnoreCase(playerData.getFaction())) {
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
                    }
                    continue;
                }
                if (plant.getWater() >= 1) {
                    plant.setYield(plant.getYield() + 10);
                }
                if (plant.getFertilizer() >= 1) {
                    plant.setYield(plant.getYield() + 10);
                }
            }
        }
        if (event.getMinute() % 15 == 0) {
            for (FactionData factionData : factionManager.getFactions().stream().filter(FactionData::isBadFrak).collect(Collectors.toList())) {
                int wateredFertilized = (int) plants.stream().filter(x -> (x.getPlanter() == factionData) && x.getWater() < 5 && x.getFertilizer() < 5).count();
                int inProgress = (int) plants.stream().filter(x -> x.getPlanter() == factionData).count() - wateredFertilized;
                int degenerate = (int) plants.stream().filter(x -> x.getPlanter() == factionData && x.getTime() < 1).count();
                if (inProgress >= 1) factionManager.sendCustomMessageToFactions("§8[§6Plantage§2]§2 " + inProgress + " Plantagen befinden sich im Reifeprozess.", factionData.getName());
                if (wateredFertilized >= 1) factionManager.sendCustomMessageToFactions("§8[§6Plantage§2]§2 " + inProgress + " Plantagen müssen gewässert/gedüngt werden.", factionData.getName());
                if (degenerate >= 1) factionManager.sendCustomMessageToFactions("§8[§6Plantage§2]§2 " + inProgress + " Plantagen verkommen.", factionData.getName());
            }
        }
    }
}
