package de.polo.core.elevators.commands;

import de.polo.api.Utils.enums.Prefix;
import de.polo.api.VoidAPI;
import de.polo.api.elevators.Elevator;
import de.polo.api.player.VoidPlayer;
import de.polo.core.elevators.dto.CreateElevatorDto;
import de.polo.core.elevators.dto.CreateFloorDto;
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
        name = "createelevatorfloor",
        usage = "/createelevatorfloor [ElevatorId] [Stage]",
        permissionLevel = 100,
        adminDuty = true
)
public class CreateElevatorFloorCommand extends CommandBase {
    public CreateElevatorFloorCommand(@NotNull CommandMeta meta) {
        super(meta);
    }

    @Override
    public void execute(@NotNull VoidPlayer player, @NotNull PlayerData playerData, @NotNull String[] args) throws Exception {
        if (args.length < 2) {
            showSyntax(player);
            return;
        }
        int id;
        int stage;
        try {
            id = Integer.parseInt(args[0]);
            stage = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage("Die ID muss eine Zahl sein.", Prefix.ERROR);
            return;
        }
        ElevatorService elevatorService = VoidAPI.getService(ElevatorService.class);
        Elevator elevator = elevatorService.getElevator(id);
        if (elevator == null) {
            player.sendMessage("Fahrstuhl mit ID " + id + " existiert nicht.", Prefix.ERROR);
            return;
        }
        CreateFloorDto floorDto = new CreateFloorDto(elevator, player.getPlayer().getLocation(), stage);
        int elevatorId = elevatorService.addFloor(floorDto);
        player.sendMessage("Ebene " + stage + " zu Fahrstuhl " + elevatorId + " hinzugefügt.", Prefix.ADMIN);
    }
}