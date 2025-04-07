package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.manager.LocationManager;
import de.polo.voidroleplay.player.services.impl.PlayerManager;
import de.polo.voidroleplay.utils.Prefix;
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

    public static void blockLeftClick(Player player, Block block) {
        //todo leftclickListener für Äpfel
    }

    public static void quitJob(Player player) {

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
                    player.sendMessage(Prefix.ERROR + "Du bist §cnicht§7 in der nähe der §cApfelplantage§7!");
                }
            } else {
                if (playerData.getVariable("job").equals("apfelsammler")) {
                    if (locationManager.getDistanceBetweenCoords(player, "apfelsammler") <= 5) {
                        player.sendMessage(prefix + "Du hast den Job Apfelsammler beendet.");
                        playerData.setVariable("job", null);
                        quitJob(player);
                    }
                } else {
                    player.sendMessage(Prefix.ERROR + "Du übst bereits den Job " + playerData.getVariable("job") + " aus.");
                }
            }
        } else {
            player.sendMessage(Prefix.error_cantinteract);
        }
        return false;
    }
}
