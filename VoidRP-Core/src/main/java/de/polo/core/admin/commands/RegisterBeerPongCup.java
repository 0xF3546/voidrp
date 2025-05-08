package de.polo.core.admin.commands;

import de.polo.api.Utils.enums.Prefix;
import de.polo.api.player.VoidPlayer;
import de.polo.core.handler.CommandBase;
import de.polo.core.handler.TabCompletion;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.storage.RegisteredBlock;
import de.polo.core.utils.Utils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static de.polo.core.Main.blockManager;

@CommandBase.CommandMeta(
        name = "registerbeerpongcup",
        permissionLevel = 100,
        adminDuty = true,
        usage = "/registerbeerpongcup [Name/Location] [Team(1/2)]"
)
public class RegisterBeerPongCup extends CommandBase implements TabCompleter {
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
        boolean isRegistered = blockManager.getBlockAtLocation(facing.getLocation()) != null;
        if (isRegistered) {
            player.sendMessage("Dieser BeerPong-Cup ist bereits registriert!", Prefix.ERROR);
            return;
        }
        String name = args[0];
        RegisteredBlock registeredBlock = new RegisteredBlock();
        registeredBlock.setLocation(facing.getLocation());
        registeredBlock.setMaterial(facing.getType());
        registeredBlock.setInfo("beerpongcup");
        registeredBlock.setInfoValue(name + "_" + args[1]);
        blockManager.addBlock(registeredBlock);
        player.sendMessage("Du hast den BeerPong-Cup " + name + " registriert!", Prefix.ADMIN);
    }


    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return TabCompletion.getBuilder(strings)
                .addAtIndex(1, "[Name]")
                .addAtIndex(2, "2")
                .build();
    }
}
