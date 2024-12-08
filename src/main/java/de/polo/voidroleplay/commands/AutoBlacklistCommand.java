package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.storage.FactionData;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.game.faction.blacklist.BlacklistReason;
import de.polo.voidroleplay.manager.FactionManager;
import de.polo.voidroleplay.manager.PlayerManager;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.utils.Utils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class AutoBlacklistCommand implements CommandExecutor, TabCompleter {
    private final PlayerManager playerManager;
    private final FactionManager factionManager;

    public AutoBlacklistCommand(PlayerManager playerManager, FactionManager factionManager) {
        this.playerManager = playerManager;
        this.factionManager = factionManager;

        Main.registerCommand("autoblacklist", this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player);
        if (playerData.getFaction() == null) {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
            return false;
        }
        FactionData factionData = factionManager.getFactionData(playerData.getFaction());
        if (!factionData.hasBlacklist()) {
            player.sendMessage(Prefix.ERROR + "Deine Fraktion hat keine Blacklist.");
            return false;
        }
        if (playerData.getFactionGrade() < 3) {
            player.sendMessage(Prefix.ERROR + "Das geht erst ab Rang 3!");
            return false;
        }
        if (args.length < 2) {
            player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /ablacklist [Spieler] [Grund]");
            return false;
        }
        OfflinePlayer offlinePlayer = Utils.getOfflinePlayer(args[0]);
        if (offlinePlayer == null) {
            player.sendMessage(Prefix.ERROR + "Der Spieler wurde nicht gefunden.");
            return false;
        }
        StringBuilder reason = new StringBuilder(args[1]);
        for (int i = 2; i < args.length; i++) {
            reason.append(" ").append(args[i]);
        }
        BlacklistReason blacklistReason = null;
        for (BlacklistReason blReason : factionData.getBlacklistReasons()) {
            if (blReason.getReason().replace(" ", "").equalsIgnoreCase(reason.toString().replace(" ", ""))) {
                blacklistReason = blReason;
            }
        }
        if (blacklistReason == null) {
            player.sendMessage(Prefix.ERROR + "Der Blacklistgrund wurde nicht gefunden.");
            return false;
        }
        player.performCommand("blacklist add " + offlinePlayer.getName() + " " + blacklistReason.getKills() + " " + blacklistReason.getPrice() + " " + blacklistReason.getReason());
        return false;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 2) {
            List<String> reasons = new ObjectArrayList<>();
            Player player = (Player) sender;
            PlayerData playerData = playerManager.getPlayerData(player);
            if (playerData.getFaction() == null) return new ObjectArrayList<>();
            FactionData factionData = factionManager.getFactionData(playerData.getFaction());
            for (BlacklistReason reason : factionData.getBlacklistReasons()) {
                reasons.add(reason.getReason());
            }
            return reasons;
        }
        return null;
    }
}
