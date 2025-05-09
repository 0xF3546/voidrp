package de.polo.core.commands;

import de.polo.api.VoidAPI;
import de.polo.api.player.VoidPlayer;
import de.polo.core.handler.CommandBase;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.player.services.PlayerService;
import de.polo.core.utils.Prefix;
import de.polo.core.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@CommandBase.CommandMeta(name = "leaderchat", usage = "/leaderchat [Nachricht]")
public class LeaderChatCommand extends CommandBase {

    public LeaderChatCommand(@NotNull CommandMeta meta) {
        super(meta);
    }

    @Override
    public void execute(@NotNull VoidPlayer player, @NotNull PlayerData playerData, @NotNull String[] args) throws Exception {
        if (!playerData.isLeader() && playerData.getPermlevel() < 70) {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
            return;
        }
        if (args.length < 1) {
            player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /leaderchat [Nachricht]");
            return;
        }
        PlayerService playerService = VoidAPI.getService(PlayerService.class);
        for (PlayerData pData : playerService.getPlayers()) {
            if (pData == null) continue;
            if (pData.getPermlevel() >= 70 || pData.isLeader()) {
                if (pData.getFaction() == null) continue;
                Player targetplayer = Bukkit.getPlayer(pData.getUuid());
                String msg = Utils.stringArrayToString(args);
                if (targetplayer == null) continue;
                targetplayer.sendMessage("§6Leader §8┃ §e➜ " + playerData.getFaction() + " " + player.getName() + ": " + msg);
            }
        }
    }
}
