package de.polo.core.faction.commands;

import de.polo.api.VoidAPI;
import de.polo.core.Main;
import de.polo.core.faction.service.LawEnforcementService;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.storage.WantedReason;
import de.polo.core.utils.Prefix;
import de.polo.core.utils.Utils;
import de.polo.api.player.enums.WantedVariation;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class WantedInfoCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final Utils utils;

    public WantedInfoCommand(PlayerManager playerManager, Utils utils) {
        this.playerManager = playerManager;
        this.utils = utils;

        Main.registerCommand("wantedinfo", this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        Player player = (Player) commandSender;
        PlayerData playerData = playerManager.getPlayerData(player);
        if (!playerData.isExecutiveFaction()) {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
            return false;
        }
        if (args.length < 1) {
            player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /wantedinfo [Spieler]");
            return false;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(Prefix.ERROR + "Der Spieler wurde nicht gefunden.");
            return false;
        }
        PlayerData targetData = playerManager.getPlayerData(target);
        if (targetData.getWanted() == null) {
            player.sendMessage(Prefix.ERROR + "Der Spieler wird nicht gesucht.");
            return false;
        }
        LawEnforcementService lawEnforcementService = VoidAPI.getService(LawEnforcementService.class);
        WantedReason wantedReason = lawEnforcementService.getWantedReason(targetData.getWanted().getWantedId());
        int wanteds = wantedReason.getWanted();
        StringBuilder reason = new StringBuilder(wantedReason.getReason());
        for (WantedVariation variation : targetData.getWanted().getVariations()) {
            wanteds += variation.getWantedAmount();
            reason.append(", ").append(variation.getName());
        }
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(targetData.getWanted().getIssuer());
        player.sendMessage("§9HQ: Fahndung " + target.getName() + ": " + reason + " (" + wanteds + " WPS)");
        player.sendMessage("§9HQ: Beamter: " + offlinePlayer.getName() + ", " + Utils.localDateTimeToReadableString(targetData.getWanted().getIssued()));
        return false;
    }
}
