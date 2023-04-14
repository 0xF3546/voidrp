package de.polo.void_roleplay.commands;

import de.polo.void_roleplay.Main;
import de.polo.void_roleplay.DataStorage.PlayerData;
import de.polo.void_roleplay.Utils.LocationManager;
import de.polo.void_roleplay.Utils.PlayerManager;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

public class lumberjackCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String prefix = "§2Holzfäller §8» §7";
        Player player = (Player) sender;
        String uuid = player.getUniqueId().toString();
        PlayerData playerData = PlayerManager.playerDataMap.get(uuid);
        if (playerData.canInteract()) {
            if (playerData.getVariable("job") == null) {
                if (LocationManager.getDistanceBetweenCoords(player, "lumberjack") <= 5) {
                    playerData.setVariable("job", "lumberjack");
                    player.sendMessage(prefix + "Du bist nun §2Holzfäller§7.");
                    player.sendMessage(prefix + "Baue nun Holzstämme ab.");
                } else {
                    player.sendMessage(Main.error + "Du bist §cnicht§7 in der nähe des §2Holzfäller§7 Jobs!");
                }
            } else {
                if (playerData.getVariable("job").equals("lumberjack")) {
                    if (LocationManager.getDistanceBetweenCoords(player, "lumberjack") <= 5) {
                        player.sendMessage(prefix + "Du hast den Job Holzfäller beendet.");
                        playerData.setVariable("job", null);
                        quitJob(player);
                    }
                } else {
                    player.sendMessage(Main.error + "Du übst bereits den Job " + playerData.getVariable("job") + " aus.");
                }
            }
        } else {
            player.sendMessage(Main.error_cantinteract);
        }
        return false;
    }

    public static void blockBroken(Player player, Block block, BlockBreakEvent event) {

    }

    public static void quitJob(Player player) {

    }
}
