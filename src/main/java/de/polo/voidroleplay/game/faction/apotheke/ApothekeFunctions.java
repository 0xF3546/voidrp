package de.polo.voidroleplay.game.faction.apotheke;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.FactionData;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.database.MySQL;
import de.polo.voidroleplay.utils.*;
import de.polo.voidroleplay.utils.InventoryManager.CustomItem;
import de.polo.voidroleplay.utils.InventoryManager.InventoryManager;
import de.polo.voidroleplay.game.events.MinuteTickEvent;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
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

public class ApothekeFunctions implements Listener {
    private final List<Apotheke> apotheken = new ArrayList<>();
    private final MySQL mySQL;
    private final Utils utils;
    private final FactionManager factionManager;
    private final PlayerManager playerManager;
    private final HashMap<Apotheke, Integer> rob = new HashMap<>();
    private final LocationManager locationManager;

    @SneakyThrows
    public ApothekeFunctions(MySQL mySQL, Utils utils, FactionManager factionManager, PlayerManager playerManager, LocationManager locationManager) {
        this.mySQL = mySQL;
        this.utils = utils;
        this.factionManager = factionManager;
        this.playerManager = playerManager;
        this.locationManager = locationManager;
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
        Statement statement = mySQL.getStatement();
        ResultSet resultSet = statement.executeQuery("SELECT * FROM apotheken");
        while (resultSet.next()) {
            Apotheke apotheke = new Apotheke();
            apotheke.setId(resultSet.getInt("id"));
            apotheke.setStaat(resultSet.getBoolean("isStaat"));
            apotheke.setOwner(resultSet.getString("owner"));
            apotheke.setLastAttack(resultSet.getTimestamp("lastAttack").toLocalDateTime());
            apotheken.add(apotheke);
        }
    }

    private Apotheke getById(int id) {
        for (Apotheke apotheke : getApotheken()) {
            if (apotheke.getId() == id) return apotheke;
        }
        return null;
    }

    private boolean canAttack(Apotheke apotheke) {
        Duration duration = Duration.between(apotheke.getLastAttack(), LocalDateTime.now());
        long minutesDifference = duration.toMinutes();
        if (minutesDifference < 60) {
            return false;
        }
        return true;
    }

    public long getMinuteDifference(Apotheke apotheke) {
        Duration duration = Duration.between(apotheke.getLastAttack(), LocalDateTime.now());
        long minutesDifference = duration.toMinutes();
        return 60 - minutesDifference;
    }

