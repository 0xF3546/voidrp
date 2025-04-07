package de.polo.voidroleplay.jobs.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.player.services.impl.PlayerManager;
import de.polo.voidroleplay.utils.Prefix;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static de.polo.voidroleplay.Main.navigationService;

public class RebstockCommand implements CommandExecutor {
    private final PlayerManager playerManager;

    public RebstockCommand(PlayerManager playerManager) {
        this.playerManager = playerManager;
        Main.registerCommand("rebstock", this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player);
        if (playerData.getVariable("job") == null) {
            player.sendMessage(Prefix.ERROR + "Du nicht den Winzer Job nicht angenommen.");
            return false;
        }
        if (!playerData.getVariable("job").toString().equalsIgnoreCase("Winzer")) {
            player.sendMessage(Prefix.ERROR + "Du nicht den Winzer Job nicht angenommen.");
            return false;
        }
        Block block = playerData.getVariable("grapevine");
        navigationService.createNaviByCord(player, block.getX(), block.getY(), block.getZ());
        player.sendMessage("§8[§5Winzer§8]§7 Dein Rebstock wurde markiert.");
        return false;
    }
}
