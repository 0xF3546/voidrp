package de.polo.core.laboratory.commands;

import de.polo.api.utils.enums.Prefix;
import de.polo.api.VoidAPI;
import de.polo.api.player.VoidPlayer;
import de.polo.core.handler.CommandBase;
import de.polo.core.laboratory.dto.CreateLaboratoryDto;
import de.polo.core.laboratory.services.LaboratoryService;
import de.polo.core.player.entities.PlayerData;
import org.jetbrains.annotations.NotNull;

@CommandBase.CommandMeta(
        name = "createlaboratory",
        usage = "/createlaboratory [Name]",
        permissionLevel = 100,
        adminDuty = true
)
public class CreateLaboratoryCommand extends CommandBase {
    public CreateLaboratoryCommand(@NotNull CommandMeta meta) {
        super(meta);
    }

    @Override
    public void execute(@NotNull VoidPlayer player, @NotNull PlayerData playerData, @NotNull String[] args) throws Exception {
        if (args.length < 1) {
            showSyntax(player);
            return;
        }
        CreateLaboratoryDto laboratoryDto = new CreateLaboratoryDto(player.getLocation(), String.join(" ", args));
        LaboratoryService laboratoryService = VoidAPI.getService(LaboratoryService.class);
        int id = laboratoryService.addLaboratory(laboratoryDto);
        player.sendMessage("Du hast ein Labor #" + id + " registriert", Prefix.ADMIN);

    }
}
