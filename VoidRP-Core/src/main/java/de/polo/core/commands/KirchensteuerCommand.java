package de.polo.core.commands;

import de.polo.core.Main;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.utils.Prefix;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class KirchensteuerCommand implements CommandExecutor {
    private final PlayerManager playerManager;

    public KirchensteuerCommand(PlayerManager playerManager) {
        this.playerManager = playerManager;

        Main.registerCommand("kirchensteuer", this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player);
        if (playerData.isChurch()) {
            if (playerData.getRelationShip().containsValue("verheiratet")) {
                player.sendMessage(Prefix.ERROR + "Du kannst nicht austreten, da du verheiratet bist.");
                return false;
            }
            playerData.setChurch(false);
            player.sendMessage("§8[§6Kirche§8]§7 Du bist aus der Kirche ausgetreten.");
            playerData.save();
            return false;
        }
        playerData.setChurch(true);
        player.sendMessage("§8[§6Kirche§8]§7 Du bist in die Kirche eingetreten.");
        playerData.save();
        return false;
    }
}
