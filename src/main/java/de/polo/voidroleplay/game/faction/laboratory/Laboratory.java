package de.polo.voidroleplay.game.faction.laboratory;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.*;
import de.polo.voidroleplay.game.faction.plants.Plant;
import de.polo.voidroleplay.utils.*;
import de.polo.voidroleplay.utils.InventoryManager.CustomItem;
import de.polo.voidroleplay.utils.InventoryManager.InventoryManager;
import de.polo.voidroleplay.utils.enums.RoleplayItem;
import de.polo.voidroleplay.game.events.MinuteTickEvent;
import lombok.SneakyThrows;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class Laboratory implements CommandExecutor, Listener {

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
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
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
                        LaboratoryAttack attack = getAttack(factionData);
                        if (attack == null) {
                            openLaboratoryAsAttacker(player, factions);
                        } else {
                            if (attack.isHackedLaboratory()) {
                                factionManager.sendCustomMessageToFaction(attack.attacker.getName(), "§8[§" + attack.attacker.getPrimaryColor() + "Labor§8]§b Ihr habt das Labor ausgeraubt!");
                                factionManager.sendCustomMessageToFaction(attack.defender.getName(), "§8[§" + attack.defender.getPrimaryColor() + "Labor§8]§c Euer Labor wurde leer geräumt!");
                                for (PlayerData playerData1 : factionManager.getFactionMemberInRange(attack.attacker.getName(), location, 30, true)) {
                                    playerManager.addExp(playerData1.getPlayer(), Main.random(30, 60));
                                }
                                clearLaboratory(attack.attacker, attack.defender);
                                attacks.remove(attack);
                            }
                        }
                        return;
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

        inventoryManager.setItem(new CustomItem(13, ItemManager.createItem(Material.CHEST, 1, 0, "§fLabor öffnen", Arrays.asList("§8 ➥ §a" + playerData.getLaboratory().getJointAmount() + " Pfeifen§", "§8 ➥ §a" + playerData.getLaboratory().getWeedAmount() + " Pfeifentabak§"))) {
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
        inventoryManager.setItem(new CustomItem(12, ItemManager.createItem(RoleplayItem.PIPE_TOBACCO.getMaterial(), 1, 0, RoleplayItem.PIPE_TOBACCO.getDisplayName(), Arrays.asList("§8 » §7" + playerData.getLaboratory().getWeedAmount() + " Stück", "", "§cKlicke zum entfernen"))) {
            @Override
            public void onClick(InventoryClickEvent event) {
                ItemManager.addCustomItem(player, RoleplayItem.PIPE_TOBACCO, playerData.getLaboratory().getWeedAmount());
                player.sendMessage("§8[§" + factionData.getPrimaryColor() + "Labor§8]§a Du hast " + playerData.getLaboratory().getWeedAmount() + " Pfeifentabak aus dem Labor genommen.");
                playerData.getLaboratory().setWeedAmount(0);
                playerData.getLaboratory().save();
                player.closeInventory();
            }
        });

        inventoryManager.setItem(new CustomItem(14, ItemManager.createItem(RoleplayItem.PIPE.getMaterial(), 1, 0, RoleplayItem.PIPE.getDisplayName(), Arrays.asList("§8 » §7" + playerData.getLaboratory().getJointAmount() + " Stück", "", "§cKlicke zum entfernen"))) {
            @Override
            public void onClick(InventoryClickEvent event) {
                Main.getInstance().beginnerpass.didQuest(player, 8, (int) playerData.getLaboratory().getJointAmount());
                ItemManager.addCustomItem(player, RoleplayItem.PIPE, (int) playerData.getLaboratory().getJointAmount());
                player.sendMessage("§8[§" + factionData.getPrimaryColor() + "Labor§8]§a Du hast " + playerData.getLaboratory().getJointAmount() + " Pfeifen aus dem Labor genommen.");
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
        int weedAmount = ItemManager.getCustomItemCount(player, RoleplayItem.PIPE_TOBACCO);
        int amountFor64 = 1;
        if (weedAmount >= 64) {
            amountFor64 = 64;
        } else {
            amountFor64 = weedAmount;
        }
        int finalAmountFor6 = amountFor64;
        inventoryManager.setItem(new CustomItem(15, ItemManager.createItem(RoleplayItem.PIPE_TOBACCO.getMaterial(), finalAmountFor6, 0, "§aAlles einlagern", "§8 ➥ §7" + weedAmount + " Stück")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                ItemManager.removeCustomItem(player, RoleplayItem.PIPE_TOBACCO, weedAmount);
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
        ResultSet res = statement.executeQuery("SELECT pl.* FROM player_laboratory AS pl LEFT JOIN players AS p ON pl.uuid = p.uuid WHERE LOWER(p.faction) = '" + defenderFaction.getName() + "'");
        int weedAmount = 0;
        int jointAmount = 0;
        while (res.next()) {
            weedAmount += res.getInt("weed");
            jointAmount += res.getInt("joints");
        }
        LaboratoryAttack attack = getAttack(factionData);
        if (attack == null) {
            if (ItemManager.getCustomItemCount(player, RoleplayItem.WELDING_MACHINE) >= 1) {
                inventoryManager.setItem(new CustomItem(13, ItemManager.createItem(Material.REDSTONE, 1, 0, "§cRaube das Labor aus", "§8 ➥ §2" + jointAmount + " Pfeifen & " + weedAmount + " Pfeifentabak")) {
                    @Override
                    public void onClick(InventoryClickEvent event) {
                        attackLaboratory(player, factionData, defenderFaction);
                    }
                });
            } else {
                inventoryManager.setItem(new CustomItem(13, ItemManager.createItem(Material.REDSTONE, 1, 0, "§cRaube das Labor aus", Arrays.asList("§8 ➥ §2" + jointAmount + " Pfeifen & " + weedAmount + " Pfeifentabak", "", "§8 ➥ §7Dafür benötigst du ein Schweißgerät."))) {
                    @Override
                    public void onClick(InventoryClickEvent event) {
                    }
                });
            }
        } else {
            LocalDateTime now = Utils.getTime();

            long remainingMinutes = 5 + ChronoUnit.MINUTES.between(now, attack.getStarted());
            long remainingSeconds = 60 + ChronoUnit.SECONDS.between(now, attack.getStarted()) % 60;
            inventoryManager.setItem(new CustomItem(13, ItemManager.createItem(Material.REDSTONE, 1, 0,
                    "§cRaube das Labor aus",
                    "§8 ➥ §fNoch " + remainingMinutes + " Minute" + (remainingMinutes != 1 ? "n" : "") +
                            " & " + remainingSeconds + " Sekunde" + (remainingSeconds != 1 ? "n" : ""))) {
                @Override
                public void onClick(InventoryClickEvent event) {
                }
            });
        }
    }

    private LaboratoryAttack getAttack(FactionData data) {
        for (LaboratoryAttack attack : attacks) {
            if (attack.defender.getName().equalsIgnoreCase(data.getName()) || attack.attacker.getName().equalsIgnoreCase(data.getName()))
                return attack;
        }
        return null;
    }

    public boolean isDoorOpened(FactionData attacker) {
        for (LaboratoryAttack attack : attacks) {
            if ((attack.defender == attacker && attack.isDoorOpened()) || (attack.attacker == attacker && attack.isDoorOpened()))
                return true;
        }
        return false;
    }

    public void attackLaboratory(Player player, FactionData attacker, FactionData defender) {
        ItemManager.removeCustomItem(player, RoleplayItem.WELDING_MACHINE, 1);
        LaboratoryAttack attack = new LaboratoryAttack(attacker, defender);
        attack.setStarted(Utils.getTime());
        factionManager.sendCustomMessageToFaction(attacker.getName(), "§8[§" + attacker.getPrimaryColor() + attacker.getName() + "§8]§e Ihr fangt an das Labor von " + defender.getFullname() + " auszurauben, bleibt 5 Minuten bei der Tür!");
        factionManager.sendCustomMessageToFaction(defender.getName(), "§8[§cLabor§8]§e Das Sicherheitssystem deines Labors meldet Alarm.");
        attacks.add(attack);
        player.closeInventory();
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
            if (playerData.getFaction() == null) continue;
            if (playerData.getFaction().equalsIgnoreCase(defender.getName())) {
                if (playerData.getLaboratory() != null) {
                    removePlayerLaboratory(playerData.getLaboratory());
                }
            }
        }
        if (defender.storage.getProceedingStarted() != null) {
            weedAmount += defender.storage.getProceedingAmount();
            defender.storage.setProceedingAmount(0);
            defender.storage.setProceedingStarted(null);
        }
        if (attacker.getName().equalsIgnoreCase("FBI") || attacker.getName().equalsIgnoreCase("Polizei")) {
            StaatUtil.Asservatemkammer.setWeed(StaatUtil.Asservatemkammer.getWeed() + weedAmount);
            StaatUtil.Asservatemkammer.setJoints(StaatUtil.Asservatemkammer.getJoints() + jointAmount);
            StaatUtil.Asservatemkammer.save();
        } else {
            attacker.storage.setWeed(attacker.storage.getWeed() + weedAmount);
            attacker.storage.setJoint(attacker.storage.getJoint() + jointAmount);
            attacker.storage.save();
        }
        PreparedStatement deleteStatement = connection.prepareStatement("DELETE pl FROM player_laboratory AS pl LEFT JOIN players AS p ON pl.uuid = p.uuid WHERE LOWER(p.faction) = ?");
        deleteStatement.setString(1, defender.getName().toLowerCase());
        deleteStatement.execute();
    }

    public void pushTick() {
        Iterator<PlayerLaboratory> iterator = playerLaboratories.iterator();
        System.out.println("has next: " + iterator.hasNext());

        while (iterator.hasNext()) {
            PlayerLaboratory laboratory = iterator.next();
            if (laboratory == null) continue;
            System.out.println("LABORATORY FOUND");

            PlayerData playerData = playerManager.getPlayerData(laboratory.getOwner());
            if (playerData == null || playerData.getFaction() == null) continue;
            System.out.println("PLAYERDATA FOUND");

            FactionData factionData = factionManager.getFactionData(playerData.getFaction());
            if (factionData == null) continue;
            System.out.println("FACTIONDATA FOUND");

            for (Plant plant : Main.getInstance().gamePlay.plant.getPlants()) {
                if (plant == null) continue;
                if (plant.getOwner() == null) continue;
                if (!plant.getOwner().equalsIgnoreCase(factionData.getName())) continue;
                System.out.println("PLANT FOR " + plant.getOwner() + " FOUND");

                if (laboratory.getWeedAmount() >= (2 * plant.getMultiplier())) {
                    laboratory.setWeedAmount(laboratory.getWeedAmount() - (int) (2 * plant.getMultiplier()));
                    laboratory.setJointAmount(laboratory.getJointAmount() + (1 * plant.getMultiplier()));
                } else {
                    laboratory.stop();
                    iterator.remove();
                    break;
                }
            }
        }
    }

    @SneakyThrows
    @EventHandler
    public void onMinute(MinuteTickEvent event) {
        LocalDateTime now = Utils.getTime();
        /*if (now.getMinute() == 0 && now.getHour() == 0 && now.getDayOfWeek() == DayOfWeek.MONDAY) {
            List<Integer> laboratories = new ArrayList<>();
            for (LocationData data : LocationManager.locationDataMap.values()) {
                if (data.getName().contains("laboratory_")) {
                    Integer id = Integer.parseInt(data.getName().replace("laboratory_", ""));
                    laboratories.add(id);
                }
            }
            Collections.shuffle(laboratories);

            for (FactionData factionData : factionManager.getFactions()) {
                if (!factionData.hasLaboratory()) continue;
                Integer labId = laboratories.get(0);
                laboratories.remove(0);
                factionData.setLaboratory(labId);
                factionManager.sendCustomMessageToFaction(factionData.getName(), "§8[§" + factionData.getPrimaryColor() + "Labor§8]§7 Euer Labor-Standort hat sich geändert. Nutze \"/findlabor\" um dieses zu finden.");
                Connection connection = Main.getInstance().mySQL.getConnection();
                PreparedStatement statement = connection.prepareStatement("UPDATE factions SET laboratory = ? WHERE id = ?");
                statement.setInt(1, labId);
                statement.setInt(2, factionData.getId());
                statement.execute();
                statement.close();
                connection.close();
                if (laboratories.isEmpty()) {
                    break;
                }
            }
        }*/
        for (LaboratoryAttack attack : attacks) {
            System.out.println(attack.getStarted());
            if (attack.isHackedLaboratory()) {
                for (RegisteredBlock registeredBlock : Main.getInstance().blockManager.getBlocks()) {
                    if (registeredBlock.getInfo() == null) continue;
                    if (registeredBlock.getInfo().equalsIgnoreCase("laboratory")) {
                        if (registeredBlock.getInfoValue() == null) continue;
                        int id = Integer.parseInt(registeredBlock.getInfoValue());
                        if (attack.defender.getLaboratory() == id) {
                            int attackerFactionNear = factionManager.getFactionMemberInRange(attack.attacker.getName(), registeredBlock.getLocation(), 100, true).size();
                            int defenderFactionNear = factionManager.getFactionMemberInRange(attack.defender.getName(), registeredBlock.getLocation(), 100, true).size();

                            if (attackerFactionNear < defenderFactionNear / 4) {
                                clearLaboratory(attack.attacker, attack.defender);
                                factionManager.sendCustomMessageToFaction(attack.defender.getName(), "§8[§" + attack.defender.getPrimaryColor() + "Labor§8]§a Die Gegnerische Fraktion hat das Labor erfolgreich ausgeraubt, da weniger als 1/4 deiner Fraktion am leben ist..");
                                factionManager.sendCustomMessageToFaction(attack.attacker.getName(), "§8[§" + attack.attacker.getPrimaryColor() + "Labor§8]§a Ihr habt es geschafft das Labor von " + attack.defender.getName() + " auszurauben.");
                                continue;
                            }
                        }
                    }
                }
            }
            if (!attack.doorOpened) {
                boolean near = false;
                RegisteredBlock block;
                for (RegisteredBlock registeredBlock : Main.getInstance().blockManager.getBlocks()) {
                    if (registeredBlock.getInfo() == null) continue;
                    if (registeredBlock.getInfo().equalsIgnoreCase("laboratory")) {
                        if (registeredBlock.getInfoValue() == null) continue;
                        int id = Integer.parseInt(registeredBlock.getInfoValue());
                        if (attack.defender.getLaboratory() == id) {
                            for (PlayerData playerData : playerManager.getPlayers()) {
                                if (playerData.getFaction() != null) {
                                    if (playerData.getFaction().equalsIgnoreCase(attack.attacker.getName())) {
                                        if (registeredBlock.getLocation().distance(playerData.getPlayer().getLocation()) < 5) {
                                            near = true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (!near) {
                    factionManager.sendCustomMessageToFaction(attack.defender.getName(), "§8[§" + attack.defender.getPrimaryColor() + "Labor§8]§a Die Angreifer haben es nicht geschafft die Tür aufzuschweißen.");
                    factionManager.sendCustomMessageToFaction(attack.attacker.getName(), "§8[§" + attack.attacker.getPrimaryColor() + "Labor§8]§a Ihr habt es nicht geschafft das Labor von " + attack.defender.getName() + " aufzuschweißen.");
                    attacks.remove(attack);
                } else {
                    LocalDateTime attackStartTime = attack.getStarted();
                    LocalDateTime after = attack.getStarted().plusMinutes(5);
                    System.out.println(after);
                    if (Utils.getTime().isAfter(after)) {
                        attack.setDoorOpened(true);
                        factionManager.sendCustomMessageToFaction(attack.attacker.getName(), "§8[§" + attack.attacker.getPrimaryColor() + "Labor§8]§a Die Tür ist nun offen, 10 Minuten bis die Inhalte geklaut werden können. (/labor)");
                        factionManager.sendCustomMessageToFaction(attack.defender.getName(), "§8[§" + attack.defender.getPrimaryColor() + "Labor§8]§a Eure Tür wurde aufgeschweißt, 10 Minuten bis die Inhalte geklaut werden können.");
                    }
                }
            } else {
                LocalDateTime attackStartTime = attack.getStarted();
                boolean near = false;
                for (RegisteredBlock registeredBlock : Main.getInstance().blockManager.getBlocks()) {
                    if (registeredBlock.getInfo().equalsIgnoreCase("laboratory")) {
                        int id = Integer.parseInt(registeredBlock.getInfoValue());
                        if (attack.defender.getLaboratory() == id) {
                            for (PlayerData playerData : playerManager.getPlayers()) {
                                if (playerData.getFaction() != null) {
                                    if (playerData.getFaction().equalsIgnoreCase(attack.attacker.getName())) {
                                        if (registeredBlock.getLocation().distance(playerData.getPlayer().getLocation()) < 15) {
                                            near = true;
                                        }

                                    }
                                }
                            }
                        }
                    }
                }
                if (near) {
                    LocalDateTime after = attack.getStarted().plusMinutes(15);
                    if (Utils.getTime().isAfter(after)) {
                        attack.setHackedLaboratory(true);
                        factionManager.sendCustomMessageToFaction(attack.attacker.getName(), "§8[§" + attack.attacker.getPrimaryColor() + "Labor§8]§a Ihr könnt nun die Inhalte entwenden.");
                    }
                } else {
                    factionManager.sendCustomMessageToFaction(attack.defender.getName(), "§8[§" + attack.defender.getPrimaryColor() + "Labor§8]§a Die Angreifer haben es nicht geschafft das Labor auszurauben.");
                    factionManager.sendCustomMessageToFaction(attack.attacker.getName(), "§8[§" + attack.attacker.getPrimaryColor() + "Labor§8]§a Ihr habt es nicht geschafft das Labor von " + attack.defender.getName() + " auszurauben.");
                    attacks.remove(attack);
                }
            }
        }
    }

    private class LaboratoryAttack {
        public final FactionData attacker;
        public final FactionData defender;
        private LocalDateTime started;
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

        public LocalDateTime getStarted() {
            return started;
        }

        public void setStarted(LocalDateTime started) {
            this.started = started;
        }
    }
}
