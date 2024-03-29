package de.polo.voidroleplay.utils.GamePlay;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.Dealer;
import de.polo.voidroleplay.dataStorage.FactionData;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.database.MySQL;
import de.polo.voidroleplay.utils.*;
import de.polo.voidroleplay.utils.InventoryManager.CustomItem;
import de.polo.voidroleplay.utils.InventoryManager.InventoryManager;
import de.polo.voidroleplay.utils.enums.Drug;
import de.polo.voidroleplay.utils.enums.RoleplayItem;
import de.polo.voidroleplay.utils.events.MinuteTickEvent;
import de.polo.voidroleplay.utils.events.SubmitChatEvent;
import de.polo.voidroleplay.utils.playerUtils.ChatUtils;
import lombok.SneakyThrows;
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
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_BURP, 1, 0);
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
            inventoryManager.setItem(new CustomItem(12, ItemManager.createItem(RoleplayItem.JOINT.getMaterial(), 1, 0, RoleplayItem.JOINT.getDisplayName(), "§8 ➥ §7" + factionData.storage.getJoint()+ " Stück")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    int amount = 0;
                    if (event.isLeftClick()) {
                        if (playerData.getFactionGrade() < 5) {
                            player.sendMessage(Main.error + "Das geht erst ab Rang 5!");
                            player.closeInventory();
                            return;
                        }
                        if (event.isShiftClick()) {
                            if (factionData.storage.getWeed() < 5) {
                                player.sendMessage(Main.error + "Deine Fraktion hat nicht genug Joints.");
                                player.closeInventory();
                                return;
                            }
                            ItemManager.addCustomItem(player, RoleplayItem.JOINT, 5);
                            factionData.storage.setJoint(factionData.storage.getJoint() - 5);
                            factionManager.sendCustomMessageToFaction(playerData.getFaction(), "§8[§2Lager§8]§7 " + player.getName() + " hat 5 Joints ausgelagert. (" + factionData.storage.getJoint() + "g)");
                            factionData.storage.save();
                        } else {
                            if (factionData.storage.getJoint() < 1) {
                                player.sendMessage(Main.error + "Deine Fraktion hat nicht genug Joints.");
                                player.closeInventory();
                                return;
                            }
                            ItemManager.addCustomItem(player, RoleplayItem.JOINT, 1);
                            factionData.storage.setJoint(factionData.storage.getJoint() - 1);
                            factionManager.sendCustomMessageToFaction(playerData.getFaction(), "§8[§2Lager§8]§7 " + player.getName() + " hat 1 Joint ausgelagert. (" + factionData.storage.getJoint() + "g)");
                            factionData.storage.save();
                        }
                    } else {
                        if (event.isShiftClick()) {
                            if (ItemManager.getCustomItemCount(player, RoleplayItem.JOINT) < 5) {
                                player.sendMessage(Main.error + "Du hast nicht genug Joints.");
                                player.closeInventory();
                                return;
                            }
                            ItemManager.removeCustomItem(player, RoleplayItem.JOINT, 5);
                            factionData.storage.setJoint(factionData.storage.getJoint() + 5);
                            factionManager.sendCustomMessageToFaction(playerData.getFaction(), "§8[§2Lager§8]§7 " + player.getName() + " hat 5 Joints eingelagert. (" + factionData.storage.getJoint() + "g)");
                            factionData.storage.save();
                        } else {
                            if (ItemManager.getCustomItemCount(player, RoleplayItem.JOINT) < 1) {
                                player.sendMessage(Main.error + "Du hast nicht genug Joints.");
                                player.closeInventory();
                                return;
                            }
                            ItemManager.removeCustomItem(player, RoleplayItem.JOINT, 1);
                            factionData.storage.setJoint(factionData.storage.getJoint() + 1);
                            factionManager.sendCustomMessageToFaction(playerData.getFaction(), "§8[§2Lager§8]§7 " + player.getName() + " hat 1 Joint eingelagert. (" + factionData.storage.getJoint() + "g)");
                            factionData.storage.save();
                        }
                    }

                }
            });
            inventoryManager.setItem(new CustomItem(14, ItemManager.createItem(RoleplayItem.COCAINE.getMaterial(), 1, 0, RoleplayItem.COCAINE.getDisplayName(), "§8 ➥ §7" + factionData.storage.getCocaine() + "g")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    int amount = 0;
                    if (event.isLeftClick()) {
                        if (playerData.getFactionGrade() < 5) {
                            player.sendMessage(Main.error + "Das geht erst ab Rang 5!");
                            player.closeInventory();
                            return;
                        }
                        if (event.isShiftClick()) {
                            if (factionData.storage.getCocaine() < 5) {
                                player.sendMessage(Main.error + "Deine Fraktion hat nicht genug Kokain.");
                                player.closeInventory();
                                return;
                            }
                            ItemManager.addCustomItem(player, RoleplayItem.COCAINE, 5);
                            factionData.storage.setCocaine(factionData.storage.getCocaine() - 5);
                            factionManager.sendCustomMessageToFaction(playerData.getFaction(), "§8[§2Lager§8]§7 " + player.getName() + " hat 5g Kokain ausgelagert. (" + factionData.storage.getCocaine() + "g)");
                            factionData.storage.save();
                        } else {
                            if (factionData.storage.getCocaine() < 1) {
                                player.sendMessage(Main.error + "Deine Fraktion hat nicht genug Kokain.");
                                player.closeInventory();
                                return;
                            }
                            ItemManager.addCustomItem(player, RoleplayItem.COCAINE, 1);
                            factionData.storage.setCocaine(factionData.storage.getCocaine() - 1);
                            factionManager.sendCustomMessageToFaction(playerData.getFaction(), "§8[§2Lager§8]§7 " + player.getName() + " hat 1g Kokain ausgelagert. (" + factionData.storage.getCocaine() + "g)");
                            factionData.storage.save();
                        }
                    } else {
                        if (event.isShiftClick()) {
                            if (ItemManager.getCustomItemCount(player, RoleplayItem.JOINT) < 5) {
                                player.sendMessage(Main.error + "Du hast nicht genug Kokain.");
                                player.closeInventory();
                                return;
                            }
                            ItemManager.removeCustomItem(player, RoleplayItem.COCAINE, 5);
                            factionData.storage.setCocaine(factionData.storage.getCocaine() + 5);
                            factionManager.sendCustomMessageToFaction(playerData.getFaction(), "§8[§2Lager§8]§7 " + player.getName() + " hat 5g Kokain eingelagert. (" + factionData.storage.getCocaine() + "g)");
                            factionData.storage.save();
                        } else {
                            if (ItemManager.getCustomItemCount(player, RoleplayItem.COCAINE) < 1) {
                                player.sendMessage(Main.error + "Du hast nicht genug Kokain.");
                                player.closeInventory();
                                return;
                            }
                            ItemManager.removeCustomItem(player, RoleplayItem.COCAINE, 1);
                            factionData.storage.setCocaine(factionData.storage.getCocaine() + 1);
                            factionManager.sendCustomMessageToFaction(playerData.getFaction(), "§8[§2Lager§8]§7 " + player.getName() + " hat 1g Kokain eingelagert. (" + factionData.storage.getCocaine() + "g)");
                            factionData.storage.save();
                        }
                    }

                }
            });
            inventoryManager.setItem(new CustomItem(16, ItemManager.createItem(RoleplayItem.NOBLE_JOINT.getMaterial(), 1, 0, RoleplayItem.NOBLE_JOINT.getDisplayName(), "§8 ➥ §7" + factionData.storage.getNoble_joint() + " Stück")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    int amount = 0;
                    if (event.isLeftClick()) {
                        if (playerData.getFactionGrade() < 5) {
                            player.sendMessage(Main.error + "Das geht erst ab Rang 5!");
                            player.closeInventory();
                            return;
                        }
                        if (event.isShiftClick()) {
                            if (factionData.storage.getNoble_joint() < 5) {
                                player.sendMessage(Main.error + "Deine Fraktion hat nicht genug veredelte Joints.");
                                player.closeInventory();
                                return;
                            }
                            ItemManager.addCustomItem(player, RoleplayItem.NOBLE_JOINT, 5);
                            factionData.storage.setNoble_joint(factionData.storage.getNoble_joint() - 5);
                            factionManager.sendCustomMessageToFaction(playerData.getFaction(), "§8[§2Lager§8]§7 " + player.getName() + " hat 5 veredelte Joints ausgelagert. (" + factionData.storage.getNoble_joint() + "g)");
                            factionData.storage.save();
                        } else {
                            if (factionData.storage.getCocaine() < 1) {
                                player.sendMessage(Main.error + "Deine Fraktion hat nicht genug veredelte Joints.");
                                player.closeInventory();
                                return;
                            }
                            ItemManager.addCustomItem(player, RoleplayItem.NOBLE_JOINT, 1);
                            factionData.storage.setNoble_joint(factionData.storage.getNoble_joint() - 1);
                            factionManager.sendCustomMessageToFaction(playerData.getFaction(), "§8[§2Lager§8]§7 " + player.getName() + " hat 1 veredelten Joint ausgelagert. (" + factionData.storage.getNoble_joint() + "g)");
                            factionData.storage.save();
                        }
                    } else {
                        if (event.isShiftClick()) {
                            if (ItemManager.getCustomItemCount(player, RoleplayItem.NOBLE_JOINT) < 5) {
                                player.sendMessage(Main.error + "Du hast nicht genug veredelte Joints.");
                                player.closeInventory();
                                return;
                            }
                            ItemManager.removeCustomItem(player, RoleplayItem.NOBLE_JOINT, 5);
                            factionData.storage.setNoble_joint(factionData.storage.getNoble_joint() + 5);
                            factionManager.sendCustomMessageToFaction(playerData.getFaction(), "§8[§2Lager§8]§7 " + player.getName() + " hat 5 veredelte Joints eingelagert. (" + factionData.storage.getNoble_joint() + "g)");
                            factionData.storage.save();
                        } else {
                            if (ItemManager.getCustomItemCount(player, RoleplayItem.NOBLE_JOINT) < 1) {
                                player.sendMessage(Main.error + "Du hast nicht genug Veredelte Joints.");
                                player.closeInventory();
                                return;
                            }
                            ItemManager.removeCustomItem(player, RoleplayItem.NOBLE_JOINT, 1);
                            factionData.storage.setNoble_joint(factionData.storage.getNoble_joint() + 1);
                            factionManager.sendCustomMessageToFaction(playerData.getFaction(), "§8[§2Lager§8]§7 " + player.getName() + " hat 1 veredelten Joint eingelagert. (" + factionData.storage.getNoble_joint() + "g)");
                            factionData.storage.save();
                        }
                    }

                }
            });
        }
    }

    @EventHandler
    public void onChatMessage(SubmitChatEvent event) {
        if (event.getSubmitTo().equalsIgnoreCase("evidence::out")) {
            if (event.isCancel()) {
                event.sendCancelMessage();
                event.end();
                return;
            }
            try {
                int amount = Integer.parseInt(event.getMessage());
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

    @SneakyThrows
    @EventHandler
    public void everyMinute(MinuteTickEvent event) {
        LocalDateTime currentDateTime = LocalDateTime.now();

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

}
