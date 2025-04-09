package de.polo.core.crew.commands;

import de.polo.api.player.VoidPlayer;
import de.polo.core.handler.CommandBase;
import de.polo.core.player.entities.PlayerData;
import org.jetbrains.annotations.NotNull;

import static de.polo.core.Main.crewService;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
@CommandBase.CommandMeta(
        name = "crewchat",
        usage = "/crewchat [Nachricht]"
)
public class CrewChatCommand extends CommandBase {
    public CrewChatCommand(@NotNull CommandMeta meta) {
        super(meta);
    }

    @Override
    public void execute(@NotNull VoidPlayer player, @NotNull PlayerData playerData, @NotNull String[] args) throws Exception {
        if (player.getData().getCrew() == null) {
            player.sendMessage("§cCrew §8┃ §c➜ §7Du bist in keiner Crew!");
            return;
        }
        if (args.length == 0) {
            showSyntax(player);
            return;
        }
        crewService.sendMessageToMembers(player.getData().getCrew(), player.getData().getCrewRank().getName() + " " + player.getName() + "§8: §7" + String.join(" ", args));
    }
}
