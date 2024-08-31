package de.polo.voidroleplay.utils.GamePlay;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.*;
import de.polo.voidroleplay.utils.*;
import de.polo.voidroleplay.utils.enums.RoleplayItem;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class MilitaryDrop implements Listener {
    public static boolean ACTIVE = false;
    private final PlayerManager playerManager;
    private final FactionManager factionManager;
    private final LocationManager locationManager;

    private final List<Player> joinedPlayers = new ArrayList<>();
    private final List<Player> alivePlayers = new ArrayList<>();

    private final HashMap<FactionData, Integer> stats = new HashMap<>();
    private final HashMap<String, Location> factionSpawns = new HashMap<>();
    private boolean freezePlayers = false;
    private boolean isRoundActive = false;
    private int currentRound = 0;

    private final List<Location> spawns = new ArrayList<>();
    private final List<Location> chestSpawns = new ArrayList<>();
    private FactionData staat = null;

    private final Location middleArena;

    private int arenaSize = 200;

    public MilitaryDrop(PlayerManager playerManager, FactionManager factionManager, LocationManager locationManager) {
        this.playerManager = playerManager;
        this.factionManager = factionManager;
        this.locationManager = locationManager;

        middleArena = locationManager.getLocation("middleArena");

        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }

    public void start() {
        ACTIVE = true;
        staat = new FactionData();
        staat.setFullname("Staat");
        staat.setName("Staat");
        staat.setPrimaryColor("9");
        staat.setId(-1);

        for (LocationData location : locationManager.getLocations()) {
            if (location.getType() == null) continue;
            if (location.getType().equalsIgnoreCase("militarydrop"))
                spawns.add(new Location(Bukkit.getWorld("world"), location.getX(), location.getY(), location.getZ()));
        }
        startRound();
    }

    public boolean end() {
        ACTIVE = false;

        for (Player player : joinedPlayers) {
            player.setGameMode(GameMode.SPECTATOR);
        }

        FactionData topFaction = null;
        int maxPoints = 0;
        int countTopFactions = 0;

        for (Map.Entry<FactionData, Integer> entry : stats.entrySet()) {
            if (entry.getValue() > maxPoints) {
                maxPoints = entry.getValue();
                topFaction = entry.getKey();
                countTopFactions = 1; // Reset count since we found a new top
            } else if (entry.getValue() == maxPoints) {
                countTopFactions++;
            }
        }

        if (countTopFactions > 1) {
            return false;
        }

        for (Player player : joinedPlayers) {
            handleQuit(player);
        }

        if (topFaction != null) {
            sendMessage("§8[§cMilitärabsturz§8]§f Die Fraktion " + topFaction.getFullname() + " hat das Event mit den meisten Punkten gewonnen!");
        }

        if (topFaction != null) GlobalStats.setValue("weapondrop", String.valueOf(topFaction.getId()), true);
        return true;
    }

    public boolean handleJoin(Player player) {
        if (!ACTIVE) return false;
        if (joinedPlayers.contains(player)) return false;
        PlayerData playerData = playerManager.getPlayerData(player);
        if (playerData.getFaction() == null) return false;
        FactionData factionData = factionManager.getFactionData(playerData.getFaction());
        if (!(playerData.getFaction().equalsIgnoreCase("FBI") || playerData.getFaction().equalsIgnoreCase("Polizei") || factionData.isBadFrak())) {
            return false;
        }

        System.out.println("Size of Team " + factionData.getName() + ": " + getFactionPlayers(factionData.getName()).size());

        if (getFactionPlayers(playerData.getFaction()).size() > 10) {
            return false;
        }
        if (playerData.getFaction().equalsIgnoreCase("FBI") || playerData.getFaction().equalsIgnoreCase("Polizei")) {
            factionManager.sendCustomMessageToFaction("Polizei", "§8[§cMilitärabsturz§8]§f " + player.getName() + " hat das Event betreten!");
            factionManager.sendCustomMessageToFaction("FBI", "§8[§cMilitärabsturz§8]§f " + player.getName() + " hat das Event betreten!");
        } else {
            factionManager.sendCustomMessageToFaction(playerData.getFaction(), "§8[§cMilitärabsturz§8]§f " + player.getName() + " hat das Event betreten!");
        }

        joinedPlayers.add(player);
        playerData.setVariable("inventory::military", player.getInventory().getContents());
        player.getInventory().clear();

        if (isRoundActive) {
            player.setGameMode(GameMode.SPECTATOR);
            handleSpawn(player);
        } else {
            equipPlayer(player);
            List<Location> locations = spawns;
            if (!locations.isEmpty()) {
                Collections.shuffle(locations);
                Location randomLocation = locations.get(0);
                boolean success = player.teleport(randomLocation);
            }
        }

        return true;
    }

    private void equipPlayer(Player player) {
        Main.getInstance().weapons.giveWeaponToPlayer(player, Material.DIAMOND_HORSE_ARMOR, WeaponType.MILITARY);
        player.getInventory().addItem(ItemManager.createItem(RoleplayItem.SNUFF.getMaterial(), 15, 0, RoleplayItem.SNUFF.getDisplayName()));
        player.getInventory().addItem(ItemManager.createItem(RoleplayItem.CIGAR.getMaterial(), 15, 0, RoleplayItem.CIGAR.getDisplayName()));
        player.getInventory().addItem(ItemManager.createItem(RoleplayItem.SMARTPHONE.getMaterial(), 1, 0, RoleplayItem.SMARTPHONE.getDisplayName()));
    }

    public void handleQuit(Player player) {
        if (!joinedPlayers.contains(player)) return;
        joinedPlayers.remove(player);
        alivePlayers.remove(player);
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        player.setGameMode(GameMode.SURVIVAL);
        if (playerData.getFaction() == null) return;
        Main.getInstance().utils.deathUtil.revivePlayer(player, false);
        locationManager.useLocation(player, playerData.getFaction());
        for (ItemStack item : player.getInventory().getContents()) {
            for (WeaponData weaponData : Weapons.weaponDataMap.values()) {
                if (weaponData.getMaterial() != null && item != null) {
                    if (item.getType() == weaponData.getMaterial()) {
                        ItemMeta meta = item.getItemMeta();
                        Weapon weapon = Main.getInstance().weapons.getWeaponFromItemStack(item);
                        if (weapon.getWeaponType() == WeaponType.MILITARY) {
                            Main.getInstance().weapons.removeWeapon(player, item);
                        }
                    }
                }
            }
        }
        player.getInventory().clear();
        player.getInventory().setContents(playerData.getVariable("inventory::military"));

        if (isRoundActive && ACTIVE) {
            rollWinner();
        }
    }

    private void rollWinner() {
        System.out.println("Teams: " + getTeamsAlive().size());
        if (getTeamsAlive().size() <= 1) {
            FactionData winner = getTeamsAlive().stream().findFirst().get();
            addPoints(winner.getName(), 20);
            sendRankingListToPlayers();
            sendMessage("§8[§cMilitärabsturz§8]§f Die Fraktion " + winner.getFullname() + " haben die Runde gewonnen!");
            isRoundActive = false;
            Main.waitSeconds(5, this::startRound);
        }
    }

    private void sendRankingListToPlayers() {
        List<Map.Entry<FactionData, Integer>> sortedStats = new ArrayList<>(stats.entrySet());
        sortedStats.sort((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()));

        StringBuilder rankingMessage = new StringBuilder("§8[§cMilitärabsturz§8]§f Rangliste nach dieser Runde:\n");
        int rank = 1;
        for (Map.Entry<FactionData, Integer> entry : sortedStats) {
            rankingMessage.append("§6§l#").append(rank).append(" §7")
                    .append(entry.getKey().getFullname()).append("§8:§e ")
                    .append(entry.getValue()).append(" Punkte\n");
            rank++;
        }

        for (Player player : joinedPlayers) {
            player.sendMessage(rankingMessage.toString());
        }
    }

    public Collection<Player> getFactionPlayers(String faction) {
        List<Player> players = new ArrayList<>();
        for (Player player : joinedPlayers) {
            PlayerData playerData = playerManager.getPlayerData(player);
            if (faction.equalsIgnoreCase("FBI") || faction.equalsIgnoreCase("Polizei")) {
                if (playerData.getFaction().equalsIgnoreCase("FBI") || playerData.getFaction().equalsIgnoreCase("Polizei")) {
                    players.add(player);
                }
            } else if (playerData.getFaction().equalsIgnoreCase(faction)) {
                players.add(player);
            }
        }

        return players;
    }

    public boolean handleDeath(Player player, Player killer) {
        alivePlayers.remove(player);
        PlayerData killerData = playerManager.getPlayerData(killer);
        String faction = killerData.getFaction();
        if (!joinedPlayers.contains(player)) return false;
        if (faction.equalsIgnoreCase("FBI") || faction.equalsIgnoreCase("Polizei")) {
            faction = "Staat";
            factionManager.sendCustomMessageToFaction("Polizei", "§8[§cMilitärabsturz§8]§7 +1 Punkt für das Töten eines Gegners (" + killer.getName() + " » " + player.getName() + ")");
            factionManager.sendCustomMessageToFaction("FBI", "§8[§cMilitärabsturz§8]§7 +1 Punkt für das Töten eines Gegners (" + killer.getName() + " » " + player.getName() + ")");
        } else {
            factionManager.sendCustomMessageToFaction(killerData.getFaction(), "§8[§cMilitärabsturz§8]§7 +1 Punkt für das Töten eines Gegners (" + killer.getName() + " » " + player.getName() + ")");
        }
        addPoints(faction, 1);
        rollWinner();
        player.setGameMode(GameMode.SPECTATOR);
        return true;
    }

    private void sendMessage(String message) {
        for (FactionData factionData : getTeams()) {
            if (staat == factionData) {
                factionManager.sendCustomMessageToFaction("Polizei", message);
                factionManager.sendCustomMessageToFaction("FBI", message);
            } else {
                factionManager.sendCustomMessageToFaction(factionData.getName(), message);
            }
        }
    }

    private Collection<FactionData> getTeamsAlive() {
        List<FactionData> factions = new ArrayList<>();
        for (Player player : alivePlayers) {
            PlayerData playerData = playerManager.getPlayerData(player);
            if (playerData.getFaction().equalsIgnoreCase("FBI") || playerData.getFaction().equalsIgnoreCase("Polizei")) {
                FactionData data = getStaat();
                if (!factions.contains(data)) factions.add(data);
                continue;
            }
            FactionData factionData = factionManager.getFactionData(playerData.getFaction());
            if (!factions.contains(factionData)) factions.add(factionData);
        }

        return factions;
    }

    private Collection<FactionData> getTeams() {
        List<FactionData> factions = new ArrayList<>();
        for (Player player : joinedPlayers) {
            PlayerData playerData = playerManager.getPlayerData(player);
            if (playerData.getFaction().equalsIgnoreCase("FBI") || playerData.getFaction().equalsIgnoreCase("Polizei")) {
                FactionData data = getStaat();
                if (!factions.contains(data)) factions.add(data);
                continue;
            }
            FactionData factionData = factionManager.getFactionData(playerData.getFaction());
            if (!factions.contains(factionData)) factions.add(factionData);
        }
        return factions;
    }

    private FactionData getStaat() {
        return staat;
    }

    public void startRound() {
        if (currentRound >= 5 && end()) {
            return;
        }

        currentRound++;
        factionSpawns.clear();
        for (Player player : joinedPlayers) {
            handleSpawn(player);
        }
        freezePlayers = true;
        clearPlayers();
        arenaSize = 200;
        sendCountdown(5);
    }

    private void handleSpawn(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player);
        String faction = playerData.getFaction();
        if (playerManager.isInStaatsFrak(player)) {
            faction = "Staat";
        }
        Location location;
        if (factionSpawns.get(faction) == null) {
            location = getRandomLocation(faction);
            factionSpawns.put(faction, location);
        }
        location = factionSpawns.get(faction);
        player.teleport(location);
    }

    private void sendCountdown(int seconds) {
        new BukkitRunnable() {
            int countdown = seconds;

            @Override
            public void run() {
                if (countdown > 0) {
                    sendTitle("§cRunde startet", "§cin " + countdown + " Sekunden");
                    countdown--;
                } else {
                    sendTitle("§aRunde startet", "");
                    freezePlayers = false;
                    isRoundActive = true;
                    alivePlayers.clear();
                    alivePlayers.addAll(joinedPlayers);
                    equipPlayers();
                    this.cancel();
                }
            }
        }.runTaskTimer(Main.getInstance(), 0, 20);
    }

    private void sendTitle(String title, String subtitle) {
        for (Player player : joinedPlayers) {
            player.sendTitle(title, subtitle, 10, 20, 10);
        }
    }

    public void spawnChests() {

    }

    private void addPoints(String faction, int points) {
        FactionData factionData;
        if (faction.equalsIgnoreCase("Staat")) {
            factionData = staat;
        } else {
            factionData = factionManager.getFactionData(faction);
        }
        if (stats.get(factionData) == null) {
            stats.put(factionData, points);
        } else {
            int p = stats.get(factionData) + points;
            stats.replace(factionData, p);
        }
    }

    public boolean isPlayerInEvent(Player player) {
        return joinedPlayers.contains(player);
    }

    private void equipPlayers() {
        for (Player player : joinedPlayers) {
            for (PotionEffect potionEffect : player.getActivePotionEffects()) {
                player.removePotionEffect(potionEffect.getType());
            }
            player.setHealth(player.getMaxHealth());
            equipPlayer(player);
        }
    }

    private void clearPlayers() {
        for (Player player : joinedPlayers) {
            player.setGameMode(GameMode.SURVIVAL);
            for (ItemStack item : player.getInventory().getContents()) {
                for (WeaponData weaponData : Weapons.weaponDataMap.values()) {
                    if (weaponData.getMaterial() != null && item != null) {
                        if (item.getType() == weaponData.getMaterial()) {
                            ItemMeta meta = item.getItemMeta();
                            Weapon weapon = Main.getInstance().weapons.getWeaponFromItemStack(item);
                            if (weapon.getWeaponType() == WeaponType.MILITARY) {
                                Main.getInstance().weapons.removeWeapon(player, item);
                            }
                        }
                    }
                }
            }
            player.getInventory().clear();
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!freezePlayers) return;
        if (!isPlayerInEvent(player)) return;
        event.setCancelled(true);
    }

    private Location getRandomLocation(String faction) {
        Random random = new Random();
        Location randomLocation;
        do {
            randomLocation = spawns.get(random.nextInt(spawns.size()));
        } while (factionSpawns.containsValue(randomLocation));
        factionSpawns.put(faction, randomLocation);
        return randomLocation;
    }

    public void everySecond() {
        /*if (!isRoundActive) return;
        if (arenaSize > 25) arenaSize--;
        drawBorder();
        for (Player player : alivePlayers) {
            if (player.getLocation().distance(middleArena) < arenaSize) continue;
            player.damage(1);
            Main.getInstance().utils.sendActionBar(player, "§cDu bist nicht in der Zone!");
        }*/
    }

    private void drawBorder() {
        int halfSize = arenaSize;
        middleArena.setWorld(Bukkit.getWorld("world"));

        Location corner1 = middleArena.clone().add(-halfSize, 0, -halfSize);
        Location corner2 = middleArena.clone().add(halfSize, 0, -halfSize);
        Location corner3 = middleArena.clone().add(halfSize, 0, halfSize);
        Location corner4 = middleArena.clone().add(-halfSize, 0, halfSize);

        drawParticleLine(corner1, corner2);
        drawParticleLine(corner2, corner3);
        drawParticleLine(corner3, corner4);
        drawParticleLine(corner4, corner1);
    }

    private void drawParticleLine(Location start, Location end) {
        double distance = start.distance(end);
        int points = (int) distance * 5;
        double dx = (end.getX() - start.getX()) / points;
        double dz = (end.getZ() - start.getZ()) / points;

        // DustOptions für den REDSTONE-Partikel
        Particle.DustOptions dustOptions = new Particle.DustOptions(Color.RED, 1);

        for (int i = 0; i <= points; i++) {
            for (int j = 0; j < 10; j++) {
                Location point = start.clone().add(dx * i, j, dz * i);
                Bukkit.getWorld("world").spawnParticle(Particle.REDSTONE, point, 1, dustOptions);
            }
        }
    }

}
