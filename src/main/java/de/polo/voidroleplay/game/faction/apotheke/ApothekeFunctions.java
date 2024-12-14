package de.polo.voidroleplay.game.faction.apotheke;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.storage.FactionData;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.database.impl.MySQL;
import de.polo.voidroleplay.game.events.MinuteTickEvent;
import de.polo.voidroleplay.manager.*;
import de.polo.voidroleplay.manager.inventory.CustomItem;
import de.polo.voidroleplay.manager.inventory.InventoryManager;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.utils.Utils;
import de.polo.voidroleplay.utils.enums.Prescription;
import de.polo.voidroleplay.utils.enums.RoleplayItem;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.SneakyThrows;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class ApothekeFunctions implements Listener {
    private final List<Apotheke> apotheken = new ObjectArrayList<>();
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
        return minutesDifference >= 60;
    }

    public long getMinuteDifference(Apotheke apotheke) {
        Duration duration = Duration.between(apotheke.getLastAttack(), LocalDateTime.now());
        long minutesDifference = duration.toMinutes();
        return 60 - minutesDifference;
    }

    private boolean isInAttack(String faction) {
        for (Apotheke apotheke : rob.keySet()) {
            if (apotheke.getAttackerFaction().equalsIgnoreCase(faction) || apotheke.getOwner().equalsIgnoreCase(faction)) {
                return true;
            }
        }
        return false;
    }

    public void openApotheke(Player player, int id) {
        Apotheke apotheke = getById(id);
        if (apotheke == null) return;

        PlayerData playerData = playerManager.getPlayerData(player);
        String owner = "§9Staat";
        boolean canAttack = false;

        FactionData apothekeFactionData = apotheke.isStaat() ? null : factionManager.getFactionData(apotheke.getOwner());
        FactionData playerFactionData = playerData.getFaction() != null
                ? factionManager.getFactionData(playerData.getFaction())
                : null;

        if (apothekeFactionData != null) {
            owner = "§" + apothekeFactionData.getPrimaryColor() + apothekeFactionData.getFullname();
            canAttack = apothekeFactionData.isBadFrak();
        }

        if (apotheke.isStaat() && playerFactionData != null
                ) {
            canAttack = true;
        }

        InventoryManager inventoryManager = new InventoryManager(player, 27,
                "§8 » §cApotheke (" + owner + "§c)", true, true);

        boolean finalCanAttack = canAttack;
        CustomItem infoItem = new CustomItem(
                26,
                ItemManager.createItem(Material.PAPER, 1, 0, "§bInformation",
                        Arrays.asList("§8 ➥ §7Besitzer§8: " + owner,
                                finalCanAttack ? (getMinuteDifference(apotheke) >= 60 || getMinuteDifference(apotheke) <= 0 ? "§8 » §cKlicke zum attackieren": "§8 ➥ §7Attackierbar§8: §e" + getMinuteDifference(apotheke) + "min") : ""))) {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (canAttack(apotheke) && finalCanAttack) {
                    handleAttack(player, playerData, apotheke);
                }
            }
        };

        inventoryManager.setItem(infoItem);

        String NO_MONEY = Prefix.ERROR + "Du hast nicht genug Bargeld dabei.";

        int i = 0;
        if (playerData.getFaction() != null && playerData.getFaction().equalsIgnoreCase("FBI")) {
            inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(RoleplayItem.ADRENALINE_INJECTION.getMaterial(), 1, 0, RoleplayItem.ADRENALINE_INJECTION.getDisplayName(), "§8 ➥ §a125$")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    if (playerData.getBargeld() < 125) {
                        player.sendMessage(NO_MONEY);
                        return;
                    }
                    playerData.removeMoney(125, "Kauf Adrenalin Spritze");
                    playerData.getInventory().addItem(RoleplayItem.ADRENALINE_INJECTION, 1);
                }
            });
            i++;
        }
        inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(Material.PAPER, 1, 0, "§cRezept einlösen")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                for (ItemStack stack : player.getInventory().getContents()) {
                    if (stack == null) continue;
                    if (!stack.getType().equals(Material.PAPER)) continue;
                    for (Prescription prescription : Prescription.values()) {
                        if (stack.getItemMeta().getDisplayName().contains(prescription.getName())) {
                            playerData.getInventory().addItem(prescription.getDrug().getItem(), Main.random(prescription.getMinAmount() * stack.getAmount(), prescription.getMaxAmount() * stack.getAmount()));
                            player.getInventory().remove(stack);
                            player.sendMessage(Component.text("§6Du hast ein " + prescription.getName() + " Rezept eingelöst."));
                        }
                    }
                }
            }
        });
        i++;
    }

    private void handleAttack(Player player, PlayerData playerData, Apotheke apotheke) {
        if (isInAttack(apotheke.getOwner())) {
            player.sendMessage(Prefix.ERROR + "Diese Fraktion ist aktuell in einem Überfall.");
            return;
        }
        if (apotheke.getOwner().equalsIgnoreCase(playerData.getFaction())) {
            player.sendMessage(Prefix.ERROR + "Du kannst deine eigenen Apotheken nicht einschüchtern.");
            return;
        }

        // Angriff starten
        player.closeInventory();
        apotheke.setLastAttack(LocalDateTime.now());

        // Nachrichten an Fraktionen senden
        if (apotheke.isStaat()) {
            factionManager.sendCustomMessageToFaction("Polizei",
                    "§8[§cApotheke-" + apotheke.getId() + "§8]§c Es wurde ein Überfall auf eine Apotheke gemeldet.");
        } else {
            factionManager.sendCustomMessageToFaction(apotheke.getOwner(),
                    "§8[§cApotheke-" + apotheke.getId() + "§8]§c Jemand versucht deine Apotheke zu übernehmen.");
        }
        factionManager.sendCustomMessageToFaction(playerData.getFaction(),
                "§8[§cApotheke-" + apotheke.getId() + "§8]§a Ihr fangt an die Apotheke zu übernehmen!");

        // Spieler benachrichtigen
        player.sendMessage("§8[§cApotheke§8]§7 Warte nun 10 Minuten, verlasse dabei die Apotheke nicht.");

        // Überfall registrieren
        apotheke.setAttacker(player);
        apotheke.setAttackerFaction(playerData.getFaction());
        rob.put(apotheke, 0);
    }


    public Collection<Apotheke> getApotheken() {
        return apotheken;
    }

    @SneakyThrows
    @EventHandler
    public void MinuteTick(MinuteTickEvent event) {
        for (Apotheke apotheke : rob.keySet()) {
            Location location = locationManager.getLocation("apotheke-" + apotheke.getId());
            if (!factionManager.isFactionMemberInRange(apotheke.getAttackerFaction(), location, 30, false)) {
                rob.remove(apotheke);
                factionManager.sendCustomMessageToFaction(apotheke.getOwner(), "§8[§cApotheke-" + apotheke.getId() + "§8]§a Die Angreifer haben aufgehört die Apotheke einzuschüchtern.");
                factionManager.sendCustomMessageToFaction(apotheke.getAttackerFaction(), "§8[§cApotheke-" + apotheke.getId() + "§8]§c Ihr habt aufgehört die Apotheke einzuschüchtern.");
                return;
            }
            int currentTime = rob.get(apotheke);
            if (currentTime >= 10) {
                factionManager.sendCustomMessageToFaction(apotheke.getOwner(), "§8[§cApotheke-" + apotheke.getId() + "§8]§c Die Angreifer haben es geschafft eure Apotheke einzuschüchtern.");
                factionManager.sendCustomMessageToFaction(apotheke.getAttackerFaction(), "§8[§cApotheke-" + apotheke.getId() + "§8]§a Ihr habt es geschafft die Apotheke einzuschüchtern.");
                for (PlayerData playerData1 : factionManager.getFactionMemberInRange(apotheke.getAttackerFaction(), location, 30, true)) {
                    playerManager.addExp(playerData1.getPlayer(), Main.random(5, 10));
                }
                if (apotheke.getAttacker().getName().equalsIgnoreCase("Polizei") || apotheke.getAttacker().getName().equalsIgnoreCase("FBI")) {
                    apotheke.setOwner("staat");
                } else {
                    apotheke.setOwner(apotheke.getAttackerFaction());
                }
                Main.getInstance().blockManager.updateBlocksAtScenario("apotheke-" + apotheke.getId(), factionManager.getFactionData(apotheke.getAttackerFaction()));
                apotheke.save();
                rob.remove(apotheke);
            } else {
                rob.replace(apotheke, ++currentTime);
                int remaining = (10 - currentTime + 1);
                factionManager.sendCustomMessageToFaction(apotheke.getOwner(), "§8[§cApotheke-" + apotheke.getId() + "§8]§c Die Angreifer haben noch " + remaining + " Minuten bis der Apotheker aufgibt!");
                factionManager.sendCustomMessageToFaction(apotheke.getAttackerFaction(), "§8[§cApotheke-" + apotheke.getId() + "§8]§a Noch " + remaining + " Minuten bis der Apotheker aufgibt!");
            }
        }
        if ((Utils.getTime().getHour() >= 16 && Utils.getTime().getHour() <= 22)) {
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
                    if (factionData.getName().equalsIgnoreCase("FBI") || factionData.getName().equalsIgnoreCase("Polizei")) {
                        factionManager.addFactionMoney("FBI", 1000, "Apotheke");
                        factionManager.addFactionMoney("Polizei", 1000, "Apotheke");
                        for (PlayerData playerData : playerManager.getPlayers()) {
                            if (playerData.getFaction() != null) {
                                if (playerData.getFaction().equalsIgnoreCase("FBI") || playerData.getFaction().equalsIgnoreCase("Polizei")) {
                                    Player player = Bukkit.getPlayer(playerData.getUuid());
                                    if (player != null)
                                        player.sendMessage("§8[§" + factionData.getPrimaryColor() + factionData.getName() + "§8]§a Deine Fraktion hat §21000$§a Steuern aus Apotheken erhalten .");
                                }
                            }
                        }
                    } else {
                        factionData.addBankMoney(1000, "Apotheken");
                        for (PlayerData playerData : playerManager.getPlayers()) {
                            if (playerData.getFaction() != null) {
                                if (playerData.getFaction().equalsIgnoreCase(factionData.getName())) {
                                    Player player = Bukkit.getPlayer(playerData.getUuid());
                                    if (player != null)
                                        player.sendMessage("§8[§" + factionData.getPrimaryColor() + factionData.getName() + "§8]§a Deine Fraktion hat §21000$ Schutzgeld§a aus den aktuell übernommenen Apotheken erhalten.");
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
