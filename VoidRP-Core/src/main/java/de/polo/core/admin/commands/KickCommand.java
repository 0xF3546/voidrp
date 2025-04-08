package de.polo.core.admin.commands;

import de.polo.core.Main;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.utils.Prefix;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class KickCommand implements CommandExecutor, TabCompleter {
    private final PlayerManager playerManager;

    public KickCommand(PlayerManager playerManager) {
        this.playerManager = playerManager;
        Main.registerCommand("kick", this);
        Main.addTabCompleter("kick", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (playerData.getPermlevel() < 70) {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
            return false;
        }
        if (args.length < 1) {
            player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /kick [Spieler] [Grund]");
            return false;
        }
        Player targetplayer = Bukkit.getPlayer(args[0]);
        if (targetplayer == null && !targetplayer.isOnline()) {
            player.sendMessage(Prefix.ERROR + args[0] + " ist nicht online.");
            return false;
        }
        StringBuilder message = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            message.append(" ").append(args[i]);
        }
        playerManager.kickPlayer(targetplayer, String.valueOf(message));
        Bukkit.broadcastMessage("§c" + playerData.getRang() + " " + player.getName() + " hat " + targetplayer.getName() + " gekickt. Grund: " + message);
        return false;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 2) {
            return Collections.singletonList("[<Grund>]");
        }
        return null;
    }
}
