package de.polo.metropiacity.commands;

import de.polo.metropiacity.Main;
import de.polo.metropiacity.dataStorage.PlayerData;
import de.polo.metropiacity.utils.PlayerManager;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GMCommand implements CommandExecutor {
    private PlayerManager playerManager;
    public GMCommand(PlayerManager playerManager) {
        this.playerManager = playerManager;
        Main.registerCommand("gm", this);
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (playerData.getPermlevel() < 100) {
            player.sendMessage(Main.error_nopermission);
            return false;
        }
        if (args.length == 0) {
            player.sendMessage(Main.error + "Syntax-Fehler: /gm [0/1/2/3]");
            return false;
        }
        switch (Integer.parseInt(args[0])) {
            case 0:
                player.setGameMode(GameMode.SURVIVAL);
                break;
            case 1:
                player.setGameMode(GameMode.CREATIVE);
                break;
            case 2:
                player.setGameMode(GameMode.ADVENTURE);
                break;
            case 3:
                player.setGameMode(GameMode.SPECTATOR);
                break;
        }
        return false;
    }
}
