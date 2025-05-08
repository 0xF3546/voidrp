package de.polo.core.admin.commands;

import de.polo.api.Utils.enums.Prefix;
import de.polo.api.player.VoidPlayer;
import de.polo.core.handler.CommandBase;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.storage.RegisteredBlock;
import de.polo.core.utils.Utils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

import static de.polo.core.Main.blockManager;

@CommandBase.CommandMeta(
        name = "registerbeerpongcup",
        permissionLevel = 100,
        adminDuty = true,
        usage = "/registerbeerpongcup [Name/Location]"
)
public class RegisterBeerPongCup extends CommandBase {
    public RegisterBeerPongCup(@NotNull CommandMeta meta) {
        super(meta);
    }

    @Override
    public void execute(@NotNull VoidPlayer player, @NotNull PlayerData playerData, @NotNull String[] args) throws Exception {
        Block facing = Utils.getPlayerFacingBlock(player.getPlayer(), 5);
        if (!facing.getType().equals(Material.FLOWER_POT)) {
            player.sendMessage("Du musst auf einen Blumentopf schauen!", Prefix.ERROR);
            return;
        }
        if (args.length < 1) {
            showSyntax(player);
            return;
        }
        String name = String.join(" ", args);
        RegisteredBlock registeredBlock = new RegisteredBlock();
        registeredBlock.setLocation(facing.getLocation());
        registeredBlock.setMaterial(facing.getType());
        registeredBlock.setInfo("beerpongcup");
        registeredBlock.setInfoValue(name);
        blockManager.addBlock(registeredBlock);
        player.sendMessage("Du hast den BeerPong-Cup " + name + " registriert!", Prefix.ADMIN);
    }
}
