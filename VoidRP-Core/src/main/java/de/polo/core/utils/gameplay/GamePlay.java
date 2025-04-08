package de.polo.core.utils.gameplay;

import de.polo.api.Utils.inventorymanager.CustomItem;
import de.polo.api.Utils.inventorymanager.InventoryManager;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.Main;
import de.polo.core.database.impl.CoreDatabase;
import de.polo.core.faction.entity.Faction;
import de.polo.core.faction.service.impl.FactionManager;
import de.polo.core.game.base.crypto.Crypto;
import de.polo.core.game.base.extra.drop.Drop;
import de.polo.core.game.base.ffa.FFA;
import de.polo.core.game.events.MinuteTickEvent;
import de.polo.core.game.events.SubmitChatEvent;
import de.polo.core.game.faction.alliance.Alliance;
import de.polo.core.game.faction.apotheke.ApothekeFunctions;
import de.polo.core.game.faction.houseban.Houseban;
import de.polo.core.game.faction.plants.PlantFunctions;
import de.polo.core.game.faction.staat.GOVRaid;
import de.polo.core.game.faction.staat.StaatsbankRob;
import de.polo.core.location.services.impl.LocationManager;
import de.polo.core.manager.*;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.storage.*;
import de.polo.core.utils.Prefix;
import de.polo.core.utils.StaatUtil;
import de.polo.core.utils.Utils;
import de.polo.core.utils.enums.CaseType;
import de.polo.core.utils.enums.Drug;
import de.polo.core.utils.enums.RoleplayItem;
import de.polo.core.utils.enums.Weapon;
import de.polo.core.utils.player.ChatUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.SneakyThrows;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Openable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static de.polo.core.Main.gamePlay;
import static de.polo.core.Main.weaponManager;

public class GamePlay implements Listener {
    public final ApothekeFunctions apotheke;
    public final Drugstorage drugstorage;
    public final PlantFunctions plant;
    public final FactionUpgradeGUI factionUpgradeGUI;
    public final Alliance alliance;
    public final MilitaryDrop militaryDrop;
    public final HashMap<Dealer, Integer> rob = new HashMap<>();
    public final List<Block> roadblocks = new ObjectArrayList<>();
    private final PlayerManager playerManager;
    private final Utils utils;
    private final CoreDatabase coreDatabase;
    private final FactionManager factionManager;
    private final LocationManager locationManager;
    private final List<Dealer> dealers = new ObjectArrayList<>();
    @Getter
    private final FFA ffa;
    private final List<Dealer> currentDealer = new ObjectArrayList<>();
    private final List<GOVRaid> govRaids = new ObjectArrayList<>();
    private final List<PlayerDrugUsage> drugUsages = new ObjectArrayList<>();
    @Getter
    private final Crypto crypto;
    private final HashMap<Player, LocalDateTime> masks = new HashMap<>();
    public Drop activeDrop = null;
    public LocalDateTime lastDrop = Utils.getTime();
    public Houseban houseban;
    public DisplayNameManager displayNameManager;
    private StaatsbankRob staatsbankRob = null;
    private boolean isStaatsbankRobBlocked = false;

    @SneakyThrows
    public GamePlay(PlayerManager playerManager, Utils utils, CoreDatabase coreDatabase, FactionManager factionManager, LocationManager locationManager) {
        this.playerManager = playerManager;
        this.utils = utils;
        this.coreDatabase = coreDatabase;
        this.factionManager = factionManager;
        this.locationManager = locationManager;
        drugstorage = new Drugstorage(playerManager, factionManager);
        apotheke = new ApothekeFunctions(coreDatabase, utils, factionManager, playerManager, locationManager);
        plant = new PlantFunctions(coreDatabase, utils, factionManager, playerManager, locationManager);
        factionUpgradeGUI = new FactionUpgradeGUI(factionManager, playerManager, utils);
        houseban = new Houseban(playerManager, factionManager);
        displayNameManager = new DisplayNameManager(playerManager, factionManager, Main.getInstance().getScoreboardAPI());
        alliance = new Alliance(playerManager, factionManager, utils);
        militaryDrop = new MilitaryDrop(playerManager, factionManager, locationManager);
        ffa = new FFA(playerManager, locationManager);
        crypto = new Crypto();

        /*
        Dealer System: deaktiviert
         */

        /*ResultSet result = statement.executeQuery("SELECT * FROM dealer");
        while (result.next()) {
            Dealer dealer = new Dealer();
            dealer.setId(result.getInt("id"));
            dealer.setLocation(new Location(Bukkit.getWorld("world"), result.getInt("x"), result.getInt("y"), result.getInt("z"), result.getFloat("yaw"), result.getFloat("pitch")));
            dealer.setGangzone(result.getString("gangzone"));
            dealers.add(dealer);
            try {
                npc.deleteNPC(dealer.getLocation(), "dealer-" + dealer.getId());
            } catch (Exception ignored) {

            }
        }

        Collections.shuffle(dealers);

        Set<String> usedGangzones = new HashSet<>();
        int addedDealers = 0;

        for (Dealer dealer : dealers) {
            if (!usedGangzones.contains(dealer.getGangzone())) {
                currentDealer.add(dealer);
                usedGangzones.add(dealer.getGangzone());
                addedDealers++;
                try {
                    npc.spawnNPC(dealer.getLocation(), "dealer-" + dealer.getId(), "§cDealer", "dealer");

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            if (addedDealers == 3) {
                break;
            }
        }*/


        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }

