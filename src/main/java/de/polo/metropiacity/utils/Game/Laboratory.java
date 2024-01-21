package de.polo.metropiacity.utils.Game;

import de.polo.metropiacity.Main;
import de.polo.metropiacity.dataStorage.FactionData;
import de.polo.metropiacity.dataStorage.PlayerData;
import de.polo.metropiacity.dataStorage.PlayerLaboratory;
import de.polo.metropiacity.utils.FactionManager;
import de.polo.metropiacity.utils.InventoryManager.CustomItem;
import de.polo.metropiacity.utils.InventoryManager.InventoryManager;
import de.polo.metropiacity.utils.ItemManager;
import de.polo.metropiacity.utils.LocationManager;
import de.polo.metropiacity.utils.PlayerManager;
import de.polo.metropiacity.utils.enums.RoleplayItem;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;

public class Laboratory implements CommandExecutor {

    private final PlayerManager playerManager;
    private final FactionManager factionManager;
    private final LocationManager locationManager;
    private final List<PlayerLaboratory> playerLaboratories = new ArrayList<>();
    private final List<LaboratoryAttack> attacks = new ArrayList<>();
    public Laboratory(PlayerManager playerManager, FactionManager factionManager, LocationManager locationManager) {
        this.playerManager = playerManager;
        this.factionManager = factionManager;
        this.locationManager = locationManager;
        Main.registerCommand("labor", this);
    }

    public Collection<PlayerLaboratory> getPlayerLaboratorries() {
        return playerLaboratories;
    }

    public void addPlayerLaboratory(PlayerLaboratory laboratory) {
        playerLaboratories.add(laboratory);
    }
    public void removePlayerLaboratory(PlayerLaboratory playerLaboratory) {
        playerLaboratories.remove(playerLaboratory);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        openInventory(player);
        return false;
    }

    private void openInventory(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player);
        if (playerData.getFaction() == null) {
            player.sendMessage(Main.error + "Du bist in keiner Fraktion.");
            return;
        }
        FactionData factionData = factionManager.getFactionData(playerData.getFaction());
        if (!factionData.hasLaboratory()) {
            player.sendMessage(Main.error + "Deine Fraktion hat kein Labor.");
            return;
        }
        if (locationManager.getDistanceBetweenCoords(player, playerData.getFaction() + "_laboratory") > 5) {
            for (FactionData factions : factionManager.getFactions()) {
                if (factions.hasLaboratory()) {
                    Location location = locationManager.getLocation(factions.getName() + "_laboratory");
                    if (location != null && location.distance(player.getLocation()) < 5) {
                        openLaboratoryAsAttacker(player, factions);
                    }
                }
            }
            player.sendMessage(Main.error + "Du bist nicht in der nähe eines Labors.");
            return;
        }

        if (playerData.getLaboratory() == null) {
            PlayerLaboratory laboratory = new PlayerLaboratory(this);
            laboratory.create(player.getUniqueId());
            playerData.setLaboratory(laboratory);
        }

