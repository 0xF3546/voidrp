package de.polo.core.elevators.commands;

import de.polo.api.utils.enums.Prefix;
import de.polo.api.VoidAPI;
import de.polo.api.player.VoidPlayer;
import de.polo.core.elevators.dto.CreateElevatorDto;
import de.polo.core.elevators.services.ElevatorService;
import de.polo.core.handler.CommandBase;
import de.polo.core.player.entities.PlayerData;
import org.jetbrains.annotations.NotNull;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
@CommandBase.CommandMeta(
        name = "createelevator",
        usage = "/createelevator [Name]",
        permissionLevel = 100,
        adminDuty = true
)
public class CreateElevatorCommand extends CommandBase {
    public CreateElevatorCommand(@NotNull CommandMeta meta) {
        super(meta);
    }

    @Override
    public void execute(@NotNull VoidPlayer player, @NotNull PlayerData playerData, @NotNull String[] args) throws Exception {
        if (args.length < 1) {
            showSyntax(player);
            return;
        }
        CreateElevatorDto elevatorDto = new CreateElevatorDto(String.join(" ", args), player.getPlayer().getLocation());
        ElevatorService elevatorService = VoidAPI.getService(ElevatorService.class);
        int elevatorId = elevatorService.addElevator(elevatorDto);
        player.sendMessage("Fahrstuhl " + elevatorId + " wurde erstellt.", Prefix.ADMIN);
    }
}
