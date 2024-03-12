package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.utils.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class PluginCommand implements CommandExecutor {
    private PlayerManager playerManager;
    public PluginCommand(PlayerManager playerManager) {
        this.playerManager = playerManager;
        Main.registerCommand("plugins", this);
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (playerData.getPermlevel() >= 90) {
            player.sendMessage("§6Plugins§8:§7 " + Arrays.toString(Bukkit.getPluginManager().getPlugins()));
            return true;
        } else {
            player.sendMessage(Main.error_nopermission);
        }
        return false;
    }
}
