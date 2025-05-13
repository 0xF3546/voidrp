package de.polo.core.faction.commands;

import de.polo.api.VoidAPI;
import de.polo.api.player.VoidPlayer;
import de.polo.core.Main;
import de.polo.core.faction.service.LawEnforcementService;
import de.polo.core.handler.CommandBase;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.utils.Prefix;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static de.polo.core.Main.utils;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */

@CommandBase.CommandMeta(name = "unarrest", leader = true, usage = "/unarrest [Spieler]")
public class UnarrestCommand extends CommandBase {
    public UnarrestCommand(@NotNull CommandMeta meta) {
        super(meta);
    }

    @Override
    public void execute(@NotNull VoidPlayer player, @NotNull PlayerData playerData, @NotNull String[] args) throws Exception {
        if (args.length < 1) {
            showSyntax(player);
            return;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(Component.text(Prefix.ERROR + "Der Spieler wurde nicht gefunden."));
            return;
        }
        PlayerData targetData = Main.playerManager.getPlayerData(target);
        if (!targetData.isJailed()) {
            player.sendMessage(Component.text(Prefix.ERROR + "Der Spieler ist nicht im Gefängnis."));
            return;
        }
        LawEnforcementService lawEnforcementService = VoidAPI.getService(LawEnforcementService.class);
        VoidPlayer targetPlayer = VoidAPI.getPlayer(target);
        lawEnforcementService.unarrestPlayer(targetPlayer);
        for (Player players : Bukkit.getOnlinePlayers()) {
            PlayerData playerData1 = Main.getPlayerManager().getPlayerData(players.getUniqueId());
            if (Objects.equals(playerData1.getFaction(), "FBI") || Objects.equals(playerData1.getFaction(), "Polizei")) {
                players.sendMessage("§8[§cGefängnis§8] §6" + Main.factionManager.getTitle(player.getPlayer()) + " " + player.getName() + " hat " + target.getName() + " entlassen.");
            }
        }
        player.getPlayer().closeInventory();
    }
}
