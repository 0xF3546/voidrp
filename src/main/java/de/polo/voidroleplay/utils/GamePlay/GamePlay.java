package de.polo.voidroleplay.utils.GamePlay;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.*;
import de.polo.voidroleplay.database.MySQL;
import de.polo.voidroleplay.game.base.extra.Drop.Drop;
import de.polo.voidroleplay.game.faction.alliance.Alliance;
import de.polo.voidroleplay.game.faction.apotheke.Apotheke;
import de.polo.voidroleplay.game.faction.apotheke.ApothekeFunctions;
import de.polo.voidroleplay.game.faction.houseban.Houseban;
import de.polo.voidroleplay.game.faction.plants.PlantFunctions;
import de.polo.voidroleplay.utils.*;
import de.polo.voidroleplay.utils.InventoryManager.CustomItem;
import de.polo.voidroleplay.utils.InventoryManager.InventoryManager;
import de.polo.voidroleplay.utils.enums.CaseType;
import de.polo.voidroleplay.utils.enums.Drug;
import de.polo.voidroleplay.utils.enums.RoleplayItem;
import de.polo.voidroleplay.game.events.MinuteTickEvent;
import de.polo.voidroleplay.game.events.SubmitChatEvent;
import de.polo.voidroleplay.utils.playerUtils.ChatUtils;
import lombok.SneakyThrows;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.potion.PotionEffect;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class GamePlay implements Listener {
    public final ApothekeFunctions apotheke;
    private final PlayerManager playerManager;
    private final Utils utils;
    private final MySQL mySQL;
    private final FactionManager factionManager;
    public final Drugstorage drugstorage;
    public final PlantFunctions plant;
    private final LocationManager locationManager;
    private final List<Dealer> dealers = new ArrayList<>();
    public final FactionUpgradeGUI factionUpgradeGUI;
    public Drop activeDrop = null;
    public LocalDateTime lastDrop = Utils.getTime();
    public Houseban houseban;
    public DisplayNameManager displayNameManager;
    public final Alliance alliance;
    private HashMap<Player, LocalDateTime> masks = new HashMap<>();
    public final MilitaryDrop militaryDrop;
    private final List<Dealer> currentDealer = new ArrayList<>();
    private final NPC npc;

    public final HashMap<Dealer, Integer> rob = new HashMap<>();
    @SneakyThrows
    public GamePlay(PlayerManager playerManager, Utils utils, MySQL mySQL, FactionManager factionManager, LocationManager locationManager, NPC npc) {
        this.playerManager = playerManager;
        this.utils = utils;
        this.mySQL = mySQL;
        this.factionManager = factionManager;
        this.locationManager = locationManager;
        drugstorage = new Drugstorage(playerManager, factionManager);
        apotheke = new ApothekeFunctions(mySQL, utils, factionManager, playerManager, locationManager);
        plant = new PlantFunctions(mySQL, utils, factionManager, playerManager, locationManager);
        factionUpgradeGUI = new FactionUpgradeGUI(factionManager, playerManager, utils);
        houseban = new Houseban(playerManager, factionManager);
        displayNameManager = new DisplayNameManager(playerManager, factionManager, Main.getInstance().getScoreboardAPI());
        alliance = new Alliance(playerManager, factionManager, utils);
        militaryDrop = new MilitaryDrop(playerManager, factionManager, locationManager);
        this.npc = npc;
        Statement statement = mySQL.getStatement();
        ResultSet result = statement.executeQuery("SELECT * FROM dealer");
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
        }


        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }

    public Collection<Dealer> getDealer() {
        return dealers;
    }

    public static void useDrug(Player player, Drug drug) {
        for (PotionEffect effect : drug.getEffects()) {
            if (!player.hasPotionEffect(effect.getType())) {
                player.addPotionEffect(effect);
            }
        }
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_BURP, 1, 1);
        ItemManager.removeCustomItem(player, drug.getItem(), 1);
        switch (drug) {
            case JOINT:
                ChatUtils.sendGrayMessageAtPlayer(player, player.getName() + " raucht eine Zigarre");
                break;
            case COCAINE:
                ChatUtils.sendGrayMessageAtPlayer(player, player.getName() + " konsumiert Schnupftabak");
                break;
            case ANTIBIOTIKUM:
                ChatUtils.sendGrayMessageAtPlayer(player, player.getName() + " nimmt Antibiotikum");
                break;
            case SCHMERZMITTEL:
                ChatUtils.sendGrayMessageAtPlayer(player, player.getName() + " nimmt Schmerzmittel");
                break;
            case ADRENALINE_INJECTION:
                ChatUtils.sendGrayMessageAtPlayer(player, player.getName() + " spritzt sich Adrenalin");
                break;
        }
    }

    public class Drugstorage {
        private final PlayerManager playerManager;
        private final FactionManager factionManager;

        public Drugstorage(PlayerManager playerManager, FactionManager factionManager) {
            this.playerManager = playerManager;
            this.factionManager = factionManager;
        }

        public void openEvidence(Player player) {
            InventoryManager inventoryManager = new InventoryManager(player, 27, "§8 » §3Asservatenkammer", true, true);
            inventoryManager.setItem(new CustomItem(10, ItemManager.createItem(RoleplayItem.PIPE_TOBACCO.getMaterial(), 1, 0, RoleplayItem.PIPE_TOBACCO.getDisplayName(), "§8 ➥ §7" + StaatUtil.Asservatemkammer.getWeed() + "g")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    interactEvidence(player, RoleplayItem.PIPE_TOBACCO);
                }
            });
            inventoryManager.setItem(new CustomItem(12, ItemManager.createItem(RoleplayItem.PIPE.getMaterial(), 1, 0, RoleplayItem.PIPE.getDisplayName(), "§8 ➥ §7" + StaatUtil.Asservatemkammer.getJoints() + " Stück")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    interactEvidence(player, RoleplayItem.PIPE);
                }
            });
            inventoryManager.setItem(new CustomItem(14, ItemManager.createItem(RoleplayItem.SNUFF.getMaterial(), 1, 0, RoleplayItem.SNUFF.getDisplayName(), "§8 ➥ §7" + StaatUtil.Asservatemkammer.getCocaine() + "g")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    interactEvidence(player, RoleplayItem.SNUFF);
                }
            });
            inventoryManager.setItem(new CustomItem(16, ItemManager.createItem(RoleplayItem.CIGAR.getMaterial(), 1, 0, RoleplayItem.CIGAR.getDisplayName(), "§8 ➥ §7" + StaatUtil.Asservatemkammer.getNoble_joints() + " Stück")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    interactEvidence(player, RoleplayItem.CIGAR);
                }
            });
        }

        private void interactDrugStorage(Player player, RoleplayItem item) {
            PlayerData playerData = playerManager.getPlayerData(player);

            InventoryManager inventoryManager = new InventoryManager(player, 27, "§8 » §3" + item.getDisplayName(), true, true);
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
                        player.sendMessage(Main.error + "Das geht erst ab Rang 3.");
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

            InventoryManager inventoryManager = new InventoryManager(player, 27, "§8 » §3" + item.getDisplayName(), true, true);
            inventoryManager.setItem(new CustomItem(11, ItemManager.createItem(Material.RED_DYE, 1, 0, "§4Verbrennen")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    if (playerData.getFactionGrade() < 6) {
                        player.sendMessage(Prefix.ERROR + "Das geht erst ab Rang 6!");
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
                    if (playerData.getFactionGrade() < 6) {
                        player.sendMessage(Main.error + "Das geht erst ab Rang 6.");
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
            FactionData factionData = factionManager.getFactionData(playerData.getFaction());
            InventoryManager inventoryManager = new InventoryManager(player, 27, "§8 » §2Drogenlager (" + factionData.getName() + ")", true, true);
            if (factionData.storage.getProceedingStarted() != null) {
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime proceedingStarted = factionData.storage.getProceedingStarted();

                LocalDateTime targetTime = proceedingStarted.plusHours(2);

                Duration duration = Duration.between(now, targetTime);
                long totalSecondsRemaining = duration.getSeconds();
                long hoursRemaining = totalSecondsRemaining / 3600;
                long minutesRemaining = (totalSecondsRemaining % 3600) / 60;
                long secondsRemaining = totalSecondsRemaining % 60;

                String timeString = String.format("%02d Stunden, %02d Minuten und %02d Sekunden", hoursRemaining, minutesRemaining, secondsRemaining);

                inventoryManager.setItem(new CustomItem(10, ItemManager.createItem(RoleplayItem.PIPE_TOBACCO.getMaterial(), 1, 0, RoleplayItem.PIPE_TOBACCO.getDisplayName(), Arrays.asList("§8 ➥ §7" + factionData.storage.getWeed() + "g", "", "§8 » §fVerarbeitung endet in " + timeString))) {
                    @Override
                    public void onClick(InventoryClickEvent event) {
                    }
                });
            } else {
                inventoryManager.setItem(new CustomItem(10, ItemManager.createItem(RoleplayItem.PIPE_TOBACCO.getMaterial(), 1, 0, RoleplayItem.PIPE_TOBACCO.getDisplayName(), Arrays.asList("§8 ➥ §7" + factionData.storage.getWeed() + "g", "", "§8 » §cKlicke zum verarbeiten"))) {
                    @Override
                    public void onClick(InventoryClickEvent event) {
                        if (playerData.getFactionGrade() < 6) {
                            player.sendMessage(Main.error + "Das geht erst ab Rang 6!");
                            return;
                        }
                        playerData.setVariable("chatblock", "proceedweed");
                        player.sendMessage("§8[§" + factionData.getPrimaryColor() + "Labor§8]§7 Gib an wie viel Marihuana verarbeitet werden soll.");
                        player.closeInventory();
                    }
                });
            }
            inventoryManager.setItem(new CustomItem(12, ItemManager.createItem(RoleplayItem.PIPE.getMaterial(), 1, 0, RoleplayItem.PIPE.getDisplayName(), "§8 ➥ §7" + factionData.storage.getJoint() + " Stück")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    interactDrugStorage(player, RoleplayItem.PIPE);
                }
            });
            inventoryManager.setItem(new CustomItem(14, ItemManager.createItem(RoleplayItem.SNUFF.getMaterial(), 1, 0, RoleplayItem.SNUFF.getDisplayName(), "§8 ➥ §7" + factionData.storage.getCocaine() + "g")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    interactDrugStorage(player, RoleplayItem.SNUFF);
                }
            });
            inventoryManager.setItem(new CustomItem(16, ItemManager.createItem(RoleplayItem.CIGAR.getMaterial(), 1, 0, RoleplayItem.CIGAR.getDisplayName(), "§8 ➥ §7" + factionData.storage.getNoble_joint() + " Stück")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    interactDrugStorage(player, RoleplayItem.CIGAR);
                }
            });
        }
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
                FactionData factionData = factionManager.getFactionData(event.getPlayerData().getFaction());
                if (item == RoleplayItem.PIPE) {
                    item = RoleplayItem.FACTION_PIPE;
                }
                if (factionData.storage.getAmount(item) < amount) {
                    event.getPlayer().sendMessage(Main.error + "So viel befindet sich nicht im Lager.");
                    return;
                }
                ItemManager.addCustomItem(event.getPlayer(), item, amount);
                factionData.storage.removeItem(item, amount);
                factionManager.sendCustomMessageToFaction(event.getPlayerData().getFaction(), "§8[§2Lager§8]§7 " + event.getPlayer().getName() + " hat " + amount + "(g/Stück) " + item.getDisplayName() + "§7 ausgelagert. (" + factionData.storage.getAmount(item) + "g/Stück)");
                factionData.storage.save();
            } catch (Exception e) {
                event.getPlayer().sendMessage(Main.error + "Die Zahl muss numerisch sein.");
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
                FactionData factionData = factionManager.getFactionData(event.getPlayerData().getFaction());
                if (ItemManager.getCustomItemCount(event.getPlayer(), item) < amount) {
                    event.getPlayer().sendMessage(Main.error + "So viel hast du nicht dabei.");
                    return;
                }
                ItemManager.removeCustomItem(event.getPlayer(), item, amount);
                factionData.storage.addItem(item, amount);
                factionManager.sendCustomMessageToFaction(event.getPlayerData().getFaction(), "§8[§2Lager§8]§7 " + event.getPlayer().getName() + " hat " + amount + "(g/Stück) " + item.getDisplayName() + "§7 eingelagert. (" + factionData.storage.getAmount(item) + "g/Stück)");
                factionData.storage.save();
            } catch (Exception e) {
                event.getPlayer().sendMessage(Main.error + "Die Zahl muss numerisch sein.");
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
                    event.getPlayer().sendMessage(Main.error + "So viel befindet sich nicht in der Asservatenkammer.");
                    return;
                }
                factionManager.sendCustomMessageToFactions("§8[§3Asservatenkammer§8]§3 " + factionManager.getTitle(event.getPlayer()) + " " + event.getPlayer().getName() + " hat " + amount + "(g/Stück) " + item.getDisplayName() + "§3 aus der Asservatenkammer genommen.", "FBI", "Polizei");
                ItemManager.addCustomItem(event.getPlayer(), item, amount);
                StaatUtil.Asservatemkammer.removeItem(item, amount);
                StaatUtil.Asservatemkammer.save();
            } catch (Exception e) {
                event.getPlayer().sendMessage(Main.error + "Die Zahl muss numerisch sein.");
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
                    event.getPlayer().sendMessage(Main.error + "So viel hast du nicht dabei.");
                    return;
                }
                factionManager.sendCustomMessageToFactions("§8[§3Asservatenkammer§8]§3 " + factionManager.getTitle(event.getPlayer()) + " " + event.getPlayer().getName() + " hat " + amount + "(g/Stück) " + item.getDisplayName() + "§3 verbrannt.", "FBI", "Polizei");
                StaatUtil.Asservatemkammer.removeItem(item, amount);
                StaatUtil.Asservatemkammer.save();
                factionManager.addFactionMoney("FBI", amount * 3, "Verbrennung von Drogen durch " + event.getPlayer().getName());
                playerManager.addExp(event.getPlayer(), amount);
            } catch (Exception e) {
                event.getPlayer().sendMessage(Main.error + "Die Zahl muss numerisch sein.");
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
                if (ItemManager.getCustomItemCount(event.getPlayer(), item) < amount) {
                    event.getPlayer().sendMessage(Main.error + "So viel hast du nicht dabei.");
                    return;
                }
                factionManager.sendCustomMessageToFactions("§8[§3Asservatenkammer§8]§3 " + factionManager.getTitle(event.getPlayer()) + " " + event.getPlayer().getName() + " hat " + amount + "(g/Stück) " + item.getDisplayName() + "§3 in die Asservatenkammer eingelagert.", "FBI", "Polizei");
                ItemManager.removeCustomItem(event.getPlayer(), item, amount);
                StaatUtil.Asservatemkammer.addItem(item, amount);
                StaatUtil.Asservatemkammer.save();
            } catch (Exception e) {
                event.getPlayer().sendMessage(Main.error + "Die Zahl muss numerisch sein.");
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
                FactionData factionData = factionManager.getFactionData(playerData.getFaction());
                if (amount > factionData.storage.getWeed()) {
                    event.getPlayer().sendMessage(Main.error + "So viel Marihuana hat deine Fraktion nicht.");
                    return;
                }
                factionData.storage.proceedWeed(amount);
                factionManager.sendCustomMessageToFaction(factionData.getName(), "§8[§" + factionData.getPrimaryColor() + "Labor§8]§7 " + factionManager.getRankName(factionData.getName(), playerData.getFactionGrade()) + " " + event.getPlayer().getName() + " hat die Verarbeitung von " + amount + " Marihuana im Labor gestartet.");
            } catch (IllegalArgumentException e) {
                event.getPlayer().sendMessage(Main.error + "Die Zahl ist nicht numerisch");
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
        List<LocationData> dropLocations = new ArrayList<>();
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
            world.setTime((long) minecraftTime);
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

        if (Utils.getTime().getDayOfWeek().equals(DayOfWeek.SUNDAY) && Utils.getTime().getHour() >= 17 && Utils.getTime().getHour() <= 19) {
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
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (getMaskState(player) != null) {
                if (Utils.getTime().isAfter(getMaskState(player))) {
                    clearMaskState(player);
                }
            }
        }

        for (FactionData factionData : factionManager.getFactions()) {
            LocalDateTime proceedingStarted = factionData.storage.getProceedingStarted();

            if (proceedingStarted != null) {
                Duration duration = Duration.between(proceedingStarted, currentDateTime);
                long hoursElapsed = duration.toHours();

                if (hoursElapsed >= 2 && proceedingStarted.getMinute() == event.getMinute()) {
                    factionData.storage.setProceedingStarted(null);
                    factionData.storage.setWeed(factionData.storage.getWeed() - factionData.storage.getProceedingAmount());
                    factionData.storage.setJoint(factionData.storage.getJoint() + (factionData.storage.getProceedingAmount() / 2));
                    factionManager.sendCustomMessageToFaction(factionData.getName(), "§8[§" + factionData.getPrimaryColor() + "Labor§8]§7 Es wurden " + factionData.storage.getProceedingAmount() + " Marihuana zu " + (factionData.storage.getProceedingAmount() / 2) + " Joints verarbeitet.");
                    factionData.storage.setProceedingAmount(0);
                    factionData.storage.save();
                }
            }
        }

        if (currentDateTime.getHour() == 0 && currentDateTime.getMinute() == 0) {
            for (PlayerData playerData : playerManager.getPlayers()) {
                playerData.setAtmBlown(0);
            }
            Connection connection = mySQL.getConnection();
            PreparedStatement statement = connection.prepareStatement("UPDATE players SET atmBlown = 0");
            statement.execute();
            statement.close();
            connection.close();
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
                    playerManager.addExp(playerData1.getPlayer(), Main.random(5, 10));
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
                for (WeaponData weapon : Weapons.weaponDataMap.values()) {
                    if (weapon.getName().equalsIgnoreCase(info)) {
                        player.getInventory().addItem(ItemManager.createItem(RoleplayItem.MAGAZIN.getMaterial(), amount, 0, "§7Magazin", "§8 ➥ " + weapon.getName()));
                    }
                }
                break;
            case "gun":
                for (WeaponData weapon : Weapons.weaponDataMap.values()) {
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

    public Collection<Dealer> getCurrentDealer() {
        return currentDealer;
    }

}
