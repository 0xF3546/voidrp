package de.polo.voidroleplay.admin.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.player.services.impl.PlayerManager;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.utils.TeamSpeak;
import de.polo.voidroleplay.utils.Utils;
import lombok.SneakyThrows;
import org.bukkit.OfflinePlayer;
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
public class
KickSecondaryTeam implements CommandExecutor {
    private final PlayerManager playerManager;

    public KickSecondaryTeam(PlayerManager playerManager) {
        this.playerManager = playerManager;

        Main.registerCommand("kicksecondaryteam", this);
    }

    @SneakyThrows
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player);
        if (playerData.getPermlevel() < 90) {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
            return false;
        }
        if (args.length < 1) {
            player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /kicksecondaryteam [Spieler]");
            return false;
        }
        OfflinePlayer offlinePlayer = Utils.getOfflinePlayer(args[0]);
        if (offlinePlayer == null) {
            player.sendMessage(Prefix.ERROR + "Spieler wurde nicht gefunden.");
            return false;
        }
        if (offlinePlayer.isOnline()) {
            PlayerData target = playerManager.getPlayerData(offlinePlayer.getUniqueId());
            target.setSecondaryTeam(null);
        }
        player.sendMessage(Prefix.MAIN + "Du hast " + offlinePlayer.getName() + " aus seinem Sub-Team gekickt.");
        Main.getInstance().getCoreDatabase().updateAsync("UPDATE players SET secondaryTeam = NULL WHERE uuid = ?", offlinePlayer.getUniqueId().toString());
        TeamSpeak.reloadPlayer(offlinePlayer.getUniqueId());
        return false;
    }
}
