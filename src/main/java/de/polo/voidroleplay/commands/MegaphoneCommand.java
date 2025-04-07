package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.handler.CommandBase;
import de.polo.voidroleplay.player.entities.VoidPlayer;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.utils.Utils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */

@CommandBase.CommandMeta(name = "megaphone", usage = "/megaphone [Nachricht]")
public class MegaphoneCommand extends CommandBase {
    public MegaphoneCommand(@NotNull CommandMeta meta) {
        super(meta);
    }

    @Override
    public void execute(@NotNull VoidPlayer player, @NotNull PlayerData playerData, @NotNull String[] args) throws Exception {
        if (!playerData.isExecutiveFaction() || playerData.getFactionGrade() < 3) {
            player.sendMessage(Component.text(Prefix.ERROR));
            return;
        }
        if (args.length < 1) {
            showSyntax(player);
            return;
        }
        String message = Utils.stringArrayToString(args);
        Bukkit.getOnlinePlayers()
                .parallelStream()
                .filter(p -> p.getLocation().distance(player.getLocation()) <= 50)
                .forEach(p -> p.sendMessage("§b " + player.getName() + "§8: §7" + message));
    }
}
