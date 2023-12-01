package de.polo.metropiacity.commands;

import de.polo.metropiacity.dataStorage.PlayerData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.utils.PlayerManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DropCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final Main.Commands commands;
    public DropCommand(PlayerManager playerManager, Main.Commands commands) {
        this.playerManager = playerManager;
        this.commands = commands;
        Main.registerCommand("drop", this);
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (playerData.getVariable("job") != null) {
            switch (playerData.getVariable("job").toString()) {
                case "lieferant":
                    commands.lebensmittelLieferantCommand.dropLieferung(player);
                    break;
                case "weizenlieferant":
                    commands.farmerCommand.dropTransport(player);
                    break;
            }
        } else {
            player.sendMessage(Main.error + "Du hast keinen Job angenommen.");
        }
        return false;
    }
}
