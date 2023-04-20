package de.polo.void_roleplay.commands;

import de.polo.void_roleplay.DataStorage.PlayerData;
import de.polo.void_roleplay.Main;
import de.polo.void_roleplay.Utils.FactionManager;
import de.polo.void_roleplay.Utils.PlayerManager;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static org.bukkit.Bukkit.getServer;

public class playerinfoCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        if (PlayerManager.perms(player) >= 60) {
            if (args.length > 0) {
                Player targetplayer = getServer().getPlayer(args[0]);
                assert targetplayer != null;
                PlayerData targetplayerdata = PlayerManager.playerDataMap.get(targetplayer.getUniqueId().toString());
                player.sendMessage("§4§lSpielerinformation§8:");
                player.sendMessage("§8 ➥ §6Name§8: §7" + targetplayer.getName());
                player.sendMessage("§8 ➥ §6Visum§8: §7" + PlayerManager.visum(targetplayer));
                player.sendMessage("§8 ➥ §6Level§8: §7" + targetplayerdata.getLevel() + " (" + targetplayerdata.getExp() + "§8/§7" + targetplayerdata.getNeeded_exp() + ")");
                player.sendMessage("§8 ➥ §6Bank§8: §7" + PlayerManager.bank(targetplayer) + "$ (" + PlayerManager.paydayDuration(player) + "/60)");
                player.sendMessage("§8 ➥ §6Bargeld§8: §7" + PlayerManager.money(targetplayer) + "$");
                player.sendMessage("§8 ➥ §6Rang§8: §7" + PlayerManager.rang(targetplayer));
                player.sendMessage("§8 ➥ §6Vorname§8: §7" + PlayerManager.firstname(targetplayer));
                player.sendMessage("§8 ➥ §6Nachname§8: §7" + PlayerManager.lastname(targetplayer));
                player.sendMessage("§8 ➥ §6Fraktion§8: §7" + FactionManager.faction(targetplayer) + " (" + FactionManager.faction_grade(player) + "/8)");
                if (!targetplayerdata.isJailed()) {
                    player.sendMessage("§8 ➥ §6Gefängnis§8: §7Nein");
                } else {
                    player.sendMessage("§8 ➥ §6Gefängnis§8: §7Ja");
                }
            } else {
                player.sendMessage(Main.admin_error + "Syntax-Fehler: /playerinfo [Spieler]");
            }
        } else {
            player.sendMessage(Main.error_nopermission);
        }
        return false;
    }
}
