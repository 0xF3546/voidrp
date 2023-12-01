package de.polo.metropiacity.commands;

import de.polo.metropiacity.Main;
import de.polo.metropiacity.utils.PlayerManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GeldbeutelCommand implements CommandExecutor {
    private PlayerManager playerManager;
    public GeldbeutelCommand(PlayerManager playerManager) {
        this.playerManager = playerManager;
        Main.registerCommand("geldbeutel", this);
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        player.sendMessage("");
        player.sendMessage("§7     ===§8[§6GELDBEUTEL§8]§7===");
        player.sendMessage("§8 ➥ §eBargeld§8:§7 " + playerManager.money(player) + "§7$");
        player.sendMessage("§8 ➥ §ePKW-Lizenz§8: " );
        player.sendMessage("§8 ➥ §eWaffenschein§8: §7§lAnzeigen");
        player.sendMessage("§8 ➥ §eDienstausweis§8: §7§lAnzeigen");
        player.sendMessage("");
        return false;
    }
}
