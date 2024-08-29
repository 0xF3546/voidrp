package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.Bomb;
import de.polo.voidroleplay.dataStorage.FactionData;
import de.polo.voidroleplay.dataStorage.NaviData;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.game.events.MinuteTickEvent;
import de.polo.voidroleplay.utils.ItemManager;
import de.polo.voidroleplay.utils.Navigation;
import de.polo.voidroleplay.utils.PlayerManager;
import de.polo.voidroleplay.utils.Utils;
import de.polo.voidroleplay.utils.enums.RoleplayItem;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.*;

public class BombeCommand implements CommandExecutor, Listener {

    private final PlayerManager playerManager;
    private final Utils utils;

    private final List<Bomb> bombLocation = new ArrayList<>();

    public static boolean ACTIVE;

    public BombeCommand(PlayerManager playerManager, Utils utils) {
        this.playerManager = playerManager;
        this.utils = utils;

        Main.getInstance().registerCommand("bombe", this);
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }

    String[] color = {"Rot", "Blau", "Grün"};
    String chosenColor = color[new Random().nextInt(color.length)];

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }

        Player player = (Player) sender;
        Location location = player.getLocation();
        PlayerData playerData = playerManager.getPlayerData(player);

        if (playerData.getFaction() == null) {
            player.sendMessage(Main.error_nopermission);
            return false;
        }

        if (!playerData.getFaction().equalsIgnoreCase("Terroristen")) {
            player.sendMessage(Main.error_nopermission);
            return false;
        }

        if (ItemManager.getCustomItemCount(player, RoleplayItem.SPRENGSTOFF) < 1) {
            player.sendMessage(Main.faction_prefix + "Du hast keinen Sprengstoff bei dir");
            return false;
        }

        if (playerData.getFactionGrade() < 6) {
            player.sendMessage(Main.error_nopermission);
            return false;
        }

        World world = location.getWorld();

        Block block = world.getBlockAt(location);

        for (Bomb bomb : bombLocation) {
            player.getLocation().getBlock().setType(Material.TNT);
            Bomb bombe = new Bomb(Utils.getTime(), block, 15);
            bombLocation.add(bombe);
            player.getInventory().addItem(ItemManager.createItem(RoleplayItem.DRAHT.getMaterial(), 1, 0, RoleplayItem.DRAHT.getDisplayName(), chosenColor));
            Bukkit.broadcastMessage("§8[§6News§8] §6Achtung! es wurde eine bombe gefunden, in der nähe von: " + Navigation.getNearestNaviPoint(bombe.getBlock().getLocation()).getName());
            ACTIVE = true;
        }

        return true;
    }

    @EventHandler
    public void onMinuteTick(MinuteTickEvent event) {
        for (Bomb bomb : bombLocation) {
            if (bomb.getMinutes() == 0) {
                explodeBomb(bomb.getBlock().getLocation());
            }
        }
    }

    public void explodeBomb(Location location) {
        location.getWorld().createExplosion(location, 0.0f, false, false);
        double radius = 40.0;

        for (Player p : Bukkit.getOnlinePlayers()) {
            for (Bomb bomb : bombLocation) {
                if (p.getLocation().distance(bomb.getBlock().getLocation()) < 20) {
                    p.setHealth(0);
                    continue;
                }
                if (p.getLocation().distance(bomb.getBlock().getLocation()) < 30) {
                    p.setHealth(5);
                    continue;
                }
                if (p.getLocation().distance(bomb.getBlock().getLocation()) < 40) {
                    p.setHealth(10);
                }
                bombLocation.remove(bomb);
                Bukkit.broadcastMessage("§8[§6News§8] §6Die Bombe konnte nicht entschärft werden!");
                bomb.getBlock().setType(Material.AIR);
                ACTIVE = false;
            }
        }
    }

    public void defuseBomb() {
        for (Bomb bomb : bombLocation) {
            bombLocation.remove(bomb);
            Bukkit.broadcastMessage("§8[§6News§8] §6Die Bombe konnte erfolgreich entschärft werden!");
            bomb.getBlock().setType(Material.AIR);
            ACTIVE = false;
        }
    }

    public String getDrahtColor() {
        return chosenColor;
    }

    public List<Bomb> getBombLocation() {
        return bombLocation;
    }
}
