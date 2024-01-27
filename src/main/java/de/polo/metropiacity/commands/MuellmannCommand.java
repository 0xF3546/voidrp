package de.polo.metropiacity.commands;

import de.polo.metropiacity.dataStorage.PlayerData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.utils.InventoryManager.CustomItem;
import de.polo.metropiacity.utils.InventoryManager.InventoryManager;
import de.polo.metropiacity.utils.playerUtils.Scoreboard;
import de.polo.metropiacity.utils.playerUtils.SoundManager;
import de.polo.metropiacity.utils.ItemManager;
import de.polo.metropiacity.utils.LocationManager;
import de.polo.metropiacity.utils.PlayerManager;
import de.polo.metropiacity.utils.ServerManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MuellmannCommand implements CommandExecutor {
    private final List<Integer> array = new ArrayList<>();
    private final PlayerManager playerManager;
    private final LocationManager locationManager;
    public MuellmannCommand(PlayerManager playerManager, LocationManager locationManager) {
        this.playerManager = playerManager;
        this.locationManager = locationManager;
        Main.registerCommand("müllmann", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (ServerManager.canDoJobs()) {
            if (locationManager.getDistanceBetweenCoords(player, "muellmann") <= 5) {
                InventoryManager inventoryManager = new InventoryManager(player, 27, "§8 » §9Müllmann", true, true);
                if (!Main.getInstance().getCooldownManager().isOnCooldown(player, "müllmann") && playerData.getVariable("job") == null) {
                    inventoryManager.setItem(new CustomItem(11, ItemManager.createItem(Material.LIME_DYE, 1, 0, "§aMüllmann starten")) {
                        @Override
                        public void onClick(InventoryClickEvent event) {
                            startTransport(player);
                            player.closeInventory();
                        }
                    });
                } else {
                    if (playerData.getVariable("job") == null) {
                        inventoryManager.setItem(new CustomItem(11, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§a§mMüllmann starten", "§8 ➥§7 Warte noch " + Main.getTime(Main.getInstance().getCooldownManager().getRemainingTime(player, "müllmann")) + "§7.")) {
                            @Override
                            public void onClick(InventoryClickEvent event) {

                            }
                        });
                    } else {
                        inventoryManager.setItem(new CustomItem(11, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§a§mMüllmann starten", "§8 ➥§7 Du hast bereits den §f" + playerData.getVariable("job") + "§7 Job angenommen.")) {
                            @Override
                            public void onClick(InventoryClickEvent event) {

                            }
                        });
                    }
                }
                if (playerData.getVariable("job") == null) {
                    inventoryManager.setItem(new CustomItem(15, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§e§mJob beenden", "§8 ➥§7 Du hast den Job nicht angenommen")) {
                        @Override
                        public void onClick(InventoryClickEvent event) {

                        }
                    });
                } else {
                    if (!playerData.getVariable("job").equals("Müllmann")) {
                        inventoryManager.setItem(new CustomItem(15, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§e§mJob beenden", "§8 ➥§7 Du hast den Job nicht angenommen")) {
                            @Override
                            public void onClick(InventoryClickEvent event) {

                            }
                        });
                    } else {
                        inventoryManager.setItem(new CustomItem(15, ItemManager.createItem(Material.YELLOW_DYE, 1, 0, "§eJob beenden", "§8 ➥ §7Müllmann beenden")) {
                            @Override
                            public void onClick(InventoryClickEvent event) {
                                quitJob(player, false);
                                player.closeInventory();
                            }
                        });
                    }
                }
            } else {
                player.sendMessage(Main.error + "Du bist §cnicht§7 in der nähe der Mülldeponie§7!");
            }
        } else {
            player.sendMessage(ServerManager.error_cantDoJobs);
        }
        return false;
    }
    public boolean canGet(int number) {
        return !array.contains(number);
    }

    public void quitJob(Player player, boolean silent) {
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        playerData.setVariable("job", null);
        if (!silent) player.sendMessage("§8[§9Müllmann§8]§7 Vielen Dank für die geleistete Arbeit.");
        SoundManager.successSound(player);
        playerData.getScoreboard("müllmann").killScoreboard();
        Main.getInstance().getCooldownManager().setCooldown(player, "müllmann", 600);
        player.closeInventory();
    }

    public void startTransport(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        playerData.setIntVariable("muell", Main.random(2, 5));
        playerData.setIntVariable("muellkg", 0);
        playerData.setVariable("job", "Müllmann");
        Scoreboard scoreboard = new Scoreboard(player);
        scoreboard.createMuellmannScoreboard();
        playerData.setScoreboard("müllmann", scoreboard);
        player.sendMessage("§8[§9Müllmann§8]§7 Entleere den Müll verschiedner Häuser.");
        player.sendMessage("§8 ➥ §7Nutze §8[§6Rechtsklick§8]§7 auf die Hausschilder.");
    }

    public void dropTransport(Player player, int house) {
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        player.sendMessage("§8[§9Müllmann§8]§7 Du den Müll von §6Haus " + house + "§7 entleert.");
        SoundManager.successSound(player);
        playerManager.addExp(player, Main.random(1, 3));
        playerData.setIntVariable("muell", playerData.getIntVariable("muell") - 1);
        playerData.setIntVariable("muellkg", playerData.getIntVariable("muellkg") + Main.random(1, 4));
        playerData.getScoreboard("müllmann").updateMuellmannScoreboard();
        if (playerData.getIntVariable("muell") <= 0) {
            int payout = Main.random(ServerManager.getPayout("muellmann"), ServerManager.getPayout("muellmann2")) * playerData.getIntVariable("muellkg");
            player.sendMessage("§8[§9Müllmann§8]§7 Du hast alles eingesammelt. Danke! §a+" + payout + "$");
            playerData.setVariable("job", null);
            quitJob(player, true);
            playerData.getScoreboard("müllmann").killScoreboard();
            try {
                playerManager.addBankMoney(player, payout, "Auszahlung Müllmann");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        array.add(house);
        player.closeInventory();
        Main.waitSeconds(1800, () -> array.removeIf(number -> number == house));
    }
}
