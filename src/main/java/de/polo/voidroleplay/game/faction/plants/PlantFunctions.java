package de.polo.voidroleplay.game.faction.plants;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.FactionData;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.database.MySQL;
import de.polo.voidroleplay.game.events.MinuteTickEvent;
import de.polo.voidroleplay.manager.FactionManager;
import de.polo.voidroleplay.manager.InventoryManager.InventoryManager;
import de.polo.voidroleplay.manager.LocationManager;
import de.polo.voidroleplay.manager.PlayerManager;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.utils.Utils;
import de.polo.voidroleplay.utils.enums.Drug;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.SneakyThrows;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

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

    public String plant(Player player, Drug drug) {
        PlayerData playerData = playerManager.getPlayerData(player);
        FactionData factionData = factionManager.getFactionData(playerData.getFaction());
        if (getPlantsByFaction(factionData).size() >= 10) {
            return "Deine Fraktion hat das limit von 10 Plantagen erreicht.";
        }
        Block block = player.getLocation().getBlock();
        Block aboveBlock = player.getLocation().add(0, 1, 0).getBlock();
        if (block.getType() != Material.AIR || aboveBlock.getType() != Material.AIR) {
            return "Es ist kein freier Punkt gefunden wurden.";
        }
        Plant plant = new Plant(factionData, Utils.getTime(), block, drug);
        plants.add(plant);
        return "Du hast eine " + drug.name() + " Plantage gelegt.";
    }

    public void openPlant(Player player, Plant plant) {
        if (plant == null) return;
        FactionData factionData = plant.getPlanter();
        PlayerData playerData = playerManager.getPlayerData(player);
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8 » §2Plantage)", true, false);

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
    }
}
