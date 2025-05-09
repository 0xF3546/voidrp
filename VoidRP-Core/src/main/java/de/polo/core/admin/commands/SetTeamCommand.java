package de.polo.core.admin.commands;

import de.polo.api.VoidAPI;
import de.polo.api.player.VoidPlayer;
import de.polo.core.Main;
import de.polo.core.admin.services.AdminService;
import de.polo.core.handler.CommandBase;
import de.polo.core.manager.ServerManager;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.player.services.PlayerService;
import de.polo.core.utils.Prefix;
import de.polo.core.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@CommandBase.CommandMeta(name = "setgroup", permissionLevel = 100)
public class SetTeamCommand extends CommandBase {

    public SetTeamCommand(@NotNull CommandMeta meta) {
        super(meta);
    }

    @Override
    public void execute(@NotNull VoidPlayer player, @NotNull PlayerData playerData, @NotNull String[] args) throws Exception {
        if (playerData.getPermlevel() >= 100) {
            if (args.length == 2) {
                OfflinePlayer offlinePlayer = Utils.getOfflinePlayer(args[0]);
                if (offlinePlayer == null) {
                    player.sendMessage(Prefix.ERROR + "Spieler wurde nicht gefunden.");
                    return;
                }
                String rank = args[1];
                if (ServerManager.rankDataMap.get(rank) == null) {
                    player.sendMessage(Prefix.ERROR + "Rang nicht gefunden.");
                    return;
                }
                if (offlinePlayer.isOnline()) {
                    Player targetplayer = Bukkit.getPlayer(offlinePlayer.getUniqueId());
                    targetplayer.sendMessage(Prefix.ADMIN + "Du bist nun §c" + rank + "§7!");
                    targetplayer.sendMessage("§b   Info§8:§f Da du nun Teammitglied bist, hast du deine Spielerränge verloren.");
                }
                player.sendMessage(Prefix.ADMIN + offlinePlayer.getName() + " ist nun §c" + rank + "§7.");
                Main.getInstance().getCoreDatabase().updateAsync("UPDATE players SET rankDuration = null WHERE uuid = ?", offlinePlayer.getUniqueId().toString());
                playerData.setRankDuration(null);

                PlayerService playerService = VoidAPI.getService(PlayerService.class);
                AdminService adminService = VoidAPI.getService(AdminService.class);
                playerService.setRang(offlinePlayer.getUniqueId(), rank);
                adminService.sendMessage(player.getName() + " hat " + offlinePlayer.getName() + " den Rang " + rank + " gegeben.", Color.RED);
            } else {
                player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /setgroup [Spieler] [Rang]");
            }
        } else {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
        }
    }
}
