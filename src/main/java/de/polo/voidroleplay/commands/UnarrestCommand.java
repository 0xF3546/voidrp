package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.handler.CommandBase;
import de.polo.voidroleplay.player.entities.VoidPlayer;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.utils.Prefix;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static de.polo.voidroleplay.Main.utils;

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
        PlayerData targetData = Main.getInstance().playerManager.getPlayerData(target);
        if (!targetData.isJailed()) {
            player.sendMessage(Component.text(Prefix.ERROR + "Der Spieler ist nicht im Gefängnis."));
            return;
        }
        utils.staatUtil.unarrestPlayer(target);
        for (Player players : Bukkit.getOnlinePlayers()) {
            PlayerData playerData1 = Main.getInstance().getPlayerManager().getPlayerData(players.getUniqueId());
            if (Objects.equals(playerData1.getFaction(), "FBI") || Objects.equals(playerData1.getFaction(), "Polizei")) {
                players.sendMessage("§8[§cGefängnis§8] §6" + Main.getInstance().factionManager.getTitle(player.getPlayer()) + " " + player.getName() + " hat " + target.getName() + " entlassen.");
            }
        }
        player.getPlayer().closeInventory();
    }
}
