package de.polo.metropiacity.utils.GamePlay;

import de.polo.metropiacity.Main;
import de.polo.metropiacity.dataStorage.Dealer;
import de.polo.metropiacity.dataStorage.FactionData;
import de.polo.metropiacity.dataStorage.PlayerData;
import de.polo.metropiacity.dataStorage.PlayerLaboratory;
import de.polo.metropiacity.database.MySQL;
import de.polo.metropiacity.utils.*;
import de.polo.metropiacity.utils.InventoryManager.CustomItem;
import de.polo.metropiacity.utils.InventoryManager.InventoryManager;
import de.polo.metropiacity.utils.enums.Drug;
import de.polo.metropiacity.utils.enums.RoleplayItem;
import de.polo.metropiacity.utils.playerUtils.ChatUtils;
import lombok.SneakyThrows;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.potion.PotionEffect;

import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GamePlay {
    public final ApothekeFunctions apotheke;
    private final PlayerManager playerManager;
    private final Utils utils;
    private final MySQL mySQL;
    private final FactionManager factionManager;
    public final Drugstorage drugstorage;
    public final PlantFunctions plant;
    private final LocationManager locationManager;
    private final List<Dealer> dealers = new ArrayList<>();
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
        Statement statement = mySQL.getStatement();
        ResultSet result = statement.executeQuery("SELECT * FROM dealer");
        while (result.next()) {
            Dealer dealer = new Dealer();
            dealer.setId(result.getInt("id"));
            dealers.add(dealer);
        }
    }

    public Collection<Dealer> getDealer() {
        return dealers;
    }
    public static void useDrug(Player player, Drug drug) {
        for (PotionEffect effect : drug.getEffects()) {
            player.addPotionEffect(effect);
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
        }
    }
    public class Drugstorage {
        private final PlayerManager playerManager;
        private final FactionManager factionManager;
        public Drugstorage(PlayerManager playerManager, FactionManager factionManager) {
            this.playerManager = playerManager;
            this.factionManager = factionManager;
        };
        public void open(Player player) {
            PlayerData playerData = playerManager.getPlayerData(player);
            FactionData factionData = factionManager.getFactionData(playerData.getFaction());
            InventoryManager inventoryManager = new InventoryManager(player,27, "§8 » §2Drogenlager (" + factionData.getName() + ")", true, true);
            inventoryManager.setItem(new CustomItem(10, ItemManager.createItem(RoleplayItem.MARIHUANA.getMaterial(), 1, 0, RoleplayItem.MARIHUANA.getDisplayName(), "§8 ➥ §7" + factionData.storage.getWeed())) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    if (event.isLeftClick()) {
                        if (playerData.getFactionGrade() < 5) {
                            player.sendMessage(Main.error + "Das geht erst ab Rang 5!");
                            player.closeInventory();
                            return;
                        }
                        if (event.isShiftClick()) {
                            if (factionData.storage.getWeed() < 5) {
                                player.sendMessage(Main.error + "Deine Fraktion hat nicht genug Marihuana.");
                                player.closeInventory();
                                return;
                            }
                            ItemManager.addCustomItem(player, RoleplayItem.MARIHUANA, 5);
                            factionData.storage.setWeed(factionData.storage.getWeed() - 5);
                            factionManager.sendCustomMessageToFaction(playerData.getFaction(), "§8[§2Lager§8]§7 " + player.getName() + " hat 5 Joints ausgelagert. (" +factionData.storage.getWeed() + "g)");
                            factionData.storage.save();
                        } else {
                            if (factionData.storage.getWeed() < 1) {
                                player.sendMessage(Main.error + "Deine Fraktion hat nicht genug Marihuana.");
                                player.closeInventory();
                                return;
                            }
                            ItemManager.addCustomItem(player, RoleplayItem.MARIHUANA, 1);
                            factionData.storage.setWeed(factionData.storage.getWeed() - 1);
                            factionManager.sendCustomMessageToFaction(playerData.getFaction(), "§8[§2Lager§8]§7 " + player.getName() + " hat 1g Marihuana ausgelagert. (" +factionData.storage.getWeed() + "g)");
                            factionData.storage.save();
                        }
                    } else {
                        if (event.isShiftClick()) {
                            if (ItemManager.getCustomItemCount(player, RoleplayItem.MARIHUANA) < 5) {
                                player.sendMessage(Main.error + "Du hast nicht genug Marihuana.");
                                player.closeInventory();
                                return;
                            }
                            ItemManager.removeCustomItem(player, RoleplayItem.MARIHUANA, 5);
                            factionData.storage.setWeed(factionData.storage.getWeed() + 5);
                            factionManager.sendCustomMessageToFaction(playerData.getFaction(), "§8[§2Lager§8]§7 " + player.getName() + " hat 5g Marihuana eingelagert. (" +factionData.storage.getWeed() + "g)");
                            factionData.storage.save();
                        } else {
                            if (ItemManager.getCustomItemCount(player, RoleplayItem.MARIHUANA) < 1) {
                                player.sendMessage(Main.error + "Du hast nicht genug Marihuana.");
                                player.closeInventory();
                                return;
                            }
                            ItemManager.removeCustomItem(player, RoleplayItem.MARIHUANA, 1);
                            factionData.storage.setWeed(factionData.storage.getWeed() + 1);
                            factionManager.sendCustomMessageToFaction(playerData.getFaction(), "§8[§2Lager§8]§7 " + player.getName() + " hat 1g Marihuana eingelagert. (" +factionData.storage.getWeed() + "g)");
                            factionData.storage.save();
                        }
                    }
                }
            });
            inventoryManager.setItem(new CustomItem(12, ItemManager.createItem(RoleplayItem.JOINT.getMaterial(), 1, 0, RoleplayItem.JOINT.getDisplayName(), "§8 ➥ §7" + factionData.storage.getJoint())) {
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
                            factionManager.sendCustomMessageToFaction(playerData.getFaction(), "§8[§2Lager§8]§7 " + player.getName() + " hat 5 Joints ausgelagert. (" +factionData.storage.getJoint() + "g)");
                            factionData.storage.save();
                        } else {
                            if (factionData.storage.getJoint() < 1) {
                                player.sendMessage(Main.error + "Deine Fraktion hat nicht genug Joints.");
                                player.closeInventory();
                                return;
                            }
                            ItemManager.addCustomItem(player, RoleplayItem.JOINT, 1);
                            factionData.storage.setJoint(factionData.storage.getJoint() - 1);
                            factionManager.sendCustomMessageToFaction(playerData.getFaction(), "§8[§2Lager§8]§7 " + player.getName() + " hat 1 Joint ausgelagert. (" +factionData.storage.getJoint() + "g)");
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
                            factionManager.sendCustomMessageToFaction(playerData.getFaction(), "§8[§2Lager§8]§7 " + player.getName() + " hat 5 Joints eingelagert. (" +factionData.storage.getJoint() + "g)");
                            factionData.storage.save();
                        } else {
                            if (ItemManager.getCustomItemCount(player, RoleplayItem.JOINT) < 1) {
                                player.sendMessage(Main.error + "Du hast nicht genug Joints.");
                                player.closeInventory();
                                return;
                            }
                            ItemManager.removeCustomItem(player, RoleplayItem.JOINT, 1);
                            factionData.storage.setJoint(factionData.storage.getJoint() + 1);
                            factionManager.sendCustomMessageToFaction(playerData.getFaction(), "§8[§2Lager§8]§7 " + player.getName() + " hat 1 Joint eingelagert. (" +factionData.storage.getJoint() + "g)");
                            factionData.storage.save();
                        }
                    }

                }
            });
            inventoryManager.setItem(new CustomItem(14, ItemManager.createItem(RoleplayItem.COCAINE.getMaterial(), 1, 0, RoleplayItem.COCAINE.getDisplayName(), "§8 ➥ §7" + factionData.storage.getCocaine())) {
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
                            factionManager.sendCustomMessageToFaction(playerData.getFaction(), "§8[§2Lager§8]§7 " + player.getName() + " hat 5g Kokain ausgelagert. (" +factionData.storage.getCocaine() + "g)");
                            factionData.storage.save();
                        } else {
                            if (factionData.storage.getCocaine() < 1) {
                                player.sendMessage(Main.error + "Deine Fraktion hat nicht genug Kokain.");
                                player.closeInventory();
                                return;
                            }
                            ItemManager.addCustomItem(player, RoleplayItem.COCAINE, 1);
                            factionData.storage.setCocaine(factionData.storage.getCocaine() - 1);
                            factionManager.sendCustomMessageToFaction(playerData.getFaction(), "§8[§2Lager§8]§7 " + player.getName() + " hat 1g Kokain ausgelagert. (" +factionData.storage.getCocaine() + "g)");
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
                            factionManager.sendCustomMessageToFaction(playerData.getFaction(), "§8[§2Lager§8]§7 " + player.getName() + " hat 5g Kokain eingelagert. (" +factionData.storage.getCocaine() + "g)");
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
            inventoryManager.setItem(new CustomItem(16, ItemManager.createItem(RoleplayItem.NOBLE_JOINT.getMaterial(), 1, 0, RoleplayItem.NOBLE_JOINT.getDisplayName(), "§8 ➥ §7" + factionData.storage.getNoble_joint())) {
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
                            factionManager.sendCustomMessageToFaction(playerData.getFaction(), "§8[§2Lager§8]§7 " + player.getName() + " hat 5 veredelte Joints ausgelagert. (" +factionData.storage.getNoble_joint() + "g)");
                            factionData.storage.save();
                        } else {
                            if (factionData.storage.getCocaine() < 1) {
                                player.sendMessage(Main.error + "Deine Fraktion hat nicht genug veredelte Joints.");
                                player.closeInventory();
                                return;
                            }
                            ItemManager.addCustomItem(player, RoleplayItem.NOBLE_JOINT, 1);
                            factionData.storage.setNoble_joint(factionData.storage.getNoble_joint() - 1);
                            factionManager.sendCustomMessageToFaction(playerData.getFaction(), "§8[§2Lager§8]§7 " + player.getName() + " hat 1 veredelten Joint ausgelagert. (" +factionData.storage.getNoble_joint() + "g)");
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
                            factionManager.sendCustomMessageToFaction(playerData.getFaction(), "§8[§2Lager§8]§7 " + player.getName() + " hat 5 veredelte Joints eingelagert. (" +factionData.storage.getNoble_joint() + "g)");
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
}
