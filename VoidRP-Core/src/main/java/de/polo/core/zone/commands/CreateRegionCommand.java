package de.polo.core.zone.commands;

import de.polo.api.Utils.enums.Prefix;
import de.polo.api.VoidAPI;
import de.polo.api.player.VoidPlayer;
import de.polo.core.handler.CommandBase;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.utils.Utils;
import de.polo.core.zone.dto.CreateRegionDto;
import de.polo.core.zone.services.RegionService;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

@CommandBase.CommandMeta(
        name = "createregion",
        adminDuty = true,
        permissionLevel = 100,
        usage = "/createRegion [Name] [X/Y/Z] [X/Y/Z]"
)
public class CreateRegionCommand extends CommandBase {

    public CreateRegionCommand(@NotNull CommandMeta meta) {
        super(meta);
    }

    @Override
    public void execute(@NotNull VoidPlayer player, @NotNull PlayerData playerData, @NotNull String[] args) throws Exception {
        if (args.length < 3) {
            showSyntax(player);
            return;
        }
        String name = args[0];
        String[] coordinates = args[1].split("/");
        if (coordinates.length != 3) {
            player.sendMessage("§cInvalid coordinates format. Use X/Y/Z.");
            return;
        }
        int x = Integer.parseInt(coordinates[0]);
        int y = Integer.parseInt(coordinates[1]);
        int z = Integer.parseInt(coordinates[2]);
        Location location1 = Utils.getLocation(x, y, z);

        coordinates = args[2].split("/");
        if (coordinates.length != 3) {
            player.sendMessage("§cInvalid coordinates format. Use X/Y/Z.");
            return;
        }
        x = Integer.parseInt(coordinates[0]);
        y = Integer.parseInt(coordinates[1]);
        z = Integer.parseInt(coordinates[2]);
        Location location2 = Utils.getLocation(x, y, z);
        CreateRegionDto regionDto = new CreateRegionDto(name, location1, location2);
        RegionService regionService = VoidAPI.getService(RegionService.class);
        int id = regionService.createRegion(regionDto);
        player.sendMessage("Region mit ID " + id + " erstellt.", Prefix.ADMIN);
    }
}
