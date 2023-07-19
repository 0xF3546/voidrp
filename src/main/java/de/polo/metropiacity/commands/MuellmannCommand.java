package de.polo.metropiacity.commands;

import de.polo.metropiacity.dataStorage.PlayerData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.playerUtils.SoundManager;
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
import org.bukkit.inventory.Inventory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MuellmannCommand implements CommandExecutor {
    public static final List<Integer> array = new ArrayList<>();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        String uuid = player.getUniqueId().toString();
        PlayerData playerData = PlayerManager.playerDataMap.get(uuid);
        if (ServerManager.canDoJobs()) {
            if (LocationManager.getDistanceBetweenCoords(player, "muellmann") <= 5) {
                playerData.setVariable("current_inventory", "müllmann");
                Inventory inv = Bukkit.createInventory(player, 27, "§8 » §9Müllmann");
                if (!Main.cooldownManager.isOnCooldown(player, "müllmann") && playerData.getVariable("job") == null) {
                    inv.setItem(11, ItemManager.createItem(Material.LIME_DYE, 1, 0, "§aMüllmann starten", null));
                } else {
                    if (playerData.getVariable("job") == null) {
                        inv.setItem(11, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§a§mMüllmann starten", "§8 ➥§7 Warte noch " + Main.getTime(Main.cooldownManager.getRemainingTime(player, "müllmann")) + "§7."));
                    } else {
                        inv.setItem(11, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§a§mMüllmann starten", "§8 ➥§7 Du hast bereits den §f" + playerData.getVariable("job") + "§7 Job angenommen."));
                    }
                }
                if (playerData.getVariable("job") == null) {
                    inv.setItem(15, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§e§mJob beenden", "§8 ➥§7 Du hast den Job nicht angenommen"));
                } else {
                    if (!playerData.getVariable("job").equals("Müllmann")) {
                        inv.setItem(15, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§e§mJob beenden", "§8 ➥§7 Du hast den Job nicht angenommen"));
                    } else {
                        inv.setItem(15, ItemManager.createItem(Material.YELLOW_DYE, 1, 0, "§eJob beenden", "§8 ➥ §7Müllmann beenden"));
                    }
                }
                for (int i = 0; i < 27; i++) {
                    if (inv.getItem(i) == null) {
                        inv.setItem(i, ItemManager.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, 0, "§8", null));
                    }
                }
                player.openInventory(inv);
            } else {
                player.sendMessage(Main.error + "Du bist §cnicht§7 in der nähe der Mülldeponie§7!");
            }
        } else {
            player.sendMessage(ServerManager.error_cantDoJobs);
        }
        return false;
    }
    public static boolean canGet(int number) {
        return !array.contains(number);
    }

    public static void quitJob(Player player, boolean silent) {
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        playerData.setVariable("job", null);
        if (!silent) player.sendMessage("§8[§9Müllmann§8]§7 Vielen Dank für die geleistete Arbeit.");
        SoundManager.successSound(player);
        playerData.getScoreboard().killScoreboard();
        Main.cooldownManager.setCooldown(player, "müllmann", 600);
    }

    public static void startTransport(Player player) {
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        playerData.setIntVariable("muell", Main.random(2, 5));
        playerData.setIntVariable("muellkg", 0);
        playerData.setVariable("job", "Müllmann");
        playerData.getScoreboard().createMuellmannScoreboard();
        player.sendMessage("§8[§9Müllmann§8]§7 Entleere den Müll verschiedner Häuser.");
        player.sendMessage("§8 ➥ §7Nutze §8[§6Rechtsklick§8]§7 auf die Hausschilder.");
    }

    public static void dropTransport(Player player, int house) {
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        player.sendMessage("§8[§9Müllmann§8]§7 Du den Müll von §6Haus " + house + "§7 entleert.");
        SoundManager.successSound(player);
        PlayerManager.addExp(player, Main.random(1, 3));
        playerData.setIntVariable("muell", playerData.getIntVariable("muell") - 1);
        playerData.setIntVariable("muellkg", playerData.getIntVariable("muellkg") + Main.random(1, 4));
        playerData.getScoreboard().updateMuellmannScoreboard();
        if (playerData.getIntVariable("muell") <= 0) {
            int payout = Main.random(ServerManager.getPayout("muellmann"), ServerManager.getPayout("muellmann2")) * playerData.getIntVariable("muellkg");
            player.sendMessage("§8[§9Müllmann§8]§7 Du hast alles eingesammelt. Danke! §a+" + payout + "$");
            playerData.setVariable("job", null);
            quitJob(player, true);
            playerData.getScoreboard().killScoreboard();
            try {
                PlayerManager.addBankMoney(player, payout, "Auszahlung Müllmann");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        array.add(house);
        Main.waitSeconds(1800, () -> array.removeIf(number -> number == house));
    }
}
