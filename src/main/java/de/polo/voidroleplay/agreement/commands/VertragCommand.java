package de.polo.voidroleplay.agreement.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.handler.CommandBase;
import de.polo.voidroleplay.storage.Agreement;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.agreement.services.VertragUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static de.polo.voidroleplay.Main.agreementService;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
@CommandBase.CommandMeta(
        name = "vertrag",
        usage = "/vertrag [Spieler] [Bedingung]"
)
public class VertragCommand extends CommandBase {

    public VertragCommand(@NotNull CommandMeta meta) {
        super(meta);
    }

    @Override
    public void execute(@NotNull Player player, @NotNull PlayerData playerData, @NotNull String[] args) throws Exception {
        if (args.length < 2) {
            player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /vertrag [Spieler] [Bedingung]");
            return;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(Prefix.ERROR + "Spieler ist nicht online.");
            return;
        }
        if (target.getLocation().distance(player.getLocation()) > 5) {
            player.sendMessage(Prefix.ERROR + target.getName() + " ist nicht in der nähe.");
            return;
        }
        StringBuilder reason = new StringBuilder(args[1]);
        for (int i = 2; i < args.length; i++) {
            reason.append(" ").append(args[i]);
        }
        target.sendMessage("§6" + player.getName() + " hat dir einen Vertrag angeboten§8:§7 " + reason);
        player.sendMessage("§6Du hast " + target.getName() + " einen Vertrag angeboten§8:§7 " + reason);

        Agreement agreement = new Agreement(player, target, "vertrag", () -> {
            player.sendMessage(Component.text("§6" + target.getName() + " hat den Vertrag angenommen."));
            target.sendMessage(Component.text("§aDu hast den Vertrag angenommen."));
        }, () -> {
            player.sendMessage(Component.text("§6" + target.getName() + " hat den Vertrag abgelehnt."));
            target.sendMessage(Component.text("§cDu hast den Vertrag abgelehnt."));
        });
        agreementService.setAgreement(player, target, agreement);
        agreementService.sendInfoMessage(target);
    }
}
