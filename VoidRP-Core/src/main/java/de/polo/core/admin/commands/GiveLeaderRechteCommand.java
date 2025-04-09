package de.polo.core.admin.commands;

import de.polo.api.VoidAPI;
import de.polo.api.player.VoidPlayer;
import de.polo.core.Main;
import de.polo.core.admin.services.AdminService;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.faction.service.impl.FactionManager;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.utils.Prefix;
import de.polo.core.utils.TeamSpeak;
import de.polo.core.utils.Utils;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.Color;
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
public class GiveLeaderRechteCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final FactionManager factionManager;

    public GiveLeaderRechteCommand(PlayerManager playerManager, FactionManager factionManager) {
        this.playerManager = playerManager;
        this.factionManager = factionManager;

        Main.registerCommand("giveleaderrechte", this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player);
        if (playerData.getPermlevel() < 90) {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
            return false;
        }
        VoidPlayer voidPlayer = VoidAPI.getPlayer(player);
        if (!voidPlayer.isAduty()) {
            player.sendMessage(Prefix.ERROR + "Du bist nicht im Admindienst.");
            return false;
        }
        if (args.length < 1) {
            player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /giveleaderrechte [Spieler]");
            return false;
        }
        OfflinePlayer offlinePlayer = Utils.getOfflinePlayer(args[0]);
        if (offlinePlayer == null) {
            player.sendMessage(Prefix.ERROR + "Spieler wurde nicht gefunden.");
            return false;
        }
        factionManager.setLeader(offlinePlayer, true);
        AdminService adminService = VoidAPI.getService(AdminService.class);
        adminService.send_message(player.getName() + " hat " + offlinePlayer.getName() + " Leaderrechte gegeben.", Color.PURPLE);
        if (offlinePlayer.isOnline() && offlinePlayer.getPlayer() != null) {
            offlinePlayer.getPlayer().sendMessage(Component.text("§6" + player.getName() + " hat dir Leaderrechte gegeben!"));
        }
        TeamSpeak.reloadPlayer(offlinePlayer.getUniqueId());
        return false;
    }
}