        InventoryManager inventoryManager = new InventoryManager(player, 27, "§7 » §" + factionData.getPrimaryColor() + "Labor", true, true);
        if (playerData.getLaboratory().isStarted()) {
            inventoryManager.setItem(new CustomItem(15, ItemManager.createItem(Material.REDSTONE, 1, 0, "§cLabor stoppen")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    playerData.getLaboratory().stop();
                    player.sendMessage("§8[§" + factionData.getPrimaryColor() + "Labor§8]§c Du hast dein Labor gestoppt.");
                    player.closeInventory();
                }
            });
        } else {
            inventoryManager.setItem(new CustomItem(15, ItemManager.createItem(Material.EMERALD, 1, 0, "§aLabor starten")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    playerData.getLaboratory().start();
                    player.sendMessage("§8[§" + factionData.getPrimaryColor() + "Labor§8]§a Du hast dein Labor gestartet.");
                    player.closeInventory();
                }
            });
        }

        inventoryManager.setItem(new CustomItem(13, ItemManager.createItem(Material.CHEST, 1, 0, "§fLabor öffnen", Arrays.asList("§8 ➥ §a" + playerData.getLaboratory().getJointAmount() + " Joints§", "§8 ➥ §a" + playerData.getLaboratory().getWeedAmount() + " Marihuana§"))) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openLabotory(player);
            }
        });

        inventoryManager.setItem(new CustomItem(11, ItemManager.createItem(Material.CHEST, 1, 0, "§bLabor befüllen")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openFillInventory(player);
            }
        });
    }
    private void openLabotory(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player);
        FactionData factionData = factionManager.getFactionData(playerData.getFaction());
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§7 » §aLabor", true, true);
        inventoryManager.setItem(new CustomItem(12, ItemManager.createItem(RoleplayItem.MARIHUANA.getMaterial(), 1, 0, RoleplayItem.MARIHUANA.getDisplayName(), Arrays.asList("§8 » §7" + playerData.getLaboratory().getWeedAmount() + " Stück", "", "§cKlicke zum entfernen"))) {
            @Override
            public void onClick(InventoryClickEvent event) {
                ItemManager.addCustomItem(player, RoleplayItem.MARIHUANA, playerData.getLaboratory().getWeedAmount());
                player.sendMessage("§8[§" + factionData.getPrimaryColor() + "Labor§8]§a Du hast " + playerData.getLaboratory().getWeedAmount() + " Marihuana aus dem Labor genommen.");
                playerData.getLaboratory().setWeedAmount(0);
                playerData.getLaboratory().save();
                player.closeInventory();
            }
        });

        inventoryManager.setItem(new CustomItem(14, ItemManager.createItem(RoleplayItem.JOINT.getMaterial(), 1, 0, RoleplayItem.JOINT.getDisplayName(), Arrays.asList("§8 » §7" + playerData.getLaboratory().getJointAmount() + " Stück", "", "§cKlicke zum entfernen"))) {
            @Override
            public void onClick(InventoryClickEvent event) {
                ItemManager.addCustomItem(player, RoleplayItem.JOINT, playerData.getLaboratory().getJointAmount());
                player.sendMessage("§8[§" + factionData.getPrimaryColor() + "Labor§8]§a Du hast " + playerData.getLaboratory().getJointAmount() + " Joints aus dem Labor genommen.");
                playerData.getLaboratory().setJointAmount(0);
                playerData.getLaboratory().save();
                player.closeInventory();
            }
        });

        inventoryManager.setItem(new CustomItem(18, ItemManager.createItem(Material.NETHER_WART, 1, 0, "§cZurück")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openInventory(player);
            }
        });
    }

    private void openFillInventory(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player);
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§7 » §aLabor befüllen", true, true);
        int weedAmount = ItemManager.getCustomItemCount(player, RoleplayItem.MARIHUANA);
        int amountFor64 = 1;
        if (weedAmount >= 64) {
            amountFor64 = 64;
        } else {
            amountFor64 = weedAmount;
        }
        inventoryManager.setItem(new CustomItem(15, ItemManager.createItem(RoleplayItem.MARIHUANA.getMaterial(), amountFor64, 0, "§aAlles einlagern", "§8 ➥ §7" + weedAmount + " Stück")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                ItemManager.removeCustomItem(player, RoleplayItem.MARIHUANA, weedAmount);
                playerData.getLaboratory().add(weedAmount);
                player.closeInventory();
                playerData.getLaboratory().save();
            }
        });
        inventoryManager.setItem(new CustomItem(18, ItemManager.createItem(Material.NETHER_WART, 1, 0, "§cZurück")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openInventory(player);
            }
        });
    }

    @SneakyThrows
    public void openLaboratoryAsAttacker(Player player, FactionData defenderFaction) {
        PlayerData playerData = playerManager.getPlayerData(player);
        FactionData factionData = factionManager.getFactionData(playerData.getFaction());
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§7 » §" + defenderFaction.getPrimaryColor() + "Labor §8× §cAngriff", true, true);
        Statement statement = Main.getInstance().mySQL.getStatement();
        ResultSet res = statement.executeQuery("SELECT * FROM player_laboratory AS pl LEFT JOIN players AS p ON LOWER(p.faction) = '" +  defenderFaction.getName() + "'");
        int weedAmount = 0;
        int jointAmount = 0;
        while (res.next()) {
            weedAmount += res.getInt("weed");
            jointAmount += res.getInt("joints");
        }
        inventoryManager.setItem(new CustomItem(13, ItemManager.createItem(Material.REDSTONE, 1, 0, "§cRaube das Labor aus", "§8 ➥ §2" + jointAmount + " Joints & " + weedAmount + " Marihuana")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                attackLaboratory(factionData, defenderFaction);
            }
        });
    }
    private LaboratoryAttack getAttack(FactionData data) {
        for (LaboratoryAttack attack : attacks) {
            if (attack.defender == data || attack.attacker == data) return attack;
        }
        return null;
    }
    public void attackLaboratory(FactionData attacker, FactionData defender) {
        LaboratoryAttack attack = new LaboratoryAttack(attacker, defender);
        factionManager.sendCustomMessageToFaction(attacker.getName(), "§8[§" + attacker.getPrimaryColor() + attacker.getName() + "§8]§e Ihr fangt an das Labor von " + defender.getFullname() + " auszurauben!");
        factionManager.sendCustomMessageToFaction(defender.getName(), "§8[§cLabor§8]§e Das Sicherheitssystem deines Labors meldet Alarm.");
    }

    public void hackLaboratory(FactionData attacker, FactionData defender) {
        factionManager.sendCustomMessageToFaction(attacker.getName(), "§8[§" + attacker.getPrimaryColor() + attacker.getName() + "§8]§e Ihr fangt an das Labor von " + defender.getFullname() + " auszurauben!");
        factionManager.sendCustomMessageToFaction(defender.getName(), "§8[§cLabor§8]§e Das Sicherheitssystem deines Labors meldet Alarm.");
    }
    @SneakyThrows
    public void clearLaboratory(FactionData attacker, FactionData defender) {
        LaboratoryAttack attack = getAttack(attacker);
        int jointAmount = 0;
        int weedAmount = 0;
        Connection connection = Main.getInstance().mySQL.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT pl.* FROM player_laboratory AS pl LEFT JOIN players AS p ON pl.uuid = p.uuid WHERE LOWER(p.faction) = ?");
        preparedStatement.setString(1, defender.getName().toLowerCase());
        ResultSet result = preparedStatement.executeQuery();
        while (result.next()) {
            jointAmount += result.getInt("joints");
            weedAmount += result.getInt("weed");
        }
        for (PlayerData playerData : playerManager.getPlayers()) {
            if (playerData.getFaction().equalsIgnoreCase(defender.getName())) {
                if (playerData.getLaboratory() != null) {
                    removePlayerLaboratory(playerData.getLaboratory());
                }
            }
        }
        if (defender.storage.getProceedingStarted() != null)  {
            weedAmount += defender.storage.getProceedingAmount();
            defender.storage.setProceedingAmount(0);
            defender.storage.setProceedingStarted(null);
        }
        attacker.storage.setWeed(attacker.storage.getWeed() + weedAmount);
        attacker.storage.setJoint(attacker.storage.getJoint() + jointAmount);
        PreparedStatement deleteStatement = connection.prepareStatement("DELETE FROM player_laboratory AS pl LEFT JOIN players AS p ON pl.uuid = p.uuid WHERE LOWER(p.faction) = ?");
        deleteStatement.setString(1, defender.getName().toLowerCase());
        deleteStatement.execute();
    }

    public void pushTick() {
        for (PlayerLaboratory laboratory : playerLaboratories) {
            if (laboratory.getWeedAmount() >= 2) {
                laboratory.setWeedAmount(laboratory.getWeedAmount() - 2);
                laboratory.setJointAmount(laboratory.getJointAmount() + 1);
            } else {
                laboratory.stop();
            }
        }
    }
    private class LaboratoryAttack {
        public final FactionData attacker;
        public final FactionData defender;
        private boolean doorOpened;
        private boolean hackedLaboratory;
        public LaboratoryAttack(FactionData attacker, FactionData defender) {
            this.attacker = attacker;
            this.defender = defender;
        }

        public boolean isDoorOpened() {
            return doorOpened;
        }

        public void setDoorOpened(boolean doorOpened) {
            this.doorOpened = doorOpened;
        }

        public boolean isHackedLaboratory() {
            return hackedLaboratory;
        }

        public void setHackedLaboratory(boolean hackedLaboratory) {
            this.hackedLaboratory = hackedLaboratory;
        }
    }
}
