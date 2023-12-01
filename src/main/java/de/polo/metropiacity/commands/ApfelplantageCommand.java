package de.polo.metropiacity.commands;

import de.polo.metropiacity.Main;
import de.polo.metropiacity.dataStorage.PlayerData;
import de.polo.metropiacity.utils.LocationManager;
import de.polo.metropiacity.utils.PlayerManager;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ApfelplantageCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final LocationManager locationManager;
    public ApfelplantageCommand(PlayerManager playerManager, LocationManager locationManager) {
        this.playerManager = playerManager;
        this.locationManager = locationManager;
        Main.registerCommand("apfelsammler", this);
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String prefix = "§8[§cApfelplantage§8] §7";
        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();
        PlayerData playerData = playerManager.getPlayerData(uuid);
        if (playerData.canInteract()) {
            if (playerData.getVariable("job") == null) {
                if (locationManager.getDistanceBetweenCoords(player, "apfelsammler") <= 5) {
                    playerData.setVariable("job", "apfelsammler");
                    player.sendMessage(prefix + "Du bist nun §cApfelsammler§7.");
                    player.sendMessage(prefix + "Pflücke nun die Äpfel von den Bäumen ab.");
                } else {
                    player.sendMessage(Main.error + "Du bist §cnicht§7 in der nähe der §cApfelplantage§7!");
                }
            } else {
                if (playerData.getVariable("job").equals("apfelsammler")) {
                    if (locationManager.getDistanceBetweenCoords(player, "apfelsammler") <= 5) {
                        player.sendMessage(prefix + "Du hast den Job Apfelsammler beendet.");
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

    public static void blockLeftClick(Player player, Block block) {
        //todo leftclickListener für Äpfel
    }

    public static void quitJob(Player player) {

    }
}