    public static void useDrug(Player player, Drug drug) {
        PlayerData playerData = Main.playerManager.getPlayerData(player);

        if (playerData.isCuffed()) {
            player.sendMessage(Prefix.ERROR + "Du bist in Handschellen.");
            return;
        }

        PlayerDrugUsage existingUsage = gamePlay.getDrugUsage(player.getUniqueId(), drug);

        for (PotionEffect effect : drug.getEffects()) {
            if (effect.getType().equals(PotionEffectType.ABSORPTION)) {
                boolean isAbsorptionActive = gamePlay.getActiveDrugUsages(player.getUniqueId())
                        .stream()
                        .anyMatch(activeUsage -> activeUsage.getDrug().getEffects()
                                .stream()
                                .anyMatch(activeEffect -> activeEffect.getType().equals(PotionEffectType.ABSORPTION)));

                if (isAbsorptionActive) {
                    LocalDateTime now = Utils.getTime();
                    long remainingSeconds = -1;

                    for (PlayerDrugUsage activeUsage : gamePlay.getActiveDrugUsages(player.getUniqueId())) {
                        if (activeUsage.getDrug().getEffects().stream()
                                .anyMatch(activeEffect -> activeEffect.getType().equals(PotionEffectType.ABSORPTION))) {
                            remainingSeconds = Duration.between(
                                    now,
                                    activeUsage.getUsage().plusSeconds(activeUsage.getDrug().getTime())
                            ).getSeconds();
                            break;
                        }
                    }

                    if (remainingSeconds > 0) {
                        Main.utils.sendActionBar(player, "§cWarte noch " + remainingSeconds + " Sekunden");
                        continue;
                    }
                }
            }
            player.addPotionEffect(effect);
        }


        PlayerDrugUsage usage = new PlayerDrugUsage(player.getUniqueId(), drug, Utils.getTime());
        gamePlay.drugUsages.add(usage);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_BURP, 1, 1);
        switch (drug) {
            case JOINT -> ChatUtils.sendGrayMessageAtPlayer(player, player.getName() + " raucht eine Zigarre");
            case COCAINE -> ChatUtils.sendGrayMessageAtPlayer(player, player.getName() + " konsumiert Schnupftabak");
            case ANTIBIOTIKUM -> ChatUtils.sendGrayMessageAtPlayer(player, player.getName() + " nimmt Antibiotikum");
            case SCHMERZMITTEL -> ChatUtils.sendGrayMessageAtPlayer(player, player.getName() + " nimmt Schmerzmittel");
            case ADRENALINE_INJECTION ->
                    ChatUtils.sendGrayMessageAtPlayer(player, player.getName() + " spritzt sich Adrenalin");
        }
    }

    public Collection<Dealer> getDealer() {
        return dealers;
    }

    private PlayerDrugUsage getDrugUsage(UUID uuid, Drug drug) {
        PlayerDrugUsage usage = drugUsages.stream()
                .filter(x -> x.getUuid().equals(uuid) && x.getDrug().equals(drug))
                .findFirst()
                .orElse(null);

        if (usage != null) {
            if (Utils.getTime().isAfter(usage.getUsage().plusSeconds(drug.getTime()))) {
                drugUsages.remove(usage);
                usage = null;
            }
        }
        return usage;
    }

    private Collection<PlayerDrugUsage> getActiveDrugUsages(UUID uuid) {
        return drugUsages.stream().filter(x -> x.getUuid().equals(uuid)).collect(Collectors.toList());
    }

    private boolean isOnAbsorption(UUID uuid) {
        PlayerDrugUsage usage = drugUsages.stream()
                .filter(x -> x.getUuid().equals(uuid))
                .filter(x -> x.getDrug().getEffects().stream()
                        .anyMatch(effect -> effect.getType() == PotionEffectType.ABSORPTION))
                .findFirst()
                .orElse(null);

        if (usage != null) {
            if (Utils.getTime().isAfter(usage.getUsage().plusSeconds(usage.getDrug().getTime()))) {
                drugUsages.remove(usage);
                usage = null;
            }
        }

        return usage != null;
    }


    public void clearDrugUsages(Player player) {
        drugUsages.removeIf(drugUsage -> drugUsage.getUuid().equals(player.getUniqueId()));
    }

    @EventHandler
    public void onChatMessage(SubmitChatEvent event) {
        if (event.getSubmitTo().equalsIgnoreCase("drugstorage::out")) {
            if (event.isCancel()) {
                event.sendCancelMessage();
                event.end();
                return;
            }
            try {
                int amount = Integer.parseInt(event.getMessage());
                if (amount < 1) {
                    event.getPlayer().sendMessage(Prefix.ERROR + "Die Anzahl muss größer als 1 sein.");
                    event.end();
                    return;
                }
                RoleplayItem item = event.getPlayerData().getVariable("drugstorage::roleplayitem");
                Faction factionData = factionManager.getFactionData(event.getPlayerData().getFaction());
                if (item == RoleplayItem.PIPE) {
                    item = RoleplayItem.FACTION_PIPE;
                }
                if (factionData.storage.getAmount(item) < amount) {
                    event.getPlayer().sendMessage(Prefix.ERROR + "So viel befindet sich nicht im Lager.");
                    return;
                }
                if (!event.getPlayerData().getInventory().addItem(item, amount)) {
                    event.getPlayer().sendMessage(Prefix.ERROR + "Du hast nicht genug Inventarplatz.");
                    return;
                }
                factionData.storage.removeItem(item, amount);
                factionManager.sendCustomMessageToFaction(event.getPlayerData().getFaction(), "§8[§2Lager§8]§7 " + event.getPlayer().getName() + " hat " + amount + "(g/Stück) " + item.getDisplayName() + "§7 ausgelagert. (" + factionData.storage.getAmount(item) + "g/Stück)");
                factionData.storage.save();
                event.end();
            } catch (Exception e) {
                event.getPlayer().sendMessage(Prefix.ERROR + "Die Zahl muss numerisch sein.");
            }
        }
        if (event.getSubmitTo().equalsIgnoreCase("drugstorage::in")) {
            if (event.isCancel()) {
                event.sendCancelMessage();
                event.end();
                return;
            }
            try {
                int amount = Integer.parseInt(event.getMessage());
                if (amount < 1) {
                    event.getPlayer().sendMessage(Prefix.ERROR + "Die Anzahl muss größer als 1 sein.");
                    event.end();
                    return;
                }
                RoleplayItem item = event.getPlayerData().getVariable("drugstorage::roleplayitem");
                if (item == RoleplayItem.PIPE) {
                    item = RoleplayItem.FACTION_PIPE;
                }
                Faction factionData = factionManager.getFactionData(event.getPlayerData().getFaction());
                if (!event.getPlayerData().getInventory().removeItem(item, amount)) {
                    event.getPlayer().sendMessage(Prefix.ERROR + "So viel hast du nicht dabei.");
                    return;
                }
                factionData.storage.addItem(item, amount);
                factionManager.sendCustomMessageToFaction(event.getPlayerData().getFaction(), "§8[§2Lager§8]§7 " + event.getPlayer().getName() + " hat " + amount + "(g/Stück) " + item.getDisplayName() + "§7 eingelagert. (" + factionData.storage.getAmount(item) + "g/Stück)");
                factionData.storage.save();
                event.end();
                event.end();
            } catch (Exception e) {
                event.getPlayer().sendMessage(Prefix.ERROR + "Die Zahl muss numerisch sein.");
            }
        }
        if (event.getSubmitTo().equalsIgnoreCase("evidence::out")) {
            if (event.isCancel()) {
                event.sendCancelMessage();
                event.end();
                return;
            }
            try {
                int amount = Integer.parseInt(event.getMessage());
                if (amount < 1) {
                    event.getPlayer().sendMessage(Prefix.ERROR + "Die Anzahl muss größer als 1 sein.");
                    event.end();
                    return;
                }
                RoleplayItem item = event.getPlayerData().getVariable("evidence::roleplayitem");
                if (StaatUtil.Asservatemkammer.getAmount(item) < amount) {
                    event.getPlayer().sendMessage(Prefix.ERROR + "So viel befindet sich nicht in der Asservatenkammer.");
                    return;
                }
                if (!event.getPlayerData().getInventory().addItem(item, amount)) {
                    event.getPlayer().sendMessage(Prefix.ERROR + "Du hast nicht genug Inventarplatz.");
                }
                factionManager.sendCustomMessageToFactions("§8[§3Asservatenkammer§8]§3 " + factionManager.getTitle(event.getPlayer()) + " " + event.getPlayer().getName() + " hat " + amount + "(g/Stück) " + item.getDisplayName() + "§3 aus der Asservatenkammer genommen.", "FBI", "Polizei");
                StaatUtil.Asservatemkammer.removeItem(item, amount);
                StaatUtil.Asservatemkammer.save();
                event.end();
            } catch (Exception e) {
                event.getPlayer().sendMessage(Prefix.ERROR + "Die Zahl muss numerisch sein.");
            }
        }
        if (event.getSubmitTo().equalsIgnoreCase("evidence::burn")) {
            if (event.isCancel()) {
                event.sendCancelMessage();
                event.end();
                return;
            }
            try {
                int amount = Integer.parseInt(event.getMessage());
                if (amount < 1) {
                    event.getPlayer().sendMessage(Prefix.ERROR + "Die Anzahl muss größer als 1 sein.");
                    event.end();
                    return;
                }
                RoleplayItem item = event.getPlayerData().getVariable("evidence::roleplayitem");
                if (StaatUtil.Asservatemkammer.getAmount(item) < amount) {
                    event.getPlayer().sendMessage(Prefix.ERROR + "So viel hast du nicht dabei.");
                    return;
                }
                factionManager.sendCustomMessageToFactions("§8[§3Asservatenkammer§8]§3 " + factionManager.getTitle(event.getPlayer()) + " " + event.getPlayer().getName() + " hat " + amount + "(g/Stück) " + item.getDisplayName() + "§3 verbrannt.", "FBI", "Polizei");
                StaatUtil.Asservatemkammer.removeItem(item, amount);
                StaatUtil.Asservatemkammer.save();
                factionManager.addFactionMoney("FBI", amount * 3, "Verbrennung von Drogen durch " + event.getPlayer().getName());
                playerManager.addExp(event.getPlayer(), amount);
                event.end();
            } catch (Exception e) {
                event.getPlayer().sendMessage(Prefix.ERROR + "Die Zahl muss numerisch sein.");
            }
        }
        if (event.getSubmitTo().equalsIgnoreCase("evidence::in")) {
            if (event.isCancel()) {
                event.sendCancelMessage();
                event.end();
                return;
            }
            try {
                int amount = Integer.parseInt(event.getMessage());
                if (amount < 1) {
                    event.getPlayer().sendMessage(Prefix.ERROR + "Die Anzahl muss größer als 1 sein.");
                    event.end();
                    return;
                }
                RoleplayItem item = event.getPlayerData().getVariable("evidence::roleplayitem");
                if (!event.getPlayerData().getInventory().removeItem(item, amount)) {
                    event.getPlayer().sendMessage(Prefix.ERROR + "So viel hast du nicht dabei.");
                    return;
                }
                factionManager.sendCustomMessageToFactions("§8[§3Asservatenkammer§8]§3 " + factionManager.getTitle(event.getPlayer()) + " " + event.getPlayer().getName() + " hat " + amount + "(g/Stück) " + item.getDisplayName() + "§3 in die Asservatenkammer eingelagert.", "FBI", "Polizei");
                StaatUtil.Asservatemkammer.addItem(item, amount);
                StaatUtil.Asservatemkammer.save();
                event.end();
            } catch (Exception e) {
                event.getPlayer().sendMessage(Prefix.ERROR + "Die Zahl muss numerisch sein.");
            }
        }
        if (event.getSubmitTo().equalsIgnoreCase("proceedweed")) {

            if (event.isCancel()) {
                event.sendCancelMessage();
                event.end();
            }
            try {
                int amount = Integer.parseInt(event.getMessage());
                if (amount < 1) {
                    event.getPlayer().sendMessage(Prefix.ERROR + "Die Anzahl muss größer als 1 sein.");
                    event.end();
                    return;
                }
                PlayerData playerData = playerManager.getPlayerData(event.getPlayer());
                Faction factionData = factionManager.getFactionData(playerData.getFaction());
                if (amount > factionData.storage.getWeed()) {
                    event.getPlayer().sendMessage(Prefix.ERROR + "So viel Marihuana hat deine Fraktion nicht.");
                    return;
                }
                factionData.storage.proceedWeed(amount);
                factionManager.sendCustomMessageToFaction(factionData.getName(), "§8[§" + factionData.getPrimaryColor() + "Labor§8]§7 " + factionManager.getRankName(factionData.getName(), playerData.getFactionGrade()) + " " + event.getPlayer().getName() + " hat die Verarbeitung von " + amount + " Pfeifentabak im Labor gestartet.");
                event.end();
            } catch (IllegalArgumentException e) {
                event.getPlayer().sendMessage(Prefix.ERROR + "Die Zahl ist nicht numerisch");
                return;
            }
            event.end();
        }
    }

    public Drop spawnDrop() {
        if (!ServerManager.canSpawnDrop()) {
            return null;
        }
        lastDrop = Utils.getTime();
        if (activeDrop != null) activeDrop.cleanup();
        List<LocationData> dropLocations = new ObjectArrayList<>();
        for (LocationData locationData : locationManager.getLocations()) {
            if (locationData.getType() == null) continue;
            if (locationData.getType().equalsIgnoreCase("drop")) {
                dropLocations.add(locationData);
            }
        }
        if (!dropLocations.isEmpty()) {
            int randomLocation = (int) (Math.random() * dropLocations.size());
            LocationData location = dropLocations.get(randomLocation);
            activeDrop = new Drop(new Location(Bukkit.getWorld("World"), location.getX(), location.getY(), location.getZ()));
        }
        return activeDrop;
    }

    @SneakyThrows
    @EventHandler
    public void everyMinute(MinuteTickEvent event) {
        LocalDateTime currentDateTime = Utils.getTime();
        int hour = currentDateTime.getHour();
        int minute = currentDateTime.getMinute();

        long minecraftTime = ((hour - 6 + 24) % 24) * 1000 + (long) (minute * 16.6667);

        if (minute % 5 == 0) {
            for (Dealer dealer : dealers) {
                dealer.setSold(0);
            }
        }

        for (World world : Bukkit.getWorlds()) {
            world.setTime(minecraftTime);
        }

        if (Utils.getTime().getHour() % 2 == 0 && event.getMinute() == 0) {
            Main.getInstance().commands.pfeifenTransport.resetTransports();
        }

        long minutesBetween = Duration.between(currentDateTime, lastDrop).toMinutes();
        if (minutesBetween <= -90) {
            double randomNumber = Math.random() * 100;
            if (randomNumber < 97) {
                spawnDrop();
            }
        }

        if (activeDrop != null) {
            if (activeDrop.dropEnded) {
                activeDrop = null;
            } else {
                activeDrop.setMinutes(activeDrop.getMinutes() - 1);
            }
        }

        /*if (Utils.getTime().getDayOfWeek().equals(DayOfWeek.SUNDAY) && Utils.getTime().getHour() >= 17 && Utils.getTime().getHour() <= 19) {
            if (Utils.getTime().getHour() == 17 && event.getMinute() == 0) {
                Bukkit.broadcastMessage("§8[§cMilitär§8]§7 In 2 Stunden werden Militärische Flugzeuge auf dem Flughafen landen!");
            }
            if (Utils.getTime().getHour() == 18 && event.getMinute() == 0) {
                Bukkit.broadcastMessage("§8[§cMilitär§8]§7 In 1 Stunde werden Militärische Flugzeuge auf dem Flughafen landen!");
            }
            if (Utils.getTime().getHour() == 18 && event.getMinute() == 30) {
                Bukkit.broadcastMessage("§8[§cMilitär§8]§7 In 30 Minuten werden Militärische Flugzeuge auf dem Flughafen landen!");
            }
            if (Utils.getTime().getHour() == 18 && event.getMinute() == 45) {
                Bukkit.broadcastMessage("§8[§cMilitär§8]§7 Eines der Militärischen Flugzeuge ist kurz vor dem Landen abgestürzt!");
                String[] factions = factionManager.getFactions().stream().map(FactionData::getName).toArray(String[]::new);
                factionManager.sendCustomMessageToFactions("§8[§cMilitär§8]§aIhr könnt in 10 Minuten dem Militärabsturz-Event im Fraktionslager beitreten!", factions);
            }
            if (Utils.getTime().getHour() == 18 && event.getMinute() == 55) {
                String[] factions = factionManager.getFactions().stream().map(FactionData::getName).toArray(String[]::new);
                factionManager.sendCustomMessageToFactions("§8[§cMilitär§8]§aIhr könnt nun dem Militärabsturz-Event im Fraktionslager beitreten!", factions);
                MilitaryDrop.ACTIVE = true;
            }
            if (Utils.getTime().getHour() == 19 && event.getMinute() == 0) {
                Bukkit.broadcastMessage("§8[§cMilitär§8]§7 Das abgestürzte Flugzeug hat mehrere Waffenkisten verloren!");
                militaryDrop.start();
            }
        }*/

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (getMaskState(player) != null) {
                if (Utils.getTime().isAfter(getMaskState(player))) {
                    clearMaskState(player);
                }
            }
        }

        for (Faction factionData : factionManager.getFactions()) {
            LocalDateTime proceedingStarted = factionData.storage.getProceedingStarted();

            if (proceedingStarted != null) {
                Duration duration = Duration.between(proceedingStarted, currentDateTime);
                long hoursElapsed = duration.toMinutes();

                if (hoursElapsed >= 120) {
                    factionData.storage.setProceedingStarted(null);
                    factionData.storage.setWeed(factionData.storage.getWeed() - factionData.storage.getProceedingAmount());
                    factionData.storage.setJoint(factionData.storage.getJoint() + (factionData.storage.getProceedingAmount() / 2));
                    factionManager.sendCustomMessageToFaction(factionData.getName(), "§8[§" + factionData.getPrimaryColor() + "Labor§8]§7 Es wurden " + factionData.storage.getProceedingAmount() + " Pfeifentabak zu " + (factionData.storage.getProceedingAmount() / 2) + " Pfeifen verarbeitet.");
                    factionData.storage.setProceedingAmount(0);
                    factionData.storage.save();
                }
            }
        }

        if (currentDateTime.getHour() == 0 && currentDateTime.getMinute() == 0) {
            for (PlayerData playerData : playerManager.getPlayers()) {
                if (playerData == null) continue;
                playerData.setAtmBlown(0);
            }
            Main.getInstance().getCoreDatabase().updateAsync("UPDATE players SET atmBlown = ?");
        }

        for (Dealer dealer : rob.keySet()) {
            if (!factionManager.isFactionMemberInRange(dealer.getAttacker(), dealer.getLocation(), 30, false)) {
                rob.remove(dealer);
                factionManager.sendCustomMessageToFaction(dealer.getOwner(), "§8[§cDealer-" + dealer.getGangzone() + "§8]§a Die Angreifer haben aufgehört den Dealer einzuschüchtern.");
                factionManager.sendCustomMessageToFaction(dealer.getAttacker(), "§8[§cDealer-" + dealer.getGangzone() + "§8]§c Ihr habt aufgehört den Dealer einzuschüchtern.");
                return;
            }
            int currentTime = rob.get(dealer);
            if (currentTime >= 10) {
                factionManager.sendCustomMessageToFaction(dealer.getOwner(), "§8[§cDealer-" + dealer.getGangzone() + "§8]§c Die Angreifer haben es geschafft euren Dealer einzuschüchtern.");
                factionManager.sendCustomMessageToFaction(dealer.getAttacker(), "§8[§cDealer-" + dealer.getGangzone() + "§8]§a Ihr habt es geschafft den Dealer einzuschüchtern.");
                for (PlayerData playerData1 : factionManager.getFactionMemberInRange(dealer.getAttacker(), dealer.getLocation(), 30, true)) {
                    playerManager.addExp(playerData1.getPlayer(), Utils.random(5, 10));
                }
                dealer.setOwner(dealer.getAttacker());
                rob.remove(dealer);
            } else {
                rob.replace(dealer, ++currentTime);
                int remaining = (10 - currentTime + 1);
                factionManager.sendCustomMessageToFaction(dealer.getOwner(), "§8[§cDealer-" + dealer.getGangzone() + "§8]§c Die Angreifer haben noch " + remaining + " Minuten bis der Dealer aufgibt!");
                factionManager.sendCustomMessageToFaction(dealer.getAttacker(), "§8[§cDealer-" + dealer.getGangzone() + "§8]§a Noch " + remaining + " Minuten bis der Dealer aufgibt!");
            }
        }

        if (event.getMinute() % 2 == 0) {
            if (staatsbankRob == null) return;
            if (staatsbankRob.getVaultsOpen() >= staatsbankRob.getVaults()) return;
            staatsbankRob.setVaultsOpen(staatsbankRob.getVaultsOpen() + 1);
            staatsbankRob.sendMessage("Ein Schließfach wurde geknackt! (" + staatsbankRob.getVaultsOpen() + "/" + staatsbankRob.getVaults() + ")", ChatColor.GRAY, staatsbankRob.getAttacker().getName());
        }
    }

    public void addQuestReward(Player player, String type, int amount, String info) {
        PlayerData playerData = playerManager.getPlayerData(player);
        switch (type.toLowerCase()) {
            case "money":
                playerData.addMoney(amount, "Reward Quest");
                break;
            case "case":
                if (info.equalsIgnoreCase("case")) {
                    player.getInventory().addItem(ItemManager.createItem(Material.CHEST, 1, 0, CaseType.DAILY.getDisplayName()));
                } else if (info.equalsIgnoreCase("xp-case")) {
                    player.getInventory().addItem(ItemManager.createItem(Material.CHEST, 1, 0, "§b§lXP-Case", "§8 ➥ §8[§6Rechtsklick§8]§7 Öffnen"));
                }
                break;
            case "ammo":
                for (Weapon weapon : Weapon.values()) {
                    if (weapon.getName().equalsIgnoreCase(info)) {
                        weaponManager.giveWeaponToCabinet(player, weapon, amount, 0);
                    }
                }
                break;
            case "gun":
                for (WeaponData weapon : WeaponManager.weaponDataMap.values()) {
                    if (weapon.getName().equalsIgnoreCase(info)) {
                        player.getInventory().addItem(ItemManager.createItem(weapon.getMaterial(), amount, 0, "§7Gepackte Waffe", "§8 ➥ " + weapon.getName()));
                    }
                }
                break;
        }
    }

    public LocalDateTime getMaskState(Player player) {
        return masks.get(player);
    }

    public void setMaskState(Player player, LocalDateTime until) {
        if (getMaskState(player) == null) {
            masks.put(player, until);
            return;
        }
        LocalDateTime dateTime = getMaskState(player);
        masks.replace(player, until);
        player.setCustomNameVisible(true);
        player.setCustomName("§k" + player.getName());
        player.setDisplayName("§k" + player.getName());
    }

    public void clearMaskState(Player player) {
        masks.remove(player);

        Utils.Tablist.updatePlayer(player);
    }

    public void openGOVRaidGUI(Faction factionData, Player attacker) {
        InventoryManager inventoryManager = new InventoryManager(attacker, 27, Component.text("§8 » §cRazzia"));
        inventoryManager.setItem(new CustomItem(13, ItemManager.createItem(Material.DIAMOND_HORSE_ARMOR, 1, 0, "§" + factionData.getPrimaryColor() + factionData.getFullname() + " raiden")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                factionManager.sendCustomMessageToFaction(factionData.getName(), "§8[§cRazzia§8]§c Der Staat führt eine Razzia durch!");

                double radius = 5.0;
                Block nearestDoorBlock = null;
                double nearestDistance = radius;
                Location playerLocation = attacker.getLocation();

                for (Block block : Main.getInstance().blockManager.getNearbyBlocks(playerLocation, radius)) {
                    Material type = block.getType();
                    if (type == Material.IRON_DOOR || type == Material.OAK_DOOR) {
                        double distance = playerLocation.distance(block.getLocation());
                        if (distance < nearestDistance) {
                            nearestDistance = distance;
                            nearestDoorBlock = block;
                        }
                    }
                }
                if (nearestDoorBlock == null) {
                    PlayerData playerData = playerManager.getPlayerData(attacker);
                    if (playerData == null) return;
                    if (playerData.getFactionGrade() < 3) {
                        attacker.sendMessage(Component.text(Prefix.ERROR + "Du musst mindestens Rang 3 sein."));
                        return;
                    }
                    if (locationManager.getDistanceBetweenCoords(attacker, "fdoor_" + factionData.getName()) > 5) {
                        attacker.sendMessage(Prefix.ERROR + "Du bist nicht in der nähe einer Fraktionstür.");
                        return;
                    }
                    List<RegisteredBlock> blocks = new ObjectArrayList<>();
                    for (RegisteredBlock block : Main.getInstance().blockManager.getBlocks()) {
                        if (block.getInfo().equalsIgnoreCase("adoor_" + factionData.getName())) {
                            blocks.add(block);
                        }
                    }
                    for (int i = 0; i < blocks.size() / 2; i++) {
                        for (RegisteredBlock block : blocks) {
                            if (Integer.parseInt(block.getInfoValue()) == i + 1) {
                                Block block1 = block.getLocation().getBlock();
                                if (block1.getType().equals(block.getMaterial())) {
                                    block1.setType(Material.AIR);
                                }
                            }
                        }
                    }
                    sendStaatRazziaMessage(factionData.getName());
                    return;
                }

                BlockState state = nearestDoorBlock.getState();
                if (state.getBlockData() instanceof Openable) {
                    PlayerData playerData = playerManager.getPlayerData(attacker);
                    if (playerData == null) return;
                    if (playerData.getFactionGrade() < 3) {
                        attacker.sendMessage(Component.text(Prefix.ERROR + "Du musst mindestens Rang 3 sein."));
                        return;
                    }
                    Openable openable = (Openable) state.getBlockData();
                    if (openable.isOpen()) {
                        attacker.sendMessage(Prefix.ERROR + "Die Tür ist bereits geöffnet.");
                    } else {
                        openable.setOpen(true);
                        state.setBlockData(openable);
                        state.update();
                        attacker.sendMessage(Prefix.MAIN + "Die Tür wurde geöffnet.");
                    }
                } else {
                    attacker.sendMessage(Prefix.ERROR + "Dies ist keine Tür.");
                    return;
                }
                sendStaatRazziaMessage(factionData.getName());
            }
        });
    }

    private void sendStaatRazziaMessage(String defender) {
        factionManager.sendCustomMessageToFactions("§8[§cRazzia§8]§7 Die Tür der Fraktionstür von " + defender + " ist offen!", "FBI", "Polizei");
    }

    public void openStaatsbankRaub(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player);
        if (playerData.getFaction() == null) return;
        Faction factionData = factionManager.getFactionData(playerData.getFaction());
        InventoryManager inventoryManager = new InventoryManager(player, 27, Component.text("§8 » §3Staatsbank"));
        if (staatsbankRob == null) {
            int count = factionManager.getOnlineMemberCount("Polizei");
            count += factionManager.getOnlineMemberCount("FBI");
            if (count < 4) {
                inventoryManager.setItem(new CustomItem(13, ItemManager.createItem(Material.PAPER, 1, 0, "§cRaub starten", "§8 ➥ §cEs müssen 4 Staatsbeamte online sein!")) {
                    @Override
                    public void onClick(InventoryClickEvent event) {

                    }
                });
            } else {
                inventoryManager.setItem(new CustomItem(13, ItemManager.createItem(Material.PAPER, 1, 0, "§cRaub starten")) {
                    @Override
                    public void onClick(InventoryClickEvent event) {
                        if (isStaatsbankRobBlocked) {
                            player.sendMessage(Prefix.ERROR + "Die Staatsbank wurde heute bereits ausgeraubt!");
                            return;
                        }
                        player.closeInventory();
                        startStaatsbankRaub(player);
                    }
                });
            }
        } else if (staatsbankRob.getAttacker().equals(factionData)) {
            int memberInRange = 0;
            memberInRange += factionManager.getFactionMemberInRange("Polizei", player.getLocation(), 100, true).size();
            memberInRange += factionManager.getFactionMemberInRange("FBI", player.getLocation(), 100, true).size();
            inventoryManager.setItem(new CustomItem(12, ItemManager.createItem(Material.PAPER, 1, 0, "§7Überwachungskameras", "§8 ➥ §c" + memberInRange + " FBI/Polizei wurden auf den Kameras gefunden (100 Meter)")) {
                @Override
                public void onClick(InventoryClickEvent event) {

                }
            });
            inventoryManager.setItem(new CustomItem(14, ItemManager.createItem(Material.GOLD_INGOT, 1, 0, "§6Schließfächer", "§8 ➥ §e" + staatsbankRob.getVaultsOpen() + "§8/§e" + staatsbankRob.getVaults())) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    openVaults(player);
                }
            });
        } else if (playerData.getFaction().equalsIgnoreCase("FBI") || playerData.getFaction().equalsIgnoreCase("Polizei")) {
            inventoryManager.setItem(new CustomItem(13, ItemManager.createItem(Material.GOLD_INGOT, 1, 0, "§cRaub beenden")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    if (factionManager.getFactionMemberInRange(staatsbankRob.getAttacker().getName(), player.getLocation(), 30, false).size() > 1) {
                        player.sendMessage(Prefix.ERROR + "Es sind noch zu viele Gegner am leben!");
                        return;
                    }
                    staatsbankRob.sendMessage("Der Staat konnte den Staatsbankraub verhindern!", ChatColor.RED, "FBI", "Polizei", factionData.getName());
                    staatsbankRob = null;
                }
            });
        }

    }

    private void openVaults(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player);
        if (playerData.getFaction() == null) return;
        Faction factionData = factionManager.getFactionData(playerData.getFaction());
        InventoryManager inventoryManager = new InventoryManager(player, 27, Component.text("§8 » §3Staatsbank §8-§6 Schließfächer"));
        for (int i = 0; i < staatsbankRob.getVaults(); i++) {
            int finalI = i + 1;
            if (staatsbankRob.getVaultsOpen() > i) {
                inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(Material.GOLD_INGOT, 1, 0, "§6Schließfach " + finalI, staatsbankRob.isVaultOpen(finalI) ? "§8 ➥ §eLeer" : "§8 ➥ §cGeschlossen")) {
                    @Override
                    public void onClick(InventoryClickEvent event) {
                        if (!staatsbankRob.openVault(finalI)) {
                            player.sendMessage(Prefix.ERROR + "Das Schließfach ist bereits entleert wurden.");
                            return;
                        }
                        player.closeInventory();
                        int amount = Utils.random(finalI * 95, finalI * 125);
                        factionData.addBankMoney(amount, "Schließfach " + finalI + " (Staatsbankraub)");
                        staatsbankRob.sendMessage("Ihr habt " + amount + "$ aus Schließfach " + finalI + " erhalten!", ChatColor.GREEN, factionData.getName());
                        if (staatsbankRob.getVaultsOpen() >= staatsbankRob.getVaults()) {
                            staatsbankRob.sendMessage("Ihr habt es geschafft die Staatsbank auszurauben!", ChatColor.GREEN, factionData.getName());
                            staatsbankRob.sendMessage("Die Angreifer haben es geschafft die Staatsbank auszurauben!", ChatColor.RED, "Polizei", "FBI");
                            staatsbankRob = null;
                        }
                    }
                });
            } else {
                inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(Material.GOLD_INGOT, 1, 0, "§6Schließfach " + finalI, "§8 ➥ §cVerschlossen")) {
                    @Override
                    public void onClick(InventoryClickEvent event) {

                    }
                });
            }
        }
    }

    private void startStaatsbankRaub(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player);
        if (playerData.getFaction() == null) {
            player.sendMessage(Prefix.ERROR + "Du bist nicht in keiner Bad Fraktion!");
            return;
        }
        Faction factionData = factionManager.getFactionData(playerData.getFaction());
        if (!factionData.isBadFrak()) {
            player.sendMessage(Prefix.ERROR + "Du bist in keiner Bad Fraktion!");
            return;
        }
        if (staatsbankRob != null) {
            player.sendMessage(Prefix.ERROR + "Es läuft bereits ein Staatsbank-Raub.");
            return;
        }
        isStaatsbankRobBlocked = true;
        staatsbankRob = new StaatsbankRob();
        staatsbankRob.setAttacker(factionData);
        staatsbankRob.sendMessage("Ihr habt den Raub auf die Staatsbank begonnen!", ChatColor.GRAY, factionData.getName());
        staatsbankRob.sendMessage("Ihr knackt alle 2 Minuten ein Schließfach auf, nehmt dann das Geld raus (Sneak + F).", ChatColor.GRAY, factionData.getName());
        staatsbankRob.sendMessage("Das Geld geht direkt auf das Fraktionskonto und erhöht sich mit der Zeit!", ChatColor.GRAY, factionData.getName());
        staatsbankRob.sendMessage("Es wurde ein Raub auf die Staatsbank gemeldet!", ChatColor.RED, "FBI", "Polizei");
    }

    public Collection<Dealer> getCurrentDealer() {
        return currentDealer;
    }

    public void openBombGUI(Player player) {
        InventoryManager inventoryManager = new InventoryManager(player, 27, Component.text("§8 » §bFraktionsupgrades"), true, true);
        inventoryManager.setItem(new CustomItem(11, ItemManager.createItem(RoleplayItem.DRAHT.getMaterial(), 1, 0, RoleplayItem.DRAHT.getDisplayName(), "§aGrün")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (Main.getInstance().commands.bombeCommand.getBomb().getColor().equalsIgnoreCase("Grün")) {
                    Main.getInstance().commands.bombeCommand.defuseBomb();
                    player.sendMessage("§8[§cBombe§8] §7Du hast die Bombe erfolgreich entschärft");
                    player.closeInventory();
                    return;
                }

                Main.getInstance().commands.bombeCommand.explodeBomb(Main.getInstance().commands.bombeCommand.getBomb().getBlock().getLocation());
                player.sendMessage("§8[§cBombe§8] §7Du hast den falschen draht durchgeschnitten");
            }
        });
        inventoryManager.setItem(new CustomItem(13, ItemManager.createItem(RoleplayItem.DRAHT.getMaterial(), 1, 0, RoleplayItem.DRAHT.getDisplayName(), "§cRot")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (Main.getInstance().commands.bombeCommand.getBomb().getColor().equalsIgnoreCase("Rot")) {
                    Main.getInstance().commands.bombeCommand.defuseBomb();
                    player.sendMessage("§8[§cBombe§8] §7Du hast die Bombe erfolgreich entschärft");
                    player.closeInventory();
                    return;
                }

                Main.getInstance().commands.bombeCommand.explodeBomb(Main.getInstance().commands.bombeCommand.getBomb().getBlock().getLocation());
                player.sendMessage("§8[§cBombe§8] §7Du hast den falschen draht durchgeschnitten");
                player.closeInventory();
            }
        });
        inventoryManager.setItem(new CustomItem(15, ItemManager.createItem(RoleplayItem.DRAHT.getMaterial(), 1, 0, RoleplayItem.DRAHT.getDisplayName(), "§9Blau")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (Main.getInstance().commands.bombeCommand.getBomb().getColor().equalsIgnoreCase("Blau")) {
                    Main.getInstance().commands.bombeCommand.defuseBomb();
                    player.sendMessage("§8[§cBombe§8] §7Du hast die Bombe erfolgreich entschärft");
                    player.closeInventory();
                    return;
                }

                Main.getInstance().commands.bombeCommand.explodeBomb(Main.getInstance().commands.bombeCommand.getBomb().getBlock().getLocation());
                player.sendMessage("§8[§cBombe§8] §7Du hast den falschen draht durchgeschnitten");
                player.closeInventory();
            }
        });
    }

    public class Drugstorage {
        private final PlayerManager playerManager;
        private final FactionManager factionManager;

        public Drugstorage(PlayerManager playerManager, FactionManager factionManager) {
            this.playerManager = playerManager;
            this.factionManager = factionManager;
        }

        public void openEvidence(Player player) {
            InventoryManager inventoryManager = new InventoryManager(player, 27, Component.text("§8 » §3Asservatenkammer"), true, true);
            inventoryManager.setItem(new CustomItem(11, ItemManager.createItem(RoleplayItem.SNUFF.getMaterial(), 1, 0, RoleplayItem.SNUFF.getDisplayName(), "§8 ➥ §7" + StaatUtil.Asservatemkammer.getCocaine() + "g")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    interactEvidence(player, RoleplayItem.SNUFF);
                }
            });
            inventoryManager.setItem(new CustomItem(13, ItemManager.createItem(RoleplayItem.CIGAR.getMaterial(), 1, 0, RoleplayItem.CIGAR.getDisplayName(), "§8 ➥ §7" + StaatUtil.Asservatemkammer.getNoble_joints() + " Stück")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    interactEvidence(player, RoleplayItem.CIGAR);
                }
            });
            inventoryManager.setItem(new CustomItem(15, ItemManager.createItem(RoleplayItem.CRYSTAL.getMaterial(), 1, 0, RoleplayItem.CRYSTAL.getDisplayName(), "§8 ➥ §7" + StaatUtil.Asservatemkammer.getCrystal() + " Stück")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    interactEvidence(player, RoleplayItem.CRYSTAL);
                }
            });
        }

        private void interactDrugStorage(Player player, RoleplayItem item) {
            PlayerData playerData = playerManager.getPlayerData(player);

            InventoryManager inventoryManager = new InventoryManager(player, 27, Component.text("§8 » §3" + item.getDisplayName()), true, true);
            inventoryManager.setItem(new CustomItem(12, ItemManager.createItem(Material.LIME_DYE, 1, 0, "§3Einlagern")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    playerData.setVariable("chatblock", "drugstorage::in");
                    playerData.setVariable("drugstorage::roleplayitem", item);
                    player.sendMessage("§8[§2Lager§8]§7 Gib nun an wie viel Gram du einlagern möchtest.");
                    player.closeInventory();
                }
            });
            inventoryManager.setItem(new CustomItem(14, ItemManager.createItem(Material.YELLOW_DYE, 1, 0, "§cAuslagern")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    if (playerData.getFactionGrade() < 3) {
                        player.sendMessage(Prefix.ERROR + "Das geht erst ab Rang 3.");
                        return;
                    }
                    playerData.setVariable("chatblock", "drugstorage::out");
                    playerData.setVariable("drugstorage::roleplayitem", item);
                    player.sendMessage("§8[§2Lager§8]§7 Gib nun an wie viel Gram du auslagern möchtest.");
                    player.closeInventory();
                }
            });
            inventoryManager.setItem(new CustomItem(18, ItemManager.createItem(Material.NETHER_WART, 1, 0, "§cZurück")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    open(player);
                }
            });
        }

        private void interactEvidence(Player player, RoleplayItem item) {
            PlayerData playerData = playerManager.getPlayerData(player);

            InventoryManager inventoryManager = new InventoryManager(player, 27, Component.text("§8 » §3" + item.getDisplayName()), true, true);
            inventoryManager.setItem(new CustomItem(11, ItemManager.createItem(Material.RED_DYE, 1, 0, "§4Verbrennen")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    if (playerData.getFactionGrade() < 4) {
                        player.sendMessage(Prefix.ERROR + "Das geht erst ab Rang 4!");
                        return;
                    }
                    playerData.setVariable("chatblock", "evidence::burn");
                    playerData.setVariable("evidence::roleplayitem", item);
                    player.sendMessage("§8[§3Asservatenkammer§8]§7 Gib nun an wie viel Gram du verbrennen möchtest.");
                    player.closeInventory();
                }
            });
            inventoryManager.setItem(new CustomItem(13, ItemManager.createItem(Material.LIME_DYE, 1, 0, "§3Einlagern")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    playerData.setVariable("chatblock", "evidence::in");
                    playerData.setVariable("evidence::roleplayitem", item);
                    player.sendMessage("§8[§3Asservatenkammer§8]§7 Gib nun an wie viel Gram du einlagern möchtest.");
                    player.closeInventory();
                }
            });
            inventoryManager.setItem(new CustomItem(15, ItemManager.createItem(Material.YELLOW_DYE, 1, 0, "§cAuslagern")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    if (playerData.getFactionGrade() < 4) {
                        player.sendMessage(Prefix.ERROR + "Das geht erst ab Rang 4.");
                        return;
                    }
                    playerData.setVariable("chatblock", "evidence::out");
                    playerData.setVariable("evidence::roleplayitem", item);
                    player.sendMessage("§8[§3Asservatenkammer§8]§7 Gib nun an wie viel Gram du auslagern möchtest.");
                    player.closeInventory();
                }
            });
            inventoryManager.setItem(new CustomItem(18, ItemManager.createItem(Material.NETHER_WART, 1, 0, "§cZurück")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    openEvidence(player);
                }
            });
        }

        public void open(Player player) {
            PlayerData playerData = playerManager.getPlayerData(player);
            Faction factionData = factionManager.getFactionData(playerData.getFaction());
            InventoryManager inventoryManager = new InventoryManager(player, 27, Component.text("§8 » §2Drogenlager (" + factionData.getName() + ")"), true, true);
            inventoryManager.setItem(new CustomItem(11, ItemManager.createItem(RoleplayItem.SNUFF.getMaterial(), 1, 0, RoleplayItem.SNUFF.getDisplayName(), "§8 ➥ §7" + factionData.storage.getCocaine() + "g")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    interactDrugStorage(player, RoleplayItem.SNUFF);
                }
            });
            inventoryManager.setItem(new CustomItem(13, ItemManager.createItem(RoleplayItem.CIGAR.getMaterial(), 1, 0, RoleplayItem.CIGAR.getDisplayName(), "§8 ➥ §7" + factionData.storage.getNoble_joint() + " Stück")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    interactDrugStorage(player, RoleplayItem.CIGAR);
                }
            });
            inventoryManager.setItem(new CustomItem(15, ItemManager.createItem(RoleplayItem.CRYSTAL.getMaterial(), 1, 0, RoleplayItem.CRYSTAL.getDisplayName(), "§8 ➥ §7" + factionData.storage.getCrystal() + " Stück")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    interactDrugStorage(player, RoleplayItem.CRYSTAL);
                }
            });
        }
    }

}
