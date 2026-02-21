package de.polo.core.oil.commands;

import de.polo.api.utils.enums.Prefix;
import de.polo.api.VoidAPI;
import de.polo.api.player.VoidPlayer;
import de.polo.core.elevators.dto.CreateElevatorDto;
import de.polo.core.elevators.services.ElevatorService;
import de.polo.core.handler.CommandBase;
import de.polo.core.oil.dto.CreateOilPumpDto;
import de.polo.core.oil.services.OilService;
import de.polo.core.player.entities.PlayerData;
import org.jetbrains.annotations.NotNull;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
@CommandBase.CommandMeta(
        name = "createoilpump",
        usage = "/createoilpump",
        permissionLevel = 100,
        adminDuty = true
)
public class CreateOilPumpCommand extends CommandBase {
    public CreateOilPumpCommand(@NotNull CommandMeta meta) {
        super(meta);
    }

    @Override
    public void execute(@NotNull VoidPlayer player, @NotNull PlayerData playerData, @NotNull String[] args) throws Exception {
        CreateOilPumpDto createOilPumpDto = new CreateOilPumpDto(player.getPlayer().getLocation());
        OilService oilService = VoidAPI.getService(OilService.class);
        int oilPumpId = oilService.addOilPump(createOilPumpDto);
        player.sendMessage("Ölpumpe " + oilPumpId + " wurde erstellt.", Prefix.ADMIN);
    }
}
