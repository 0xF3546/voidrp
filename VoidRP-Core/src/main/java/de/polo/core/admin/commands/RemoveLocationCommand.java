package de.polo.core.admin.commands;

import de.polo.api.Utils.enums.Prefix;
import de.polo.api.VoidAPI;
import de.polo.api.player.VoidPlayer;
import de.polo.core.handler.CommandBase;
import de.polo.core.location.services.LocationService;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.utils.Utils;
import org.jetbrains.annotations.NotNull;

@CommandBase.CommandMeta(
        name = "removelocation",
        usage = "/removelocation [Name]",
        permissionLevel = 100,
        adminDuty = true
)
public class RemoveLocationCommand extends CommandBase {
    public RemoveLocationCommand(@NotNull CommandMeta meta) {
        super(meta);
    }

    @Override
    public void execute(@NotNull VoidPlayer player, @NotNull PlayerData playerData, @NotNull String[] args) throws Exception {
        LocationService locationService = VoidAPI.getService(LocationService.class);
        if (args.length < 1) {
            showSyntax(player);
            return;
        }
        String locationName = Utils.stringArrayToString(args);
        if (locationService.getLocation(locationName) == null) {
            player.sendMessage("Die Location wurde nicht gefunden,", Prefix.ERROR);
            return;
        }
        locationService.removeLocation(locationName);
        player.sendMessage("Die Location wurde entfernt.", Prefix.ADMIN);
    }
}
