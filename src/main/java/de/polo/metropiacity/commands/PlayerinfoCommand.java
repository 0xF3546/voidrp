package de.polo.metropiacity.commands;

import de.polo.metropiacity.DataStorage.FactionData;
import de.polo.metropiacity.DataStorage.PlayerData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.Utils.FactionManager;
import de.polo.metropiacity.Utils.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PlayerinfoCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        if (args.length > 0) {
                Player targetplayer = Bukkit.getPlayer(args[0]);
            if (targetplayer != null) {
                PlayerData targetplayerdata = PlayerManager.playerDataMap.get(targetplayer.getUniqueId().toString());

                if (PlayerManager.playerDataMap.get(player.getUniqueId().toString()).isAduty()) {
                    player.sendMessage("§4§lSpielerinformation§8:");
                    player.sendMessage("§8 ➥ §6Name§8: §7" + targetplayer.getName());
                    player.sendMessage("§8 ➥ §6Visum§8: §7" + PlayerManager.visum(targetplayer));
                    player.sendMessage("§8 ➥ §6Level§8: §7" + targetplayerdata.getLevel() + " (" + targetplayerdata.getExp() + "§8/§7" + targetplayerdata.getNeeded_exp() + ")");
                    player.sendMessage("§8 ➥ §6Bank§8: §7" + PlayerManager.bank(targetplayer) + "$ (" + PlayerManager.paydayDuration(targetplayer) + "/60)");
                    player.sendMessage("§8 ➥ §6Spielzeit§8: §7" + targetplayerdata.getHours() + " Stunden & " + targetplayerdata.getMinutes() + " Minuten");
                    player.sendMessage("§8 ➥ §6Bargeld§8: §7" + PlayerManager.money(targetplayer) + "$");
                    player.sendMessage("§8 ➥ §6Rang§8: §7" + PlayerManager.rang(targetplayer));
                    player.sendMessage("§8 ➥ §6Vorname§8: §7" + PlayerManager.firstname(targetplayer));
                    player.sendMessage("§8 ➥ §6Nachname§8: §7" + PlayerManager.lastname(targetplayer));
                    player.sendMessage("§8 ➥ §6Fraktion§8: §7" + FactionManager.faction(targetplayer) + " (" + FactionManager.faction_grade(targetplayer) + "/8)");
                    if (!targetplayerdata.isJailed()) {
                        player.sendMessage("§8 ➥ §6Gefängnis§8: §7Nein");
                    } else {
                        player.sendMessage("§8 ➥ §6Gefängnis§8: §7Ja");
                    }
                    player.sendMessage("§8 ➥ §6Hausslots§8: §7" + targetplayerdata.getHouseSlot());
                } else {
                    String faction = "Zivilist";
                    if (targetplayerdata.getFaction() != null) {
                        FactionData factionData = FactionManager.factionDataMap.get(targetplayerdata.getFaction());
                        faction = factionData.getFullname();
                    }
                    if (targetplayerdata.isAduty()) {
                        player.sendMessage("§8 » §7" + targetplayer.getName() + "§8 | §7Nummer: " + targetplayerdata.getId() + "§8 | §7Fraktion: " + faction + "§8 | §7Ping: " + targetplayer.getPing() + "ms §8 | §7§oAdmindienst");
                    } else {
                        player.sendMessage("§8 » §7" + targetplayer.getName() + "§8 | §7Nummer: " + targetplayerdata.getId() + "§8 | §7Fraktion: " + faction + "§8 | §7Ping: " + targetplayer.getPing() + "ms");
                    }
                }
            } else {
                player.sendMessage(Main.error + "Spieler nicht gefunden.");
            }
        } else {
                player.sendMessage(Main.admin_error + "Syntax-Fehler: /playerinfo [Spieler]");
            }
        return false;
    }
}
