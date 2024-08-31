package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.utils.PlayerManager;
import de.polo.voidroleplay.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

public class SperrinfoCommand implements CommandExecutor {

    private  final PlayerManager playerManager;

    public SperrinfoCommand(PlayerManager playerManager) {
        this.playerManager = playerManager;

        Main.registerCommand("sperrinfo", this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }

        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player);

        Duration duration = Duration.between(Utils.getTime(), playerData.getFactionCooldown());
        long hours = duration.toHours();
        long minutes = duration.minusHours(hours).toMinutes();

        player.sendMessage("§7   ===§8[§cCooldowns§8]§7===");

        if (playerData.getFactionCooldown() != null) {
            if (hours < 6) {
                player.sendMessage("§8 ➥ §6Fraktions-sperre: §7" + hours + ":" + minutes);
            }
        }

        return true;
    }
}
