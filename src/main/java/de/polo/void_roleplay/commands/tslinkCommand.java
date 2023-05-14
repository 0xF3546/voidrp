package de.polo.void_roleplay.commands;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;
import de.polo.void_roleplay.DataStorage.FactionData;
import de.polo.void_roleplay.DataStorage.PlayerData;
import de.polo.void_roleplay.Main;
import de.polo.void_roleplay.Utils.FactionManager;
import de.polo.void_roleplay.Utils.PlayerManager;
import de.polo.void_roleplay.Utils.TeamSpeak;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class tslinkCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        if (args.length >= 1) {
            Client client = TeamSpeak.getAPI().getClientByUId(args[0]);
            TeamSpeak.getAPI().sendPrivateMessage(client.getId(), "Du wurdest verifiziert.");
            player.sendMessage("§8[§3TeamSpeak§8]§b Bitte bestätige deine Idendität.");
            PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
            if (playerData.getFaction() != null && !playerData.getFaction().equals("Zivilist")) {
                FactionData factionData = FactionManager.factionDataMap.get(playerData.getFaction());
                TeamSpeak.getAPI().addClientToServerGroup(client.getId(), factionData.getTeamSpeakID());
            }
        } else {
            player.sendMessage(Main.error + "Syntax-Fehler: /tslink [Eindeutige ID]");
        }
        return false;
    }
}
