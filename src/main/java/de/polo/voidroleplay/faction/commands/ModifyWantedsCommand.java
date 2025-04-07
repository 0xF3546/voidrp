package de.polo.voidroleplay.faction.commands;

import de.polo.voidroleplay.handler.CommandBase;
import de.polo.voidroleplay.handler.TabCompletion;
import de.polo.voidroleplay.player.entities.VoidPlayer;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.utils.enums.WantedVariation;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

import static de.polo.voidroleplay.Main.factionManager;
import static de.polo.voidroleplay.Main.playerManager;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
@CommandBase.CommandMeta(name = "modifywanteds", usage = "/modifywanteds [Spieler] [Grund]")
public class ModifyWantedsCommand extends CommandBase implements TabCompleter {
    public ModifyWantedsCommand(@NotNull CommandMeta meta) {
        super(meta);
    }

    @Override
    public void execute(@NotNull VoidPlayer player, @NotNull PlayerData playerData, @NotNull String[] args) throws Exception {
        if (!playerData.isExecutiveFaction()) {
            player.sendMessage(Component.text(Prefix.ERROR_NOPERMISSION));
            return;
        }
        if (args.length < 2) {
            showSyntax(player);
            return;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(Component.text(Prefix.ERROR + "Der Spieler wurde nicht gefunden."));
            return;
        }
        WantedVariation variation = null;
        StringBuilder inputVariation = new StringBuilder(args[1]);
        for (int i = 2; i < args.length; i++) {
            inputVariation.append(" ").append(args[i]);
        }
        for (WantedVariation v : WantedVariation.values()) {
            if (v.name().equalsIgnoreCase(inputVariation.toString()) || v.getName().equalsIgnoreCase(inputVariation.toString())) variation = v;
        }
        if (variation == null) {
            player.sendMessage(Component.text(Prefix.ERROR + "Die Variation wurde nicht gefunden."));
            return;
        }
        PlayerData targetData = playerManager.getPlayerData(target);
        if (targetData.getWanted() == null) {
            player.sendMessage(Component.text(Prefix.ERROR + "Der Spieler wird nicht gesucht."));
            return;
        }
        if (targetData.getWanted().hasVariation(variation)) {
            player.sendMessage(Component.text(Prefix.ERROR + "Der Spieler hat diese Variation bereits."));
            return;
        }
        factionManager.sendCustomMessageToFactions("§9HQ: " + factionManager.getTitle(player.getPlayer()) + " " + player.getName() + " hat " + target.getName() + "'s Wanteds angepasst. Variation: " + variation.getName(), "Polizei", "FBI");
        targetData.getWanted().addVariationAsync(variation);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return TabCompletion.getBuilder(args)
                .addAtIndex(1, Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .toList())
                .addAtIndex(2, Arrays.stream(WantedVariation.values())
                        .map(WantedVariation::getName)
                        .toList())
                .build();
    }
}
