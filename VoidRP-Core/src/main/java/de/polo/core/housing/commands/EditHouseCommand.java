package de.polo.core.housing.commands;

import de.polo.api.Utils.enums.Prefix;
import de.polo.api.VoidAPI;
import de.polo.api.player.VoidPlayer;
import de.polo.core.game.base.housing.House;
import de.polo.core.handler.CommandBase;
import de.polo.core.handler.TabCompletion;
import de.polo.core.housing.enums.HouseType;
import de.polo.core.housing.services.HouseService;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.storage.RegisteredBlock;
import de.polo.core.utils.Utils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.TileState;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

import static de.polo.core.Main.blockManager;

@CommandBase.CommandMeta(
        name = "edithouse",
        usage = "/edithouse [Nummer] [Typ] [<Wert>]",
        adminDuty = true,
        permissionLevel = 100
)
public class EditHouseCommand extends CommandBase implements TabCompleter {
    public EditHouseCommand(@NotNull CommandMeta meta) {
        super(meta);
    }

    @Override
    public void execute(@NotNull VoidPlayer player, @NotNull PlayerData playerData, @NotNull String[] args) throws Exception {
        if (args.length < 2) {
            showSyntax(player);
            return;
        }
        HouseService houseService = VoidAPI.getService(HouseService.class);
        int houseNumber;
        try {
            houseNumber = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            player.sendMessage("Die Hausnummer muss eine numerisch sein!", Prefix.ERROR);
            return;
        }
        House house = houseService.getHouse(houseNumber);
        if (args[1].equalsIgnoreCase("Location")) {
            RegisteredBlock block = blockManager.getBlocks().stream().filter(x -> x.getInfo().equalsIgnoreCase("house") && x.getInfoValue().equalsIgnoreCase(String.valueOf(house.getNumber()))).findFirst().orElse(null);
            if (block == null) {
                player.sendMessage("Das Haus " + house.getNumber() + " existiert nicht!", Prefix.ERROR);
                return;
            }
            Block oldSignBlock = block.getBlock();
            if (oldSignBlock != null) {
                oldSignBlock.setType(Material.AIR);
            }
            Block b = Utils.getPlayerFacingBlock(player.getPlayer(), 10);
            TileState state = (TileState) b.getState();
            if (!(state instanceof Sign)) {
                player.sendMessage("Du schaust kein Schild an!", Prefix.ERROR);
                return;
            }
            block.setLocation(b.getLocation());
            blockManager.updateBlock(block);
            houseService.updateSign(house);
            player.sendMessage("Du hast das Haus " + house.getNumber() + " geändert!", Prefix.GAMEDESIGN);
        } else if (args[1].equalsIgnoreCase("Price")) {
            if (args.length < 3) {
                showSyntax(player);
                return;
            }
            int price;
            try {
                price = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                player.sendMessage("Der Preis muss eine numerisch sein!", Prefix.ERROR);
                return;
            }
            houseService.setHousePrice(houseNumber, price);
        } else if (args[1].equalsIgnoreCase("Delete")) {
            houseService.deleteHouse(house.getNumber());
            RegisteredBlock block = blockManager.getBlocks().stream().filter(x -> x.getInfo().equalsIgnoreCase("house") && x.getInfoValue().equalsIgnoreCase(String.valueOf(house.getNumber()))).findFirst().orElse(null);
            block.getBlock().setType(Material.AIR);
            blockManager.deleteBlock(block.getId());
            player.sendMessage("Das Haus " + house.getNumber() + " wurde gelöscht!", Prefix.ADMIN);
        } else if (args[1].equalsIgnoreCase("Refund")) {
            houseService.refundHouse(house.getNumber());
            player.sendMessage("Du hast das Haus " + house.getNumber() + " zurückerstattet!", Prefix.ADMIN);
        } else if (args[1].equalsIgnoreCase("Typ")) {
            if (args.length < 3) {
                showSyntax(player);
                return;
            }
            HouseType type = Arrays.stream(HouseType.values()).toList().stream().filter(x -> x.getName().equalsIgnoreCase(args[2])).findFirst().orElse(null);
            if (type == null) {
                player.sendMessage("Der Typ " + args[2] + " existiert nicht!", Prefix.ERROR);
                return;
            }
            houseService.updateType(house, type);
            player.sendMessage("Du hast den Typ des Hauses " + house.getNumber() + " zu " + type.getName() + " geändert!", Prefix.GAMEDESIGN);
        } else {
            showSyntax(player);
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        HouseService houseService = VoidAPI.getService(HouseService.class);
        return TabCompletion.getBuilder(strings)
                .addAtIndex(1, houseService.getHouses().stream().map(house -> String.valueOf(house.getNumber())).toList())
                .addAtIndex(2, Arrays.asList("Location", "Price", "Delete", "Refund", "Typ"))
                .addAtIndexIf(3, 2, "Typ", Arrays.stream(HouseType.values()).map(HouseType::getName).toList())
                .build();
    }
}
