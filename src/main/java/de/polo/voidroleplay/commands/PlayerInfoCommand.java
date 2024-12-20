package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.storage.FactionData;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.manager.FactionManager;
import de.polo.voidroleplay.manager.PlayerManager;
import de.polo.voidroleplay.utils.Prefix;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PlayerInfoCommand implements CommandExecutor {
    private final FactionManager factionManager;
    private final PlayerManager playerManager;

    public PlayerInfoCommand(PlayerManager playerManager, FactionManager factionManager) {
        this.playerManager = playerManager;
        this.factionManager = factionManager;
        Main.registerCommand("playerinfo", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        if (args.length > 0) {
            Player targetplayer = Bukkit.getPlayer(args[0]);
            if (targetplayer != null) {
                PlayerData targetplayerdata = playerManager.getPlayerData(targetplayer.getUniqueId());

                if (playerManager.getPlayerData(player.getUniqueId()).isAduty()) {
                    player.sendMessage("§4§lSpielerinformation§8:");
                    player.sendMessage("§8 ➥ §6Name§8: §7" + targetplayer.getName());
                    player.sendMessage("§8 ➥ §6Visum§8: §7" + playerManager.visum(targetplayer));
                    player.sendMessage("§8 ➥ §6Level§8: §7" + targetplayerdata.getLevel() + " (" + targetplayerdata.getExp() + "§8/§7" + targetplayerdata.getNeeded_exp() + ")");
                    player.sendMessage("§8 ➥ §6Bank§8: §7" + playerManager.bank(targetplayer) + "$ (" + playerManager.paydayDuration(targetplayer) + "/60)");
                    player.sendMessage("§8 ➥ §6Spielzeit§8: §7" + targetplayerdata.getHours() + " Stunden & " + targetplayerdata.getMinutes() + " Minuten");
                    player.sendMessage("§8 ➥ §6Bargeld§8: §7" + playerManager.money(targetplayer) + "$");
                    player.sendMessage("§8 ➥ §6Rang§8: §7" + playerManager.rang(targetplayer));
                    player.sendMessage("§8 ➥ §6Vorname§8: §7" + playerManager.firstname(targetplayer));
                    player.sendMessage("§8 ➥ §6Nachname§8: §7" + playerManager.lastname(targetplayer));
                    player.sendMessage("§8 ➥ §6Fraktion§8: §7" + factionManager.faction(targetplayer) + " (" + factionManager.faction_grade(targetplayer) + "/6)");
                    if (!targetplayerdata.isJailed()) {
                        player.sendMessage("§8 ➥ §6Gefängnis§8: §7Nein");
                    } else {
                        player.sendMessage("§8 ➥ §6Gefängnis§8: §7Ja");
                    }
                    player.sendMessage("§8 ➥ §6Hausslots§8: §7" + targetplayerdata.getHouseSlot());
                } else {
                    if (targetplayerdata.isDead()) {
                        player.sendMessage("§8 » §7" + targetplayer.getName() + " | Bewusstlos");
                        return false;
                    }
                    if (targetplayerdata.isJailed()) {
                        player.sendMessage("§8 » §7" + targetplayer.getName() + " | Im Gefängnis");
                        return false;
                    }
                    String faction = "Zivilist";
                    if (targetplayerdata.getFaction() != null) {
                        FactionData factionData = factionManager.getFactionData(targetplayerdata.getFaction());
                        faction = factionData.getFullname();
                    }
                    if (targetplayerdata.isAduty()) {
                        player.sendMessage("§8 » §7" + targetplayer.getName() + "§8 | §7Level: " + targetplayerdata.getLevel() + "§8 | §7Fraktion: " + faction + " (Rang " + targetplayerdata.getFactionGrade() + ")§8 | §7Ping: " + targetplayer.getPing() + "ms §8 | §7§oAdmindienst");
                    } else {
                        player.sendMessage("§8 » §7" + targetplayer.getName() + "§8 | §7Level: " + targetplayerdata.getLevel() + "§8 | §7Fraktion: " + faction + " (Rang " + targetplayerdata.getFactionGrade() + ")§8 | §7Ping: " + targetplayer.getPing() + "ms");
                    }
                }
            } else {
                player.sendMessage(Prefix.ERROR + "Spieler nicht gefunden.");
            }
        } else {
            player.sendMessage(Prefix.ADMIN_ERROR + "Syntax-Fehler: /playerinfo [Spieler]");
        }
        return false;
    }
}
