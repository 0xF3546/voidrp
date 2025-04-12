package de.polo.core.crew.commands;

import de.polo.api.VoidAPI;
import de.polo.api.player.VoidPlayer;
import de.polo.core.crew.services.CrewService;
import de.polo.core.handler.CommandBase;
import de.polo.core.player.entities.PlayerData;
import org.jetbrains.annotations.NotNull;

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
        CrewService crewService = VoidAPI.getService(CrewService.class);
        crewService.sendMessageToMembers(player.getData().getCrew(), player.getData().getCrewRank().getName() + " " + player.getName() + "§8: §7" + String.join(" ", args));
    }
}
