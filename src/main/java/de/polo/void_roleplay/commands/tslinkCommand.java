package de.polo.void_roleplay.commands;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;
import com.github.theholywaffle.teamspeak3.api.wrapper.ServerGroup;
import de.polo.void_roleplay.DataStorage.FactionData;
import de.polo.void_roleplay.DataStorage.PlayerData;
import de.polo.void_roleplay.Main;
import de.polo.void_roleplay.Utils.FactionManager;
import de.polo.void_roleplay.Utils.PlayerManager;
import de.polo.void_roleplay.Utils.TeamSpeak;
import jdk.internal.net.http.hpack.NaiveHuffman;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class tslinkCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        if (args.length >= 1) {
            PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
            if (playerData.getTeamSpeakUID() == null) {
                Client client = TeamSpeak.getAPI().getClientByUId(args[0]);
                String code = Main.generateRandomCode(12);
                if (client != null) {
                    TeamSpeak.verifyCodes.put(code, client);
                    TeamSpeak.getAPI().sendPrivateMessage(client.getId(), "Dein Bestätigungscode lautet: [b]" + code + "[/b]");
                    TeamSpeak.getAPI().sendPrivateMessage(client.getId(), "Nutze [b]/verify " + code + "[/b] um dich zu verifizieren.");
                    player.sendMessage("§8[§3TeamSpeak§8]§b Dir wurde eine Nachricht im TS3 geschickt!");
                } else {
                    player.sendMessage(Main.error + "Die UID konnte nicht gefunden werden.");
                }
            } else {
                player.sendMessage(Main.error + "Dein TeamSpeak ist bereits verifiziert.");
            }
        } else {
            player.sendMessage(Main.error + "Syntax-Fehler: /tslink [Eindeutige ID]");
        }
        return false;
    }
}
