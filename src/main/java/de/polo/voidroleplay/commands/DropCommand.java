package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.utils.PlayerManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DropCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    public DropCommand(PlayerManager playerManager) {
        this.playerManager = playerManager;
        Main.registerCommand("drop", this);
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (playerData.getVariable("job") != null) {
            switch (playerData.getVariable("job").toString()) {
                case "lieferant":
                    Main.getInstance().commands.lebensmittelLieferantCommand.dropLieferung(player);
                    break;
                case "weizenlieferant":
                    Main.getInstance().commands.farmerCommand.dropTransport(player);
                    break;
                case "pfeifentransport":
                    Main.getInstance().commands.pfeifenTransport.dropTransport(player);
                    break;
            }
        } else {
            player.sendMessage(Main.error + "Du hast keinen Job angenommen.");
        }
        return false;
    }
}
