package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.storage.FactionData;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.game.events.SubmitChatEvent;
import de.polo.voidroleplay.manager.FactionManager;
import de.polo.voidroleplay.manager.inventory.CustomItem;
import de.polo.voidroleplay.manager.inventory.InventoryManager;
import de.polo.voidroleplay.manager.ItemManager;
import de.polo.voidroleplay.manager.LocationManager;
import de.polo.voidroleplay.manager.PlayerManager;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.utils.Utils;
import de.polo.voidroleplay.utils.enums.EXPType;
import de.polo.voidroleplay.utils.enums.RoleplayItem;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class PfeifenTransport implements CommandExecutor, Listener {
    private final PlayerManager playerManager;
    private final FactionManager factionManager;
    private final LocationManager locationManager;
    private final HashMap<String, Integer> transports = new HashMap<>();
    private final List<UUID> cooldownUser = new ObjectArrayList<>();
    private LocalDateTime lastTransport = Utils.getTime();

    public PfeifenTransport(PlayerManager playerManager, FactionManager factionManager, LocationManager locationManager) {
        this.playerManager = playerManager;
        this.factionManager = factionManager;
        this.locationManager = locationManager;

        Main.registerCommand("pfeifentransport", this);
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player);
        if (playerData.getFaction() != null) {
            player.sendMessage(Prefix.ERROR + "Diesen Transport können nur Zivilisten machen!");
            return false;
        }
        if (locationManager.getDistanceBetweenCoords(player, "drugtransport") > 5) {
            player.sendMessage(Prefix.ERROR + "Du bist nicht in der nähe des Pfeifentransport.");
            return false;
        }
        if (cooldownUser.contains(player.getUniqueId())) {
            player.sendMessage(Prefix.ERROR + "Du kannst den Job nur alle 2 Stunden machen.");
            return false;
        }
        if (!Utils.getTime().plusMinutes(15).isAfter(lastTransport) && !playerData.isAduty()) {
            player.sendMessage(Prefix.ERROR + "Der Transport wurde in den letzten 15 Minuten bereits ausgeführt! (" + Duration.between(lastTransport, Utils.getTime()) + "min noch)");
            return false;
        }
        openMenu(player);
        return false;
    }

    private void openMenu(Player player) {
        int i = 0;
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8 » §7Pfeifentransport");
        for (FactionData factionData : factionManager.getFactions()) {
            if (!factionData.isActive()) continue;
            if (factionData.isBadFrak() || factionData.getName().equalsIgnoreCase("ICA")) {
                int amountDelivered = 0;
                if (transports.get(factionData.getName()) != null)
                    amountDelivered = transports.get(factionData.getName());
                if (amountDelivered >= 50) {
                    inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(Material.PAPER, 1, 0, "§" + factionData.getPrimaryColor() + factionData.getFullname(), "§8 ➥ §c50§8/§c50")) {
                        @Override
                        public void onClick(InventoryClickEvent event) {

                        }
                    });
                } else {
                    inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(Material.PAPER, 1, 0, "§" + factionData.getPrimaryColor() + factionData.getFullname(), "§8 ➥ §7" + amountDelivered + "§8/§c50")) {
                        @Override
                        public void onClick(InventoryClickEvent event) {
                            player.closeInventory();
                            PlayerData playerData = playerManager.getPlayerData(player);
                            playerData.setVariable("transport:faction", factionData);
                            playerData.setVariable("chatblock", "transport::pfeife");
                            player.sendMessage(Prefix.MAIN + "Gib nun an wie viel du Liefern möchtest.");
                        }
                    });
                }
                i++;
            }
        }
    }

    private void startTransport(Player player, FactionData factionData, int amount) {
        PlayerData playerData = playerManager.getPlayerData(player);
        if (locationManager.getDistanceBetweenCoords(player, "drugtransport") > 5) {
            player.sendMessage(Prefix.ERROR + "Du bist nicht in der nähe des Pfeifentransport.");
            return;
        }
        lastTransport = Utils.getTime();
        player.sendMessage(Prefix.MAIN + "Du startest den Transport, begib dich zum Navipunkt!");
        playerData.setVariable("transport::amount", amount);
        Main.getInstance().utils.navigationManager.createNaviByLocation(player, factionData.getName());
        playerData.setVariable("job", "pfeifentransport");
        if (amount < 1 || amount > 50) {
            player.sendMessage(Prefix.ERROR + "Du kannst nur zwischen 1 und 50 Pfeifen transportieren");
            return;
        }
        if (amount >= 40) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 2, true, false));
        } else if (amount >= 20) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 1, true, false));
        }

        if (amount >= 20) {
            factionManager.sendCustomMessageToFaction("Polizei", "§8[§bInformant§8]§7 Ein Drogentransport wurde gestartet.");
        }
    }

    public void dropTransport(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player);
        FactionData factionData = playerData.getVariable("transport:faction");
        if (locationManager.getDistanceBetweenCoords(player, factionData.getName()) > 5) {
            player.sendMessage(Prefix.ERROR + "Du bist nicht in der nähe von " + factionData.getName());
            return;
        }

        int amount = playerData.getVariable("transport::amount");
        if (transports.get(factionData.getName()) != null && transports.get(factionData.getName()) >= amount) {
            player.sendMessage(Prefix.ERROR + "Die Fraktion hat bereits genug erhalten!");
            return;
        }
        playerData.addMoney(amount * 100, "Drogentransport");
        player.sendMessage("§a+" + amount * 100 + "$");
        transports.put(factionData.getName(), amount);
        cooldownUser.add(player.getUniqueId());
        int random = Main.random(1, 3);
        RoleplayItem item;
        if (random == 1) {
            item = RoleplayItem.SNUFF;
        } else if (random == 2) {
            item = RoleplayItem.CIGAR;
        } else {
            item = RoleplayItem.PIPE;
        }

        factionManager.sendCustomMessageToFaction(factionData.getName(), "§8[§cTransport§8]§7 " + player.getName() + " hat deiner Fraktion " + amount + " " + item.getDisplayName() + "§7 gebracht.");
        factionData.storage.addItem(item, amount);
        playerManager.addExp(player, Main.random(20, 50));
        playerManager.addExp(player, EXPType.POPULARITY, Main.random(100, 200));
        player.removePotionEffect(PotionEffectType.SLOW);
        playerData.setVariable("job", null);
    }

    @EventHandler
    public void onChatSubmit(SubmitChatEvent event) {
        if (event.getSubmitTo() == null) return;
        if (event.getSubmitTo().equalsIgnoreCase("transport::pfeife")) {
            try {
                int amount = Integer.parseInt(event.getMessage());
                FactionData factionData = event.getPlayerData().getVariable("transport:faction");
                if (transports.get(factionData.getName()) != null) {
                    int fStats = transports.get(factionData.getName());
                    if (fStats + amount > 50) {
                        event.getPlayer().sendMessage(Prefix.ERROR + "Du kannst maximal " + (50 - fStats) + "g mit nehmen!");
                        return;
                    }
                }
                startTransport(event.getPlayer(), factionData, amount);
            } catch (Exception ex) {
                event.getPlayer().sendMessage(Prefix.ERROR + "Die Zahl muss numerisch sein!");
            }
        }
    }

    public void resetTransports() {
        transports.clear();
        cooldownUser.clear();
    }
}
