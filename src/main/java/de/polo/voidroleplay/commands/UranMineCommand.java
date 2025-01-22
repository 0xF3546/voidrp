package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.game.events.NaviReachEvent;
import de.polo.voidroleplay.handler.CommandBase;
import de.polo.voidroleplay.manager.ItemManager;
import de.polo.voidroleplay.manager.LocationManager;
import de.polo.voidroleplay.manager.ServerManager;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.storage.RegisteredBlock;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.utils.enums.RoleplayItem;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static de.polo.voidroleplay.Main.*;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */

@CommandBase.CommandMeta(name = "uranmine")
public class UranMineCommand extends CommandBase implements Listener {
    private List<Location> rollOutLocations = new ObjectArrayList<>();

    public UranMineCommand(@NotNull CommandMeta meta) {
        super(meta);
        rollOutLocations = blockManager.getBlocks()
                .stream()
                .filter(x -> x.getInfoValue() != null && x.getInfoValue().equalsIgnoreCase("mine"))
                .map(RegisteredBlock::getLocation)
                .toList();

        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }

    @Override
    public void execute(@NotNull Player player, @NotNull PlayerData playerData, @NotNull String[] args) throws Exception {
        if (locationManager.getDistanceBetweenCoords(player, "uranmine") > 5 && locationManager.getDistanceBetweenCoords(player, "atomkraftwerk") > 5) {
            player.sendMessage(Component.text(Prefix.ERROR + "Du bist nicht in der nähe der Uranmine."));
            return;
        }
        if (playerData.getVariable("job") != null) {
            if (playerData.getVariable("job").equals("Urantransport")) {
                removeEquip(player);
                playerData.setVariable("job", null);
                player.sendMessage(Component.text(Prefix.MAIN + "Du hast den Job beendet."));
                return;
            }
            player.sendMessage(Component.text(Prefix.ERROR + "Du hast bereits einen Job angenommen."));
            return;
        }
        if (args.length >= 1 && args[0].equalsIgnoreCase("drop")) {
            drop(player);
            return;
        }
        playerData.setVariable("job", "Urantransport");
        player.sendMessage(Component.text(Prefix.MAIN + "Finde die Uranquelle (Smaragderz) und baue Sie ab. Bringe diese anschließend zum Atomkraftwerk"));
        equip(player);
        checkForRollout();
    }

    private void rollOutMine() {
        Location randomLocation = rollOutLocations.get(Main.random(0, rollOutLocations.size()));
        randomLocation.getBlock().setType(Material.EMERALD_ORE);
    }

    private void checkForRollout() {
        for (Location location : rollOutLocations) {
            if (location.getBlock().getType().equals(Material.EMERALD_ORE)) return;
        }
        rollOutMine();
    }

    private void equip(Player player) {
        ItemStack item = new ItemStack(Material.IRON_PICKAXE);
        player.getInventory().addItem(item);
    }

    private void removeEquip(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null) continue;
            if (item.getType() != Material.IRON_PICKAXE) continue;
            player.getInventory().removeItem(item);
        }
    }

    private void drop(Player player) {
        PlayerData playerData = Main.getInstance().playerManager.getPlayerData(player);
        if (playerData == null) return;
        if (locationManager.getDistanceBetweenCoords(player, "atomkraftwerk") > 10) {
            player.sendMessage(Prefix.ERROR + "Du bist nicht in der nähe des Atomkraftwerks.");
            return;
        }
        if (ItemManager.getCustomItemCount(player, RoleplayItem.URAN) < 1) {
            player.sendMessage(Component.text(Prefix.ERROR + "Du hast kein Uran dabei."));
            return;
        }
        ItemManager.removeCustomItem(player, RoleplayItem.URAN, 1);
        playerData.setVariable("job", null);
        playerData.addMoney(ServerManager.getPayout("uran"), "Urantransport");
        playerManager.addExp(player, Main.random(12, 20));
        player.sendMessage(Component.text("§8[§cAKW§8]§7 Danke für das Uran! §a+" + ServerManager.getPayout("uran")  + "$"));
    }

    private boolean isBlock(Block block) {
        for (Location location : rollOutLocations) {
            if (LocationManager.isLocationEqual(block.getLocation(), location)) return true;
        }
        return false;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = playerManager.getPlayerData(player);
        if (playerData == null) return;
        if (playerData.getVariable("job") == null || playerData.getVariable("job") != "Urantransport")return;

        if (ItemManager.getCustomItemCount(player, RoleplayItem.URAN) >= 1) {
            player.sendMessage(Component.text(Prefix.ERROR + "Du hast bereits ein Uran dabei."));
            return;
        }
        if (!isBlock(event.getBlock())) return;

        ItemManager.addCustomItem(player, RoleplayItem.URAN, 1);
        player.sendMessage(Component.text(Prefix.MAIN + "Du hast ein Uran abgebaut. Bringe es nun zum Atomkraftwerk"));
        utils.navigationManager.createNavi(player, "Atomkraftwerk", true);
        event.getBlock().setType(Material.STONE);
        removeEquip(player);
        rollOutMine();
    }

    @EventHandler
    public void onNaviReach(NaviReachEvent event) {
        Player player = event.getPlayer();
        if (!event.getNavi().equalsIgnoreCase("Atomkraftwerk")) return;
        drop(player);
    }
}
