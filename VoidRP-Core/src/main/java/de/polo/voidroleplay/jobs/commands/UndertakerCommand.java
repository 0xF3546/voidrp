package de.polo.voidroleplay.jobs.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.VoidAPI;
import de.polo.voidroleplay.jobs.enums.MiniJob;
import de.polo.voidroleplay.storage.Corpse;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.utils.inventory.CustomItem;
import de.polo.voidroleplay.utils.inventory.InventoryManager;
import de.polo.voidroleplay.manager.ItemManager;
import de.polo.voidroleplay.location.services.impl.LocationManager;
import de.polo.voidroleplay.player.services.impl.PlayerManager;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.utils.Utils;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import static de.polo.voidroleplay.Main.navigationService;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class UndertakerCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final LocationManager locationManager;

    public UndertakerCommand(PlayerManager playerManager, LocationManager locationManager) {
        this.playerManager = playerManager;
        this.locationManager = locationManager;

        Main.registerCommand("undertaker", this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player);
        if (locationManager.getDistanceBetweenCoords(player, "undertaker") > 5) {
            player.sendMessage(Prefix.ERROR + "Du bist nicht in der nähe des Bestatters.");
            return false;
        }
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8 » §7Bestatter");
        if (playerData.getVariable("job") != null) {
            if (playerData.getVariable("job") == "corpse") {
                boolean state = playerData.getVariable("job::corpse::pickedup");
                if (state) {
                    inventoryManager.setItem(new CustomItem(13, ItemManager.createItem(Material.EMERALD, 1, 0, "§aAbgeben")) {
                        @Override
                        public void onClick(InventoryClickEvent event) {
                            player.closeInventory();
                            handlePayout(player);
                            playerData.setVariable("job", null);
                            playerData.setVariable("job::corpse", null);
                        }
                    });
                } else {
                    inventoryManager.setItem(new CustomItem(13, ItemManager.createItem(Material.WITHER_SKELETON_SKULL, 1, 0, "§7Route anzeigen")) {
                        @Override
                        public void onClick(InventoryClickEvent event) {
                            player.closeInventory();
                            showRoute(player, playerData.getVariable("job::corpse"));
                        }
                    });
                }
            } else {
                inventoryManager.setItem(new CustomItem(13, ItemManager.createItem(Material.PAPER, 1, 0, "§cDu hast bereits einen Job angenommen!")) {
                    @Override
                    public void onClick(InventoryClickEvent event) {

                    }
                });
            }
        } else {
            int i = 0;
            for (Corpse corpse : Main.getInstance().utils.deathUtil.getCorbses()) {
                if (corpse.isJobActive()) continue;
                int price = (int) (corpse.getSkull().getLocation().distance(player.getLocation()) / 2);
                if (corpse.getPrice() == 0) corpse.setPrice(price);
                inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(Material.WITHER_SKELETON_SKULL, 1, 0, "§8Leiche", "§8 ➥ §a" + Utils.toDecimalFormat(price) + "$")) {
                    @Override
                    public void onClick(InventoryClickEvent event) {
                        player.closeInventory();
                        playerData.setVariable("job", "corpse");
                        playerData.setVariable("job::corpse", corpse);
                        playerData.setVariable("job::corpse::pickedup", false);
                        VoidAPI.getPlayer(player).setMiniJob(MiniJob.UNDERTAKER);
                        showRoute(player, corpse);
                        player.sendMessage(Prefix.MAIN + "Die Leiche wurde markiert, begib dich hin und sammel Sie auf (Shift + F).");
                    }
                });
                i++;
            }
        }
        return false;
    }

    private void showRoute(Player player, Corpse corpse) {
        navigationService.createNaviByCord(player, (int) corpse.getSkull().getLocation().getX(), (int) corpse.getSkull().getLocation().getY(), (int) corpse.getSkull().getLocation().getZ());
    }

    private void handlePayout(Player player) {
        VoidAPI.getPlayer(player).setMiniJob(null);
        PlayerData playerData = playerManager.getPlayerData(player);
        Corpse corpse = playerData.getVariable("job::corpse");
        playerData.addMoney(corpse.getPrice(), "Bestatter");
        player.sendMessage("§8[§7Bestatter§8]§7 Danke für deine Arbeit. Hier hast du §a" + Utils.toDecimalFormat(corpse.getPrice()) + "$§7!");
        Main.getInstance().utils.deathUtil.removeCorpse(corpse);
    }
}
