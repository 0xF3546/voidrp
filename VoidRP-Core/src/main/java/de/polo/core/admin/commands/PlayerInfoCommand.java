package de.polo.core.admin.commands;

import de.polo.core.Main;
import de.polo.core.faction.entity.Faction;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.faction.service.impl.FactionManager;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.utils.Prefix;
import de.polo.core.utils.player.PlayerInventoryItem;
import net.kyori.adventure.text.Component;
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
                    player.sendMessage(Component.text("§8 ➥ §6Inventar§8:§7 " + targetplayerdata.getInventory().getWeight() + "/" + targetplayerdata.getInventory().getSize() + ":"));
                    for (PlayerInventoryItem item : targetplayerdata.getInventory().getItems()) {
                        player.sendMessage(Component.text("§8  » §e" + item.getItem().getClearName() + "§8:§7 " + item.getAmount()));
                    }
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
                        Faction factionData = factionManager.getFactionData(targetplayerdata.getFaction());
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
            player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /playerinfo [Spieler]");
        }
        return false;
    }
}
