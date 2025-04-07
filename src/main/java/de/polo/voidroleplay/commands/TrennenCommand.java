package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.player.services.impl.PlayerManager;
import de.polo.voidroleplay.utils.Prefix;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TrennenCommand implements CommandExecutor {
    private final PlayerManager playerManager;

    public TrennenCommand(PlayerManager playerManager) {
        this.playerManager = playerManager;
        Main.registerCommand("trennen", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (!playerData.getRelationShip().isEmpty()) {
            for (Map.Entry<String, String> entry : playerData.getRelationShip().entrySet()) {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(entry.getKey()));
                if (offlinePlayer.isOnline()) {
                    Player targetplayer = Bukkit.getPlayer(offlinePlayer.getName());
                    targetplayer.sendMessage("§c" + player.getName() + " hat sich von dir getrennt...");
                    PlayerData targetplayerData = playerManager.getPlayerData(targetplayer.getUniqueId());
                    targetplayerData.setRelationShip(new HashMap<>());
                }
                playerData.setRelationShip(new HashMap<>());
                player.sendMessage("§cDu hast dich von " + offlinePlayer.getName() + " getrennt...");
                Main.getInstance().getCoreDatabase().updateAsync("UPDATE players SET relationShip = '{}' WHERE uuid = ?", player.getUniqueId().toString());
                Main.getInstance().getCoreDatabase().updateAsync("UPDATE players SET relationShip = '{}' WHERE uuid = ?", offlinePlayer.getUniqueId().toString());

            }
        } else {
            player.sendMessage(Prefix.ERROR + "Du bist in keiner Beziehung.");
        }
        return false;
    }
}
