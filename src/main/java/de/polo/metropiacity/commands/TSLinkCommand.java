package de.polo.metropiacity.commands;

import com.github.theholywaffle.teamspeak3.api.exception.TS3ConnectionFailedException;
import com.github.theholywaffle.teamspeak3.api.wrapper.Client;
import de.polo.metropiacity.dataStorage.PlayerData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.utils.PlayerManager;
import de.polo.metropiacity.utils.TeamSpeak;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TSLinkCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    public TSLinkCommand(PlayerManager playerManager) {
        this.playerManager = playerManager;
        Main.registerCommand("tslink", this);
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        if (args.length >= 1) {
            PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
            if (playerData.getTeamSpeakUID() == null) {
                Client client = null;
                try {
                    client = TeamSpeak.getTeamSpeak().getAPI().getClientByUId(args[0]);
                } catch (TS3ConnectionFailedException e) {
                    player.sendMessage(Main.error + "Client konnte nicht auf dem TeamSpeak gefunden werden.");
                    return false;
                }
                if (client == null) {
                    player.sendMessage(Main.error + "Client konnte nicht auf dem TeamSpeak gefunden werden.");
                    return false;
                }
                String code = Main.generateRandomCode(12);
                TeamSpeak.verifyCodes.put(code, client);
                TeamSpeak.getTeamSpeak().getAPI().sendPrivateMessage(client.getId(), "Dein Bestätigungscode lautet: [b]" + code + "[/b]");
                TeamSpeak.getTeamSpeak().getAPI().sendPrivateMessage(client.getId(), "Nutze [b]/verify " + code + "[/b] um dich zu verifizieren.");
                player.sendMessage("§8[§3TeamSpeak§8]§b Dir wurde eine Nachricht im TS3 geschickt!");
            } else {
                player.sendMessage(Main.error + "Dein TeamSpeak ist bereits verifiziert.");
            }
        } else {
            player.sendMessage(Main.error + "Syntax-Fehler: /tslink [Eindeutige ID]");
        }
        return false;
    }
}
