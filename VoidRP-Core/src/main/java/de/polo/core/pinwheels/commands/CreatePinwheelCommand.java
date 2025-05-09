package de.polo.core.pinwheels.commands;

import de.polo.api.Utils.enums.Prefix;
import de.polo.api.VoidAPI;
import de.polo.api.player.VoidPlayer;
import de.polo.core.handler.CommandBase;
import de.polo.core.pinwheels.dto.CreatePinwheelDto;
import de.polo.core.pinwheels.services.PinwheelService;
import de.polo.core.player.entities.PlayerData;
import org.jetbrains.annotations.NotNull;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
@CommandBase.CommandMeta(
        name = "createpinwheel",
        usage = "/createpinwheel [Name]",
        permissionLevel = 100,
        adminDuty = true
)
public class CreatePinwheelCommand extends CommandBase {
    public CreatePinwheelCommand(@NotNull CommandMeta meta) {
        super(meta);
    }

    @Override
    public void execute(@NotNull VoidPlayer player, @NotNull PlayerData playerData, @NotNull String[] args) throws Exception {
        if (args.length < 1) {
            showSyntax(player);
            return;
        }
        CreatePinwheelDto pinwheel = new CreatePinwheelDto(player.getLocation(), String.join(" ", args));
        PinwheelService pinwheelService = VoidAPI.getService(PinwheelService.class);
        int id = pinwheelService.addPinwheel(pinwheel);
        player.sendMessage("Du hast ein Windrad #" + id + " registriert", Prefix.ADMIN);
    }
}