    public void openApotheke(Player player, int id) {
        Apotheke apotheke = getById(id);
        if (apotheke == null) return;
        boolean canAttack = false;
        PlayerData playerData = playerManager.getPlayerData(player);
        FactionData factionData = factionManager.getFactionData(playerData.getFaction());
        if (factionData.isBadFrak()) canAttack = true;
        String owner = "§9Staat";
        if (apotheke.isStaat()) {
            if (playerData.getFaction().equalsIgnoreCase("FBI") || playerData.getFaction().equalsIgnoreCase("Polizei")) canAttack = true;
        }
        if (!apotheke.getOwner().equalsIgnoreCase("staat")) {
            factionData = factionManager.getFactionData(apotheke.getOwner());
            owner = "§" + factionData.getPrimaryColor() + factionData.getFullname();
        }
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8 » §cApotheke (" + owner + "§c)", true, true);
        if (!canAttack) return;
        if (canAttack(apotheke)) {
            inventoryManager.setItem(new CustomItem(13, ItemManager.createItem(Material.PAPER, 1, 0, "§bInformation", Arrays.asList("§8 ➥ §7Besitzer§8: " + owner, "§8 ➥ §cKlicke zum attackieren"))) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    if (apotheke.getOwner().equalsIgnoreCase(playerData.getFaction())) {
                        player.sendMessage(Main.error + "Du kannst deine eigene Apotheken nicht einschüchtern.");
                        return;
                    }
                    player.closeInventory();
                    apotheke.setLastAttack(LocalDateTime.now());
                    if (apotheke.isStaat()) {
                        factionManager.sendCustomMessageToFaction("Polizei", "§8[§cApotheke-" + apotheke.getId() + "§8]§c Es wurde ein Überfall auf eine Apotheke gemeldet.");
                    }
                    if (!apotheke.getOwner().equalsIgnoreCase("staat")) {
                        factionManager.sendCustomMessageToFaction(apotheke.getOwner(), "§8[§cApotheke-" + apotheke.getId() + "§8]§c Jemand versucht deine Apotheke zu übernehmen.");
                    }
                    factionManager.sendCustomMessageToFaction(playerData.getFaction(), "§8[§cApotheke-" + apotheke.getId() + "§8]§a Ihr fangt an die Apotheke zu übernehmen!");
                    player.sendMessage("§8[§cApotheke§8]§7 Warte nun 10 Minuten, verlasse dabei die Apotheke nicht.");
                    apotheke.setAttacker(player);
                    apotheke.setAttackerFaction(playerData.getFaction());
                    rob.put(apotheke, 0);
                }
            });
        } else {
            inventoryManager.setItem(new CustomItem(13, ItemManager.createItem(Material.PAPER, 1, 0, "§bInformation", Arrays.asList("§8 ➥ §7Besitzer§8: " + owner, "§8 ➥ §7Attackierbar§8:§e " + getMinuteDifference(apotheke) + "min"))) {
                @Override
                public void onClick(InventoryClickEvent event) {

                }
            });
        }
    }

    public Collection<Apotheke> getApotheken() {
        return apotheken;
    }

    @EventHandler
    public void MinuteTick(MinuteTickEvent event) {
        for (Apotheke apotheke : rob.keySet()) {
            if (!apotheke.getAttacker().isOnline()) {
                rob.remove(apotheke);
                factionManager.sendCustomMessageToFaction(apotheke.getOwner(), "§8[§cApotheke-" + apotheke.getId() + "§8]§a Die Angreifer haben aufgehört die Apotheke einzuschüchtern.");
                factionManager.sendCustomMessageToFaction(apotheke.getAttackerFaction(), "§8[§cApotheke-" + apotheke.getId() + "§8]§c Ihr habt aufgehört die Apotheke einzuschüchtern.");
                return;
            }
            PlayerData playerData = playerManager.getPlayerData(apotheke.getAttacker());
            if (playerData.isDead()) {
                rob.remove(apotheke);
                factionManager.sendCustomMessageToFaction(apotheke.getOwner(), "§8[§cApotheke-" + apotheke.getId() + "§8]§a Die Angreifer haben aufgehört die Apotheke einzuschüchtern.");
                factionManager.sendCustomMessageToFaction(apotheke.getAttackerFaction(), "§8[§cApotheke-" + apotheke.getId() + "§8]§c Ihr habt aufgehört die Apotheke einzuschüchtern.");
                return;
            }
            if (locationManager.getDistanceBetweenCoords(apotheke.getAttacker(), "apotheke-" + apotheke.getId()) >= 25) {
                rob.remove(apotheke);
                factionManager.sendCustomMessageToFaction(apotheke.getOwner(), "§8[§cApotheke-" + apotheke.getId() + "§8]§a Die Angreifer haben aufgehört die Apotheke einzuschüchtern.");
                factionManager.sendCustomMessageToFaction(apotheke.getAttackerFaction(), "§8[§cApotheke-" + apotheke.getId() + "§8]§c Ihr habt aufgehört die Apotheke einzuschüchtern.");
                return;
            }
            int currentTime = rob.get(apotheke);
            if (currentTime >= 10) {
                factionManager.sendCustomMessageToFaction(apotheke.getOwner(), "§8[§cApotheke-" + apotheke.getId() + "§8]§c Die Angreifer haben es geschafft eure Apotheke einzuschüchtern.");
                factionManager.sendCustomMessageToFaction(apotheke.getAttackerFaction(), "§8[§cApotheke-" + apotheke.getId() + "§8]§a Ihr habt es geschafft die Apotheke einzuschüchtern.");
                if (apotheke.getAttacker().getName().equalsIgnoreCase("Polizei") || apotheke.getAttacker().getName().equalsIgnoreCase("FBI")) {
                 apotheke.setOwner("staat");
                } else {
                    apotheke.setOwner(apotheke.getAttackerFaction());
                }
                apotheke.save();
                rob.remove(apotheke);
            } else {
                rob.replace(apotheke, ++currentTime);
                int remaining = (10 - currentTime + 1);
                factionManager.sendCustomMessageToFaction(apotheke.getOwner(), "§8[§cApotheke-" + apotheke.getId() + "§8]§c Die Angreifer haben noch " + remaining + " Minuten bis der Apotheker aufgibt!");
                factionManager.sendCustomMessageToFaction(apotheke.getAttackerFaction(), "§8[§cApotheke-" + apotheke.getId() + "§8]§a Noch " + remaining + " Minuten bis der Apotheker aufgibt!");
            }
        }
        if ((LocalDateTime.now().getHour() >= 16 && LocalDateTime.now().getHour() <= 22)) {
            for (FactionData factionData : factionManager.getFactions()) {
                int plus = 0;
                for (Apotheke apotheke : getApotheken()) {
                    if (apotheke.getOwner() != null) {
                        if (factionData.getName() != null) {
                            if (apotheke.getOwner().equalsIgnoreCase(factionData.getName())) {
                                if (apotheke.getLastAttack().getMinute() == event.getMinute()) {
                                    if (apotheke.isStaat()) plus += ServerManager.getPayout("apotheke_besetzt_staat");
                                    else plus += ServerManager.getPayout("apotheke_besetzt_normal");
                                }
                            }
                        }
                    }
                }
                factionData.setJointsMade(plus);
                if (plus >= 1) {
                    for (PlayerData playerData : playerManager.getPlayers()) {
                        if (playerData.getFaction() != null) {
                            if (playerData.getFaction().equalsIgnoreCase(factionData.getName())) {
                                Player player = Bukkit.getPlayer(playerData.getUuid());
                                player.sendMessage("§8[§" + factionData.getPrimaryColor() + factionData.getName() + "§8]§a Deine Fraktion hat §2" + plus + " Joints§a aus den aktuell übernommenen Apotheken erhalten.");
                                factionData.storage.setJoint(factionData.storage.getJoint() + plus);
                                factionData.storage.save();
                            }
                        }
                    }
                }
            }
        }
    }
}
