package de.polo.core.faction.commands;

import de.polo.api.VoidAPI;
import de.polo.core.Main;
import de.polo.core.location.services.NavigationService;
import de.polo.core.storage.Bomb;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.game.events.MinuteTickEvent;
import de.polo.core.faction.service.impl.FactionManager;
import de.polo.core.manager.ItemManager;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.utils.Prefix;
import de.polo.core.utils.Utils;
import de.polo.core.utils.enums.RoleplayItem;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.time.LocalDateTime;

public class BombeCommand implements CommandExecutor, Listener {

    public static boolean ACTIVE;
    private final PlayerManager playerManager;
    private final Utils utils;
    private final FactionManager factionManager;
    public LocalDateTime lastBomb = Utils.getTime().minusHours(6);
    private Bomb bomb = null;

    public BombeCommand(PlayerManager playerManager, Utils utils, FactionManager factionManager) {
        this.playerManager = playerManager;
        this.utils = utils;
        this.factionManager = factionManager;

        Main.registerCommand("bombe", this);
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return false;
        }

        Location location = player.getLocation();
        PlayerData playerData = playerManager.getPlayerData(player);

        if (playerData.getFaction() == null) {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
            return false;
        }

        if (!playerData.getFaction().equalsIgnoreCase("Terroristen")) {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
            return false;
        }

        if (bomb != null) {
            player.sendMessage(Prefix.ERROR + "Es ist bereits eine Bombe im gange.");
            return false;
        }

        if (!Utils.getTime().isAfter(lastBomb.plusHours(6))) {
            player.sendMessage(Prefix.ERROR + "In den letzten 6 Stunden war bereits eine Bombe.");
            return false;
        }

        if (ItemManager.getCustomItemCount(player, RoleplayItem.SPRENGSTOFF) < 1) {
            player.sendMessage(Prefix.FACTION + "Du hast keinen Sprengstoff bei dir");
            return false;
        }

        if (!playerData.isLeader()) {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
            return false;
        }

        int count = factionManager.getOnlineMemberCount("Polizei");
        count += factionManager.getOnlineMemberCount("FBI");
        if (count < 4) {
            player.sendMessage(Prefix.ERROR + "Es sind nicht genug Staatsmitglieder online.");
            return false;
        }

        World world = location.getWorld();

        Block block = world.getBlockAt(location);

        player.getLocation().getBlock().setType(Material.TNT);
        ItemManager.removeCustomItem(player, RoleplayItem.SPRENGSTOFF, 1);
        bomb = new Bomb(Utils.getTime(), block, 15);
        NavigationService navigationService = VoidAPI.getService(NavigationService.class);
        player.getInventory().addItem(ItemManager.createItem(RoleplayItem.DRAHT.getMaterial(), 1, 0, RoleplayItem.DRAHT.getDisplayName(), bomb.getColor()));
        Bukkit.broadcastMessage("§8[§6News§8] §6Achtung! es wurde eine bombe gefunden, in der nähe von: " + navigationService.getNearestNaviPoint(bomb.getBlock().getLocation()).getName().replace("&", "§"));
        ACTIVE = true;
        lastBomb = Utils.getTime();

        return true;
    }

    @EventHandler
    public void onMinuteTick(MinuteTickEvent event) {
        if (bomb == null) return;
        bomb.setMinutes(bomb.getMinutes() - 1);
        if (bomb.getMinutes() == 0) {
            explodeBomb(bomb.getBlock().getLocation());
        }
    }

    public void explodeBomb(Location location) {
        location.getWorld().createExplosion(location, 0.0f, false, false);
        double radius = 40.0;
        try {
            factionManager.addFactionMoney("Terroristen", 20000, "Bombe");
            factionManager.removeFactionMoney("Polizei", 10000, "Bombe");
            factionManager.removeFactionMoney("FBI", 10000, "Bombe");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getLocation().distance(bomb.getBlock().getLocation()) < 20) {
                p.setHealth(0);
                continue;
            }
            if (p.getLocation().distance(bomb.getBlock().getLocation()) < 30) {
                p.setHealth(p.getHealth() - 10);
                continue;
            }
            if (p.getLocation().distance(bomb.getBlock().getLocation()) < 40) {
                p.setHealth(p.getHealth() - 20);
            }
        }
        Bukkit.broadcastMessage("§8[§6News§8] §6Die Bombe konnte nicht entschärft werden!");
        cleanUpBomb();
    }

    public void defuseBomb() {
        Bukkit.broadcastMessage("§8[§6News§8] §6Die Bombe konnte erfolgreich entschärft werden!");
        cleanUpBomb();
        try {
            factionManager.addFactionMoney("Polizei", 10000, "Bombe");
            factionManager.addFactionMoney("FBI", 10000, "Bombe");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void cleanUpBomb() {
        bomb.getBlock().setType(Material.AIR);
        ACTIVE = false;
        bomb = null;
    }

    public Bomb getBomb() {
        return bomb;
    }
}
