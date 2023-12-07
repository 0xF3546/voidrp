package de.polo.metropiacity.commands;

import de.polo.metropiacity.dataStorage.PlayerData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.playerUtils.Scoreboard;
import de.polo.metropiacity.playerUtils.SoundManager;
import de.polo.metropiacity.utils.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PostboteCommand implements CommandExecutor {
    private final List<Integer> array = new ArrayList<>();
    private final PlayerManager playerManager;
    private final LocationManager locationManager;
    public PostboteCommand(PlayerManager playerManager, LocationManager locationManager) {
        this.playerManager = playerManager;
        this.locationManager = locationManager;
        Main.registerCommand("postbote", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (ServerManager.canDoJobs()) {
            if (locationManager.getDistanceBetweenCoords(player, "postbote") <= 5) {
                playerData.setVariable("current_inventory", "postbote");
                Inventory inv = Bukkit.createInventory(player, 27, "§8 » §ePostbote");
                if (!Main.getInstance().getCooldownManager().isOnCooldown(player, "postbote") && playerData.getVariable("job") == null) {
                    inv.setItem(11, ItemManager.createItem(Material.LIME_DYE, 1, 0, "§aPostbote starten", null));
                } else {
                    if (playerData.getVariable("job") == null) {
                        inv.setItem(11, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§a§mPostbote starten", "§8 ➥§7 Warte noch " + Main.getTime(Main.getInstance().getCooldownManager().getRemainingTime(player, "postbote")) + "§7."));
                    } else {
                        inv.setItem(11, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§a§mJob starten", "§8 ➥§7 Du hast bereits den §f" + playerData.getVariable("job") + "§7 Job angenommen."));
                    }
                }
                if (playerData.getVariable("job") == null) {
                    inv.setItem(15, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§e§mJob beenden", "§8 ➥§7 Du hast den Job nicht angenommen"));
                } else {
                    if (!playerData.getVariable("job").equals("Postbote")) {
                        inv.setItem(15, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§e§mJob beenden", "§8 ➥§7 Du hast den Job nicht angenommen"));
                    } else {
                        inv.setItem(15, ItemManager.createItem(Material.YELLOW_DYE, 1, 0, "§eJob beenden", "§8 ➥ §7Postbote beenden"));
                    }
                }
                for (int i = 0; i < 27; i++) {
                    if (inv.getItem(i) == null) {
                        inv.setItem(i, ItemManager.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, 0, "§8", null));
                    }
                }
                player.openInventory(inv);
            } else {
                player.sendMessage(Main.error + "Du bist §cnicht§7 in der nähe des Nachrichtengebäudes§7!");
            }
        } else {
            player.sendMessage(ServerManager.error_cantDoJobs);
        }
        return false;
    }
    public boolean canGive(int number) {
        return !array.contains(number);
    }

    public void quitJob(Player player, boolean silent) {
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        playerData.setVariable("job", null);
        if (!silent) player.sendMessage("§8[§ePostbote§8]§7 Vielen Dank für die geleistete Arbeit.");
        SoundManager.successSound(player);
        playerData.getScoreboard("postbote").killScoreboard();
        Main.getInstance().getCooldownManager().setCooldown(player, "postbote", 600);
    }

    public void startTransport(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        playerData.setIntVariable("post", Main.random(2, 5));
        playerData.setVariable("job", "Postbote");
        Scoreboard scoreboard = new Scoreboard(player);
        scoreboard.createPostboteScoreboard();
        playerData.setScoreboard("postbote", scoreboard);
        player.sendMessage("§8[§ePostbote§8]§7 Bringe die Post zu verschiedenen Häusern.");
        player.sendMessage("§8 ➥ §7Nutze §8[§6Rechtsklick§8]§7 auf die Hausschilder.");
    }

    public void dropTransport(Player player, int house) {
            PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
            int payout = Main.random(ServerManager.getPayout("postbote"), ServerManager.getPayout("postbote2"));
            player.sendMessage("§8[§ePostbote§8]§7 Du hast Post bei §6Haus " + house + "§7 abgeliefert. §a+" + payout + "$");
            SoundManager.successSound(player);
            playerManager.addExp(player, Main.random(1, 3));
            playerData.setIntVariable("post", playerData.getIntVariable("post") - 1);
            playerData.getScoreboard("postbote").updatePostboteScoreboard();
            try {
                playerManager.addBankMoney(player, payout, "Auszahlung Postbote");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            if (playerData.getIntVariable("post") <= 0) {
                player.sendMessage("§8[§ePostbote§8]§7 Du hast alles abgegeben. Danke!");
                playerData.setVariable("job", null);
                quitJob(player, true);
                playerData.getScoreboard("postbote").killScoreboard();
            }
            array.add(house);
            Main.waitSeconds(1800, () -> array.removeIf(number -> number == house));
    }
}
