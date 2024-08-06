package de.polo.voidroleplay.game.faction.plants;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.FactionData;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.database.MySQL;
import de.polo.voidroleplay.utils.*;
import de.polo.voidroleplay.utils.InventoryManager.CustomItem;
import de.polo.voidroleplay.utils.InventoryManager.InventoryManager;
import de.polo.voidroleplay.utils.enums.RoleplayItem;
import de.polo.voidroleplay.game.events.MinuteTickEvent;
import lombok.SneakyThrows;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class PlantFunctions implements Listener {
    private final List<Plant> plantagen = new ArrayList<>();
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
        Statement statement = mySQL.getStatement();
        ResultSet resultSet = statement.executeQuery("SELECT * FROM plantagen");
        while (resultSet.next()) {
            Plant plant = new Plant();
            plant.setId(resultSet.getInt("id"));
            plant.setStorage(resultSet.getInt("storage"));
            plant.setOwner(resultSet.getString("owner"));
            plant.setLastAttack(resultSet.getTimestamp("lastAttack").toLocalDateTime());
            plant.setMultiplier(resultSet.getFloat("multiplier"));
            plantagen.add(plant);
        }
    }

    private Plant getById(int id) {
        for (Plant plant : getPlants()) {
            if (plant.getId() == id) return plant;
        }
        return null;
    }

    private boolean canAttack(Plant plant) {
        Duration duration = Duration.between(plant.getLastAttack(), LocalDateTime.now());
        long minutesDifference = duration.toMinutes();
        if (minutesDifference < 60) {
            return false;
        }
        return true;
    }

    public long getMinuteDifference(Plant plant) {
        Duration duration = Duration.between(plant.getLastAttack(), LocalDateTime.now());
        long minutesDifference = duration.toMinutes();
        return 360 - minutesDifference;
    }
    public long getTakeOutDifference(Plant plant) {
        LocalDateTime now = Utils.getTime();
        LocalDateTime lastAttack = plant.getLastAttack();

        int nowMinutes = now.getMinute();
        int lastAttackMinutes = lastAttack.getMinute();

        return Math.abs(nowMinutes - lastAttackMinutes);
    }

    public void openPlant(Player player, int id) {
        Plant plant = getById(id);
        if (plant == null) return;
        FactionData factionData = factionManager.getFactionData(plant.getOwner());
        PlayerData playerData = playerManager.getPlayerData(player);
        FactionData playerFactionData = factionManager.getFactionData(playerData.getFaction());
        if (!playerFactionData.isBadFrak()) return;
        String owner = "§" + factionData.getPrimaryColor() + factionData.getFullname();
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8 » §2Plantage (" + plant.getMultiplier() + "x)", true, true);
        if (canAttack(plant)) {
            inventoryManager.setItem(new CustomItem(13, ItemManager.createItem(Material.PAPER, 1, 0, "§bInformation", Arrays.asList("§8 ➥ §7Besitzer§8: " + owner, "§8 ➥ §cKlicke zum attackieren"))) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    if (plant.getOwner().equalsIgnoreCase(playerData.getFaction())) {
                        player.sendMessage(Main.error + "Du kannst deine eigene Plantage nicht übernehmen.");
                        return;
                    }
                    player.closeInventory();
                    plant.setLastAttack(LocalDateTime.now());
                    factionManager.sendCustomMessageToFaction(plant.getOwner(), "§8[§2Plantage§8]§c Jemand versucht deine deine Plantage zu übernehmen.");
                    factionManager.sendCustomMessageToFaction(playerData.getFaction(), "§8[§2Plantage§8]§a Ihr fangt an die Plantage von " + plant.getOwner() +" zu übernehmen!");
                    player.sendMessage("§8[§2Plantage§8]§7 Die nächsten 10 Minuten muss mindestens ein Fraktionmitglied im Umkreis von 30 Metern leben.");
                    plant.setAttacker(player);
                    plant.setAttackerFaction(playerData.getFaction());
                    rob.put(plant, 0);
                }
            });
        } else {
            inventoryManager.setItem(new CustomItem(13, ItemManager.createItem(Material.PAPER, 1, 0, "§bInformation", Arrays.asList("§8 ➥ §7Besitzer§8: " + owner, "§8 ➥ §7Attackierbar§8:§e " + getMinuteDifference(plant) + "min"))) {
                @Override
                public void onClick(InventoryClickEvent event) {

                }
            });
        }
        if (plant.getOwner().equalsIgnoreCase(playerData.getFaction())) {
            if (plant.hasTookout(player.getUniqueId())) {
                inventoryManager.setItem(new CustomItem(11, ItemManager.createItem(RoleplayItem.PIPE_TOBACCO.getMaterial(), 1, 0, RoleplayItem.PIPE_TOBACCO.getDisplayName(), "§8 » §cDu kannst in " + getTakeOutDifference(plant) + "min wieder Marihuana entnehmen!")) {
                    @Override
                    public void onClick(InventoryClickEvent event) {

                    }
                });
            } else {
                int takeOutAmount = Math.round(10 * (plant.getMultiplier() + factionData.upgrades.getDrugEarningLevel()));
                inventoryManager.setItem(new CustomItem(11, ItemManager.createItem(RoleplayItem.PIPE_TOBACCO.getMaterial(), 1, 0, RoleplayItem.PIPE_TOBACCO.getDisplayName(), Arrays.asList("§8 » §cKlick um dir " + takeOutAmount + " zu entnehmen"))) {
                    @Override
                    public void onClick(InventoryClickEvent event) {
                        if (plant.hasTookout(player.getUniqueId())) {
                            return;
                        }
                        player.closeInventory();
                        Main.getInstance().beginnerpass.didQuest(player, 5, takeOutAmount);
                        Main.getInstance().seasonpass.didQuest(player, 18, takeOutAmount);
                        ItemManager.addCustomItem(player, RoleplayItem.PIPE_TOBACCO, takeOutAmount);
                        plant.addTookout(player.getUniqueId());
                        plant.save();
                    }
                });
            }
        }
    }

    public Collection<Plant> getPlants() {
        return plantagen;
    }

    @EventHandler
    public void MinuteTick(MinuteTickEvent event) {
        for (Plant plant : plantagen) {
            if (plant.getLastAttack().getMinute() == event.getMinute()) {
                plant.clearTookout();
            }
        }
        for (Plant plant : rob.keySet()) {
            Location location = locationManager.getLocation("plant-" + plant.getId());
            if (!factionManager.isFactionMemberInRange(plant.getAttackerFaction(), location, 30, false)) {
                rob.remove(plant);
                factionManager.sendCustomMessageToFaction(plant.getOwner(), "§8[§2Plantage§8]§a Die Angreifer haben aufgehört die Plantage zu übernehmen.");
                factionManager.sendCustomMessageToFaction(plant.getAttackerFaction(), "§8[§2Plantage§8]§c Ihr habt aufgehört die Plantage zu übernehmen.");
                return;
            }
            int currentTime = rob.get(plant);
            if (currentTime >= 10) {
                factionManager.sendCustomMessageToFaction(plant.getOwner(), "§8[§2Plantage§8]§c Die Angreifer haben es geschafft eure Plantage zu übernehmen.");
                factionManager.sendCustomMessageToFaction(plant.getAttackerFaction(), "§8[§2Plantage§8]§a Ihr habt es geschafft die Plantage zu übernehmen.");
                for (PlayerData playerData1 : factionManager.getFactionMemberInRange(plant.getAttackerFaction(), location, 30, true)) {
                    playerManager.addExp(playerData1.getPlayer(), Main.random(15, 30));
                }
                for (Plant p : getPlants()) {
                    if (p.getOwner().equalsIgnoreCase(plant.getAttackerFaction())) {
                        p.setOwner(plant.getOwner());
                        p.save();
                    }
                }
                plant.setOwner(plant.getAttackerFaction());
                plant.save();
                rob.remove(plant);
            } else {
                rob.replace(plant, ++currentTime);
                int remaining = (10 - currentTime + 1);
                factionManager.sendCustomMessageToFaction(plant.getOwner(), "§8[§2Plantage§8]§c Die Angreifer haben noch " + remaining + " Minuten bis die Plantage übernommen ist.");
                factionManager.sendCustomMessageToFaction(plant.getAttackerFaction(), "§8[§2Plantage§8]§a Noch " + remaining + " Minuten bis die Plantage übernommen ist.");

            }
        }
        if (LocalDateTime.now().getHour() < 16 && LocalDateTime.now().getHour() > 22) return;
        /*for (FactionData factionData : factionManager.getFactions()) {
            int plus = 0;
            for (Plant plant : getPlants()) {
                if (apotheke.getOwner().equalsIgnoreCase(factionData.getName())) {
                    if (apotheke.getLastAttack().getMinute() == event.getMinute()) {
                        if (apotheke.isStaat()) plus += ServerManager.getPayout("apotheke_besetzt_staat");
                        else plus += ServerManager.getPayout("apotheke_besetzt_normal");
                    }
                }
            }
            factionData.setJointsMade(plus);
            if (plus >= 1) {
                for (PlayerData playerData : playerManager.getPlayers()) {
                    if (playerData.getFaction().equalsIgnoreCase(factionData.getName())) {
                        Player player = Bukkit.getPlayer(playerData.getUuid());
                        player.sendMessage("§8[§" + factionData.getPrimaryColor() + factionData.getName() + "§8]§a Deine Fraktion hat §2" + plus + " Joints§a aus den aktuell übernommenen Apotheken erhalten.");
                        factionData.storage.setJoint(factionData.storage.getJoint() + plus);
                        factionData.storage.save();
                    }
                }
            }
        }*/
    }
}
