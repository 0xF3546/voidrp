package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.utils.Navigation;
import de.polo.voidroleplay.utils.PlayerManager;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class RebstockCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final Navigation navigation;
    public RebstockCommand(PlayerManager playerManager, Navigation navigation) {
        this.playerManager = playerManager;
        this.navigation = navigation;
        Main.registerCommand("rebstock", this);
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player);
        if (playerData.getVariable("job") == null) {
            player.sendMessage(Main.error + "Du nicht den Winzer Job nicht angenommen.");
            return false;
        }
        if (!playerData.getVariable("job").toString().equalsIgnoreCase("Winzer")) {
            player.sendMessage(Main.error + "Du nicht den Winzer Job nicht angenommen.");
            return false;
        }
        Block block = playerData.getVariable("grapevine");
        navigation.createNaviByCord(player, block.getX(), block.getY(), block.getZ());
        player.sendMessage("§8[§5Winzer§8]§7 Dein Rebstock wurde markiert.");
        return false;
    }
}
