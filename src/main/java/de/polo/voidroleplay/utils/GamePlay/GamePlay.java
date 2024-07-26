package de.polo.voidroleplay.utils.GamePlay;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.*;
import de.polo.voidroleplay.database.MySQL;
import de.polo.voidroleplay.game.base.extra.Drop.Drop;
import de.polo.voidroleplay.game.faction.apotheke.ApothekeFunctions;
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
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.potion.PotionEffect;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

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

    @SneakyThrows
    public GamePlay(PlayerManager playerManager, Utils utils, MySQL mySQL, FactionManager factionManager, LocationManager locationManager) {
        this.playerManager = playerManager;
        this.utils = utils;
        this.mySQL = mySQL;
        this.factionManager = factionManager;
        this.locationManager = locationManager;
        drugstorage = new Drugstorage(playerManager, factionManager);
        apotheke = new ApothekeFunctions(mySQL, utils, factionManager, playerManager, locationManager);
        plant = new PlantFunctions(mySQL, utils, factionManager, playerManager, locationManager);
        factionUpgradeGUI = new FactionUpgradeGUI(factionManager, playerManager, utils);
        Statement statement = mySQL.getStatement();
        ResultSet result = statement.executeQuery("SELECT * FROM dealer");
        while (result.next()) {
            Dealer dealer = new Dealer();
            dealer.setId(result.getInt("id"));
            dealers.add(dealer);
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
                ChatUtils.sendGrayMessageAtPlayer(player, player.getName() + " konsumiert einen Joint");
                break;
            case COCAINE:
                ChatUtils.sendGrayMessageAtPlayer(player, player.getName() + " konsumiert Kokain");
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
            inventoryManager.setItem(new CustomItem(10, ItemManager.createItem(RoleplayItem.MARIHUANA.getMaterial(), 1, 0, RoleplayItem.MARIHUANA.getDisplayName(), "§8 ➥ §7" + StaatUtil.Asservatemkammer.getWeed() + "g")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    interactEvidence(player, RoleplayItem.MARIHUANA);
                }
            });
            inventoryManager.setItem(new CustomItem(12, ItemManager.createItem(RoleplayItem.JOINT.getMaterial(), 1, 0, RoleplayItem.JOINT.getDisplayName(), "§8 ➥ §7" + StaatUtil.Asservatemkammer.getJoints() + " Stück")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    interactEvidence(player, RoleplayItem.JOINT);
                }
            });
            inventoryManager.setItem(new CustomItem(14, ItemManager.createItem(RoleplayItem.COCAINE.getMaterial(), 1, 0, RoleplayItem.COCAINE.getDisplayName(), "§8 ➥ §7" + StaatUtil.Asservatemkammer.getCocaine() + "g")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    interactEvidence(player, RoleplayItem.COCAINE);
                }
            });
            inventoryManager.setItem(new CustomItem(16, ItemManager.createItem(RoleplayItem.NOBLE_JOINT.getMaterial(), 1, 0, RoleplayItem.NOBLE_JOINT.getDisplayName(), "§8 ➥ §7" + StaatUtil.Asservatemkammer.getNoble_joints() + " Stück")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    interactEvidence(player, RoleplayItem.NOBLE_JOINT);
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
                    if (playerData.getFactionGrade() < 6) {
                        player.sendMessage(Main.error + "Das geht erst ab Rang 6.");
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

                inventoryManager.setItem(new CustomItem(10, ItemManager.createItem(RoleplayItem.MARIHUANA.getMaterial(), 1, 0, RoleplayItem.MARIHUANA.getDisplayName(), Arrays.asList("§8 ➥ §7" + factionData.storage.getWeed() + "g", "", "§8 » §fVerarbeitung endet in " + timeString))) {
                    @Override
                    public void onClick(InventoryClickEvent event) {
                    }
                });
            } else {
                inventoryManager.setItem(new CustomItem(10, ItemManager.createItem(RoleplayItem.MARIHUANA.getMaterial(), 1, 0, RoleplayItem.MARIHUANA.getDisplayName(), Arrays.asList("§8 ➥ §7" + factionData.storage.getWeed() + "g", "", "§8 » §cKlicke zum verarbeiten"))) {
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
            inventoryManager.setItem(new CustomItem(12, ItemManager.createItem(RoleplayItem.JOINT.getMaterial(), 1, 0, RoleplayItem.JOINT.getDisplayName(), "§8 ➥ §7" + factionData.storage.getJoint() + " Stück")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    interactDrugStorage(player, RoleplayItem.JOINT);
                }
            });
            inventoryManager.setItem(new CustomItem(14, ItemManager.createItem(RoleplayItem.COCAINE.getMaterial(), 1, 0, RoleplayItem.COCAINE.getDisplayName(), "§8 ➥ §7" + factionData.storage.getCocaine() + "g")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    interactDrugStorage(player, RoleplayItem.COCAINE);
                }
            });
            inventoryManager.setItem(new CustomItem(16, ItemManager.createItem(RoleplayItem.NOBLE_JOINT.getMaterial(), 1, 0, RoleplayItem.NOBLE_JOINT.getDisplayName(), "§8 ➥ §7" + factionData.storage.getNoble_joint() + " Stück")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    interactDrugStorage(player, RoleplayItem.NOBLE_JOINT);
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
                if (item == RoleplayItem.JOINT) {
                    item = RoleplayItem.FACTION_JOINT;
                }
                if (factionData.storage.getAmount(item) < amount) {
                    event.getPlayer().sendMessage(Main.error + "So viel befindet sich nicht im Lager.");
                    return;
                }
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
                if (item == RoleplayItem.JOINT) {
                    item = RoleplayItem.FACTION_JOINT;
                }
                FactionData factionData = factionManager.getFactionData(event.getPlayerData().getFaction());
                if (ItemManager.getCustomItemCount(event.getPlayer(), item) < amount) {
                    event.getPlayer().sendMessage(Main.error + "So viel hast du nicht dabei.");
                    return;
                }
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

        if (Duration.between(currentDateTime, lastDrop).toMinutes() >= 90) {
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


        for (FactionData factionData : factionManager.getFactions()) {
            LocalDateTime proceedingStarted = factionData.storage.getProceedingStarted();

            if (proceedingStarted != null) {
                Duration duration = Duration.between(proceedingStarted, currentDateTime);
                long hoursElapsed = duration.toHours();

                if (hoursElapsed >= 2 && proceedingStarted.getMinute() == event.getMinute()) {
                    if (true) {
                        factionData.storage.setProceedingStarted(null);
                        factionData.storage.setWeed(factionData.storage.getWeed() - factionData.storage.getProceedingAmount());
                        factionData.storage.setJoint(factionData.storage.getJoint() + (factionData.storage.getProceedingAmount() / 2));
                        factionManager.sendCustomMessageToFaction(factionData.getName(), "§8[§" + factionData.getPrimaryColor() + "Labor§8]§7 Es wurden " + factionData.storage.getProceedingAmount() + " Marihuana zu " + (factionData.storage.getProceedingAmount() / 2) + " Joints verarbeitet.");
                        factionData.storage.setProceedingAmount(0);
                        factionData.storage.save();
                    }
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
    }

    public void addQuestReward(Player player, String type, int amount, String info) {
        PlayerData playerData = playerManager.getPlayerData(player);
        switch (type) {
            case "money":
                playerData.addMoney(amount);
                break;
            case "case":
                if (info == "case") {
                    player.getInventory().addItem(ItemManager.createItem(Material.CHEST, 1, 0, CaseType.DAILY.getDisplayName()));
                } else if (info == "xp-case") {
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

}
